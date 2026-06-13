---
title: 'biff.fx: lightweight effects system'
description: Turn your functions into pure state machines
slug: fx
image: https://biffweb.com/cards/fx.png
published: 2026-06-16T10:00:00 AM
---

I'm releasing a couple more Biff 2 libraries. The smaller one is
[biff.config](https://github.com/jacobobryant/biff/tree/v2.x/libs/config) which
provides a [biff.core](https://github.com/jacobobryant/biff/tree/v2.x/libs/core)
component for Juxt's Aero configuration library. It's a drop-in replacement for
the old com.biffweb/use-aero-config component, with some tweaks.

The other library is
[biff.fx](https://github.com/jacobobryant/biff/tree/v2.x/libs/fx). It's a
lightweight approach to removing effects from your application logic, which
makes that logic easier to understand, test, and reuse.

There are basically two ideas here. First is the common approach of having your
code return data describing effects (http requests, database
queries/transactions, etc) it wants to run instead of running those effects
directly. So for example, instead of calling `(http/request
"https://example.com" {:query-params {:foo "bar"}})`, you would return a vector
like `[:my-application.fx/http "https://example.com" {:query-params {:foo
"bar"}}]`, and then some sort of orchestrator would call `http/request` for you.
Then it's easy to unit-test your code since it's pure, and if you wanted to swap
out certain effect implementations when running integration tests, that's easy
to do too.

You could set something like that up with Ring middleware where effects run
before and after the handler. Handler functions could somehow declare what input
data they need/what database query they need to run, if any, and then they could
return some effect data for any database transactions etc they need to do
afterward.

That works as long as you can structure your logic and effects like a sandwich,
with effects on the outside and gooey, pure logic on the inside. What if you
need logic on either side of an effect though, i.e. what if you need to
interleave logic and effects? For example, in one of my apps I have some code to
initialize a Stripe checkout session. It has to:

- Query our database to see if the current user has a Stripe customer ID
- If not: hit the Stripe API to create a new customer and save the returned ID
  in our database
- Hit the Stripe API to create a new checkout session
- Save the session ID in our database and redirect to a URL returned by the
  Stripe API

The database bits can be pushed before and after our logic, however the HTTP
requests can't. So what do we do?

One approach would be to just have a special case for situations like this under
the assumption that _most_ of your code can in fact be structured as a sandwich.
i.e. write a plain-old-impure-function and be on with your day.

Another approach is to use data not just to describe effects but also to
describe control flow. One of the shapes that approach can take is:

- Each "chunk" of logic from a previously impure function is extracted into a
  separate, pure function.
- Those functions can return data that describes both (1) what effects they need
  to run, and (2) which pure logic function should run next.

i.e. you make a state machine where the states are pure logic and effects happen
in the transitions. And that's what
[biff.fx](https://github.com/jacobobryant/biff/tree/v2.x/libs/fx) does.
