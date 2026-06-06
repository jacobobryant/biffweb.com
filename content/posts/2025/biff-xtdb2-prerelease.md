---
title: "Biff support for XTDB v2 is in pre-release"
description: The time is at hand
slug: xtdb2-prerelease
image: https://biffweb.com/cards/xtdb2-prerelease.png
published: 2025-11-09T03:00:00 AM
---

I've been working on/preparing for migrating Biff to XTDB v2 since that [became generally
available](https://xtdb.com/blog/launching-xtdb-v2) in June. After investigating the deployment
options and performance characteristics, I've added some XTDB v2 helper functions to the Biff
library (under a new `com.biffweb.experimental` namespace) and I've made a version of the starter
project that uses XTDB v2.

You can create a new XTDB v2 Biff project by running `clj -M -e '(load-string (slurp
"https://biffweb.com/new.clj"))' -M xtdb2`. See [this
gist](https://gist.github.com/jacobobryant/7c2853f2fa391d8d30f19f363709ffc5) for a diff between the
old/main starter project and this new one.

To give you a quick overview of what Biff provides:

- There are `use-xtdb2` and `use-xtdb2-listener` components, roughly the same as we have already for
  XTDB v1.
- The `ctx` map will have a `:biff/conn` key in it (a Hikari connection pool object) which you can
  pass to `xtdb.api/q` to do queries.
- There is no longer a custom Biff transaction format. There is still a lightweight wrapper
  function, `com.biffweb.experimental/submit-tx`, which will apply Malli validation to any
  `:put-docs` / `:patch-docs` operations.

There's still plenty of work to do before XTDB v2 support in Biff is officially released and becomes
the default:

- Next up, I'm migrating [Yakread](https://github.com/jacobobryant/yakread) to XTDB v2. This will
  help me find any more issues that need to be addressed/make sure that Biff is indeed ready for
  XTDB v2.
- After that I need to update a bunch of documentation, including the tutorial project.

Since those next two steps will take a while, I wanted to do this "pre-release" for anyone who would
like to get a head start on trying out Biff with XTDB v2. If you do so, let me know whatever
questions/comments you have. Just note that the new functions in Biff's API are still experimental
and might have breaking changes before I do the official release.

And for anyone who would rather not deal with migrating an existing app, Biff will still support
XTDB v1. It's totally fine to stay on that.

Finally: I'll be at Clojure/Conj next week, at least if my flight doesn't get canceled. Come say hi.
