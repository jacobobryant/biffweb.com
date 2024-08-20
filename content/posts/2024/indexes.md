---
title: 'Indexes pre-release'
slug: indexes-prerelease
description: 'The new "indexes" feature makes it easy to denormalize your main XT database so you can query for things faster.'
image: /cards/indexes-prerelease.png
published: 2024-06-19T05:50:00 PM
---

*Quick pre-announcement: I got my ticket for the conj! Come say hi if you'll be there. I'll be one of the ponytailed dudes.*

I just finished adding support for a new "indexes" feature. This makes it easy to create materialized
views/derivations/denormalizations of the data in your main XT database so you can query for things faster&mdash;it
solves the problem of "this query was fast when my app was a prototype but now I actually have some users and the query
is too slow." It's built on the same (undocumented) API that XTDB's [Lucene
module](https://v1-docs.xtdb.com/extensions/1.24.3/full-text-search/) uses to provide a full-text search index.

Each index gets its own XT node (persisted to the filesystem). You define a function that takes the current index DB
value and a new transaction from your main XT node and returns a new transaction for your index XT node. For example,
I've made an index in the starter app for keeping track of how many users have signed up:

```clojure
;; src/com/example/worker.clj
;; ...
(defn print-usage [ctx]
  (let [{:keys [n-users]} (biff/index-snapshots ctx)
        value (get (xt/entity n-users :n-users) :value 0)]
    (log/info "There are" value "users.")))

(defn index-n-users [{:xtdb.api/keys [tx-ops tx-id]
                      :biff.index/keys [docs db]}]
  (let [tx (keep (fn [{:keys [user/email]}]
                   (when (and (some? email)
                              (empty? (xt/entity db email)))
                     [::xt/put {:xt/id email}]))
                 docs)
        new-value (+ (:value (xt/entity db :n-users) 0)
                     (count tx))]
    (when (not-empty tx)
      (conj tx [::xt/put {:xt/id :n-users, :value new-value}]))))

(def module
  {:indexes [{:id :n-users
              :version 1
              :indexer #'index-n-users}]
   ;; ...
   })
```

So you just need to define these "indexer" functions, and Biff + XTDB handle keeping the indexes up to date.
If you increment your index's version, Biff will re-index from the start of the main transaction log.
`biff/index-snapshots` gives you a set of index DB snapshots (and a snapshot of your main database) that are all
consistent; i.e. the index DBs contain the result of indexing everything that's in the main database snapshot and
nothing more. (Getting that in place was most of the work for this feature.) If your indexer throws an exception or
returns an invalid XT transaction, Biff will log an error message and continue indexing subsequent transactions (unless
you set `:abort-on-error true`). There's a `biff/replay-indexer` function you can use to debug indexing failures.

It's on an experimental `indexes` branch for now. You can look at [this
commit](https://github.com/jacobobryant/biff/commit/2e253115906f90713c25abb62594dff567af6866) to see how the feature
looks in the starter app (I made two indexes and displayed their results in the web app) and [this
commit](https://github.com/jacobobryant/biff/commit/e070f723fe2e87d63c736364ad2a078099d0930c) if you're interested in
the implementation. If you'd like to play around with it, you can either update your Biff
version to <code style="word-wrap:anywhere;">com.biffweb/biff {:git/url "https://github.com/jacobobryant/biff", :git/sha
"2e253115906f90713c25abb62594dff567af6866"}</code>, or create a new project with `clj -M -e '(load-string (slurp
"https://biffweb.com/new.clj"))' -M indexes`. Just be aware that since this is a pre-release, there may be breaking
changes.

I'm planning start using this in [Yakread](https://yakread.com) which has several pages that are in sore need of faster
queries; after the feature has been proved out there, I'll write reference docs, add a section to the tutorial, maybe
even throw in some unit tests, then merge it into master. In the mean time, give it a spin and tell me how it goes. I've
been looking forward to having this feature for a long time.