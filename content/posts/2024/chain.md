---
title: 'Removing effects from business logic'
slug: removing-effects
description: "Here's a thing I'm trying out to make testing easier."
image: /cards/rocksdb-indexes-yakread.png
published: 2024-10-05T12:15:00 PM
---

This is a thing (state machine? effects-processing pipeline?) I'm trying out to keep effectful code separate from
business logic, to make testing easier. It's partially inspired by re-frame's event handling. The idea isn't new, but
I'd never actually done it for my own code until now. I'd be curious to hear what other things people have done in this
vein. I like this more than the typical approach of just mocking out the effectful bits.

I was working on the following function, which is the HTTP request handler for a "subscribe to RSS feed" button. You
enter a URL into a form, then this handler:

1. Fetches the URL and figures out if it goes directly to an RSS feed _or_ if it goes to a page that has an RSS feed(s)
   in its metadata.

2. If there aren't any feeds, show an error message. Otherwise, save the feeds to the database and redirect to the
   user's subscriptions page.

```clojure
(def rss-route
  ["/dev/subscriptions/add/rss"
   {:name :app.subscriptions.add/rss
    :post
    (fn [{:keys [session
                 params]
          :as ctx}]
      (let [url (lib.rss/fix-url (:url params))
            http-response (http/get url {"User-Agent" "https://yakread.com"})
            feed-urls (->> (lib.rss/parse-urls (assoc http-response :url url))
                           (mapv :url)
                           (take 20)
                           vec)]
        (if (empty? feed-urls)
          {:status                     303
           :biff.response/route-name   :app.subscriptions.add/page
           :biff.response/route-params {:error "invalid-rss-feed"}}
          (do
            (biff/submit-tx ctx
              (for [url feed-urls]
                {:db/doc-type :conn/rss
                 :db.op/upsert {:conn/user (:uid session)
                                :conn.rss/url url}
                 :conn.rss/subscribed-at :db/now}))
            {:status                     303
             :biff.response/route-name   :app.subscriptions/page
             :biff.response/route-params {:added-feeds (count feed-urls)}}))))}])
```

(I have other middleware that takes the `:biff.response/*` values and converts them to `:headers {"location" ...}`.)

This handler has two effects: first it it calls `clj-http.client/get` on the URL that the user submitted; second, it
calls `com.biffweb/submit-tx` if it gets a valid RSS url(s).

And here is the purified version of that handler, which I'll explain in a moment:

```clojure
(def rss-route
  ["/dev/subscriptions/add/rss"
   {:name :app.subscriptions.add/rss
    :post
    (fn [{:keys [session
                 params
                 biff.chain/state]
          :as ctx}]
      (case state
        nil
        {:biff.chain/queue      [:biff.chain/http ::add-urls]
         :biff.chain.http/input {:url     (lib.rss/fix-url (:url params))
                                 :method  :get
                                 :headers {"User-Agent" "https://yakread.com/"}}}

        ::add-urls
        (let [feed-urls (->> (lib.rss/parse-urls (:biff.chain.http/output ctx))
                             (mapv :url)
                             (take 20)
                             vec)]
          (if (empty? feed-urls)
            {:status                     303
             :biff.response/route-name   :app.subscriptions.add/page
             :biff.response/route-params {:error "invalid-rss-feed"}}
            {:biff.chain/queue    [:biff.chain/tx ::success]
             :biff.chain.tx/input (vec
                                   (for [url feed-urls]
                                     {:db/doc-type :conn/rss
                                      :db.op/upsert {:conn/user (:uid session)
                                                     :conn.rss/url url}
                                      :conn.rss/subscribed-at :db/now}))
             ::feed-urls feed-urls}))

        ::success
        {:status                     303
         :biff.response/route-name   :app.subscriptions/page
         :biff.response/route-params {:added-feeds (count (::feed-urls ctx))}}))}])
```

