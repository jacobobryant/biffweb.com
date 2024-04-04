---
description: As a way to help people learn Biff, I've been thinking about creating a "large" (compared to toy projects, at least) example application. Ideally it would be something genuinely useful that would warrant ongoing development for a long time, perhaps indefinitely.
tags:
- biff
slug: platypub-plans
title: 'Platypub: plans for building a blogging platform with Biff'
published: 2022-04-18T23:17:00 PM
content-type: html
---

<div data-mobiledoc="{&quot;version&quot;:&quot;0.3.2&quot;,&quot;atoms&quot;:[],&quot;cards&quot;:[],&quot;markups&quot;:[[&quot;a&quot;,[&quot;href&quot;,&quot;https://thesample.ai/&quot;]],[&quot;a&quot;,[&quot;href&quot;,&quot;https://www.tiny.cloud/&quot;]],[&quot;em&quot;],[&quot;code&quot;],[&quot;a&quot;,[&quot;href&quot;,&quot;https://documentation.mailgun.com/en/latest/api-mailinglists.html&quot;]],[&quot;a&quot;,[&quot;href&quot;,&quot;https://documentation.mailgun.com/en/latest/api-email-validation.html&quot;]]],&quot;sections&quot;:[[1,&quot;p&quot;,[[0,[],0,&quot;As a way to help people learn Biff, I" is="">As a way to help people learn Biff, I've been thinking about creating a "large" (compared to toy projects, at least) example application. Ideally it would be something genuinely useful that would warrant ongoing development for a long time, perhaps indefinitely. A project like this could serve several purposes:
<div>
<ul>
<li>Give people a code base to read and learn from.</li>
<li>Give people opportunities to make PRs for an application they use themselves.</li>
<li>Give me another project to dogfood Biff on (in addition to my startup).</li>
<li>Give me direction on what to write and create videos about. e.g. after implementing a feature that's likely to be needed in other projects, it would take little effort to then write a "How to do X with Biff" tutorial. It also might be fun to record pair programming sessions, where someone else implements a feature and I provide guidance as needed.</li>
<li>Help people get interested in Biff and/or Clojure in the first place (because they use the application)</li>
</ul>
<p>An ulterior motive is that I always have several ideas for web apps I wish existed, and it'd be swell if the time I spend on Biff could also help those ideas get implemented.</p>
<p>My preferred project idea for this is a blogging and newsletter platform, which&mdash;after finding that the .com was available&mdash;I'm naming "Platypub." I have about five different places I publish content to semi-regularly, and for months I've been pining for a single piece of software that would handle all of my needs in one place. Plus lots of programmers have blogs, so perhaps many Biff users would have fun hacking on this.</p>
<p>(This also would be complementary with <a href="https://thesample.ai/">my startup</a>, a newsletter aggregator. The most common platform that people use by far is Substack, but I have several beefs with them and wouldn't mind having a good alternative to recommend to people. At some point I might deploy a Platypub instance myself for that purpose, but if someone else wanted to try doing it as their own bootstrapped business, that would be even better.)</p>
<p>At a high level, I'd like to:</p>
<ul>
<li>Write all my posts in one place, regardless of where they end up getting published.</li>
<li>Define site themes with Clojure and share them with other Platypub users.</li>
<li>Use managed services for hosting the site and for newsletter link handling (e.g. unsubscribe requests). If Platypub goes down, I don't want that to impact anyone reading my site/newsletter.</li>
<li>Have the option to use Platypub locally for myself (by serving the app on localhost). This is made possible by the previous point.</li>
<li>Deploy a Platypub instance and have it be usable by non-programmers, with an economically viable free plan (i.e. a single instance should serve multiple users).</li>
</ul>
<h3>Implementation details</h3>
<p>It looks like <a href="https://www.tiny.cloud/">TinyMCE</a> would be a good choice for a drop-in rich text editor. There should also be an API for adding posts. <em>Maybe</em> support "import plugins" which would sync content from external sources regularly (for example, there could be a plugin for importing tweets from your Twitter account).</p>
<p>There should be a UI for creating new sites. Each site has its own configuration settings, including filters to specify which posts belong to which site. I guess we also need "pages" (like "home", "about", ...). There might also be theme-specific configuration.</p>
<p>A theme should be, in essence, a Clojure function that takes as input a site configuration and a set of posts and outputs a map from file paths to file contents (for example, <code>{"/index.html" "&lt;html&gt;...&lt;/html&gt;"}</code>). This does present a difficulty: if you want to permit 3rd party themes (and we do), the theme code must run in some kind of sandbox. This might be a pain (hopefully it's feasible at least), but it doesn't have to be done right at the start. After that's figured out we can decide on how to package and distribute themes. Themes also will need to handle email templates for newsletters.</p>
<p>Sites can be deployed to Netlify. We can use Mailgun's <a href="https://documentation.mailgun.com/en/latest/api-mailinglists.html">Mailing List API</a> for newsletters. Mailgun will handle open and click tracking (if you choose to enable it) and unsubscribes. However we'll have to provide the signup form. The Netlify site can include a backend function (probably written with plain Javascript) that will take a form submission, validate a Recaptcha token, check the address with Mailgun's <a href="https://documentation.mailgun.com/en/latest/api-email-validation.html">validation API</a>, and then (on success) add the address to the mailing list and trigger a welcome email. If the Recaptcha token returns a high probability of the user being a bot, a confirmation email should be triggered. (You should also be able to require a confirmation email for every signup if you so choose.)</p>
<p>Finally, the Platypub API should also let you retrieve both raw posts <em>and </em>the rendered files for a site. That way you have the option of deploying from a separate application (i.e. have the app put a site in one of its own subdirectories) while still letting Platypub do all the rendering.</p>
<p>When ready, I'll start using Platypub to publish this website, followed gradually by my other sites.</p>
</div>
</div>