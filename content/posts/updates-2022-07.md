---
description: Stuff that happened in July, plus some future plans.
tags:
- biff
slug: updates-2022-07
title: Biff updates for July
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/bc20541f-b72c-4abb-bf08-1ee05fc0361c
published: 2022-07-31T19:46:56 PM
content-type: html
---

<p><em>In an attempt to be more organized and to avoid posting too often, I've decided to send this newsletter once per month (on the first Monday of each month) and batch up any Biff-related announcements etc. here. <span class="webonly">You can <a href="https://biffweb.com/newsletter/">subscribe</a> to get the next monthly update by email. If you're unfamiliar with Biff, see the <a href="https://biffweb.com/">landing page</a>.</span></em></p>
<p><strong>Meetups.</strong> We've started doing recorded pair programming sessions twice per month. These are meant to be pretty laid back; a relaxing way to learn some Clojure web dev with Biff together. We've had two so far:</p>
<ul>
<li>Jeff Parker closed three issues in <a href="https://github.com/jacobobryant/platypub">Platypub</a> related to the UI for sending newsletters (<a href="https://biffweb.com/p/bpp-2/">recording</a>).</li>
<li>Jeremy Taylor fixed some bugs in Platypub in which users who hadn't added various API keys yet would see an Internal Server Error (<a href="https://biffweb.com/p/bpp-1/">recording</a>).</li>
</ul>
<p>We had 5-10 people at each of these, which is more than I was expecting! I just created <a href="https://www.meetup.com/biff-pair-programming/">a meetup group</a> for the future sessions. You can <a href="https://www.meetup.com/biff-pair-programming/events/287500351/">RSVP to the next one</a>, which will be on August 5th at 16:30 UTC (this Friday). <a href="https://www.meetup.com/biff-pair-programming/events/287500547/">The following meetup</a> will be on the 18th. You can also <a href="https://forms.gle/hm7Mqgzh93ieGdAm7">sign up</a> to drive.</p>
<p><strong>Platypub improvements.</strong> There have been a handful of merged PRs in July in addition to the issues we closed in the meetups:</p>
<ul>
<li><a href="https://github.com/jacobobryant/platypub/pull/29">Add subscribers page</a></li>
<li><a id="issue_38_link" class="Link--primary v-align-middle no-underline h4 js-navigation-open markdown-title" href="https://github.com/jacobobryant/platypub/pull/38" data-hovercard-type="pull_request" data-hovercard-url="/jacobobryant/platypub/pull/38/hovercard">Generate slug from title when blank</a></li>
<li><a id="issue_45_link" class="Link--primary v-align-middle no-underline h4 js-navigation-open markdown-title" href="https://github.com/jacobobryant/platypub/pull/45" data-hovercard-type="pull_request" data-hovercard-url="/jacobobryant/platypub/pull/45/hovercard">Filter reserved and unsafe chars in slug</a></li>
</ul>
<p>Thanks to Jeff Parker for submitting all of these. See the list of <a href="https://github.com/jacobobryant/platypub/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22">good first issues</a> if you'd like to start contributing.</p>
<p><strong>Asynchronous code review. </strong>This is another new experiment. I've created a <a href="https://github.com/jacobobryant/biff-code-review">GitHub repo</a> where anyone can ask for feedback on their code. From the README:</p>
<blockquote>
<p dir="auto">If you're working on a Biff project and need help or would otherwise like some feedback on your code, <a href="https://github.com/jacobobryant/biff-code-review/issues/new">create an issue</a>. Include a link to the relevant files and/or commits in your project.</p>
<p dir="auto">I (Jacob O'Bryant) will try to respond to all the feedback requests, and anyone else in the Biff community is also encouraged to respond if you have some time (especially if this becomes popular!). I mostly work on Biff on Fridays, so that's when I'll be most available for code review; but if I can answer a question quickly then I may do it throughout the week as well.</p>
</blockquote>
<p>I don't know if anyone will use it, but at least now you know it's there!</p>
<p><strong>Private mentoring.</strong> A couple people have asked about this, so I thought this would be worth mentioning: if you'd like some one-on-one help with learning Biff/Clojure web dev, I can do private mentoring over Zoom for $75/hour, with the first hour free. <a href="mailto:hello@jacobobryant.com">Email me</a> if you'd like to set something up. That being said, I would encourage anyone interested to also/instead participate in the free "support" channels (<a href="https://www.meetup.com/biff-pair-programming/">meetups</a>, <a href="https://github.com/jacobobryant/biff-code-review">asynchronous code review</a>, and the #biff channel on <a href="http://clojurians.net/">Clojurians Slack</a>).</p>
<h2>Future plans</h2>
<p>I've thought a lot about how I should use my "Biff time" (i.e. Fridays) going forward. I've decided I'd like to split it more-or-less evenly between three areas:</p>
<p><strong>Community building/support.</strong> Organizing the meetups, doing async code review, answering any questions that come up on the #biff channel, writing this newsletter, responding to PRs and issues on Platypub. Since the Biff community is still quite nascent, this doesn't take much time yet<em>&mdash;</em>the meetups are the main thing currently.</p>
<p><strong>Developing Platypub.</strong> Since <a href="https://github.com/jacobobryant/platypub">Platypub</a> is meant to give people an opportunity to contribute to an open-source Biff project, I have purposely avoided working on a lot of the issues myself. However, some issues are more complex and involve a bunch of details that are mostly in my head. I'd like to keep those issues out of the way so that they don't become blockers for any other contributors.</p>
<p>I'd also like to take care of the issues on <a href="https://github.com/jacobobryant/platypub#roadmap">the critical path</a> so that I can provide a hosted instance of Platypub. Then anyone can try it out without needing to run the code themselves first. In its final form, I'd like Platypub to become a gateway to Clojure and possibly programming in general: you could think of it as "WordPress, but themes are written in Babashka."</p>
<p><strong>Documentation.</strong> Right now Biff only has <a href="https://biffweb.com/docs/">reference docs</a>. I want to add a lot more, such as:</p>
<ul>
<li>A series of tutorials that show you how to build some application with Biff, step-by-step. Perhaps a forum + real-time chat application, like Discourse and Slack in one.</li>
<li>A page that curates/recommends resources for learning Clojure and getting a dev environment set up. Aimed at those who are brand new to Clojure and want to use it for web dev. If needed I might write up some of my own articles to go along with it, though I'd prefer to curate existing resources as much as possible.</li>
<li>A series of tutorials/explanatory posts that teach the libraries Biff uses. Each tutorial will have readers implement some web dev functionality without using Biff (like HTML rendering), after which they'll be shown how to do it with Biff. (Spoiler: the tutorials will secretly have readers implement all the helper functions that Biff provides<em>&mdash;</em>by the end, readers will have implemented all/most of Biff from scratch.) This is intended for those who prefer a bottom-up approach to learning, or for those who are familiar with Biff and want to deepen their understanding.</li>
</ul>
<p>As part of that, I plan to restructure <a href="https://biffweb.com/">the website</a>, while taking lessons from <a href="https://documentation.divio.com/">The Grand Unified Theory of Documentation</a> into account.</p>
<hr>
<p>So yeah, that should be enough to keep me busy for a long time 🙂. Somewhat amusingly, "writing code for Biff" isn't even one of those categories, but it'll happen as needed (e.g. I might port certain features from Platypub back into Biff).</p>
<h2>Reminders</h2>
<ul>
<li>Come chat with us in #biff on <a href="http://clojurians.net/">Clojurians Slack</a> if you haven't joined already.</li>
<li>Big thanks to JUXT, Tom Brooke, Jeff Parker, John Shaffer, Wuuei, one anonymous donor, and previously Clojurists Together for sponsoring Biff.</li>
<li>If you'd like to help support Biff, you can <a href="https://github.com/sponsors/jacobobryant/">become a sponsor</a> too.</li>
<li>I also have some availability for consulting; <a href="mailto:hello@jacobobryant.com">email me</a> if you have a project you'd like to discuss.</li>
<li>If you're interested in Biff, you may also be interested in my other work: <a href="https://thesample.ai/">The Sample</a> and <a href="https://blog.thesample.ai/p/tfos/">Tools for Online Speech</a>.</li>
</ul>