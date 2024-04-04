---
description: I got a job, set up a new Biff forum, received Clojurists Together funding, got interviewed on the Defn podcast, and did a little coding.
slug: summer-updates
title: Summer Biff updates
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/f7f4fd1e-1a6a-41da-bec3-ddfa7081e1d7
published: 2023-09-06T21:50:16 PM
content-type: html
---

<p><strong>Clojurists Together funding</strong></p>
<div class="cooked">
<p>Biff was selected for <a href="https://www.clojuriststogether.org/news/q3-2023-funding-announcement/">another grant</a> from Clojurists Together! Thanks to everyone who donates to them. This time I&rsquo;ll be working to write up a bunch of the stuff that I have listed under the Roadmap section of Biff&rsquo;s <a href="https://biffweb.com/docs/library/">content library</a>. It falls under three categories:</p>
<ul>
<li>
<p>how-tos, especially for demonstrating how to swap out Biff&rsquo;s default choices with other stuff (like XTDB &rarr; Postgres, htmx &rarr; Re-frame, that sort of thing).</p>
</li>
<li>
<p>essays, especially for explaining the rationale behind Biff&rsquo;s default choices.</p>
</li>
<li>
<p>&ldquo;Biff from scratch&rdquo;, a series of guides that will show you how to build a Biff-style web app the traditional Clojure way&ndash;by piecing all the libraries together yourself, without a framework. It&rsquo;s meant to give you a deeper understanding of how Biff works under the hood.</p>
</li>
</ul>
<p>I&rsquo;m hoping that these docs will help people to really master the fundamentals of both Biff and Clojure web dev in general.</p>
</div>
<p><a href="https://forum.tfos.co/t/clojurists-together-funding">Discussion &gt;&gt;</a></p>
<hr>
<p><strong>Defn podcast interview</strong></p>
<div class="cooked">
<p><a href="https://soundcloud.com/defn-771544745/90-jacob-obryant">I was interviewed</a> back in July. We talked about htmx for a bit and probably some other stuff too. It was a fun chat!</p>
<p><a href="https://forum.tfos.co/t/defn-podcast-interview">Discussion &gt;&gt;</a></p>
</div>
<hr>
<p><strong>I got a job</strong></p>
<div class="cooked">
<p><a href="https://tfos.co/p/i-got-a-job/">I mentioned this</a> in my other newsletter already, but: I have a job now (yay). Since late July I&rsquo;ve been working at <a href="https://tyba.ai">Tyba</a>, an eight-person startup in the renewable energy space. <a href="https://www.reddit.com/r/linuxmemes/comments/9xgfxq/why_i_use_arch_btw/">They use Clojure btw</a>.</p>
<p>What this means for Biff is: not much. I&rsquo;ve been spending about the same amount of time working on it as I was previously. The main difference is that I&rsquo;ll be sticking to a more regular schedule of working on Biff a little bit each week instead of doing a bunch of work every 4-6 weeks.</p>
</div>
<p><a href="https://forum.tfos.co/t/i-got-a-job/68">Discussion <span class="box">&gt;&gt;</span></a></p>
<hr>
<p><strong>New Biff forum</strong></p>
<p>With my new work schedule, I've been thinking about how to still find time to write (Biff may have survived me getting a job, but my writing habit, not so much). I thought it might be fun to restore&nbsp;<a href="https://forum.tfos.co/">an old Discourse forum</a> that I experimented with for a few months last year. I'm using it for all my projects, and there's <a href="https://forum.tfos.co/c/biff">a section</a> for Biff. I'll be posting to the forum regularly (🤞), and for this newsletter I'll just copy-and-paste stuff from the forum every once in a while (hence the "Discussion" links). Hopefully I'll be able to stick with this workflow.</p>
<p>You're welcome to post to the forum as well. I'm going to update the Biff website so it mentions both the forum and the #biff Slack channel as good places to ask questions.</p>
<p><a href="https://forum.tfos.co/t/new-biff-forum">Discussion &gt;&gt;</a></p>
<hr>
<p><strong>Updates and roadmap</strong></p>
<div class="cooked">
<p>As per <a href="https://forum.tfos.co/t/clojurists-together-funding/63/1">Clojurists Together funding</a>, I&rsquo;m planning to spend about half of my Biff time over the next few months on writing documentation/content. The remaining time will be spent on code.</p>
<p>I cut a release for <a href="https://github.com/jacobobryant/biff/releases/tag/v0.7.9">v0.7.9</a> a couple months ago; it has a few small tweaks. I&rsquo;ve also technically made a v0.7.10 release, though I haven&rsquo;t got around to publishing a release post on GitHub. <a href="https://github.com/jacobobryant/biff/commit/d8c83c4cc25123b67e14751ff5d19e6b24f7317c">That one</a> modifies the template project so that in new projects, the middleware stack is copied into your project source. That way it&rsquo;s easier to modify and debug.</p>
<p>Other than that, I&rsquo;m currently modifying the <code>bb deploy</code> and <code>bb soft-deploy</code> commands so that they always use rsync to push your code to the server. They&rsquo;ll fall back to <code>git push</code> only if rsync isn&rsquo;t avaliable (e.g. if you&rsquo;re on Windows without WSL). It occurred to me that there&rsquo;s no real advantage to using git push if rsync is an option, and this will eliminate the need to deal with some <code>git push</code>-related <a href="https://github.com/jacobobryant/biff/issues/155">inconveniences</a>/<a href="https://github.com/jacobobryant/biff/issues/164">bugs</a> that have popped up.</p>
<p>Following that I&rsquo;ll be sanding off a few rough edges that have come up on Slack lately, and then I&rsquo;ll be doing a bunch of XTDB stuff. I still have not had a chance to play with <a href="https://www.xtdb.com/v2">v2</a> yet. I&rsquo;d like to make a proof-of-concept fork/branch of Biff with that replacing v1. In the mean time before v2 is production-ready, I&rsquo;ve thought it might be nice to revisit Biff&rsquo;s transaction format and see if it could be made a little more ergonomic. That would also be a good chance to refactor the <code>biff/submit-tx</code> code, which is a bit hairy.</p>
<p>(I also wonder if some of this XTDB helper stuff might be worth releasing as a standalone library, so it can be used outside of Biff projects&hellip;)</p>
<p>Another thing I'd like to do is figure out a good story for materialized views in Biff, so queries can stay fast as your database grows in size. This has been the main pain point I've had in my own apps. A secondary search index(es) for XTDB might work well; that's how the <a href="https://docs.xtdb.com/extensions/1.24.0/full-text-search/">Lucene module</a> works. It also wouldn't hurt to check out <a href="https://redplanetlabs.com/">Rama</a>.</p>
<p>If I actually finish all that and nothing else jumps to the head of the TODO list, the final things I have planned are to work on Yakread and Platypub. I&rsquo;m planning to&nbsp;<a href="https://forum.tfos.co/t/yakread-latest-features-and-roadmap/64/1">open-source Yakread</a> and finish implementing a few features that I&rsquo;d like for myself, at which point I&rsquo;d move on to Platypub for a while and get that fully baked and approachable for potential contributors. Then I&rsquo;ll circle back around to Yakread and make that approachable as well&mdash;although I'll open-source Yakread in the initial pass, I won&rsquo;t spend much time trying to simplify things for contributors until after I&rsquo;m satisfied with Platypub.</p>
<p>We&rsquo;ll see how far I get into that before all my plans get reorganized 🙂.</p>
<p><a href="https://forum.tfos.co/t/biff-updates-and-roadmap">Discussion &gt;&gt;</a></p>
</div>