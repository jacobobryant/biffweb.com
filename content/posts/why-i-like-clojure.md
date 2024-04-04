---
description: 'Most of the reasons fall into a few categories: data orientation, the JVM, and the REPL.'
slug: why-i-like-clojure
title: Why I like Clojure as a solo developer
short-title: Why I like Clojure
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/c79e0475-d356-4a63-b7e8-7d190e331b50
published: 2023-04-18T13:02:27 PM
content-type: html
---

<p>Like many, the original reason I got into Clojure was because I spent too much time reading Paul Graham essays. During college I started working on some undergrad research projects and decided to use them as an opportunity to finally get into Lisp. The only question: should I begin with Common Lisp or Scheme?</p>
<p>While Googling for the answer, I came across Clojure. The decision of which Lisp to learn was pretty fuzzy for me, but Clojure seemed "modern," or something, and one person on Twitter shared an anecdote of emailing Paul Graham and receiving the one-line reply, "start with Clojure." Seven years later, here I am: writing Clojure full-time, livin' the dream.</p>
<p>A far more interesting question than "why did I start using Clojure" is "why am I&nbsp;<em>still</em> using it." In that vein, I'll describe a hodgepodge of things that I personally like about Clojure. If any of them resonate for you, then maybe Clojure would be at least worth a look. All of these things have been discussed plenty of times elsewhere; this is simply my own contribution to the genre.</p>
<h2>Data-orientation</h2>
<p><strong>HTML templating.</strong> This might seem mundane, but if you're working on web apps all day, it really is a significant quality-of-life improvement. In Clojure, HTML is Just Data&trade;:</p>
<pre class="language-clojure"><code>(defn article-card [article]
  [:div
   [:div.text-sm (:published-at article)]
   [:div.font-bold.text-lg
    [:a {:href (:url article) :target "_blank"}
     (:title article)]]
   [:p (:excerpt article)]])</code></pre>
<p>This is a function that returns some plain Clojure data (think JSON). You could translate it into JavaScript like so:</p>
<pre class="language-javascript"><code>function articleCard(article) {
  return ["div",
    ["div.text-sm", article.publishedAt],
    ["div.font-bold.text-lg",
     ["a", {href: article.url, target: "_blank"},
      article.title]],
    ["p", article.excerpt]
  ];
}</code></pre>
<p>There are several Clojure libraries that take data structures like these and render them to HTML. You don't have to render HTML this way, but it's very common.</p>
<p>Creating a reusable bit of HTML in Clojure is extremely convenient: just make a plain function. Similarly, you don't need to go through a templating language to use logic like loops or ifs in your HTML: you just write plain Clojure; you've got the power of a full programming language right there. Combining it with Tailwind is simply *chef's kiss*.</p>
<p><strong>Libraries speak a common language.</strong> Another perk from the It's Just Data department. Library functions typically return plain data structures. This means you can understand a function's return value immediately just by looking at it; no need to read the documentation for the Foo class or call its methods. Consider, by way of illustration, the <a href="https://github.com/igrishaev/remus">Remus</a> library which is used for fetching and parsing RSS feeds:</p>
<pre class="language-clojure"><code>(remus/parse-url "https://feed.tedium.co")
=&gt;
{:response
 {:status 200,
  :length -1,
  ...},
 :feed
 {:title "Tedium: The Dull Side of the Internet.",
  :description "A twice-weekly newsletter that takes a deep-dive into the depths of the long tail. Our goal with Tedium? We're trying to reach the bottom.",
  :entries
  ({:title "Socket To Me",
    :description
    {:type "text/html",
     :mode nil,
     :value
     "Why the processor socket, an important part of most desktop computers, lost its upgrade path as computers became smaller and more integrated."},
    :published-date #inst "2023-04-15T12:00:00.000-00:00",
    :author "Ernie Smith",
    :uri "https://tedium.co/2023/04/15/processor-socket-history/",
    :contents
    ({:type "html",
      :mode nil,
      :value
      "&lt;div class=\"md-whitebox\"&gt;\n..."}),
    :authors nil,
    :enclosures nil},
   ...)}}</code></pre>
