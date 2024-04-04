---
description: A few small releases, some meetups, Platypub + Yakread updates, and some future plans for code and docs.
slug: updates-2022-08
comments-url: https://github.com/jacobobryant/biff/discussions/133
title: Biff updates for August
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/a4b1ebe2-c5bd-4749-86f1-b94f17afaddc
published: 2022-09-05T13:08:39 PM
content-type: html
---

<p class="webonly"><em>For the uninitiated: <a href="https://biffweb.com/">Biff</a> is a Clojure web framework aimed at solo developers.<br></em></p>
<p><strong>Releases</strong></p>
<ul>
<li><a href="https://github.com/jacobobryant/biff/releases/tag/v0.4.3-beta">v0.4.3-beta</a> and <a href="https://github.com/jacobobryant/biff/releases/tag/v0.4.4-beta">v0.4.4-beta</a>: a few small bug fixes and enhancements. Thanks to <a href="https://github.com/N-litened">@N-litened</a> for a PR.</li>
</ul>
<p><strong>Coding meetups</strong></p>
<ul>
<li><a href="https://biffweb.com/p/bpp-3/">5 August</a>: I made a bare-bones RSS reader</li>
<li><a href="https://biffweb.com/p/bpp-4/">19 August</a>: we had a pair session with Tom Brooke, mainly focused on some details of Platypub theme development</li>
<li><a href="https://biffweb.com/p/biff-coding-password-auth/">1 September</a>: I did a demo of implementing password authentication (since Biff currently only comes with email link authentication out of the box)</li>
</ul>
<p>This month we have meetups scheduled on <a href="https://www.meetup.com/biff-coding/events/288235307/">the 15th</a> and <a href="https://www.meetup.com/biff-coding/events/288235328/">the 29th</a>. Follow the links to RSVP and get the Zoom link.</p>
<p>I renamed <a href="https://www.meetup.com/biff-coding/">the meetup</a> from "Biff Pair Programming" to "Biff Coding" since probably most of these will be solo coding sessions done by yours truly 🙂. But <a href="https://forms.gle/cfrQmmwVeXaJGgYx6">sign up here</a> if you'd like to participate in a pair programming session.</p>
<p><strong>Code review</strong></p>
<ul>
<li><a href="https://github.com/soex201">@soex201</a> ported the <a href="https://flask.palletsprojects.com/en/2.1.x/tutorial/">Python Flaskr</a> example project to Biff and I left <a href="https://github.com/jacobobryant/biff-code-review/issues/1">a few comments</a>.</li>
</ul>
<p>If you'd like some code review for your own project, make an issue on <a href="https://github.com/jacobobryant/biff-code-review">this repo</a>.</p>
<p><strong>Projects made with Biff</strong></p>
<p><a href="https://github.com/jacobobryant/platypub">Platypub</a>:</p>
<ul>
<li><a href="https://github.com/jeffp42ker">@jeffp42ker</a> submitted <a href="https://github.com/jacobobryant/platypub/pulls?q=is%3Apr+merged%3A2022-08-01..2022-08-31+">several bug fixes and UI enhancements</a>.</li>
<li>I completely overhauled the data model. This allows custom themes to define not only how your data should be rendered, but also what kind of data there should be in the first place. See <a href="https://github.com/jacobobryant/platypub/releases/tag/2022-08-06">the release</a>, and also my post <a href="https://tfos.co/p/flexible-themes/">Flexible themes in Platypub</a> for a long-winded explanation.</li>
<li>I've also changed the structure of <a href="https://github.com/jacobobryant/platypub/tree/master/themes/default">the default theme</a>. Previously it was a couple of standalone Babashka scripts. Now it has a <code>deps.edn</code> file and a <code>src</code> folder, so other themes can reuse the code without having to copy and paste. This is the first time I've used <a href="https://book.babashka.org/#tasks">Babashka tasks</a>.</li>
<li>I also made the default theme look more spiffy (<a href="https://tfos.co/">example</a>), and I added <a href="https://github.com/jacobobryant/platypub-theme-minimalist">another theme</a> called "Minimalist" (<a href="https://jacobobryant.com/">example</a>).</li>
</ul>
<p><a href="https://yakread.com/">Yakread</a>: this is my current "main thing" as of several weeks ago. Yakread imports your content from various places (like RSS, newsletters, ...) and merges it all into one algorithmically-curated feed. I've finished the essential features and am using it happily myself. Next step is to work on the onboarding experience and marketing. I've written several <a href="https://tfos.co/p/tweets-n-books/">explanatory posts</a>.</p>
<p>If you're building something with Biff and would like a small amount of exposure, let me know and I'll mention it in next month's newsletter.</p>
<p><strong>Future plans</strong></p>
<p>Code:</p>
<ul>
<li>Replace Biff's default <a href="https://github.com/jacobobryant/biff/blob/master/example/task">task</a> shell script with Babashka tasks and move the task definitions into a library. This does mean I'll need to add Babashka to Biff's <a href="https://github.com/jacobobryant/biff/blob/master/example/task">list of requirements</a>, but worth it IMO.</li>
<li><a href="https://github.com/jacobobryant/biff/issues/128">Some enhancements</a> to <code>com.biffweb/submit-tx</code>.</li>
<li>Experiment with moving Biff's authentication code into a plugin. The current <a href="https://github.com/jacobobryant/biff/blob/master/example/src/com/example/feat/auth.clj">email link code</a> gets copied-and-pasted into new projects. I will probably add password authentication soon. Since Biff is organized around the idea of <a href="https://biffweb.com/docs/#code-organization">feature maps</a>, it might work nicely to have library code that returns a feature map. Something like <code>(def features (com.biffweb.plugins.authentication/features {})</code>, which would define a set of HTTP routes used for authentication. Then maybe add in a <code>com.biffweb.plugins.authentication/form</code> function that renders a signup/sign-in form, with some options for styling and selecting which authentication methods to enable.</li>
<li>In a somewhat similar vein, I might try to turn more template code into libary code by <a href="https://github.com/jacobobryant/biff/blob/6bf7d470a55f88172ccf80c92df6564987f37bf1/example/src/com/example.clj#L21-L40">moving this</a> into a "<code>com.biffweb/disaggregate</code>" helper function, which would take a list of your feature maps and return a map with keys for <code>handler</code>, <code>on-tx</code>, etc. (This and the previous bullet point were inspired by <a href="https://clojureverse.org/t/namespace-inheritance-a-la-elixir-use-using-is-this-madness/9199">this Clojureverse discussion</a> about inversion of control in web frameworks).</li>
</ul>
<p>Documentation:</p>
<ul>
<li>I really need to update the docs for Platypub. The data model and theme structure updates I made this month have made Platypub more flexible, but the cost is that it's getting more complex and there are more rough edges.</li>
<li>Building on that, I'd like to explore GitHub's <a href="https://github.com/jacobobryant/platypub/projects">projects</a> thing and use it to lay out a better roadmap for Platypub&mdash;both to give clarity on what I'm planning to build, and to make it easier for others to contribute. Sanding down some of the aforementioned rough edges would be a particularly nice thing to highlight.</li>
<li>And I'm still planning to work on the <a href="https://biffweb.com/p/updates-2022-07/">documentation plans</a> I described last month, e.g. restructuring the <a href="https://biffweb.com/">Biff website</a> and adding a series of how-to-build-a-discussion-forum-with-biff tutorials, for starters.</li>
</ul>
<p>These will not all be complete in September, to be clear 🙂.</p>
<p><strong>Discussion</strong></p>
<p>I think I'll try to do more with <a href="https://github.com/jacobobryant/biff/discussions">GitHub Discussions</a> too, i.e. use it as a companion to the #biff Slack channel. At a minimum I'll make a post there each month for these newsletter issues. Feel free to <a href="https://github.com/jacobobryant/biff/discussions/133">leave a comment</a>.</p>
<p><strong>[Slightly off-topic] Ideas for stuff to build</strong></p>
<p>This section ended up being kind of long, so I broke it out into <a href="https://biffweb.com/p/some-ideas/">a separate post</a>.</p>
<p><strong>Reminders</strong></p>
<ul>
<li>Come chat with us in #biff on <a href="http://clojurians.net/">Clojurians Slack</a> if you haven't joined already. Also feel free to create <a href="https://github.com/jacobobryant/biff/discussions">a discussion thread</a> about anything.</li>
<li>Big thanks to JUXT, Tom Brooke, Jeff Parker, John Shaffer, Wuuei, one anonymous donor, and previously Clojurists Together for sponsoring Biff. If you'd like to help support Biff, please consider <a href="https://github.com/sponsors/jacobobryant/">becoming a sponsor</a> too.</li>
<li>I'm available for short-term consulting engagements; <a href="mailto:hello@jacobobryant.com">email me</a> if you have a project you'd like to discuss.</li>
</ul>