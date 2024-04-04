---
description: Including htmx by default is one of the main design decisions I've made in Biff. If htmx is a good fit for your project, you might find it has a pretty high bang-for-buck ratio—that's been my experience at least.
slug: understanding-htmx
title: Understanding htmx
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/d9771476-b3de-41b3-bcdf-8759c7830939
published: 2023-09-25T09:22:06 AM
content-type: html
---

<p>Including <a href="https://htmx.org">htmx</a> by default is one of the main design decisions I've made in Biff. You don't <em>have</em> to use htmx; it's pretty straightforward to set up ClojureScript and React instead. But if htmx is a good fit for your project, you might find it has a pretty high bang-for-buck ratio&mdash;that's been my experience at least.</p>
<h2 id="htmwhat-">htmwhat?</h2>
<p>Before we get into htmx's tradeoffs, let's briefly cover what it <em>is</em>. htmx is a Javascript library that helps you do server-side rendering with <em>snippets</em> of HTML instead of <em>entire pages</em>. To explain what I mean, consider this humble form:</p>
<pre class="language-markup"><code>&lt;form action="/set-foo" method="POST"&gt;
  &lt;div&gt;Current foo value: 7&lt;/div&gt;
  &lt;input type="number" name="foo" /&gt;
  &lt;button type="submit"&gt;Update&lt;/button&gt;
  ...
&lt;/form&gt;</code></pre>
<p>In Clojure, you might create a request handler that saves the input data from this form somewhere and then redirects back to the page the user was already on:</p>
<pre class="language-clojure"><code>(defn set-foo-handler [{:keys [params]}]
  (let [foo (parse-long (:foo params))]
    (save-foo! foo)
    {:status 303
     :headers {"location" "/foo-page"}}))</code></pre>
<p>After the user gets redirected, the entire page they were on will get re-rendered.</p>
<p>With htmx, we can instead re-render <em>just the form</em> instead of the entire page. First we use a couple <code>hx-*</code> attributes that tell htmx to do its thing:</p>
<pre class="language-markup"><code>&lt;form hx-post="/set-foo" hx-swap="outerHTML"&gt;
  &lt;div&gt;Current foo value: 7&lt;/div&gt;
  &lt;input type="number" name="foo" /&gt;
  &lt;button type="submit"&gt;Update&lt;/button&gt;
  ...
&lt;/form&gt;
</code></pre>
<p>And then we modify <code>set-foo-handler</code> so that instead of redirecting, it renders a new version of the form:</p>
<pre class="language-clojure"><code>(defn set-foo-handler [{:keys [params]}]
  (let [foo (parse-long (:foo params))]
    (save-foo! foo)
    {:status 200
     :headers {"content-type" "text/html"}
     :body (rum/render-static-markup
            [:form {:hx-post "/set-foo" :hx-swap "outerHTML"}
             [:div "Current foo value: " foo]
             [:input {:type "number" :name "foo"}]
             [:button {:type "submit"} "Update"]
             ;; ...
             ])}))</code></pre>
