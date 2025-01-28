---
title: Structuring large Clojure codebases with Biff
description: I've been rewriting my reading app from scratch and open-sourcing it in the progress. I'm using it to experiment with new Biff features.
slug: structuring-large-codebases
image: https://biffweb.com/cards/structuring.png
published: 2025-01-28T10:30:00 AM
tags:
- unlisted
---

<!-- email

My Biff work over the past six months or so has been focused on rewriting
[Yakread](https://yakread.com) from scratch and implementing new framework
features along the way to make the v2 app more maintainable and performant. This
is a fairly large project, and I won't be officially releasing any new Biff
features until the rewrite is done (I want to give myself a chance to kick the
tires first). In the interim, I briefly tried making some informal videos to
document my progress:

- [Yakread schema](https://biffweb.com/p/yakread-schema/)
- [Yakread + Pathom](https://biffweb.com/p/yakread-pathom/)

But after two videos I decided that I'd rather spend my time writing 3 or 4
high-quality articles per year than making a couple mediocre videos per month.
I'm also trying to write said articles with both Clojure and non-Clojure
audiences in mind (gotta get that HN karma).

So here's the first article. Hope you like it.

&ndash;[Jacob](https://obryant.dev)

---

-->

I've been making some progress on rewriting [Yakread](https://yakread.com/) (a
fancy reading app) from \~scratch and open-sourcing it in the process. Along the
way I'm experimenting with potential new features for
[Biff](https://biffweb.com/), my Clojure web framework, which Yakread is built
with. In particular I'm working on approaches for keeping Biff apps more
manageable as the codebase grows: the original Yakread codebase was about 10k
lines and was already getting pretty crufty. I've also learned some things from
contributing to our ~85k-line Clojure codebase at work.

I thought it'd be worth going over the main new architectural approaches in
Yakread for anyone interested in poking around the code/as a preview of what to
expect in Biff later on. The [open-source
repo](https://github.com/jacobobryant/yakread) has only a sliver of the
production app's functionality so far, but it has examples of all the
approaches described below.

## Materialized views

"Old Yakread" has a lot of slow queries. For example, loading the subscriptions
page on my account takes more than 10 seconds: for each of my hundreds of
subscriptions, it has to run a query to figure out how many unread posts there
are and when the most recent post was published. This is currently done the dumb
way, i.e. Yakread queries for every single post and then computes the aggregate
data.

The traditional way to solve this would be to denormalize the data model (add
fields for “\# unread items” and “last published at” to the subscription model)
and keep it up to date manually (update those fields whenever a new post is
published, whenever the user reads a post, etc). However, [this approach can get
out of
hand](https://lironshapira.medium.com/data-denormalization-is-broken-7b697352f405).

I’ve addressed this in a cleaner way by [implementing materialized
views](https://github.com/jacobobryant/biff/blob/indexes/src/com/biffweb/impl/index.clj)
for XTDB. I store them in a dedicated RocksDB instance. For each piece of
denormalized data you need, [you define a pure "denormalizer"
function](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/model/subscription.clj#L88)\*
which takes in an item from XTDB’s transaction log along with the current
RocksDB state and returns a map of key-value pairs that will be written back to
RocksDB. Biff handles everything else: setting up RocksDB, running your
denormalizer functions whenever there’s a new XTDB transaction, and providing a
RocksDB snapshot for querying that’s consistent with your current XTDB snapshot
(we retain XTDB's database-as-a-value semantics).

\*Still deciding on the name... the codebase calls them "indexer" functions
currently, but I decided "materialized views" are a clearer/more accurate term
than "indexes."

This is a lower-level approach than something like
[Materialize](https://materialize.com/), which lets you write regular SQL
queries instead of defining these denormalizer functions (i.e. you’re defining a
function of `current DB state -> materialized view` instead of `new
transaction, current materialized view -> new materialized view`). However,
when I experimented with Materialize several years ago I found that its memory
overhead made it untenable for my use case. I’m sure it’s much better for, say,
aggregating metrics from large real-time systems, even if it sadly didn’t work
out for the simplify-random-guy’s-RSS-reader use case. (I’d also like to look
into other things in this space like [Rama](https://redplanetlabs.com/) and
[Feldera](https://github.com/feldera/feldera)).

Writing the [incremental view
maintenance](https://www.google.com/search?q=incremental+view+maintenance) logic
by hand is somewhat tedious, but the testing approach I'm using makes it really
not bad. I’ve [written
code](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/test/com/yakread/lib/test.clj#L180)
with [Fugato](https://github.com/vouch-opensource/fugato) that can take the
database schema for your app and generate test data for use with test.check
(Clojure’s property-based testing library). All you have to do is write an
“oracle” function that takes a database snapshot and computes what the
materialized view should look like for that snapshot. e.g. for the “subscription
last published at” materialized view, [the oracle
function](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/test/com/yakread/model/subscription_test.clj#L56)
simply gets all the posts for each subscription and then finds the one with the
latest published-at date. Then the testing code ensures that the materialized
view computed by your denormalizer function matches.

## Separating application logic from effects

100% of the application code in "New Yakread" is pure. The unit tests never have
to set up mocks or check the results of side effects. Every unit test has the
form “pass some data to this pure function and make sure the output matches this
constant.”

I accomplish this by turning each function with application logic into [a little
state
machine](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/app/subscriptions/add.clj#L176),
where each state is either a unit of pure computation (app logic) or an effect
handler. Whenever the pure app logic needs to do something effectful, like run
an HTTP request or save something to the database, it returns [some
data](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/app/subscriptions/add.clj#L179)
describing the effect, which causes the machine to transition to an effectful
state.

[These effectful
states](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/lib/pipeline.clj#L52)
perform the effects and then transition back to your pure application logic.
They're centralized in one spot in your app, and they’re kept as dumb as
possible: take some input data, do the effect, return the output.

For testing, each pure app logic state can be called individually without
running the whole machine. This facilitates an approach to unit testing that
I’ve been enjoying quite a bit: you define only [the input
data](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/test/com/yakread/app/subscriptions/add_test.clj#L26)
for your function, and then the [expected return
value](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/test/com/yakread/app/subscriptions/add_test/examples.edn#L17)
is generated by calling your function. The expected value is saved to an EDN
file and checked into source, at which point you ensure the expected value is,
in fact, what you expect. Then going forward, the unit test simply checks that
what the function returns still matches what’s in the EDN file. If it’s supposed
to change, you regenerate the EDN file and inspect it before committing.

This is so convenient that for once I’ve actually been writing unit tests as I
develop code instead of testing manually e.g. via the browser and then (maybe)
writing tests after the fact. Thanks to [Chris
Badahdah](https://github.com/djblue) for telling me about this approach.

This also has benefits for observability: the state-machine-executor code is [a
convenient
place](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/lib/pipeline.clj#L35)
to put tracing and exception handling code. Whenever your app logic throws
an exception, you'll be guaranteed to have the necessary input data to reproduce
it, without having to add logging yourself.

## Pathom

The final big change I’ve made is that the codebase is now split up into a bunch
of [Pathom](https://pathom3.wsscode.com/) resolvers. I've also written a few
Pathom/Biff helper functions, such as [this
one](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/util/biff_staging.clj#L50)
which auto-generates resolvers for your XTDB entities.

It took me a while to “get” Pathom, but now I’m a big fan. My latest attempt at
explaining it is to make a comparison with OOP. It's standard to have classes
for your domain entities (`User`, `Product`, whatever) which have some fields
that correspond to columns in the corresponding database record and some
fields/methods that return derived data. A common example would be having
columns for `first_name` and `last_name` in the database, and then the class
could also have a derived `full_name` method which combines the two.

Pathom lets you do a similar thing: you define "resolvers" for each of the
fields in your domain; some resolvers fetch stuff from the database, while other
resolvers return derived data built on top of those values.

The big difference between Pathom and OOP-style "model code" (not sure what to
call it) is that In the OOP style, your model code and app code gets
interleaved. With Pathom, you first submit a query and get the results as a
plain data structure, then you pass the data structure to your application code.
Like the effects stuff in the previous section, this helps your application code
stay pure.

I use Pathom heavily to separate my model and view code. Each GET request
handler [defines a Pathom
query](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/app/subscriptions.clj#L70),
and I have middleware which runs the query and passes the results to the handler
function. Those handler functions thus never need to include code at the top
level for querying the database, fetching things from S3, or augmenting database
records with custom business logic: that’s all done by Pathom resolvers in other
namespaces. This makes the request handlers easier to write and understand.

I also have resolvers that return UI fragments. For example, the subscriptions
page needs to render a list of all your RSS and newsletter subscriptions. I have
[a
resolver](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/app/subscriptions.clj#L21)
which renders an individual subscription card (which includes things like the
subscription title and the number of unread posts). The parent
resolver&mdash;the one that renders the entire list of
subscriptions&mdash;doesn’t need to specify all the data needed to render each
card. It just queries for a list of “[subscription
cards](https://github.com/jacobobryant/yakread/blob/cbb46eb8454a78f78b82fcf2cc33cf2bbb56643b/src/com/yakread/app/subscriptions.clj#L72),”
which again helps to keep each individual resolver small and easy to understand.

This approach is conceptually similar to what
[Fulcro](https://fulcro.fulcrologic.com/) does: each UI component declares its
own query. The difference is that Fulcro is a SPA framework; Fulcro includes a
bunch of plumbing so that you can extend this programming model to the frontend.
Since Yakread is server-side rendered with htmx, it just uses plain backend
Pathom resolvers for everything.

---

The Yakread rewrite has a long ways to go. However I think the
experimentation phase may be largely over: the framework features I’ve described
above are all in place; now I just need to bang out the rest of the app. Once
that's done, I'll start extracting various parts out of Yakread and releasing
them as part of Biff.
