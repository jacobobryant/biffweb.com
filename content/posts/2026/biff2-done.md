---
title: Biff 2.0 is released
description: hashtag so glad that's over
slug: biff2-released
image: https://biffweb.com/cards/biff2-done.png
published: 2026-09-10T09:00:00 AM
---

Last April I outlined [some changes](https://biffweb.com/p/biff2/) I had planned
for Biff, which included making SQLite the default database, splitting the
monolithic `com.biffweb` namespace into a bunch of independent libraries, using
Datastar by default, introducing some new approaches for keeping large codebases
maintainable, blah blah blah, and bumping the version to `2.0.0`. I'm pleased
and slightly exhausted to announce that those changes are SHIPPED and you can
try them out like this:

```bash
git clone https://github.com/jacobobryant/biff-starter my-project
cd my-project
clj -M:run dev
```

I have [tweaked the landing page](https://biffweb.com), [written the
documentation](https://github.com/jacobobryant/biff), and even started, just
barely, to actually use Biff 2 to make a new app.

If you've used Biff 1, then please be advised that there are technically no
breaking changes (since everything is in new namespaces) and that I've written
up [some
guidance](https://github.com/jacobobryant/biff/blob/master/docs/migrating-from-biff1.md)
on gradually introducing Biff 2 into a Biff 1 codebase, if you so desire. There
_are_ [some breaking
changes](https://github.com/jacobobryant/biff/blob/master/CHANGELOG.md) if
you've already been trying out the Biff 2 prereleases.

A few things from Biff 2 that I find particularly interesting, some of which are
covered in more detail by the aforementioned [blog
post](https://biffweb.com/p/biff2):

- This
  [demo.clj](https://github.com/jacobobryant/biff-starter/blob/main/src/com/example/app/demo.clj)
  file from the starter project gives a short yet representative taste of what
  application code in a Biff project actually feels like. Note the parameters
  injected into `demo-page` by biff.graph; the POST request handlers that return
  their side effects as data via biff.fx; the fact that only a single handler
  needs to return HTML and those POST request handlers don't need to concern
  themselves with rendering at all, and yet the page is fully reactive--thanks
  to Datastar.

- The [starter project](https://github.com/jacobobryant/biff-starter) is now a
  standalone repo and can be easily forked and modified if you want to create an
  alternative starter project (with, say, a different database).

- Speaking of using different databases, there is a guide on [writing a database
  adapter](https://github.com/jacobobryant/biff/blob/master/docs/db-adapters.md).
  This makes switching out the default database much easier than it was in Biff
  1 (no need to rewrite the authentication module, for example).

- biff.core's [new module
  system](https://github.com/jacobobryant/biff/tree/master/libs/core#concepts).
  The flip side of making Biff more modular is that there's an increased need to
  have well-defined interfaces for the modular pieces to plug into. I decided to
  extract Biff's "framework" logic into a library not just to reduce
  boilerplate but also to ensure things are being done the way that Biff 2
  libraries expect.

- [`defpipeline`](https://github.com/jacobobryant/biff/tree/master/libs/fx#pipelines),
  a very recent addition to biff.fx that cuts down the boilerplate and IMO makes
  using biff.fx feel pretty ergonomic.

- [biff.graph](https://github.com/jacobobryant/biff/tree/master/libs/graph) of
  course. The whole thing.

- [biff.run](https://github.com/jacobobryant/biff/tree/master/libs/run) and
  [biff.tasks](https://github.com/jacobobryant/biff/tree/master/libs/tasks), the
  latter of which has a video demo of using the `prod-setup` and `deploy` tasks
  to deploy a vanilla (non-Biff) Clojure app to a fresh VPS.

My overall thoughts on where Biff has ended up: I'm definitely taking some bets
here. biff.fx and biff.graph are a bit weird. Awesome, but weird. Will the
benefits really matter for the projects people use Biff for? biff.core's module
system, despite being fairly lightweight, is still not as lightweight as the
5-line `reduce` call that Biff 1 used. Does that similarly push Biff further out
of good-for-a-weekend-project territory? At the same time as I'm adding these
features to benefit large codebases, does switching to SQLite--an embedded
database--make Biff less attractive for projects that are likely to end up with
large codebases?

My north star is still "what do I want for myself," so despite the hypotheticals
above, I'm not actually _that_ concerned: I think all this new stuff is
ridiculously sweet. And I do think the modularity and the related ease of making
alternative starter projects is somewhat huge. Biff is much more evolvable now.
If I've gotten anything wrong, it really shouldn't be that difficult for anyone
(including my future self) to fork my starter project and fix whatever it is
that needs fixing.

Except for biff.core; we're stuck with that part now.
