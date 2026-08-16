---
title: A bundle of CLI tasks for tools.deps projects
description: I've released biff.run and biff.tasks
slug: tasks
image: https://biffweb.com/cards/tasks.png
published: 2026-08-18T10:00:00 AM
---

I've just released
[biff.run](https://github.com/jacobobryant/biff/tree/v2.x/libs/run), a very
light `clj`-based task runner; and
[biff.tasks](https://github.com/jacobobryant/biff/tree/v2.x/libs/tasks), a bunch
of default CLI tasks that can be used in tools.deps projects.

The tasks are mostly the same as what I've already been including with Biff
projects for the past several years. I've reworked them a bit, added a few new
tasks, and have structured them to be hopefully useful for tools.deps projects
in general rather than being coupled to "Biff projects."

The biff.tasks docs linked above gives a good overview of what tasks it
includes. Some unorganized thoughts/random stuff I think is interesting/design
opinions:

- biff.run is an answer to the question "what's an ergonomic way to publish a
  curated collection of tasks rather than a single task." Instead of one
  deps.edn alias per task, there's a single `:run` alias for all your tasks.

- A criticism of tools.deps is that it's lacked various functionality that lein
  has had built in (simple but not necessarily easy). biff.tasks is my take on
  addressing that, and I'm interested in if it could help people get going with
  Clojure more smoothly. However Biff users specifically are still the main
  audience I'm designing stuff for; time's limited.

- Startup time has been fine. I've settled on a few approaches for managing it:
  each task goes in a separate namespace so biff.run can load only the code for
  the task you're running; tasks use `requiring-resolve` when they have heavy
  dependencies that are used conditionally; some tasks shell out to pre-compiled
  binaries (like `cljfmt` and `clj-kondo`). In return, being able to do
  everything in plain Clojure and use the regular dependency tooling is nice.

- I did tinker with passing the classpath to Babashka like `bb -cp $(clj -A:run
  -Spath) -m com.biffweb.tasks.lib -h`. Seems like it'd be kinda cool to default
  to plain clj but then say "push this button to get instant startup times with
  Babashka instead." It hasn't been a huge need for me though so I haven't
  explored it too much.

- Speaking of pre-compiled binaries, biff.tasks uses some [internal
  machinery](https://github.com/jacobobryant/biff/blob/v2.x/libs/stuff/src/com/biffweb/stuff/bin.clj)
  that makes working with these binaries pretty seamless. biff.tasks specifies a
  default version for each binary, users can set a different version in their
  project config if desired, and whenever a task needs to use that binary,
  biff.tasks ensures the right version is installed. If not, biff.tasks
  downloads the correct version.
  [Example](https://github.com/jacobobryant/biff/blob/e93a094ba5127a55875af661f435312bedcd6bb0/libs/tasks/src/com/biffweb/tasks/impl/format.clj#L42).

- biff.tasks doesn't include a task for creating a new project because IMO
  project templates should provide a way to use them without having to install
  another tool first. That's perhaps the one bit of functionality that might be
  nice to have baked into `clj`; only then could I as a project template author
  safely assume that all my users will already have the task installed.

- biff.tasks _does_ come with an `init` task that renames the current project's
  main namespace from `com.example` to a namespace that the user provides. The
  idea is that you use project templates by doing `git clone <some template>; rm
  -rf .git; git init; clj -M:run init`. (The project template would have
  biff.tasks already installed in its deps.edn). Probably with that all wrapped
  up into a little script so you get a nice one-liner.

- For library projects, I settled on a `docs` task that turns your docstrings
  into markdown files [like this
  one](https://github.com/jacobobryant/biff/blob/97ed32fe82ee6de46bdc27d3cfa2f8f26034484a/libs/tasks/docs/api/com.biffweb.tasks.md).
  I'm quite fond of it.
