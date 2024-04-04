---
description: This past week I've been playing around with serverless functions on DigitalOcean. They're nice for using JS libs without needing to run Node and the JVM on the same machine.
slug: js-from-clj
title: Using Javascript libraries from Clojure
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/5e2bb823-80c3-4a8d-9590-a5d68f8bff86
published: 2023-02-25T07:12:58 AM
content-type: html
---

<p class="emailonly">I'm going to try a new thing with this newsletter: instead of only sending it out once a month or so when there's a new release, I'll send it each Saturday with various Biff tips, mini-howtos, things that were discussed on the Slack channel, etc.</p>
<p>This past week I've been playing around with serverless functions on DigitalOcean. They launched those in May of last year, after <a href="https://www.digitalocean.com/blog/nimbella-joins-the-digitalocean-family">acquiring</a> a startup that was doing serverless functions as a standalone platform in the previous year.</p>
<p>The serverless functions make a nice complement to droplets. Specifically, there are a couple Javascript libraries that I use in my app: <a href="https://github.com/mozilla/readability">Readability</a>, an article parser/extractor that powers Firefox's reader mode; and <a href="https://github.com/Automattic/juice">Juice</a>, a library that takes some HTML and converts all the styles to inline. For example, if you passed this to Juice:</p>
<pre class="language-html"><code>&lt;html&gt;
  &lt;head&gt;
    &lt;style&gt;
    .red {
      color: red;
      }
    &lt;/style&gt;
  &lt;/head&gt;
  &lt;body&gt;
    &lt;p class="red"&gt;Hello&lt;/p&gt;
  &lt;/body&gt;
&lt;/html&gt;</code></pre>
<p>It would return the following:</p>
<pre class="language-html"><code>&lt;html&gt;
  &lt;body&gt;
    &lt;p style="color:red;"&gt;Hello&lt;/p&gt;
  &lt;/body&gt;
&lt;/html&gt;</code></pre>
<p>The most common use-case for this is sending email&mdash;some email clients only support inline styles. I was under the impression that Gmail in particular didn't, but it seems my knowledge on that was&nbsp;<a href="https://developers.google.com/gmail/design/css">out-of-date</a> by about seven years. I wouldn't be surprised if Outlook still has issues...</p>
<p>Anyway, another way Juice is helpful&mdash;and the way I'm using it in my app&mdash;is that it can help you embed 3rd party HTML + CSS in your site. My app can receive and display emails, and inlining the styles prevents them from messing with the rest of the page.</p>
<p>Back on topic. Since I'm using Biff, my app is running on the JVM, not Node. And I need some way to run Javascript.</p>
<p>The Bad Old Way that I've been using for about... six months, is I have a <a href="https://gist.github.com/jacobobryant/1cd4088336a4cbcb431ba17d37cc4ab0#file-tools-js">tools.js</a> file in my resources directory. I have a <code>with-js-tools</code> helper function which runs the file via Node in a subprocess:</p>
<pre class="language-clojure"><code>(defn with-js-tools [f]
  (let [path (.getPath (io/resource "tools.js"))
        proc (babashka.process/process ["node" path])
        lock (Object.)]
    (try
     (with-open [stdin (io/writer (:in proc))
                 stdout (io/reader (:out proc))]
       (f (fn [command opts]
            (locking lock
              (binding [*out* stdin]
                (println (cheshire/generate-string
                           (assoc opts :command command))))
              (binding [*in* stdout]
                (cheshire/parse-string (read-line) true))))))
     (catch Exception e
       (println (slurp (:err proc)))
       (throw e)))))</code></pre>
<p>This allows me to communicate with the Node process via pipes. From the Clojure code, I get a nice function that I can call, for example:</p>
<pre class="language-clojure"><code>(with-js-tools
  (fn [js]
    (let [url "https://example.com"
          html (slurp url)
          parsed-article (js :readability {:url url :html html})]
      ...)))</code></pre>
<p>The benefit of doing this instead of just calling <code>(clojure.java.shell/sh "node" "readability" ...)</code> is that you can pass in multiple documents for parsing/inlining without needing to structure your code so they're all be done together in a batch. You only have to start up a Node process once, and then you get a handy&nbsp;<code>js</code> function that you can pass around and call from wherever you like.</p>
<p>However, this still has major downsides: you're running the JVM and Node on the same machine, which is hard on your RAM. So far I've mitigated this issue by, erm, upgrading to an 8GB droplet. (Perhaps you can tune the JVM/Node so they live happily together on a single machine, but I never figured out how... the memory limit options seemed to be treated more like <em>guidelines,</em> or they only applied to the heap, or something like that.)</p>
<p>Serverless functions are a much better solution. Package up each JS lib as a separate function and let DigitalOcean host them somewhere other than your droplet. I've made two tiny functions, <a href="https://gist.github.com/jacobobryant/1cd4088336a4cbcb431ba17d37cc4ab0#file-readability-js">one</a> for Readability and <a href="https://gist.github.com/jacobobryant/1cd4088336a4cbcb431ba17d37cc4ab0#file-juice-js">one</a> for Juice. I have a utility function for calling them:</p>
<pre class="language-clojure"><code>(defn cloud-fn [{:keys [biff/secret cloud-fns/base-url} endpoint opts]
  (http/post (str base-url endpoint)
             {:headers {"X-Require-Whisk-Auth" (secret :cloud-fns/secret)}
              :as :json
              :form-params opts}))

(cloud-fn ctx "juice" {:html "&lt;html&gt;..."})</code></pre>
<p>Developing and deploying the functions was pretty convenient. I made a few Babashka tasks to help out:</p>
<pre class="language-clojure"><code>(defn fn-logs []
  (shell "doctl" "serverless" "activations" "logs"
         "--limit" "3"
         "--follow"))

(defn fn-deploy []
  (shell "doctl" "serverless" "deploy" "cloud-fns"))

(defn fn-dev []
  (future (shell "doctl" "serverless" "watch" "cloud-fns"))
  (fn-logs))</code></pre>
<p><code>fn-logs</code> prints the logs from the deployed functions to my terminal, <code>fn-deploy</code> deploys them, and <code>fn-dev</code> deploys the functions whenever I save a file. You can install dependencies with e.g.&nbsp;<code>npm install --save juice</code> and it Just Works, with one caveat: deployment was quite slow after I added some NPM dependencies. I guess it has to zip up the whole <code>node_modules</code> directory and upload it each time, and my internet connection isn't the fastest.</p>
<p>I think you can alternately deploy the functions by hooking up a GitHub repo to DigitalOcean, and then the functions get built on The Cloud. If I do much more function developin', I'll probably look into that.</p>
<div class="emailonly"><hr>
<p>Last week on the #biff Slack channel (you'll need to <a href="http://clojurians.net">join Clojurians</a> for these links to work):</p>
<ul>
<li><a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677002850679029">Tips on upgrading</a> to the new authentication plugin in Biff v0.7.0.</li>
<li>Writing <a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677101340765339">unit tests</a> for biff/submit-tx.</li>
<li>Notes on <a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677170031829999">how to use upsert</a>.</li>
<li>How to make Biff <a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677247593631479">refresh the web page</a> automatically when you make changes.</li>
<li><a href="https://clojurians.slack.com/archives/C013Y4VG20J/p1677263415039759">Code review</a> of adding a new entity to the database with read/write UI.</li>
</ul>
<p><em>Anything in particular you'd like me to write about? Hit reply.</em></p>
</div>