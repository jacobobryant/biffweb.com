---
description: Biff's method of system composition is quite minimalist. I've been thinking about how it relates to other approaches like that of Integrant.
slug: thinking-about-system-composition
title: Thinking about system composition
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/d2f812cc-d2b5-4ff3-b84f-4089766b7349
published: 2023-03-05T06:14:59 AM
content-type: html
---

<p>One of my guiding principles for Biff is to focus on current needs rather than worrying too much about accommodating future situations which may or may not ever be that relevant for myself or other Biff users. Especially since another Biff principle is that your project should be able to smoothly grow beyond Biff as your needs evolve. I think of Biff like those sticks that you tie saplings to&mdash;eventually the saplings grow into trees, and you don't need the sticks anymore.</p>
<p>While the individual components of Biff can all be replaced straightforwardly&mdash;even the database isn't that hard to swap out&mdash;the&nbsp;<em>way</em> those components are all wired together is more baked in. So it's worth some extra effort there to try to get things right.</p>
<p>Biff's method of system composition is quite minimalist. There is currently a <code>biff/start-system</code> function, but in the next release I'm thinking about deprecating it and moving all the component-wiring code directly into your project. So when you create a new app, the main namespace would start out looking something like the following.</p>
<p>You organize your application code into plugins (currently called "feature maps"&mdash;another thing I'm going to change in a future release):</p>
<pre class="language-clojure"><code>(def plugin
  {:routes [["/app" {:get my-app}]
            ...]
   :tasks [...]
   ...})

;; Everything past this point is in e.g. the com.example
;; namespace, your app's main namespace.

(def plugins
  [app/plugin
   home/plugin
   worker/plugin
   ...])</code></pre>
<p>Your plugins are packaged in a "system map" together with a few other bits and bobs&mdash;but the plugins are the most important thing here:</p>
<pre class="language-clojure"><code>(def initial-system
  {:biff/plugins #'plugins
   :biff/stop '()
   ...})</code></pre>
<p>Anything that's stateful or depends on the environment goes in a component. A component is a function that takes the system map as a parameter and returns a modified version. Shutdown functions are stored under the <code>:biff/stop</code> key. Many of the components take code from your plugins (like your HTTP routes) and connect them to the relevant stateful resources (like a Jetty webserver).</p>
<pre class="language-clojure"><code>(def components
  [biff/use-config
   biff/use-secrets
   biff/use-xt
   biff/use-queues
   biff/use-tx-listener
   biff/use-jetty
   biff/use-chime
   biff/use-beholder])</code></pre>
<p>Components also pass the system map to their children, e.g. the <code>use-jetty</code> component merges the system map with the request map. (Or at least, it will&mdash;currently this is done by a separate middleware.)</p>
<p>The start function takes your initial system map and threads it through your component functions, kind of like a Ring request getting passed through a series of middleware.</p>
<pre class="language-clojure"><code>(defonce system (atom {}))

(defn start []
  (let [sys (reduce (fn [system component]
                      (component system))
                    initial-system
                    components)]
    (reset! system sys)
    ...))</code></pre>
<p>If you modify something that happens during system startup, you can restart the system by calling (refresh) from your repl.</p>
<pre class="language-clojure"><code>(defn refresh []
  (let [{:biff/keys [stop]} @system]
    (doseq [f stop]
      (log/info "stopping:" (str f))
      (f))
    (clojure.tools.namespace.repl/refresh :after `start)))</code></pre>
<p>(Calling refresh in Biff is relatively rare; I've tried to e.g. use late binding so that as much as possible application code changes don't require system restarts.)</p>
<p>I like this approach because it has so little surface area. You can read the whole implementation (I mean, it's basically just the&nbsp;<code>reduce</code> function) and understand how it works. And it still allows me to define components in the Biff library so you don't have to write them all yourself.</p>
<p>It's also good enough, at least for the use-case which Biff prioritizes: solo developers. Having been using Biff for my own apps over the past few years, I'm pretty confident in predicting that this organizational approach will work fine for just about any app written by a single developer. (In my current 7-month-old app, I've added only a single component, which uses&nbsp;<a href="https://github.com/voodoodyne/subethasmtp">Subetha</a> to receive emails. If you're curious, it has 23 plugins.)</p>
<p>However: I'd also like Biff to be suitable at least for small teams, even if that use-case takes a backseat to solo developers. And I don't really know how well this approach will work if you've got a team of people cranking out code all the time.</p>
<p>As I understand it, a few of the fundamental differences between this and Integrant/Component are:</p>
<ol>
<li>With Biff, you define the component start order manually, and dependencies are implicit. With Integrant, you define the dependencies, and the start order is inferred.</li>
<li>With Integrant, components only receive the result of other components that they explicitly depend on. In Biff, components receive the whole system map, which includes anything that was added by any of the previous components.</li>
</ol>
<p>I think #1 is fine: if you get to the point where you have so many components that the dependency relationships are confusing, you can switch to Integrant. I think it shouldn't be <em>too</em> difficult to go through your components and write out the dependencies explicitly.</p>
<p>But in regards to #2, I worry that the Biff style of passing the system map around everywhere will have a tendency to lead to spaghetti. The <em>components</em>' dependencies might not be terribly hard to sort out, but your application code's dependencies are another matter. It might not be so easy in such a case to retrofit Integrant onto your Biff app.</p>
<p>So that's what occupies some of my hammock time currently. I'd like to study some large codebases, like perhaps Metabase's, to get a better feel for things. The last large codebase I worked in was unfortunately not written in Clojure (it was Scala + TypeScript)&mdash;my own apps are mostly what I know when it comes to Clojure.</p>
<p>Anyway, I've been thinking about a couple potential mitigations. Not necessarily code changes for Biff itself, but at least recommendations about how to write your application code&mdash;like "don't pass the system map deeply down the call stack," or something. I should reread <a href="https://clojureverse.org/t/architecture-of-big-applications/9574">this ClojureVerse discussion</a>.</p>
<p>And at the end of the day, it wouldn't hurt to at least make a proof-of-concept for Biff + Integrant and see how it feels. Even if I stick with Biff's component system, it would likely be an educational exercise.</p>
<hr>
<p>Last week on the #biff Slack channel (you'll need to&nbsp;<a href="http://clojurians.net">join Clojurians</a> for these links to work):</p>
<ul>
<li><a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677465853261339?thread_ts=1677377139.925279&amp;amp;cid=C013Y4VG20J">A nifty snippet</a> for getting Cider to call <code>bb dev</code> when you jack in.</li>
<li>When using websockets, you may need to&nbsp;<a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677428916990009">get a fresh db instance</a> manually.</li>
<li>Congrats to macrobartfest on&nbsp;<a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677480003522259">the new job</a>.</li>
<li>Notes about bb tasks: they're&nbsp;<a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677539996687789">not by default acessible</a> from your main project's repl, although you can change that if you really want to.</li>
<li>The authentication plugin originally&nbsp;<a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677613078333549">hardcoded</a> the <code>:user/email</code> attribute. Now there's a <code>:biff.auth/get-user-id</code> option. I haven't published an official release, but it's on master (tagged as v0.7.2). Thanks to m.warnock for the PR.</li>
<li><a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677624953206769">Passwords</a>, or Biff's lack thereof.</li>
<li>A&nbsp;<a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677783883968029">potential pitfall</a> if you copy and paste code from the tutorial's git repo instead of using the code on the website. I need to do some upgrades in the tutorial.</li>
<li><a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677881563200359">Production resource requirements</a> + using managed services (including <a href="https://gist.github.com/jacobobryant/02de6c2b3a1dae7c86737a2610311a3a">a helper fn</a> I use for uploading stuff to/downloading stuff from S3).</li>
<li>Some jankiness around Biff's <a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677900111690369">handling of the secrets.env</a> file in dev.</li>
</ul>