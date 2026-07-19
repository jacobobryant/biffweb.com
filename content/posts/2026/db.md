---
title: Database adapters in Biff 2
description: I've released biff.sqlite and biff.xtdb
slug: db-adapters
image:
published: 2026-07-21T10:00:00 AM
---

TODO push latest changes and update links

I've released two new Biff libraries, both database adapters:
[biff.sqlite](https://github.com/jacobobryant/biff/tree/v2.x/libs/sqlite)
and
[biff.xtdb](https://github.com/jacobobryant/biff/tree/v2.x/libs/xtdb).
Both of them implement some interfaces used by various other Biff libraries, and
they also both implement additional functionality that can be useful to Clojure
apps even if they aren't using Biff.

# The interfaces

So far, modifying a Biff app to use a different database than the default has
been [kind of
inconvenient](https://biffweb.com/p/how-to-use-postgres-with-biff/), as a result
of [philosophy](https://biffweb.com/p/philosophy-of-biff/) about modularity for
Biff 1. The ability to swap out defaults was more of an escape hatch so that
starting out with Biff doesn't mean your locked into all its choices forever. So
that meant if you wanted to swap out the database, you had to, for example, copy
and paste all of Biff's sign-in-via-email code and rewrite the queries.

With Biff 2, modularity is becoming [more of a first-class
citizen](https://biffweb.com/p/biff2/#alternate-starter-projects-will-get-easier).
Hence these new "interfaces." And the main interface here addresses the question
of, you know, how do you put things in a database. And get them back out. For
Biff 2, I wanted be able to package up shared application functionality that
needs persistence (such as the authentication module) in a database-agnostic
way.

So Biff 2 now defines a key-value store interface which can be implemented by
database adapter libraries like the two I've just released:

- [`:biff.core/kv-set`](https://github.com/jacobobryant/biff/blob/v2.x/libs/core/docs/reference/schema.md#biffcorekv-set)
- [`:biff.core/kv-get`](https://github.com/jacobobryant/biff/blob/v2.x/libs/core/docs/reference/schema.md#biffcorekv-get)
- [`:biff.core/kv-list`](https://github.com/jacobobryant/biff/blob/v2.x/libs/core/docs/reference/schema.md#biffcorekv-list)

And my two implementations:
[sqlite](
https://github.com/jacobobryant/biff/blob/4fa6e380275d412458c16623036729a386299cd7/libs/sqlite/src/com/biffweb/sqlite/impl/kv.clj)
and
[xtdb](https://github.com/jacobobryant/biff/blob/4fa6e380275d412458c16623036729a386299cd7/libs/xtdb/src/com/biffweb/xtdb/impl/kv.clj).
These KV-store functions are exposed via a biff.core module, [for
example](https://github.com/jacobobryant/biff/blob/4fa6e380275d412458c16623036729a386299cd7/libs/sqlite/src/com/biffweb/sqlite/impl/system.clj#L34).

In general I try to avoid adding layers to things unnecessarily, so it took a
little thinking to arrive at this solution. My main thought was that I'm
primarily interested in database-agnosticism for libraries, not applications.
Migrating a single application's database is a different use case than wanting
to provide functionality for multiple applications using different databases,
and it's the latter use case that I'm trying to address.

So I didn't want to introduce some sort of "Biff query/transaction language"
that you'd use whenever writing a Biff app; that would be overkill. The
database-agnostic libraries I wanted to write have only basic persistence needs,
so a key-value interface is sufficient. And that's an easy interface for
database adapters to implement.

The second biggest interface-type-thing is that both adapters come with
`make-resolvers` functions ([sqlite] / [xtdb]) which generate a set of
[biff.graph resolvers] for the tables in your application schema.

# The features

Besides the key-value functions and a couple other doodads, the remaining
functionality in these database libraries is just whatever stuff I thought would
be nice to have when writing a Clojure app using the respective databases. From
biff.sqlite's README:

> - Sane defaults like WAL mode, STRICT tables, etc.
> - Backup/restore via [Litestream](https://litestream.io).
> - Migrations via [sqldef](https://github.com/sqldef/sqldef).
> - Rich schema types: define columns as e.g. booleans, instants, nested maps,
>   etc; and biff.sqlite converts them to/from SQLite's supported types
>   (ints, blobs, etc).
> - Validate transactions based on centralized authorization rules you define
>   (helps to keep LLM code secure).

And for biff.xtdb:

> - Start up an in-process node with high-level config defaults.
> - Custom `:biff/upsert` and `:biff/assert-unique` transaction operations.
> - Optionally enforce Malli schemas on write.
> - Define centralized authorization rules for validating transactions.

biff.sqlite is a chonkier library due to all the logic needed for supporting
rich schema types.

# Write your own database adapter

Finally, I have written a [database adapter
guide](https://github.com/jacobobryant/biff/blob/v2.x/docs/db-adapters.md) which
lists all the interfaces that adapters should implement and suggests additional
features that may or may not be relevant for any given database. Between that
guide and the two sqlite/xtdb reference implementations, I'm hoping it should be
straightforward to implement an adapter for whatever database you want to use. I
will probably only maintain the SQLite and XTDB adapters, but I might publish
some as-is code for other databases which could be picked up and maintained
anyone who so chooses.
