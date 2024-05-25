---
description: I've created an example repo that starts with a fresh Biff project and then modifies it to use Postgres instead of XTDB, for those who would like to use a traditional RDBMS.
slug: how-to-use-postgres-with-biff
title: How to use Postgres with Biff
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/4bea0bc9-77d6-45d3-91b5-9a4f5d5df0e2
published: 2023-11-22T13:15:34 PM
---

I've created [an example repo](https://github.com/jacobobryant/biff-postgres) that starts with a fresh Biff project and
then modifies it to use Postgres instead of XTDB, for those who would like to use a traditional RDBMS. If you'd like to
use Datomic or any other database, you can also use this as a more general guide for what parts of your project will
need to be changed. If you're unfamiliar with XTDB, [here's some background
information](https://biffweb.com/p/xtdb-compared-to-other-databases/) on why it's the default in Biff.

I've split the steps up into [separate commits](https://github.com/jacobobryant/biff-postgres/commits/master); you can
read those and apply the same changes in your own project (be sure to read the commits from bottom to top). I've written
commentary on the changes below.

Also: since [XTDB v2](https://www.xtdb.com/v2) is adding SQL as a first-class citizen, when that becomes stable I'll
probably merge a lot of these changes into Biff's default template project (sans the actual Postgres bits). SQL would be
the default since more people are familiar with it, but those who want to (including me!) can easily switch to Datalog.

### 1\. Start a new project

[Pretty straightforward](https://biffweb.com/docs/get-started/new-project/). I've used `com.biffweb.example.postgres`
for the main namespace.

### 2\. Make \`clj -M:dev dev\` start a Postgres container with Docker

[Diff](https://github.com/jacobobryant/biff-postgres/commit/6e293ebbbee6d1f8128ea3b95514b46a4a315b93). I copied over
[the
source](https://github.com/jacobobryant/biff/blob/165b8aa11d47da5535b2267db51f32e838b6e19e/libs/tasks/src/com/biffweb/tasks.clj#L258)
for the regular `clj -M:dev dev` command, updated namespaces appropriately, and then had it call a new `start-postgres`
function in the background. That function will run `docker pull postgres` if needed and start up a Postgres container
using the configuration you specify. The container will be deleted when you stop your app (Ctrl-C), but data will be
persisted to the `storage/postgres` directory.

You'll need to have Docker set up already. As long as you can run `docker ps` successfully you should be good to go.
After making the above changes, run `clj -M:dev dev` and make sure you don't get any error messages. You should be able
to get an interactive prompt with  `psql postgresql://user:abc123@localhost:5432/main`.

### 3\. Connect to Postgres from the REPL

[Diff](https://github.com/jacobobryant/biff-postgres/commit/19e04cfb3230103d41a5d1320241094f16ecb770). I've added a
`use-postgres` component that will run migrations on app startup, via a very simple approach [courtesy of
pesterhazy](https://clojureverse.org/t/how-do-you-do-database-migration-evolution/2005/2). The `migrations.sql` file
creates all the tables corresponding to the existing schema both for [the starter
app](https://github.com/jacobobryant/biff/blob/165b8aa11d47da5535b2267db51f32e838b6e19e/starter/src/com/example/schema.clj)
and [for the authentication
module](https://github.com/jacobobryant/biff/blob/165b8aa11d47da5535b2267db51f32e838b6e19e/src/com/biffweb/impl/auth.clj#L262).
I'm using [next.jdbc](https://github.com/seancorfield/next-jdbc) as the Postgres client. I'm not using any SQL wrapper
libs like [HoneySQL](https://github.com/seancorfield/honeysql) or [HugSQL](https://www.hugsql.org/), but you can add
them yourself if desired.

**Note:** the diff includes a `resources/config.template.env` file which defines a `DEV_POSTGRES_URL` environment
variable. You'll need to add that variable to `config.env`.

In `dev/repl.clj`, the XTDB examples have been replaced with Postgres examples. After starting up the app with `clj
-M:dev dev`, you should be able to evaluate them via the REPL.

There is also a `com.biffweb.example.postgres.util.postgres` namespace which contains some functions used in the
following sections.

### 4\. Copy over the authentication module code

[Diff](https://github.com/jacobobryant/biff-postgres/commit/de8560445a994183db95139b17c9d1d4269dede0). Biff's
authentication module assumes you're using XTDB. We'll copy-and-paste it into our project so that we can modify it to
use Postgres.

[The original
module](https://github.com/jacobobryant/biff/blob/165b8aa11d47da5535b2267db51f32e838b6e19e/src/com/biffweb/impl/auth.clj)
uses a bunch of `com.biffweb.impl.*` namespaces which are not part of Biff's public API. All the functions are aliased
in the public `com.biffweb` namespace however, so we can find-and-replace all the functions over to that.

### 5\. Modify the authentication module to use Postgres

[Diff](https://github.com/jacobobryant/biff-postgres/commit/f722cc96ac992b00436329adf5051365bccc39d3). This a fairly
mechanical transformation; we just look for all the places XTDB is used and translate that code to Postgres.

The original module had `:biff.auth/get-user-id` and `:biff.auth/new-user-tx` options which could be used to override
the default implementations. Since the module is part of our project, there's no need for those options. If you want to
change your user-creation code, you can just update `util-pg/new-user-statement`.

At this point, you should be able to visit `localhost:8080` and log in to the app.

### 6\. Modify the rest of your app to use Postgres

[Diff](https://github.com/jacobobryant/biff-postgres/commit/44d55ac76b8c4b991ce5b658de0a7880e5fbb6b8). Basically the
same thing as the previous step, but for the remaining parts of the codebase. I also
renamed `:com.biffweb.example.postgres/chat-clients` to `:example/chat-clients` for convenience. I'll admit, some of
these diffs are reminding me that even Biff's `submit-tx` wrapper is not as succinct as it could be for common
use-cases.

This diff isn't a complete 1:1 translation because I haven't added an equivalent to XTDB's transaction listeners. Maybe
there's a not-terribly-complicated way to add transaction listeners to Postgres (?), but I suspect if you want similar
functionality, you'll want to add some queues (either [via
Postgres](https://www.startpage.com/do/dsearch?query=postgres+queues), or you could throw in Redis...) and
publish/subscribe to those.

Normally, the starter Biff app uses transaction listeners to watch for new chat messages. Whenever a message comes in,
the app sends it to all the currently connected clients via websocket. This works even if you have multiple web servers;
each web server will send the chat message to their own set of clients.

In this Postgres repo, I've modified the `send-message` function (which normally just inserts new chat messages into
XTDB) so that it also pushes new messages to websocket clients. This will work fine as long as you only have one web
server, which I'm sure is the most common case for Biff users anyway.

The app should be fully functional now; head over to `localhost:8080` again and give it a spin.

### 7\. Remove the XTDB dependency

[Diff](https://github.com/jacobobryant/biff-postgres/commit/4b445959db818bf1f1ad62ecf73952e00884bfb2). Now that all of
our app's functionality has been migrated over to Postgres, we can remove any lingering XTDB usages, particularly the
`use-xt` and `use-tx-listener` components. I've also set the `:biff/merge-context-fn` system map key to `identity`. The
default function for that key uses the `xtdb.api/db` function to insert database objects into incoming Ring requests.

The final step is to exclude XTDB from our `deps.edn` dependencies. I haven't bothered to split Biff up into separate
libraries that can be mixed-and-matched. Instead, I've created a `com.biffweb/xtdb-mock` library which includes
replacements for all of the XTDB functions that Biff calls. If you actually call any of them, they throw an exception.
Once you add this library to your dependencies, you can safely add `com.xtdb/xtdb-core` etc. to the `:exclusions` key
for `com.biffweb/biff`.

Appendix: Biff's approach to project template customization -----------------------------------------------------------

Applying these changes manually will be fairly tedious, but at least it's easier than figuring out how to do it all on
your own 🙂. For a new project, you could just fork this repo and change the namespaces, but there are a couple
advantages if you instead create a new project [the regular way](https://biffweb.com/docs/get-started/new-project/) and
then apply the changes manually:

*   You're guaranteed to be on the latest version of Biff (including any updates to the app template). I'm not planning
    to keep this repo up-to-date with new versions of Biff.
*   You can combine this with other changes if you want, e.g. I'll soon be writing another how-to guide that shows how
    to replace/complement htmx with [re-frame](https://day8.github.io/re-frame/re-frame/).
*   You don't have to change the namespaces manually (including keyword namespaces in various places, like the config
    file).

(If you decide to fork this repo anyway, we can still be friends.)

I think leaving this as a manual process [fits well with Biff](https://biffweb.com/p/philosophy-of-biff/), since I
expect most Biff users will stick with the default stack anyway and I'd prefer to focus most my efforts there. This is
probably the main conceptual difference between Biff and [Kit](https://kit-clj.github.io/), by the way. If you're
interested in a framework that has robust support for automatically modifying the stack, even after you've created your
project, then [Kit's module system](https://yogthos.net/posts/2022-01-08-IntroducingKit.html) is definitely worth
checking out.

That being said, going through this process has given me some ideas for some conventions that Biff could adopt around
3rd-party project templates. If you want to create and maintain one of those, feel free to message me or ask about it on
`#biff`.