<p>When you submit the form, htmx will trigger a POST request, as with the normal form at the beginning. The difference is that htmx will take the HTML response and swap it into the current page where the old form used to be. The rest of the page is untouched.</p>
<p><img src="https://platypub.sfo3.cdn.digitaloceanspaces.com/bd02db3c-49ab-4163-8042-7d7c71f93d13" width="500"></p>
<p>And crucially, this is done in the same style as traditional server-side rendering: you just write request handlers that return HTML; there's not much client-side logic.</p>
<h3 id="why-not-just-re-render-the-whole-page-">Why not just re-render the whole page?</h3>
<p>In some cases that&rsquo;s fine. But in other cases, you don&rsquo;t want to lose the state that&rsquo;s already on the page. Suppose you&rsquo;re building the next hot social network, and you&rsquo;ve got a page that shows a feed of posts:</p>
<p><img src="https://platypub.sfo3.cdn.digitaloceanspaces.com/ee70edb5-e95d-4fa3-817c-db9f507c9601" width="400"></p>
<p>Now you need to implement the heart button so that people can heart their favorite posts. However, if your heart button is a plain-old-form that causes the entire page to reload, there are several potentially undesirable consequences:</p>
<ul>
<li>All the posts in the feed will have to be fetched again.</li>
<li>The posts that get fetched might be different.</li>
<li>The user might lose their scroll position.</li>
<li>The user might lose their draft if they were in the middle of typing a post.</li>
</ul>
<p>If you instead implement the heart button as an htmx form that only reloads the current post&mdash;or even just the heart button itself&mdash;then hearting a post will be faster and better.</p>
<p><img src="https://platypub.sfo3.cdn.digitaloceanspaces.com/467b973f-9bd7-4999-8899-83e5473153da" width="400"></p>
<hr>
<p>Those are some minimal examples of what htmx can do. A few other important features:</p>
<ul>
<li>You can put <code>hx-post</code> (or <code>hx-get</code> / <code>hx-put</code> / <code>hx-delete</code>) on other elements, like individual inputs, not just forms.</li>
<li>You can use <a href="https://htmx.org/attributes/hx-trigger/">hx-trigger</a> to change the event that triggers the request, such as <code>click</code>, <code>change</code>, or <code>load</code>.</li>
<li><a href="https://htmx.org/attributes/hx-target/">hx-target</a> lets you specify where the response body should go, using a CSS selector (e.g. <code>#output</code> or <code>closest div</code>).</li>
<li>With <a href="https://htmx.org/attributes/hx-swap/">hx-swap</a> you can change the way the response body is inserted into the DOM, e.g. set it to <code>beforeend</code> to insert something at the end of a list.</li>
</ul>
<p>htmx takes the standard HTML behavior of "when the user submits a form / clicks a link, send an HTTP request and load a new page" and generalizes it to "when [something happens], send an HTTP request and put the response [somewhere in the DOM]."</p>
<h2 id="how-to-think-about-htmx">How to think about htmx</h2>
<p>In a nutshell: you're not "building an htmx app," you're building a server-side rendered app with htmx. Let me expand on that.</p>
<p>Not all of your app's interactions have to go through htmx. If a plain-old-form has good enough UX for a particular interaction, just use that. You don't have to put <code>hx-post</code> on everything. Similarly, it's OK to use some Javascript. If you were building a simple, "traditional" server-side rendered app, there would be nothing wrong with throwing in a little <a href="http://vanilla-js.com/">vanilla JS</a> to spruce things up a bit. Adding htmx into the mix doesn't change that.</p>
<p>Standalone JS components also work nicely. I use a&nbsp;<a href="https://www.tiny.cloud/">rich text editor</a> component in one of my apps, and from htmx's perspective, it behaves just like a regular <code>textarea</code>. The main thing is that the <em>majority</em> of your application logic should be happening on the server. (See <a href="https://htmx.org/essays/hypermedia-friendly-scripting/">Hypermedia-Friendly Scripting</a> for more details.)</p>
<p>So the fundamental tradeoffs of htmx are similar to the tradeoffs of traditional server-side rendering vs. SPAs. Server-side rendering (thin client) simplifies the programming model because you don't have much distributed state, but it has a lower UX ceiling than a SPA (thick client). If server-side rendering is good enough for your app, then doing it that way will likely take less effort than building it as a SPA. But there is a threshold for your app's interaction requirements above which you're better off going with a SPA.</p>
<p>htmx raises the threshold.</p>
<h2 id="is-htmx-right-for-you-">Is htmx right for you?</h2>
<p>Given all that, an initial question to ask is "how far would you be able to get with traditional server-side rendering&mdash;plain old links and forms?" If the answer is "pretty far," htmx might be worth a shot; if the answer is "no way Jose," htmx probably won't change that.</p>
<p>Carson Gross (htmx author) has an essay <a href="https://htmx.org/essays/when-to-use-hypermedia/">When should you use hypermedia?</a> that goes into the details of what kinds of interactions can (and can't) be handled well by htmx. He mentions some specific apps near the end:</p>
<blockquote>
<p>To give an example of two famous applications that we think <em>could</em> be implemented cleanly in hypermedia, consider Twitter or GMail. Both web applications are text-and-image heavy, with coarse-grain updates and, thus, would be quite amenable to a hypermedia approach.</p>
<p>Two famous examples of web applications that would <em>not</em> be amenable to a hypermedia approach are Google Sheets and Google Maps. Google Sheets can have a large amount of state within and interdependencies between many cells, making it untenable to issue a server request on every cell update. Google Maps, on the other hand, responds rapidly to mouse movements and simply can&rsquo;t afford a server round trip for every one of them.</p>
</blockquote>
<p>Besides all that, I'd also emphasize that there are plenty of apps for which either approach will be just fine. htmx can take you pretty far, but at the same time, building your app as a SPA doesn't mean it instantly becomes a big ball of mud&mdash;especially in ClojureScript, where the React wrappers are *chef's kiss*.</p>
<p>In these cases, <em>you're</em> the most important factor.[1] If you're already productive and happy writing SPAs with <a href="https://day8.github.io/re-frame/">re-frame</a> or what-have-you, I would probably just stick with that, unless you want to experiment with htmx for the sake of learning something new. I think htmx really shines for people who feel most at home on the backend and for those who are still in the earlier stages of learning web dev.</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p><strong>Notes</strong></p>
<p>[1] I'm mainly addressing solo developers here, since that's the audience Biff is targeted to. There will of course be other factors to consider if you're in a team context.</p>