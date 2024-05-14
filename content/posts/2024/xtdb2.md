---
title: 'Trying out XTDB2'
slug: trying-out-xtdb2
description: I recently finished an experimental migration of the Biff starter app to use XTDB v2 instead of v1.
image: /cards/xtdb2.png
published: 2024-05-14T09:30:00 AM
---

I recently finished an experimental migration of the Biff starter app to use [XTDB v2](https://xtdb.com/) instead of
XTDB v1. (For those unfamiliar with XTDB, see [XTDB compared to other
databases](https://biffweb.com/p/xtdb-compared-to-other-databases/).) I've pushed it to an [`xtdb2`
branch](https://github.com/jacobobryant/biff/tree/xtdb2/starter-xtdb2) where it will remain until v2 hits general
availability. If you'd like to poke around the code, you can clone the repo:

```clojure
git clone https://github.com/jacobobryant/biff
cd biff
git checkout xtdb2
cd starter-xtdb2
clj -M:run dev
```

All Biff's library code is bundled into a big giant `com.biffweb` namespace, which includes various functions that call in to
XTDB v1. To facilitate using v2 instead, I've created a separate small library that provides a `com.biffweb.xtdb`
namespace, containing a bunch of v2-related stuff. It mocks out all the v1 functions that `com.biffweb` uses, so
you can (and must) safely exclude v1 from your transitive dependencies:

```clojure
{:deps {com.biffweb/biff       {...
                                :exclusions [com.xtdb/xtdb-core
                                             com.xtdb/xtdb-jdbc
                                             com.xtdb/xtdb/rocksdb]}
        com.biffweb/biff-xtdb2 {...}
```

`com.biffweb.xtdb` has a few different things in it. First, there's a `use-xtdb` Biff component:

```clojure
(def biff-tx-fns
  '{:biff/if-exists (fn [[query query-args] true-branch false-branch]
                      (if (not-empty (q query {:args query-args}))
                        true-branch
                        false-branch))})

(defn use-xtdb [ctx]
  (let [node (xt-node/start-node {})
        stop #(.close node)]
    (xt/submit-tx node (for [[fn-key fn-body] biff-tx-fns]
                         [:put-fn fn-key fn-body]))
    (-> ctx
        (assoc :biff/node node)
        (update :biff/stop conj stop))))
```

This'll start up an XTDB node and put it in your system map. It also creates a `:biff/if-exists` transaction function
that I've used to implement an upsert operation (more on that below). For now the component is hard-coded to create an
in-memory node; I haven't bothered to make it configurable since this was just an exploratory exercise. I should also
make it only save `biff-tx-fns` if the values are different from what's in the DB.

### Transactions

Next, there's a `submit-tx` function which wraps `xtdb.api/submit-tx` (I couldn't resist). Like the `biff/submit-tx` function for XTDB v1,
this new `submit-tx` adds schema checking with Malli and supports a few additional custom operations. The implementation
is a lot simpler though (and extensible via multimethod!):

```clojure
(defmulti compile-op (fn [[op & _]] op))

(defmethod compile-op :biff/upsert
  [[_ table on-doc & [{set-doc :set :keys [defaults]}]]]
  (check-args table :keyword
              on-doc map?
              set-doc [:maybe map?]
              defaults [:maybe map?])
  (let [new-doc (merge {:xt/id (random-uuid)} set-doc defaults on-doc)
        _ (check-table-schema table new-doc)
        on-keys (keys on-doc)
        query (xt/template (-> (from ~table [~(bind-template on-keys)])
                               (limit 1)))]
    [[:call :biff/if-exists [query on-doc]
      (when (not-empty set-doc)
        [[:update {:table table
                   :bind [(bind-template on-keys)]
                    ;; TODO try to pass in set-doc via args? does it even matter?
                   :set set-doc}
          on-doc]])
      [[:put-docs table new-doc]]]]))

...

(defn compile-tx [biff-tx]
  (into [] (mapcat compile-op biff-tx)))

(defn submit-tx [node biff-tx]
  (xt/submit-tx node (compile-tx biff-tx)))

;; Example:

(submit-tx node
  [[:biff/upsert :user
                 {:email "alice@example.com"}
                 {:set {:color "blue"}
                  :defaults {:joined-at (Instant/now)}}]])
```

`biff-xt/submit-tx` has the same signature as `xt/submit-tx`. You pass in a node and a transaction, i.e. a collection of
operations&mdash;like `[:put-docs ...]` (built-in) or `[:biff/upsert ...]` (added by Biff). Each operation gets passed
through `compile-op`, a multimethod that returns a vector of one or more operations.

I've written methods for all the built-in operations. These perform schema checking and then return the given
operation without changes. e.g. if you do `[:put-docs :user ...]`, Biff will ensure that all the docs you include
conform to the `:user` schema. (I've changed the starter project to pass your application schema to
`malli.registry/set-default-registry!`, so `submit-tx` can access your `:user` schema without you having to pass it in
as an extra argument somewhere).

```clojure
(ns com.example.schema)

(def schema
  {::short-string [:string {:min 1 :max 100}]
   ::long-string  [:string {:min 1 :max 5000}]

   :user [:map {:closed true}
          [:xt/id     :uuid]
          [:email     [:and ::short-string [:re #".+@.+"]]]
          [:joined-at :time/instant]
          [:foo {:optional true} ::short-string]
          [:bar {:optional true} ::short-string]]

   :message [:map {:closed true}
             [:xt/id   :uuid]
             [:user    :uuid]
             [:text    ::long-string]
             [:sent-at :time/instant]]})

(def module
  {:schema schema})
```

I've also written methods for three custom operations: `:biff/upsert` as shown above, `:biff/update`, and
`:biff/delete`. The latter two are simple convenience wrappers around the built-in `:update` and `:delete` operations.
Here's an example from the starter app:

```clojure
(bxt/submit-tx node
  [[:biff/update :user {:set {:foo (:foo params)}
                        :where {:xt/id (:uid session)}}]])
```

That's equivalent to the following SQL, which by the way, is also something you can do with XTDB v2!

```clojure
(submit-tx node
  [[:sql "UPDATE user SET foo = ? WHERE user.xt$id = ?"
    [(:foo params) (:uid session)]]])
```

It's really quite cool how easy it is to mix-and-match XTQL and SQL. No need to set up a separate SQL driver or
anything. From XT's perspective, `:sql` is just another XTQL operation that happens to take a string as its first
parameter.

Unfortunately Biff's schema checking doesn't work with SQL, so my advice to Biff users will probably be to stick with
XTQL/Biff's custom operations for transactions, and only use SQL if desired for...

### Queries

Querying with SQL is similarly convenient:

```clojure
(xt/q node "SELECT * FROM user WHERE user.xt$id = ?" {:args [(:uid session)]})
(xt/q node '(-> (from :user [*]) (where (= xt$id $uid))) {:args session})
```

For simple queries like this one, `com.biffweb.xtdb` has a couple convenience functions like `(lookup node :user :email
"bob@example.com")`, which retrieves a document based on a given key-value pair(s). Same as the `lookup*` functions Biff
provides for XTDB v1.

### What's missing

The main blocker to using this in production is that the [only options](https://docs.xtdb.com/config.html) for
persistent storage as of right now are (1) local disk, (2) Kafka + object storage on AWS/Azure/Google Cloud. Since Biff
is targeted toward solo developers, XTDB v1's ability to piggy-back off of Postgres is a killer feature&mdash;Postgres
is everywhere. If you're building a side project and you know you'll be fine running it on a single machine forever, you
could use local disk storage with automated backups; I just wouldn't want to make XTDB v2 the default in Biff until
there are more options.

(For the record I mentioned this on the forum and [@jarohen
replied](https://discuss.xtdb.com/t/v2-start-node-listens-on-port-8080/399/5?u=jacobobryant), "we’re certainly aware of
and agree with the desire for a non-Kafka tx-log.")

XTDB v2 also does not yet support [transaction listeners](https://v1-docs.xtdb.com/clients/clojure/#_listen) or
[open-tx-log](https://v1-docs.xtdb.com/clients/clojure/#_open_tx_log) yet. Biff's
starter app uses one of those to watch for new chat messages so it can push them out to websocket clients; basically you
can re-use the transaction log as a message queue. For the `xtdb2` branch I've restructured the starter app so that the
web server that saves the new message also immediately pushes it to websocket clients. This works fine for a single web
server, but if you scale to N web servers you'd want to set up e.g. Redis and use that for the queue.

A more interesting benefit of being able to access the transaction log directly is that you can use it for event
sourcing/materialized views, which is useful as soon as you start running into queries that take too long to execute. I
have so far never actually *done* this with XTDB v1 (apologies to my [Yakread](https://yakread.com) users who have
imported thousands of RSS feeds and can no longer load the subscriptions page), but I've always wanted to and it is in
fact the very next thing on my Biff todo list after publishing this post. I'm hoping this will end up being another
Killer Feature® for Biff once I get something working and polished.

### So yeah, go give XTDB v2 a spin

Besides the performance benefits (columnar storage something something? Forgive me for not having actually done any
benchmarks as part of this exercise), I'm very excited&mdash;or at least, as excited as an introvert can get&mdash;about
having a database that embraces immutability and is aiming for mainstream adoption. I may not use XTDB v2's SQL support
myself, but I'm very happy that everyone will have the option. ["We need to make XTDB as easy to use for Elixir and
Python users as it currently is for Clojure (and other JVM) users."](https://xtdb.com/blog/dev-diary-may-22)