<p>I compare this to my previous experience in Python, where in this situation I would most likely be given a Feed object. I'd pass it to the <code>help</code> function and then start trying out the various methods to see what the internal data looks like. In Clojure, that process evaporates. And like the HTML-templating thing, the resulting reduction in friction adds up.</p>
<h2>The JVM</h2>
<p><strong>High-level, yet fast.</strong> Clojure provides the same speed of development that you'd get from using a scripting language like Python or Ruby, but since it runs on the JVM, you also get execution speed. This has been extremely helpful for me since most of the apps I've built have a recommender system component, which involves crunching a lot of data.</p>
<p>Take <a href="https://yakread.com">Yakread</a> for example, the main app I'm currently developing. It takes your newsletters, RSS feeds, and bookmarks, and presents them all in an algorithmic feed. (That might not be clear from the landing page, but eh&mdash;that's marketing.) The ranking algorithm uses your reading history as training data: if you tend to click on articles from a particular RSS feed, then future articles from that feed will get ranked higher, etc.</p>
<p>Thanks to Clojure, I'm able to go about that ranking process in a completely stupid way: I do everything at page-load time. There's no persistent model, no batch training pipeline, no workers. Every time you open Yakread, it queries for every possible item that could appear in your feed, while concurrently fetching your entire reading history. Yakread turns your reading history into a model on the fly and uses that to sort all of the candidate items. For my account, which has a lot of data, it takes three seconds.</p>
<p><strong>The library ecosystem.</strong> I love having access to everything on the JVM. You get the benefits of Clojure without the downsides of using a niche language (in terms of code, at least)&mdash;even if there isn't a Clojure library for something you need, there's often one written in Java, and it's often battle-tested.</p>
<p>A few of the top-level Java libraries I'm currently using include Jetty, Apache Tika (via <a href="https://github.com/michaelklishin/pantomime">Pantomime</a>), <a href="https://github.com/voodoodyne/subethasmtp">Subetha</a>, ROME (via the aforementioned Remus), and jsoup.</p>
<h2>The REPL</h2>
<p><strong>REPL-driven development.</strong> Clojure gives you more than an interactive prompt, like in Python. When developing Clojure, you generally have a plugin for your editor (I use <a href="https://github.com/Olical/conjure">Conjure</a>) which lets you select bits of code that you're developing and send them to the running program for evaluation. You can gradually build up your program by inserting new functions into it, without needing to restart the process.</p>
<p>That helps to shorten the development feedback loop. Take the Remus code above as an example: you can fetch an RSS feed, store it in memory, experiment with different ways to extract and present the data from it, and see the results&mdash;all without leaving your editor. It's especially powerful thanks to the "libraries speak a common language" thing. When you have a block of code just the way you like it, you can wrap it in a function and add it to the program.</p>
<p>Again, it's a seemingly small thing that adds up.</p>
<p><strong>Developing in prod.</strong> This is an extension of REPL-driven development: since you can develop your program in this way without doing any restarts, there's nothing stopping you from connecting your editor to your&nbsp;<em>production</em> app process and hacking away on that. I've written some tooling that essentially auto-deploys my code every time I save a file, in just a few seconds. I do the majority of my development this way. This workflow might be counterproductive on a team, but as a solo developer, it's huge.</p>
<p>The production REPL also makes for a nice admin console: all my apps include a <code>repl.clj</code> file in which I accumulate functions for querying the production DB and performing various actions. I can use these functions from the comfort of my editor, no need to code up a web UI. As one example, in each of my main apps I have functions for charging advertisers' credit cards (a weekly task which I've not yet had the guts to automate fully).</p>
<hr>
<p>There are other benefits&mdash;I haven't even talked about functional vs. object-oriented programming, the culture of stability, macros, or&nbsp;<em>all those lovely parentheses&mdash;</em>but hopefully this gives you a taste of why Clojure matters to me.</p>
<p>Going in the other direction, I don't think I could sum up why I like Clojure into a single point without much lossiness, but if I were to try, I'd probably just quote <a href="https://www.linkedin.com/feed/update/urn:li:activity:7037001728583442433">Malcolm Sparks</a>:</p>
<blockquote>
<p>I use Clojure because I don't want to have to think about my programming language, but on the problem I'm trying to solve. Clojure just gets out of my way.</p>
</blockquote>
<p>The biggest&nbsp;<em>impediment</em> I had with getting productive with Clojure was arriving at a web dev stack that fit my needs, which is why&nbsp;<a href="https://biffweb.com/">Biff</a> now exists. I'll probably write more about the rationale behind that in the next essay.</p>