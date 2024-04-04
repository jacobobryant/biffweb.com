---
description: Last night I finished writing the last section of Biff's new official tutorial. It takes you through the process of building an app called eelchat.
slug: tutorial
title: 'Biff tutorial: build a chat app step-by-step'
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/ebfe1198-b9c3-4e09-9e0d-bb77d526fee3
published: 2022-11-22T18:33:20 PM
content-type: html
---

<p>Last night I finished writing the last section of <a href="https://biffweb.com/docs/tutorial/build-a-chat-app/">Biff's new official tutorial</a>. This project was funded by a grant from <a href="https://www.clojuriststogether.org/">Clojurists Together</a>. A huge thank you to them and all their donors, as well as to JUXT&nbsp;<a href="https://github.com/sponsors/jacobobryant/">and others</a> for their ongoing sponsorship of Biff.</p>
<p>The new tutorial takes you through the process of building an app called&nbsp;<em>eelchat.</em></p>
<p><em><img src="https://platypub.sfo3.cdn.digitaloceanspaces.com/ebfe1198-b9c3-4e09-9e0d-bb77d526fee3" alt=""></em></p>
<p>eelchat is like a barebones version of Slack, Discord, and the like. From <a href="https://biffweb.com/docs/tutorial/build-a-chat-app/">the introduction</a>:</p>
<blockquote>
<p>In eelchat, users can create communities, each of which has a collection of channels for text chat. Channels can only be created and deleted by the user who created the community (i.e. the admin). The admin can also add RSS subscriptions to channels, so new posts will be displayed.</p>
</blockquote>
<p>In building eelchat, you'll get a tour of all of Biff's main parts, including:</p>
<ul>
<li>Creating and deploying a new project (I like to get new apps in production right away)</li>
<li>Rendering pages with Rum and Tailwind</li>
<li>Modeling your app's domain with Malli schemas</li>
<li>Doing CRUD with XTDB and htmx</li>
<li>Pushing updates to the client with transaction listeners, htmx, and hyperscript</li>
<li>Handling background jobs with scheduled tasks and in-memory queues</li>
</ul>
<p>All of the code is available <a href="https://github.com/jacobobryant/eelchat">on GitHub</a>, with a <a href="https://github.com/jacobobryant/eelchat/commits/master">separate commit for each section</a>. Links to the commits are included throughout the tutorial. And at the end there are suggestions for additional features you can add to eelchat, so you can get a chance to spread your wings.&nbsp;</p>
<p>A few people over in #biff on <a href="http://clojurians.net">Clojurians Slack</a> have already started working through the tutorial as I've been releasing the sections over the past weeks, and I haven't been alerted to any glaring faults yet. I did do some refactoring last night though, and I don't have it in me to go through the entire tutorial myself just yet, so let me know if you run into any snags.</p>
<p>I am planning to update the code formatting. Most of the code blocks were pasted verbatim from <code>git diff</code>, which is a quick way to write a tutorial though perhaps not the pinnacle of UX. You'll have to delete a bunch of&nbsp;<code>+</code>s at least. (Maybe I'll finish that tonight and render this paragraph obsolete... but if you're reading this, that didn't happen.)</p>
<p>Now that the tutorial is finished, my next order of business is to get <a href="https://github.com/jacobobryant/platypub#platypub">Platypub</a> to a state where I can run a publicly available instance of it. When that's finished, Biff will have not just a fabulous tutorial, but also a killer app that anyone can try out easily. Part of my grand scheme is still to have Platypub become a hook that brings more people into the Clojure ecosystem&mdash;e.g. custom themes in Platypub are just Babashka scripts.</p>
<p>Concurrently, I'll continue to write more documentation. I'd like to add a page with links to resources for learning Clojure, and I've got a handful of how-to articles I'd like to write (such as "how to do pagination with htmx," "how to receive email," and "how to scale out"). In general my plan is to spend my time roughly 50/50 between writing open-source apps with Biff and writing documentation. (I'd also like to start doing meetups again, but I'm going to keep those on hiatus at least until the end of the year.)</p>
<p>If there's anything you'd like to see in Biff world, let me know. And remember...</p>
<p><img id="done-img" src="https://i.imgflip.com/71tdeg.jpg"></p>