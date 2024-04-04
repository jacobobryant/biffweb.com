---
description: New articles, a couple releases, the roadmap, and some thoughts on Pathom.
slug: fall-biff-updates
title: Fall Biff updates
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/51caaf71-dd95-4489-875a-4d0ab4425f65
published: 2023-12-09T09:27:00 AM
content-type: html
---

<p>Hola! Since <a href="https://biffweb.com/p/summer-updates/">last time</a>, I've mostly published a bunch of articles:</p>
<ul>
<li><a href="https://biffweb.com/p/understanding-htmx/">Understanding htmx</a></li>
<li><a href="https://biffweb.com/p/xtdb-compared-to-other-databases/">XTDB compared to other databases</a></li>
<li><a href="https://biffweb.com/p/philosophy-of-biff/">Philosophy of Biff</a> (see the <a href="https://news.ycombinator.com/item?id=38535853">HN discussion</a> for Back-to-the-Future quotes)</li>
<li><a href="https://biffweb.com/p/should-you-use-biff/">Should you use Biff?</a></li>
<li><a href="https://biffweb.com/p/how-to-use-postgres-with-biff/">How to use Postgres with Biff</a></li>
<li><a href="https://biffweb.com/p/how-to-use-reframe-with-biff/">How to use re-frame with Biff</a></li>
</ul>
<p>Thanks again to <a href="https://www.clojuriststogether.org/">Clojurists Together</a> for the grant. I've also cut a couple new releases:</p>
<ul>
<li><a href="https://github.com/jacobobryant/biff/releases/tag/v0.7.11">v0.7.11</a> (Sep 9): small improvements to the template project and bb tasks, a new&nbsp;<code>com.biffweb/s3-request</code> function (great for DigitalOcean Spaces), and a couple bug fixes.</li>
<li><a href="https://github.com/jacobobryant/biff/releases/tag/v0.7.15">v0.7.15</a> (Sep 20): fixes some regressions in v0.7.11, and modifies <code>bb deploy</code> and other tasks so you don't have to enter your password more than once (only applicable if you don't already have&nbsp;<code>ssh-agent</code> set up).</li>
</ul>
<p>And there's a new <a href="https://biffweb.com/docs/community/">Community Projects</a> page that lists a few things that people have built.</p>
<p>In the works I have <a href="https://github.com/jacobobryant/biff-docker2">an example repo</a> which adds a Dockerfile + Uberjar compilation to a Biff app. I've sucessfully deployed that&nbsp;repository to Fly.io and DigitalOcean App Platform, and I almost got it fully working on DigitalOcean Kubernetes (I gave up when I got to the&nbsp;<a href="https://www.digitalocean.com/community/tutorials/how-to-secure-your-site-in-kubernetes-with-cert-manager-traefik-and-let-s-encrypt">SSL cert instructions</a>). I was going to write up a "How to deploy Biff with Docker/Kubernetes/what-have-you" guide, but instead I think I'll just merge the Dockerfile + Uberjar stuff into the main Biff repo and maybe include a few pointers in the comments. Expect that to be merged in the next week or two.</p>
<p>(Aside: I'm excited about <a href="https://fly.io/">Fly.io</a>. I think I'll eventually have that be the default/recommended deployment platform for Biff. However, so far I've hit weird bugs every time I've tried&nbsp;to use it, possibly due to <a href="https://community.fly.io/t/reliability-its-not-great/11253">Fly's popularity/scaling issues</a>. Reliability is my #1 requirement for a deployment platform, so for now I'll be cheering from the sidelines.)</p>
<p><strong>Roadmap</strong></p>
<p>Coming up, I've got... (*checks notes*):</p>
<ul>
<li>Switch the recommended email provider from <a href="https://postmarkapp.com/">Postmark</a> to <a href="https://www.mailersend.com/">Mailersend</a> (the latter is cheaper and easier to set up).</li>
<li>Think about redoing config. Instead of having a <code>config.edn</code> file, maybe convert that to a <code>com.example/config.clj</code> file that runs on startup? Most people probably want/are fine with config (not secrets) being checked into source anyway, and doing the config primarily in Clojure instead of EDN would be a little more expressive. Any config values that you don't want in source can always be put in environment variables along with the secrets. I should take a look at how Django et. al. do things.</li>
<li>Custom XTDB indexes for derived data/materialized views. (!)</li>
<li>Updates for the Biff/XTDB transaction format, possibly released as a standalone library.</li>
<li>Open-source <a href="https://yakread.com">Yakread</a>!</li>
<li>Rewrite <a href="https://github.com/jacobobryant/platypub">Platypub</a> from scratch!</li>
</ul>
<p><strong>You say potato, I say potato</strong></p>
<p>I've also been toying with the idea of renaming&nbsp;<strong>plugins</strong> to&nbsp;<strong>modules</strong>. Two people have mentioned that they found Biff's usage of the term "plugin" to be confusing&mdash;normally, "plugin" means "3rd-party plugin" / something "extra" that you add to your already working system. But Biff plugins are primarily 1st-party, and they define your core application logic. I called them plugins because that made architectural sense to me: your Biff components constitute your app's "framework code," and your application code plugs in to that framework.</p>
<p>But if "plugin" is throwing some people off, perhaps "module" would be a decent substitute with fewer misleading connotations? In any case, I already renamed them from "features" to "plugins" previously, so if I change again, I'd like to make sure I'm confident in the new term.</p>
<p><strong>Pathom</strong></p>
<p>Finally&mdash;I don't know if or when I'll actually get to this, but I've been thinking a lot about <a href="https://pathom3.wsscode.com/">Pathom</a> because we use it at work (along with Fulcro). When I initially tried Pathom (also along with Fulcro) four years ago, I was a bit wary. It felt like something that might be handy in a microservices environment where you have a bunch of different data sources that you wanted to query together. But I'm just building relatively small monolithic apps with a single database.</p>
<p>However, since starting my job last summer, I've realized that your app's <em>business logic</em> is effectively a second data source, and Pathom lets you combine all that code along with your database into a single queryable graph. Pathom also lets you do dependency injection: instead of thinking "what exactly does this function need again? Just the ID, or the whole document? What about sub-documents? Do I need to pass the document through another function first?", you just turn the function into a resolver and let Pathom wire up all the inputs in the required format.</p>
<p>In Yakread and The Sample&mdash;both apps with 10k-15k lines of code&mdash;I have started to feel the application logic becoming hard to keep track of. I think Pathom is the solution. (The custom indexes I mentioned above would also help.) I don't think I'd want to include Pathom in new Biff apps by default, but I do think Pathom would be a valuable addition for most apps once they've reached a certain size. Thus, a prime topic for a how-to guide at least.</p>
<p>And what would be&nbsp;<em>really</em> interesting would be to make a sort of server-side version of Fulcro: take all your view functions (the ones that return Rum/Hiccup/HTML) and couple them with Pathom resolvers that grab all the input data. When you load a page (or page fragment with htmx), the root view function's resolver loads the data both for the root function and for all the child view functions, recursively.</p>
<p>The last piece would be some plumbing around forms. Normally in Biff apps I convert form data to EDN (namespaced keywords and such) by hand. I also do validation manually. This is OK for the simple forms in my Biff apps, but it would be painful for a form with, say, 30 fields, like the ones I <s>have</s> get to deal with at work. It shouldn't be too hard to make server-side parsing and validation more automatic.</p>
<p>Take all that, put it on Kubernetes, and then we'd have... Biff Enterprise&reg;! (SLAs and support contracts available.)</p>
<p>Or to put it more seriously: we'd have a solid upgrade path for anyone who wants to use Biff in a commercial context and wants to be confident that their codebase won't turn into a hairball as it grows.</p>
<p><strong>Thanks for reading</strong></p>
<p><a href="mailto:hello@tfos.co&amp;subject=Re: Fall Biff updates">Hit reply</a>, post on <a href="https://forum.tfos.co">the forum</a>, or come chat on <code>#biff</code> @ <a href="http://clojurians.net">Clojurians Slack</a>.</p>