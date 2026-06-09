---
title: 'New library: biff.core'
description: System composition and other interfaces for Biff projects
slug: core
image: https://biffweb.com/cards/core.png
published: 2026-06-09T10:00:00 AM
---

As I wrote about [previously](https://biffweb.com/p/biff2/), I've been working
on splitting Biff up into a bunch of separate libraries and changing various
things along the way. I've completed a rough draft of [all twelve
libraries](https://github.com/jacobobryant/biff/tree/v2.x#libraries) and am now
going through them one-by-one to polish and release them. The first library is
now ready.

[biff.core](https://github.com/jacobobryant/biff/tree/v2.x/libs/core): system
composition and other interfaces for Biff projects. This is the glue that holds
all the other libraries together, and that's why I'm releasing it first.

For a long time Biff has had this "modules and components" structure where each
application namespace in your project exposes a "module" map, then you have a
bunch of boilerplate to combine stuff from those modules into a single "system"
map, and then we thread the system map through your "component" functions on
startup. Biff 2 retains that structure, and it has some additional stuff to deal
with that boilerplate.

For an example of what I'm talking about, see [this
code](https://github.com/jacobobryant/biff/blob/e164e341fd51ca361f065a5ecd59873c39f247b9/starter/src/com/example.clj#L25-L31)
which takes the `:routes` (and `:api-routes`) keys from your modules and turns
them into a `:biff/handler` value for the system map. I wanted a first-class way
to be able to extract that kind of logic cleanly into a library so that the
library's instructions can just be "add this module to your project" without an
accompanying "and then paste all this stuff into your main namespace."

So this new biff.core library includes a concept of "init functions." These are
functions that take a collection of modules and return a single map that can be
merged into your system map. Ta da. [Here's an
example](https://github.com/jacobobryant/biff/blob/ece1cb9dcb27c1dc6bb033f76a0a370a8e1eacd7/libs/ring/src/com/biffweb/ring.clj#L407).
Init functions are stored in the `:biff.core/init` key in your module maps, so
we get that nice "all you need are modules (well, and components)" effect.

The main complication here is that the boilerplate of defining a `(def handler
...)` var in your application code actually has a nice side benefit: late
binding. If you change any of your modules, the handler var will get updated,
and if you set `:biff/handler` in your system map to the var instead of the
value (`#’handler`), incoming Ring requests get the latest handler without you
having to restart the web server. If we extract that boilerplate into library
code, we don't get the var.

I ended up on this solution:

- Init functions take a _var_ of your modules vector, not the vector value
  itself.
- Anything in the system map that you want to get updated without a restart
  needs to be a function. In some cases this means instead of setting a
  `:com.example/my-thing` key on the system map, you need to set a
  `:com.example/get-my-thing` function which returns `my-thing`.
- That function on the system map should dereference the modules var and pass it
  to a memoized function that builds whatever thing it is you need (like the
  Ring handler).

Again, see [this
example](https://github.com/jacobobryant/biff/blob/ece1cb9dcb27c1dc6bb033f76a0a370a8e1eacd7/libs/ring/src/com/biffweb/ring.clj#L407).
The result is kind of aesthetically pleasing: you get a nice clean [main
namespace](https://github.com/jacobobryant/biff/blob/ece1cb9dcb27c1dc6bb033f76a0a370a8e1eacd7/demo/src/com/biffweb/demo.clj)
that shouldn't need to change much, and all you do is add
[modules](https://github.com/jacobobryant/biff/blob/ece1cb9dcb27c1dc6bb033f76a0a370a8e1eacd7/demo/src/com/biffweb/demo/modules.clj)
and
[components](https://github.com/jacobobryant/biff/blob/ece1cb9dcb27c1dc6bb033f76a0a370a8e1eacd7/demo/src/com/biffweb/demo/components.clj).

There's always the temptation to consolidate things further. Why even have a
separate components vector? Why not have modules support `:biff.core/on-start`
and `:biff.core/on-stop` keys and then have some way to express dependencies
between these lifecycle functions so we can call them in the right order?

And the answer is so that we don't have to have some way to express dependencies
between these lifecycle functions so we can call them in the right order. It's
not that hard to put the components in the right order yourself (especially
since the Biff starter project does that for you), and then it's easier to
understand how components work. It's just a sequence of functions that you pass
a map through. If you work on a project with so many stateful resources that
it's hard to keep track of them all, you can always layer something on top that
figures out what your components vector should be before you pass it to
biff.core.

*Plug: [my team is
hiring](https://jobs.ashbyhq.com/tyba/efd553f6-0e29-4827-af95-0fc41f063042?utm_source=5P6E9lPL4v)
for a senior software engineer, writing ClojureScript and Python mostly. We make modeling software
for renewable energy projects.*
