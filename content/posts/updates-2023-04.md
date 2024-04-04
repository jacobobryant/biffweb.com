---
description: This release mainly contains a bunch of stylistic and structural changes to the starter project.
slug: updates-2023-04
title: Biff v0.7.4 – updated docs – site redesign – roadmap
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/a42f23f6-fc14-44ab-a714-1f75a8283a71
published: 2023-04-19T10:54:33 AM
content-type: html
---

<p>I've just published a new essay, <a href="https://biffweb.com/docs/essays/why-i-like-clojure/">Why I like Clojure as a solo developer</a>. You can comment&nbsp;<a href="https://www.reddit.com/r/Clojure/comments/12s3qqq/why_i_like_clojure_as_a_solo_developer/">on Reddit</a>.</p>
<hr>
<p><a href="https://github.com/jacobobryant/biff/releases/tag/v0.7.4">Biff v0.7.4</a> has been released. This release mainly contains a bunch of stylistic and structural changes to the starter project. Upgrading for existing projects isn't crucial, but it&nbsp;wouldn't hurt to glance over the release notes + commits linked therein so you're familiar with what new projects will look like going forward.</p>
<p>For example, the biggest change is that <code>biff/start-system</code> and company are now deprecated: since Biff's dependency injection/component system has such a small implementation anyway, I decided it makes a lot more sense to inline it into new projects instead of hiding it away behind a library function. This way it takes less effort to see how it works.</p>
<p>This points to&nbsp;one of Biff's design principles: I try to write the code in a way that supports learning and understanding. I don't want Biff to be a black box; I want people to be able to grasp how it's working under the hood. Towards that end I often think about which details are important for people to know about right away, and which can be tucked away behind library code until you decide to read some of Biff's source.</p>
<hr>
<p>I've made a pass over almost the entirety of Biff's documentation&nbsp;and have brought it all up-to-date.</p>
<p>The <a href="https://biffweb.com/docs/get-started/intro/">Get Started</a> section is more concise and consolidated. I removed entirely the bit that talked about the various libraries and such that make up Biff, since I figured it was redundant with the landing page and the reference section.</p>
<p>The <a href="https://biffweb.com/docs/tutorial/build-a-chat-app/">tutorial</a> has been rewritten for Biff v0.7.4, as has the <a href="https://github.com/jacobobryant/eelchat">accompanying git repo</a>. The project in that repo is also easier to start up since the <code>config.edn</code> file has been checked into source, which has been a point of friction for at least a few people. The code diffs in the tutorial are easier to copy and paste now because I discovered the <code>user-select: none</code> CSS setting.</p>
<p>The <a href="https://biffweb.com/docs/reference/architecture/">reference</a> section has also been brought up-to-date with Biff v0.7.4, and there are a few larger changes:</p>
<ul>
<li>There's a new <a href="https://biffweb.com/docs/reference/architecture/">Architecture</a> page, which replaces the old Project Structure and System Composition page.</li>
<li>There are new pages for <a href="https://biffweb.com/docs/reference/config/">Configuration</a> and <a href="https://biffweb.com/docs/reference/bb-tasks/">Babashka Tasks</a>.</li>
<li>The <a href="https://biffweb.com/docs/reference/security/">Authentication</a> (renamed to Security), <a href="https://biffweb.com/docs/reference/htmx/">htmx</a>, and <a href="https://biffweb.com/docs/reference/production/">Production</a> pages have been expanded.</li>
</ul>
<p>Finally, I've added a brand-new section: Essays. I've just published the first essay there, mentioned above. Later I intend to create additional sections for how-to articles and videos, respectively.</p>
<hr>
<p>If you peruse <a href="https://biffweb.com">the site</a>, you may also notice (depending on how plugged into Biff you are) that I've redesigned the whole thing. Biff is in a fairly polished state right now, and&nbsp;I wanted the website to reflect that 🙂.</p>
<p><img src="https://platypub.sfo3.cdn.digitaloceanspaces.com/8850434d-37c8-40a0-9c75-0a675aea9806" alt=""></p>
<hr>
<p>Finally, the roadmap: over the next month or two at least I plan to focus on writing/creating more essays, how-to articles and videos. I think that Biff's core features and documentation are sufficient for now and that the most leverage will come from marketing, basically.</p>
<p>I'm especially interested in reaching people who aren't yet in the Clojure community and using Biff as a way to bring them in, at least for those who do web dev. Partially this means I'll be writing essays with a non-Clojure audience in mind. I'd also like to learn more about SEO, as I think search traffic would be an appropriate channel to focus on for Biff.</p>
<p>This is my current content TODO list. Let me know if there are any items you'd be particularly interested in.</p>
<p>How-to:</p>
<ul>
<li>Use Electric in a Biff project (i.e. as a replacement/complement to htmx)</li>
<li>Set up a product usage dashboard (from scratch or with Metabase)</li>
<li>Deploy with Docker/digitalocean app platform/digitalocean kubernetes instead of a plain VM</li>
<li>Scale out beyond a single web server</li>
<li>Replace Biff's component system with Integrant</li>
<li>Use Alpine.js (complementary with htmx)</li>
<li>Use Firebase/Auth0 for authentication</li>
<li>Make an app that receives email</li>
<li>Integrate with Stripe + implement a few common scenarios</li>
<li>Debug your app</li>
<li>Modify the framework</li>
<li>Test your app</li>
<li>Set up your development environment</li>
</ul>
<p>Essays:</p>
<ul>
<li>Rationale / philosophy of Biff</li>
<li>Comparison to other frameworks / when should(n't) you use Biff</li>
<li>Understanding htmx (conceptual explanation)</li>
<li>Why XTDB instead of Postgres</li>
</ul>
<p>Videos:</p>
<ul>
<li>Starting a new Biff project</li>
<li>Provisioning the server + first deploy</li>
<li>Workflow for developing a feature</li>
<li>Develop-in-prod workflow</li>
</ul>
<hr>
<p>Thanks for reading! Feel free to <a href="https://biffweb.com/consulting/">reach out</a> if your company is interested in building something with Biff. And as always, a big thanks to JUXT and&nbsp;<a href="https://github.com/sponsors/jacobobryant/">other sponsors</a> for supporting my work.</p>