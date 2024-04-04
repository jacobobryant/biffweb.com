---
description: I've made a couple releases recently that overhaul the way Biff does secrets management and authentication.
slug: secrets-auth-plugins
title: Secrets, authentication, and plugins
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/c90038da-e9b0-4148-a095-8699ba64403f
published: 2023-02-18T11:43:22 AM
content-type: html
---

<p>Last month I released&nbsp;<a href="https://github.com/jacobobryant/biff/releases/tag/v0.6.0">Biff v0.6.0</a>, which primarily changed the way Biff does secrets management. The system map now includes a function, stored under the <code>:biff/secret</code> key, which takes a keyword&nbsp;&ndash; like&nbsp;<code>:biff/jwt-secret</code>, or <code>:postmark/api-key</code> &ndash; and returns the associated secret. This makes secrets management pluggable, since you can define the <code>:biff/secret</code> function however you like. It also makes it easier to not shoot yourself in the foot, since previously if you accidentally e.g. logged your system map or rendered it in one of your HTML templates, it would've exposed all your secrets.</p>
<p>The default <code>:biff/secret</code> implementation uses environment variables. Your <code>config.edn</code> file stores the name of the environment variable, like <code>:postmark/api-key "POSTMARK_API_KEY"</code>, so a call to <code>(secret :postmark/api-key)</code> returns the value of the <code>POSTMARK_API_KEY</code> environment variable.</p>
<p>It might be an interesting exercise to modify Biff's server setup script so that it loads secrets from <a href="https://www.doppler.com/">Doppler</a>. Though if you're deploying to a regular VM/DigitalOcean droplet, I don't&nbsp;<em>think</em> that would actually provide any security benefits over storing the secrets in the <code>secrets.env</code> file. You still have to store the Doppler key on the filesystem somewhere. I think Doppler is mostly useful if you're deploying to a platform that can inject secrets into your environment at runtime (?).&nbsp;</p>
<hr>
<p>As of a few minutes ago, I have also released <a href="https://github.com/jacobobryant/biff/releases/tag/v0.7.0">Biff v0.7.0</a>. This one adds support for six-digit signin codes, and it restructures Biff's authentication code in general. Previously the authentication code was all included within the example project, in an <code>auth.clj</code> file that was copied into new projects.&nbsp;</p>
<p>However, Biff already has a concept of feature maps, where you can define Reitit routes, scheduled tasks and such:</p>
<pre class="language-clojure"><code>(def features
  {:routes [["/" {:get home-page}]
            ...]})</code></pre>
<p>For a while I've thought it might be interesting to provide library functions that return these feature maps, with some configuration options as needed.</p>
<p>That's what I now do for authentication. There's a <code>com.biffweb/authentication-plugin</code> function which returns the relevant <a href="https://github.com/jacobobryant/biff/blob/6a5ba0a7a0a052d3ee88adaad645dcddf8bc3876/src/com/biffweb/impl/auth.clj#L243">backend routes and schema</a>. Now your project code only needs to define signin/signup forms and email templates (new projects come with default implementations).</p>
<pre class="language-clojure"><code>(def features
  [app/features
   (biff/authentication-plugin {})
   home/features
   schema/features
   worker/features])</code></pre>
<p>Aside: "feature maps" is an awkward term and I've decided it would make a lot more sense to just call these "plugins," since that's what they are. Coming soon in... Biff v0.7.1, perhaps.</p>
<p>Biff plugins are analogous to <a href="https://firebase.google.com/products/extensions">Firebase extensions</a>. They're "pre-packaged solutions"/library functions that can define HTTP routes, store things in the database, create scheduled tasks and transaction listeners, etc. They're "large building blocks" that you can drop into your application.</p>
<p>I'm using the word "they" in a somewhat hypothetical sense, since there's currently only one Biff plugin that's defined as a library function. It could be interesting to write more! (I like the word "interesting" since it doesn't necessarily imply "useful," though it of course doesn't imply the absence of usefulness either.)</p>
<p>One possibility that immediately comes to mind is an RSS sync plugin. I've already written RSS functionality for&nbsp;<a href="https://yakread.com">Yakread</a>, but wouldn't it be nifty if you could just add <code>(my-rss-plugin {})</code> to your app, and then subscribe to feeds by writing a document to your database? Perhaps you create a <code>{:xt/id ..., :com.myrssplugin/feed-url "https://example.com/feed.xml", ...}</code> document, and then the plugin creates separate documents for all the feed items. Delete the feed document to unsubscribe.</p>
<hr>
<p>Next on the docket, after I spend a few weeks on Yakread, I'll be working on Biff's deployment story. I'm going to create a <a href="https://www.digitalocean.com/community/tags/one-click-install-apps">1-click install image</a> for DigitalOcean, so you can deploy using that instead of running the server setup script yourself. I'd also like to write a how-to guide for deploying to DigitalOcean's container-based <a href="https://www.digitalocean.com/products/app-platform">app platform</a>. I still find all these "Heroku, but cheaper" solutions to be uncomfortably expensive compared to using a plain VM or two, but they probably make a lot more sense when you're scaling beyond a single developer/single web server. In any case, a how-to guide would be useful for anyone who wants to deploy Biff via containers, whether you're using DO's app platform or something else.</p>
<p>Thanks for reading, and thanks to everyone who <a href="https://github.com/sponsors/jacobobryant/">sponsors Biff</a>. Be sure to hop on #biff over at <a href="http://clojurians.net">Clojurians Slack</a> if you haven't already, and hit me up if you'd like to <a href="https://biffweb.com/consulting/">use Biff in your company</a>.</p>