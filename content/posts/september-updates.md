---
description: There's a new Biff release out, and I'm being sponsored by Clojurists Together.
slug: september-updates
comments-url: https://github.com/jacobobryant/biff/discussions/139
title: 'Biff September updates: Clojurists Together, documentation, in-memory queues'
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/223e141a-e864-41a4-b6b1-d0201ba359b4
published: 2022-10-03T11:20:17 AM
content-type: html
---

<p><a href="https://biffweb.com/p/updates-2022-07/">Two months ago</a> I mentioned I had some plans for adding a bunch of documentation to Biff:</p>
<blockquote>
<p>Right now Biff only has <a href="https://biffweb.com/docs/">reference docs</a>. I want to add a lot more, such as:</p>
<ul>
<li>A series of tutorials that show you how to build some application with Biff, step-by-step. Perhaps a forum + real-time chat application, like Discourse and Slack in one.</li>
<li>A page that curates/recommends resources for learning Clojure and getting a dev environment set up. Aimed at those who are brand new to Clojure and want to use it for web dev. If needed I might write up some of my own articles to go along with it, though I'd prefer to curate existing resources as much as possible.</li>
<li>A series of tutorials/explanatory posts that teach the libraries Biff uses. [...]&nbsp;This is intended for those who prefer a bottom-up approach to learning, or for those who are familiar with Biff and want to deepen their understanding.</li>
</ul>
<p>As part of that, I plan to restructure <a href="https://biffweb.com/">the website</a>, while taking lessons from <a href="https://documentation.divio.com/">The Grand Unified Theory of Documentation</a> into account.</p>
</blockquote>
<p>This was secretly a copy-and-paste-and-slight-edit of my Clojurists Together application, which has been funded! (The grants were announced the day after my last monthly update went out, which is why I'm mentioning this a little late.) Huge thanks to them and everyone who donates! Also huge thanks to JUXT for their continuing sponsorship of Biff.</p>
<p><strong>Documentation</strong></p>
<p>I mentioned in my application that this is a long-term project (especially the third bullet), and so with the funding I'm mainly planning to complete at least the first bullet (the forum tutorial) along with the website restructuring. And then we'll see how far I get into the other bullet points. They'll happen eventually in any case.</p>
<p>Last month I completed the website restructuring. <a href="https://biffweb.com/docs/get-started/intro/">It's very spiffy</a>. Previously the reference docs were on a big single-page thingy rendered with <a href="https://github.com/slatedocs/slate">Slate</a>, and the API docs were rendered with <a href="https://github.com/weavejester/codox">Codox</a>. Now I've written custom code to render both of those alongside the rest of the Biff website. The site is more cohesive now, and it will be easier to add additional documentation sections. Currently there are three sections ("Get Started", "Reference", and "API"); ultimately I plan to have the following sections:</p>
<ul>
<li>Get Started</li>
<li>Tutorial (i.e. the forum tutorial)</li>
<li>Reference</li>
<li>How-To</li>
<li>API</li>
<li>Background Info (this might have essays about design philosophy, for example)</li>
<li>Learn Clojure*</li>
</ul>
<p>*About the last point: I'm currently waffling over whether this should stay as a single page under the "Get Started" section, or if I should combine it with my plans for "a series of tutorials/explanatory posts that teach the libraries Biff uses" as mentioned above. i.e. if I do actually get around to writing a mini book/course thing that teaches Biff from the ground up (e.g. "here's how to start a new project", "here's how to render a static site," and so on), maybe it will be natural to make it accessible for people who are brand new to Clojure. 🤷&zwj;♂️. No need to make a decision now I guess.</p>
<p><strong>v0.5.0: in-memory queues</strong></p>
<p>I cut a <a href="https://github.com/jacobobryant/biff/releases">new Biff release</a>:</p>
<ul>
<li>Biff's XTDB dependency has been bumped to 1.22.0.</li>
<li><code>add-libs</code> is now used to add new dependencies from deps.edn whenever you save a file; no need to restart the REPL.</li>
<li>Biff's feature maps now support a <code>:queues</code> key, which makes it convenient to create <a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/PriorityBlockingQueue.html">BlockingQueues</a> and thread pools for consuming them:</li>
</ul>
<pre class="language-clojure"><code>(defn echo-consumer [{:keys [biff/job] :as sys}]
  (prn :echo job)
  (when-some [callback (:biff/callback job)]
    (callback job)))

(def features
  {:queues [{:id :echo
             :n-threads 1
             :consumer #'echo-consumer}]})

(biff/submit-job sys :echo {:foo "bar"})
=&gt;
(out) :echo {:foo "bar"}
true

@(biff/submit-job-for-result sys :echo {:foo "bar"})
=&gt;
(out) :echo {:foo "bar", :biff/callback #function[...]}
{:foo "bar", :biff/callback #function[...]}</code></pre>
<p>I added these since I have a bunch of background job stuff in <a href="https://yakread.com/">Yakread</a> and it was getting out of hand. Especially since Yakread uses some JavaScript and Python code (specifically, <a href="https://github.com/mozilla/readability">Readability</a>, <a href="https://github.com/Automattic/juice">Juice</a>, and <a href="https://surpriselib.com/">Surprise</a>&mdash;they're opened as subprocesses, and communication happens over pipes) and I want to make sure there isn't more than one Node/Python process running at a time.</p>
<p>So far I've set up a queue + consumer for doing recommendations with Surprise (with more queues to come next week). Each job it receives has a user ID and a set of item IDs. The consumer opens a Python subprocess which loads the recommendation model into memory, takes in the user ID + item IDs over stdin, and spits out a list of predicted ratings on stdout. The queue consumer keeps the subprocess open until all the jobs currently on the queue have been handled.</p>
<p>Having a priority queue will also be handy. Some of the recommendations happen in batch once per day, to make sure users always have something fresh (made with an up-to-date model) ready to go. But Yakread also needs to make additional recommendations while people use the app. For the latter, jobs can be given a higher priority, so they'll still get done quickly even if we're in the middle of a large batch thing.</p>
<p>(Eventually I'd really like to replace all the Python/Javascript stuff with Clojure code so it takes fewer resources, but it's just not worth it at this stage.)</p>
<p>I wondered about if I should try to make something like <a href="https://github.com/ivarref/yoltq">yoltq</a> but for XTDB instead of Datomic, so jobs could be persisted to the database, in order to facilitate retries + distributing to separate worker machines. I decided to stick with the current minimal in-memory implementation since that really is all I need personally at the moment. Persistance can be added to these queues from application code, though. All you have to do is:</p>
<ol>
<li>Instead of calling <code>biff/submit-job</code> directly, save the job as a document in XTDB.</li>
<li>Create a <a href="https://biffweb.com/docs/reference/transaction-listeners/">transaction listener</a> which calls <code>biff/submit-job</code> whenever a job document is created.</li>
</ol>
<p>The sky's the limit from there, I guess. You could:</p>
<ul>
<li>On startup, load any unfinished jobs into the appropriate queues.</li>
<li>Add a wraper to your consumer functions which catches exceptions and marks the job as failed (or marks the job as complete if there isn't an exception).</li>
<li>Create another transaction listener that watches for failed jobs and puts them into a <a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/DelayQueue.html">DelayQueue</a> for retrying.</li>
<li>Add a <a href="https://biffweb.com/docs/reference/scheduled-tasks/">scheduled task</a> that retries any jobs which have been in-progress for too long.</li>
<li>Scale out to a degree by creating a separate worker for each queue (there's a <code>:biff.queues/enabled-ids</code> option for this; you can specify which queues should be enabled on which machines).</li>
</ul>
<p>The big thing you <em>can't</em> do (at least, not well) is have the same queue be consumed by multiple machines. See yoltq's <a href="https://github.com/ivarref/yoltq#limitations">Limitations</a> section, which would also apply to an equivalent XTDB setup. My plan is that when I get to the point where I need more than one machine to consume a single queue (either for throughput or for high availability), I'll just throw in a Redis instance or something and use an already-written job queue library.</p>
<p>As such, while there is more functionality which could be built on top of Biff's in-memory queues, I'm not sure how much of it is really needed. We'll see.</p>
<p>Here's <a href="https://github.com/jacobobryant/biff/blob/master/src/com/biffweb/impl/queues.clj">the implementation</a> for anyone who would like to peruse the code.</p>
<p><strong>Roadmap</strong></p>
<ol>
<li>This weekend I plan to make some more code updates. Mainly I'll replace the <code>task</code> shell script with <a href="https://book.babashka.org/#tasks">bb tasks</a>, so that the task implementations can be stored as library code instead of needing to be copied into new projects.</li>
<li>After that I'll work on the forum tutorial discussed above until it's complete. (This might happen mostly in November since baby #2 will arrive in a few weeks.)</li>
<li>Then I'll add various other documentation, like a page with a curated list of resources for learning Clojure, some how-to articles, a reference page for the new queues feature, maybe an essay or two.</li>
<li>Finally get a public <a href="https://github.com/jacobobryant/platypub">Platypub</a> instance deployed, and make some usability improvements in general. Update the GitHub issues, make a roadmap, and write some contributor docs so it's easier for people to help out.&nbsp;</li>
<li>Take the forum thing mentioned in #2 and turn that into a real-world, useful application like Platypub. Unlike Platypub, I intend the forum to primarily be an educational resource (as the subject of a tutorial), but I do also think it would be fun to have a lightweight Slack/Discourse/Discord/etc alternative to experiment with.</li>
<li>Start working on that "Learn Clojure/Biff from scratch" project I discussed above, unless I think of something better to work on by the time I get through #1-#5.</li>
</ol>
<p>This should last me well into next year.</p>
<p><strong>Meetups</strong></p>
<p>We had two meetups in September: <a href="https://biffweb.com/p/bc-6/">first we played around</a> with Babashka tasks, and then <a href="https://biffweb.com/p/bc-7/">we attempted</a> to use the Fly Machines API as a sandbox for untrusted code. The second one turned out to be at the precise moment that Fly's Machines API was experiencing downtime, so the latter half of the recording might not be very interesting to watch 🙂.</p>
<p>There will be one meetup in October, on Thursday the 13th (<a href="https://www.meetup.com/biff-coding/events/288850026/">RSVP here</a>). After that, it's up in the air due to the "baby #2" thing mentioned above.</p>
<p><strong>Reminders</strong></p>
<ul>
<li>Come chat with us in #biff on <a href="http://clojurians.net/">Clojurians Slack</a> if you haven't joined already. Also feel free to create <a href="https://github.com/jacobobryant/biff/discussions">a discussion thread</a> about anything.</li>
<li>Again, thank you to everyone who sponsors Biff. If you'd like to help support Biff, please consider&nbsp;<a href="https://github.com/sponsors/jacobobryant/">becoming a sponsor</a> too.</li>
<li>I'm available for short-term consulting engagements; <a href="mailto:hello@jacobobryant.com">email me</a> if you have a project you'd like to discuss.</li>
</ul>