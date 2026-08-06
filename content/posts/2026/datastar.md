---
title: biff.datastar, biff.ring
description: The releases will continue until morale improves
slug: datastar
image: https://biffweb.com/cards/datastar.png
published: 2026-08-11T10:00:00 AM
---

A couple more Biff 2 libraries are out the door:

- [biff.datastar](https://github.com/jacobobryant/biff/tree/v2.x/libs/datastar):
  the dumbest/awesomest possible way to make a reactive, server-side-rendered
  web app. Over the past several years I haven't put much priority on making it
  easy to make fancy reactive/real-time/collaborative UIs since my UI needs are
  typically pretty simple. But this architecture is actually really nice even
  when you don't need the fancy stuff.

- [biff.ring](https://github.com/jacobobryant/biff/tree/v2.x/libs/ring): mostly
  some Biff-related plumbing code. I think the `defroute` macro is pretty nice.
  There's a cool `wrap-csrf-protection` middleware that [doesn't use
  tokens](https://words.filippo.io/csrf).

The last "big" Biff 2 library I need to fix up and release will be biff.tasks,
the one for all the CLI tasks. Other than that there's biff.authentication, the
email-powered authentication module thing (now with a default sign-in form
included) and a few other doodads. And then a starter app. And some
documentation that ties all the libraries together, not just documentation for
the individual libraries (which I've been writing as I go).

I'm still shooting to have all that released before the conj, which is... coming
up.

--Jacob
