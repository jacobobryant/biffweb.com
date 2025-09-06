---
title: "Relaunching Yakread: an algorithmic reading app"
description: It's written in Clojure btw
slug: yakread-relaunch
image: https://biffweb.com/cards/relaunching-yakread.png
published: 2025-09-06T05:30:00 AM
---

I've recently finished a year-long rewrite of the Yakread
[codebase](https://github.com/jacobobryant/yakread) and have released it under a source-available
license. [Yakread](https://yakread.com/) is a reading app that makes heavy use of algorithmic
recommendation/filtering. I originally launched it in 2022 during the last leg of my time as a
full-time entrepreneur. It's written with [Biff](https://biffweb.com/), a Clojure web framework that
I also created during that time.

I'm publishing Yakread's source code mainly so that it can serve as
a non-toy example project for Biff users. It's about 10K lines of code as of writing. I'm also
using Yakread to experiment with potential framework features before adding them into Biff.

### The app

I like reading stuff on the internet. Social media tends to be pretty shallow, though. Long-form
content (articles, blogs, newsletters) is better on average but can take more work to manage: RSS
readers and email inboxes tend to get filled up pretty easily, and sorting things chronologically
benefits the most frequent publishers. I've found there's a certain amount of mental overhead
associated with long-form content that, when you have only a few minutes to read something, often
makes it easier to just pull up Reddit.

Yakread is my attempt to make reading long-form content as frictionless as reading social media.
It's structured as a daily email with links to articles. New users start out getting five links a
day to articles that were liked by other Yakread users. You can also add your own newsletter/RSS
subscriptions to Yakread, and there's support for bookmarking individual articles to read later a la
Pocket or Instapaper. Posts from these content sources are also compiled algorithmically so that:

- Blogs that publish a few times per year don't get buried by daily newsletters and other frequent
  publishers.
- Subscriptions that you interact with the most don't get buried by dozens of other subscriptions
  that you signed up for on a whim.
- Articles you miss get resurfaced repeatedly, so you don't feel like you have to "keep up" with
  anything.

The web app also features a "for you" feed, similar to the daily emails, which lets you read
on-demand. There are pages that list your content chronologically in case you want to read something
specific: the recommendation algorithm is there as a default, but it's not the only way to read.
There's a "favorites" page which lists articles that you've thumbs-upped.

Yakread is monetized through native ads (mostly for newsletters) and a "premium" subscription which
removes ads.

### The code

The README has [a
section](https://github.com/jacobobryant/yakread?tab=readme-ov-file#code-structure) describing the
parts of Yakread's code structure that differ from regular Biff projects. Here are a few high-level
points, written without assuming any prior knowledge of Biff.

"The algorithm":

- For recommending new articles (i.e. not ones from your own subscriptions or bookmarks), Yakread
  uses Spark MLlib's [collaboritive
  filtering](https://spark.apache.org/docs/latest/mllib-collaborative-filtering.html)
  implementation. There are controls layered on top that bias the results toward articles that have
  been recommended a fewer number of times across all users (exploration vs. exploitation).

- Ads are selected via the same collaborative filtering model. The predicted rating for each ad is treated as a
  probability that the user will click on that ad, then we calculate the expected value of showing
  each ad (i.e. probability of a click multiplied by how much the advertiser is bidding for each
  click) and charge the advertiser (in the case of a click) via a [second-price
  auction](https://en.wikipedia.org/wiki/Generalized_second-price_auction).

- The algorithm for selecting articles from your subscriptions and bookmarks is [a few hundred
  lines](https://github.com/jacobobryant/yakread/blob/b2f0a93a896112479dfaf17fa0ebae765047547a/src/com/yakread/model/recommend.clj)
  of custom code, which e.g.: computes a pair of "affinity" scores (lower bound and
  upper bound) for each of your subscriptions based on your previous interactions; ranks
  subscriptions based on affinity score, again with controls for exploration vs. exploitation; ranks
  articles based on how recently they were recommended and how recently they were published; figures
  out the right balance between recommending subscription posts and recommending bookmarks.

Everything else:

- Yakread is a server-side rendered app that uses [htmx](https://htmx.org/). There's nothing too
  fancy going on in the UI ([the biggest form](https://yakread.com/advertise) in the app has 8
  fields), so I'm keeping it simple.

- It uses [XTDB](https://xtdb.com/) for the database. You could think of XTDB's immutable
  architecture kind of like "distributed SQLite." Queries operate on a local point-in-time snapshot
  of the data, so you can run multiple queries while handling a given request without worrying about
  network latency (pretty helpful for a recommender system).

- The app's data model is organized via [Pathom](https://pathom3.wsscode.com/). I sometimes think of
  Pathom as "data-oriented dependency injection." It gives you the benefit of ORM model objects from
  OOP languages but without having to pass around a database connection. You specify up front what
  entities and fields you want, then Pathom wires up the data in the correct shape for you.

- I use state machines to separate pure application logic from effectful code. Application logic
  returns data describing the effects it needs to perform (Pathom queries, network requests,
  database transactions, etc), then the machine transitions to other states that perform the
  effects, then results are passed to the next pure logic state, and so on. This makes unit tests
  easy to write since you never have to mock anything or check the results of side effects.

- The tests are largely [inline snapshot tests](https://biffweb.com/p/edn-tests/).

- Yakread currently deployed as a monolith to a single DigitalOcean droplet which handles both web
  requests and background jobs. If/when the time comes to deploy a separate worker (which probably
  needs to happen soon...), the same deployment artifact can be ran with a `BIFF_PROFILE=worker` env
  var set.

- I deploy Yakread with `rsync`. Even though there's only a single web server, most deploys can be done
  without downtime via the REPL: after `rsync` finishes, new code is evaluated while the application
  runs. Full restarts typically only happen when new dependencies are added.

- Yakread does a lot of email: I use [SubethaSMTP](https://github.com/voodoodyne/subethasmtp) to
  receive email newsletters (set an MX record and open up port 25) and
  [MailerSend](https://www.mailersend.com/) for sending the daily digests.

### The theory

I've been interested in recommender systems for a long time, starting with music and then moving
into written content. In both domains I've been attracted to the idea of a system that can handle
most of the tedious organizational work while only requiring you to do the part that only you, as
the human, can do: give feedback on which things you like and don't like. I think there's plenty of
unrealized potential for recommender systems to help people learn, enjoy life, and coordinate.

Recommender systems often get a bad rap, and for good reason: most people's exposure to them comes
in the form of large companies trying to shove the digital equivalent of potato chips down their
throat. However I see that as a problem of business incentives rather than a problem with
algorithmic recommendation in general; there aren't any technical barriers to writing algorithms
that serve up salad instead of potato chips.

So I don't think a mass return to reverse-chronological feeds is the answer: competition is. Ideally
we'd have a larger distribution of companies offering recommendation-powered services that had to
compete based on the quality of their results. Instead we mostly have a few behemoths that are
optimized to squeeze every bit of interaction from you that they can, the long-forgotten sanctity of
your notifications tab be damned.

I hope that Yakread makes the internet a little bit better in that regard. Although I've pretty much
given up on trying to take over the world, I still like the idea of being part of a movement. And
there is interesting stuff happening: Bluesky, for instance, has had far more success than I thought
it would when it was announced back in 2019. The popularity of email newsletters is encouraging.

The internet is still young. Maybe the next decade can be a phase of building [digital public
infrastructure](https://knightcolumbia.org/content/the-case-for-digital-public-infrastructure).