The handler is meant to be called multiple times by some sort of orchestrator (I'll get to that in a minute). Every time
the original function would have performed an effect, this function instead returns some data that describes the effect.
The orchestrator runs the effect and then passes the effect's output back to the handler function.

The handler uses the value of `:biff.chain/state` to know which "segment" of the business logic is being executed
currently, and `:biff.chain/queue` tells the orchestrator which segments/effects should happen next. The first time the
function is called, `state` is `nil`; i.e. we use that as the start state (I could've set it to something like
`:biff.chain/start`, but eh).

Our effects code is stored in a `lib.chain/globals` map which is shared across the codebase:

```clojure
(ns com.yakread.lib.chain
  (:require [clj-http.client :as http]
            [com.biffweb :as biff]))

...

(def globals
  {:biff.chain/http
   (fn [{:keys [biff.chain.http/input] :as ctx}]
     (assoc ctx :biff.chain.http/output (-> (http/request input)
                                            (assoc :url (:url input))
                                            (dissoc :http-client))))

   :biff.chain/tx
   (fn [{:keys [biff.chain.tx/input] :as ctx}]
     (assoc ctx :biff.chain.tx/output (biff/submit-tx ctx input)))})
```

It's as dumb as possible: take some input, do the effect, return the output. I wrap the functions so that the input and
output goes under namespaced keys.

Going back to the handler function, the chain of states will look like this in the success case:

`nil` -> `:biff.chain/http` -> `::add-urls` -> `:biff.chain/tx` -> `::success`

And it'll look like this in the failure case:

`nil` -> `:biff.chain/http` -> `::add-urls`

The orchestrator gets the function associated with each state and calls them in order, taking the return value of each
function and passing it to the next.


```clojure
(defn orchestrate [{:keys [biff.chain/globals] :as ctx} f]
  (loop [{[state & remaining] :biff.chain/queue :as result} (f ctx)]
    (if-not state
      result
      (recur ((get globals state f)
              (merge ctx result {:biff.chain/state state
                                 :biff.chain/queue remaining}))))))

(defn wrap-chain [handler]
  (fn [ctx]
    (orchestrate ctx handler)))
```

I apply the `wrap-chain` middleware to all my handlers. Since the "non-chain" handlers don't return `:biff.chain/queue`,
they work the same as if they were called directly.

Testing is now really easy. Just a bunch of plain function calls; no need to set up e.g. a mock database or anything.
For example:

```clojure
(deftest rss-route
  (let [[_ {:keys [post]}] sut/rss-route]
    (is (= {:biff.chain/queue
            [:biff.chain/http :com.yakread.app.subscriptions.add/add-urls],
            :biff.chain.http/input
            {:url "https://example.com",
             :method :get,
             :headers {"User-Agent" "https://yakread.com/"}}}
           (post {:params {:url "example.com"}})))
    ...))
```

For the parts of the handler that depend on effect output, I wrote a function that generates the fixtures. I
call it manually and check the results into source control. Then tests can use the `read-fixtures` function:

```clojure
(defn write-fixtures! []
  (let [{:biff.chain/keys [http]} (:biff.chain/globals main/initial-system)
        http-get (fn [url]
                   (http {:biff.chain.http/input {:url url
                                                  :method :get
                                                  :headers {"User-Agent" "https://yakread.com"}}}))]
    (with-open [o (io/writer "test/com/yakread/app/subscriptions/add_test/fixtures.edn")]
      (pprint
       {:example-com          (http-get "https://example.com")
        :obryant-dev          (http-get "https://obryant.dev")
        :obryant-dev-feed-xml (http-get "https://obryant.dev/feed.xml")}
       o))))

(defn read-fixtures []
  (edn/read-string (slurp (io/resource "com/yakread/app/subscriptions/add_test/fixtures.edn"))))

...

(deftest rss-route
  (let [[_ {:keys [post]}] sut/rss-route
        {:keys [example-com
                obryant-dev
                obryant-dev-feed-xml]} (read-fixtures)]
    ...
    (is (= {:biff.chain/queue
            [:biff.chain/tx :com.yakread.app.subscriptions.add/success],
            :biff.chain.tx/input
            [{:db/doc-type :conn/rss,
              :db.op/upsert
              {:conn/user 1, :conn.rss/url "https://obryant.dev/feed.xml"},
              :conn.rss/subscribed-at :db/now}],
            :com.yakread.app.subscriptions.add/feed-urls
            ["https://obryant.dev/feed.xml"]}
           (post (merge {:biff.chain/state ::sut/add-urls
                         :session {:uid 1}}
                        obryant-dev))))))
```

`write-fixtures!` calls the functions from `:biff.chain/globals`, e.g. instead of calling `clj-http.client/get`
directly, to ensure the fixtures match what the effect code will produce. Whenever I update the effect code, I can call
`write-fixtures!` again and make sure the tests still pass.

Of course you don't have to write tests that are all of the form `(is (= <constant> (my-function ...)))`; you could do
fancier things too like property-based testing. Or you could pass in a `:biff.chain/globals` map that has mocked effects
so you can write integration tests in the imperative style while keeping your unit tests in the functional style.
