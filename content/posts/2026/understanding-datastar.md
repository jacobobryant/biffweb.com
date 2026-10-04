---
title: Understanding Datastar
description: and how's it different from htmx
slug: understanding-datastar
image: https://biffweb.com/cards/understanding-datastar.png
published: 2026-10-01T08:00:00 PM
---

Previously I wrote [Understanding
htmx](https://biffweb.com/p/understanding-htmx/) which was meant to explain htmx
conceptually, what the main tradeoffs are, and when you might want to use or not
use it. This post is a followup to that for [Datastar](https://data-star.dev).
Both of these posts describe how I personally think of these tools; if others
disagree with my framing, I make no apologies.

Also note that the web apps I make do _not_ typically include collaborative or
real-time features, yet I still think Datastar is nice for this case. My
explanation here focuses on things that matter for people like me who make
boring apps.

---

htmx and Datastar are both tools for doing server-side rendering instead of e.g.
using React; they both ask "what if we went back to how things were pre-jquery
and tried to extend that model of thin-client web development instead of moving
toward SPAs." They differ in how they implement that vision.

> make an MPA<br>
> each page is a resource<br>
> keep a stream open on the current state of that resource
>
> -- a guy from the Datastar community

Imagine: it's 2005. You have an MPA. Each page in your web app--a social network
for Tapirs--has a GET request handler (which returns a bunch of HTML for the
page) and an assortment of POST request handlers that do stuff and then redirect
back to that HTML endpoint. But then you run into the problems I brought up in
[Understanding htmx](https://biffweb.com/p/understanding-htmx/):

> You need to implement the heart button so that people can heart their
> favorite posts. However, if your heart button is a plain-old-form that causes
> the entire page to reload, there are several potentially undesirable
> consequences:
>
> - All the posts in the feed will have to be fetched again.
> - The posts that get fetched might be different.
> - The user might lose their scroll position.
> - The user might lose their draft if they were in the middle of typing a post.

The Datastar approach looks something like this:

1. The initial page load opens a long-lived SSE connection. Whenever backend
   state changes (e.g. a database transaction is committed), the entire page is
   re-rendered and pushed up to the client over the SSE connection&mdash;a bit
   like telling the client to refresh the page but without doing an actual
   browser refresh.

2. When the user takes an action, like hearting a post, Datastar makes an ajax
   request to a POST request handler which updates the database. Since that
   triggers a re-render by the SSE connection, the POST handler doesn't
   redirect or return any HTML; it simply returns an empty 204 response.

3. Any state on the page that you don't want to recompute (e.g. a list of
   recommended posts) can be stored in server-side "tab state," keyed by some ID
   that's unique to a single browser tab. For example, on page load, you could
   trigger an action/POST request that computes the recommended posts and stores
   them in tab state. The GET request handler _does not_ compute recommended
   posts; it just reads tab state. If tab state hasn't been populated yet, it
   shows a loading indicator.

4. If you have an interaction that needs to be fast and you don't want to wait
   for a server round-trip before re-rendering happens, you can have the
   interaction update some "signals," which are Datastar's mechanism for
   handling frontend-only state. A common use case for this is storing form
   field values: when you're typing into a text field, you don't want to wait
   for the network before the characters show up.

Your application code has the same structure as the MPA-in-2005-thing; the main
differences are that your POST handlers return `204` instead of `303`, and you
instrument all your form fields so they're bound to signals. You get to keep the
single page rendering endpoint/function which gives you the whole "view is a
function of your state" thing, and then you can get rich interactivity on top
thanks to the SSE stuff.

The cost: more moving parts on the backend, which means more work to set up your
codebase and more opportunities to screw something up. And your page rendering
function needs to be _fast_ since it's going to get called a bunch of times;
that could require some restructuring (such as the example above of using an
action on page load to compute recommended posts).

htmx on the other hand doesn't try to overhaul your backend architecture. You're
typically doing standard request/response rather than this SSE thing. The
downside is that your application code has to do more stuff. You click a button,
htmx triggers a POST request, then the backend request handler has to know what
chunk(s) of html needs to be re-rendered and where those chunks should be
inserted in the DOM. Which is kind of imperative! You no longer have this dead
simple "the page is rendered by a single function" thing.

So when should you use Datastar? My take: I don't think the downsides of
Datastar I've mentioned above are _that_ big of a deal, especially if you're
using a library that sets up the plumbing for you
([ahem](https://github.com/jacobobryant/biff/tree/master/libs/datastar)).
The main situation in which I'd be tempted to not use Datastar is if I'm
building something very small where plain-old form-posts-with-redirects is fine.

I'm not sure I see any cases where I would use htmx again: I figure if I'm
making something complex enough to warrant htmx instead of plain form posts etc,
then the overhead of setting up Datastar is probably negligible.
