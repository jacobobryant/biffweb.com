---
description: I've created a git repository that contains the default Biff starter project, modified to use Electric instead of htmx.
slug: how-to-use-electric
title: How to use Electric in a Biff project
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/2661a020-e4c7-49d2-81d5-0947e7d741a6
published: 2023-06-15T08:00:00 AM
content-type: html
---

<p><a href="https://github.com/hyperfiddle/electric">Electric</a> is a library/framework/language that lets you write reactive single-page web apps in Clojure/ClojureScript without having to deal with network IO. You can write code as if the frontend and backend were both in the same "place," and Electric handles shuttling data across the network for you.</p>
<p>By default, Biff uses <a href="https://htmx.org">htmx</a> to handle network IO. htmx is very lightweight and is a good fit for simple UIs (probably most UIs), but if you're developing something complex and/or collaborative (like, say, a board game), you might want to try out Electric. In general I think a good approach is to start with htmx and then consider introducing Electric if/when things start to get hairy.</p>
<p>I've created <a href="https://github.com/jacobobryant/biff-electric">a git repository</a> that contains the default Biff starter project, modified to use Electric instead of htmx. You can clone it and run&nbsp;<code>bb dev</code> to try it out. If you're already a Biff user and have wanted to try out Electric, this should come in handy. If you haven't used Biff before, you can think of this as an alternative to the official Electric&nbsp;<a href="https://github.com/hyperfiddle/electric-starter-app">starter</a> <a href="https://github.com/hyperfiddle/electric-xtdb-starter">apps</a> that has a bunch of additional stuff included (like authentication, routing, XTDB helpers + schema, ...).</p>
<p>&nbsp;A note from the readme:</p>
<blockquote>
<p>View <a href="https://github.com/jacobobryant/biff-electric/commits/master">the latest commit</a> to see how this differs from the regular Biff example app. To use Electric in your own Biff project, it's recommended to create a Biff app <a href="https://biffweb.com/docs/get-started/new-project/" rel="nofollow">the normal way</a> and then manually apply the changes in this project's latest commit. This ensures that your project will be created with the latest version of Biff&mdash;this repo won't necessarily be upgraded to future Biff releases.</p>
</blockquote>
<p>So again,&nbsp;if you'd like to try it out, head over to the <a href="https://github.com/jacobobryant/biff-electric">git repo</a>. Otherwise, read on for some additional commentary.</p>
<p>The main things to understand about integrating Electric into your Biff project are:</p>
<ul>
<li>How do you access the system map from your Electric code?</li>
<li>How do you access data in XTDB from your Electric code and react to new transactions?</li>
</ul>
<p>The first one is simple: when your Electric code is being evaluated,&nbsp;the <code>hyperfiddle.electric/*http-request*</code> dynamic var will be bound to the request map that initiated the websocket connection. Since Biff merges the system map with incoming requests, that dynamic var will include everything in the system map.</p>
<p>So if you e.g. need to submit a transaction, you can use&nbsp;<code>e/*http-request*</code> where you would have normally used <code>ctx</code>:</p>
<pre class="language-clojure"><code>...
(dom/on "click" (e/fn [e]
                  (when (not-empty text)
                    (e/server
                     (biff/submit-tx e/*http-request*
                       [{:db/doc-type :msg
                         :msg/user (:xt/id user)
                         :msg/text text
                         :msg/sent-at :db/now}]))
                    (reset! !text ""))))</code></pre>
<p>For querying and reacting to data in XTDB, take a look at the <a href="https://github.com/jacobobryant/biff-electric/blob/master/src/com/biffweb/examples/electric/signals.clj">signals.clj</a> file. (Disclaimer: I have no idea if "signals" is the right term for what's in that file. Moving on...) The key concepts here are that you need to load some data from XTDB when the relevant part of your app loads initially, and then you need to potentially update that data whenever a new transaction gets indexed.</p>
<p>That file contains an <code>xt-signal</code> helper function to do just that. You define an initial value and a reducer function. The reducer takes that initial value you provided along with a new XT transaction and returns an updated value. Given those two things, <code>xt-signal</code> can handle the remaining wiring for you. Here we define a <code>user</code> signal/whatever for the document corresponding to the current signed-in user:</p>
<pre class="language-clojure"><code>(defn user [{:keys [biff.xtdb/node session] :as ctx}]
  (let [db (xt/db node)
        initial-value (xt/entity db (:uid session))
        reducer (fn [user tx]
                  (or (-&gt;&gt; (::xt/tx-ops tx)
                           (keep (fn [[op maybe-doc]]
                                   (when (and (= op ::xt/put)
                                              (= (:xt/id maybe-doc)
                                                 (:xt/id initial-value)))
                                     maybe-doc)))
                           first)
                      user))]
    (xt-signal (merge ctx {::init initial-value ::reducer reducer}))))
</code></pre>
<p>Back in our UI code, we can use this function like so:</p>
<pre class="language-clojure"><code>(e/def user)

(e/defn SignOut []
  (dom/div
   (dom/text "Signed in as " (e/server (:user/email user)) ". ")
   ...))

(e/defn App []
  (e/server
   (binding [user (new (signals/user e/*http-request*))]
     (e/client
      (dom/div
       (SignOut.)
       ...)))))</code></pre>
<p>One fairly unpolished piece of this repo is the way I handle the production build. The default way to deploy Biff is on a VPS, with a <code>bb deploy</code> task that uploads your code with&nbsp;<code>git push</code> and your config + generated files with <code>rsync</code>. Ideally, you probably want to compile your JS bundle locally and then rsync it to the server, the same way Biff does it <a href="https://github.com/jacobobryant/biff/blob/master/tasks/src/com/biffweb/tasks.clj#L200-L217">for your Tailwind build</a>. However I was too lazy to redefine the <code>deploy</code> and <code>soft-deploy</code> tasks in this repo, so instead I'm just building the JS bundle on app startup. It adds 20-30 seconds to the startup time on my local machine, so on a small server it will take... longer. This all may be a moot point if/when I set up container-based deployment for Biff (with <a href="https://fly.io/">Fly</a>), which I may actually get around to soon.</p>