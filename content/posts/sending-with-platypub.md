---
description: I've mostly recovered from going on a vacation with a small child, and just a few minutes ago I finished adding support for newsletter signup forms on Platypub. I also restructured the way themes work again.
tags:
- biff
- unlisted
slug: sending-with-platypub
title: I'm sending this email with Platypub
published: 2022-05-31T16:58:53 PM
content-type: html
---

<p>I've mostly recovered from going on a vacation with a small child, and just a few minutes ago I finished adding support for newsletter signup forms on Platypub. I also restructured the way themes work again.</p>
<p><img src="https://platypub.sfo3.cdn.digitaloceanspaces.com/822172f3-f678-47e4-8be2-3b1cf8cd3b04" width="550" height="329"></p>
<p><a href="https://biffweb.com/newsletter/">This signup form</a> is backed by a Netlify backend function (<a href="https://github.com/jacobobryant/platypub-biffweb-theme/blob/master/netlify/functions/subscribe.js">source code</a>), and it's replaced the old embedded form (from <a href="https://beehiiv.com/">Beehiiv</a>) that I was using previously. So from here on out, the Biff website and newsletter are built 100% with Platypub.</p>
<p>This also means that all the core functionality for a v0.1 release of Platypub is done. With that 80% of the work out of the way, I just need to complete the remaining 80% of the work and then it'll be ready for anyone to jump in and write some PRs. And I can assure you, there is need for many PRs.</p>
<p>Since I do technically have a job/business, my next priority is to use Platypub to set up a newsletter for that which I've been wanting to do for a month or two. When that's done, I'll go back to mainly working on Biff/Platypub on Fridays. Then I'll need to:</p>
<ol>
<li>Make sure Platypub themes have a good-enough development workflow (the use of Netlify functions complicates this somewhat).</li>
<li>Probably add a <em>very</em> barebones default theme.</li>
<li>Update the README so people can get started running it locally and doing theme dev. (Note, the current READMEs for both Platypub and the theme for biffweb.com are out of date).</li>
<li>Add a whole bunch of issues to the Github repo.</li>
<li>Write an announcement post and launch it.</li>
</ol>
<p>&mdash;Jacob</p>