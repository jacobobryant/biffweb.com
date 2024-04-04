---
description: If you'd like to use Biff but you've decided that you'd prefer to use ClojureScript and React instead of htmx, I've made a demonstration of how to integrate re-frame with Biff.
slug: how-to-use-reframe-with-biff
title: How to use re-frame with Biff
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/3415c709-a169-4a34-8cba-a2275738812e
published: 2023-12-05T09:00:18 AM
content-type: html
---

<p>In <a href="https://biffweb.com/p/understanding-htmx/">Understanding htmx</a> I discussed some of the trade-offs of building a primarily server-side-rendered app. If you'd like to use Biff but for whatever reason you've decided that you'd prefer to use ClojureScript and React instead of htmx, I've created <a href="https://github.com/jacobobryant/biff-reframe">an example repo</a> that demonstrates how to integrate <a href="https://day8.github.io/re-frame/">re-frame</a> with Biff. The steps are split up into <a href="https://github.com/jacobobryant/biff-reframe/commits/master">separate commits</a>.</p>
<p>Read on for commentary. You'll want to <a href="https://biffweb.com/docs/get-started/new-project/">create a new Biff project</a> before you start.</p>
<h3>1. Migrate the re-frame template app to Biff</h3>
<p><a href="https://github.com/jacobobryant/biff-reframe/commit/b27ffe208d9f4f884e5baf26d91b5a18049bf4fe">Diff</a>. I generated <code>package.json</code> and <code>package-lock.json</code> by running <code>npm install react react-dom; npm install --save-dev shadow-cljs</code>. Then I generated a separate re-frame project via <code>lein new re-frame reframe-template</code> and mostly just copied and pasted the contents of that into my Biff project.</p>
<p>At this point, you can run&nbsp;<code>clj -M:dev dev</code> to start the backend, and then in a separate terminal tab run <code>npx shadow-cljs watch app</code> to compile the CLJS code. Open <code>localhost:8080</code>, sign in, and then you should see "Hello from re-frame." (I've structured the app so we still use server-side rendering for the landing page and authentication flows, and then after you've signed in you're taken to the re-frame app.)</p>
<p>In a subsequent step we'll have <code>clj -M:dev dev</code> start Shadow CLJS for us.</p>
<h3>2. Rewrite the Biff example app</h3>
<p><a href="https://github.com/jacobobryant/biff-reframe/commit/7cd8a9b85669739ff3ac010a4930e27e0dc58c96">Diff</a>. Ta-da... the app is now fully functional, including the websocket-powered chat. I went through the contents of <code>app.clj</code> and re-implemented it in CLJS.</p>
<p>I'm no re-frame expert; I studied it a bit several years ago but have never built anything with it. So there may be better ways to do things, but this should at least get you started.</p>
<p>The websocket integration in particular is not very robust. If you're actually planning to build something with websockets, you'll at least want to add reconnect logic, and maybe use <a href="https://github.com/taoensso/sente">Sente</a>.</p>
<p>Although Biff provides an <code>/auth/signout</code> endpoint by default, that one redirects to the landing page afterward, which causes the browser to do an additional, unnecessary <code>GET /</code> when you sign out. So I added a different signout endpoint that just returns an empty response.</p>
<h3>3. Update tasks so they run Shadow CLJS</h3>
<p><a href="https://github.com/jacobobryant/biff-reframe/commit/f9e5ecda0b7dfbbba991138ce27ac7b8442e7a8f">Diff</a>. We'll need to modify the <code>dev</code>, <code>deploy</code>, <code>soft-deploy</code>, and <code>uberjar</code> tasks so they compile your CLJS code. We also need to make Dockerfile install <code>npm</code> since it's used for CLJS compilation.</p>
<p>The general approach is that for each task we add a custom function to&nbsp;<code>dev/tasks.clj</code>, have it run the Shadow CLJS command, and then have it delegate to the original task function for the remainder. If we had to make deeper changes, we could copy the task functions' implementations into <code>dev/tasks.clj</code> instead of delegating.</p>
<p>We also update the <code>:biff.tasks/deploy-untracked-files</code> config option so that the <code>deploy</code> and <code>soft-deploy</code> tasks will copy our compiled CLJS to the server.</p>