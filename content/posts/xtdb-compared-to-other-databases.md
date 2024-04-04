---
description: XTDB, the database that Biff uses by default, is still fairly niche. Why not go with Postgres or Datomic?
slug: xtdb-compared-to-other-databases
title: XTDB compared to other databases
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/e0dbed33-48c4-4d26-899a-08cd518fbc97
published: 2023-10-31T10:15:41 AM
content-type: html
---

<p><a href="https://www.xtdb.com/">XTDB</a>, the database that Biff uses by default, is still fairly niche. Why not go with Postgres? Datomic is another relevant option since it has a lot in common with XTDB and has been around longer. I&rsquo;ve used and like all three databases, but XTDB is my personal preference. I&rsquo;d like to explain that here, especially for anyone who&rsquo;s considering using Biff and is wondering if XTDB is a good fit for them.</p>
<p>As always, I&rsquo;m speaking from the standpoint of a solo developer; my analysis would be different if I were, say, the head of an engineering department. <em>In particular</em>, I&rsquo;m making the assumption that any differences in query and transaction performance will be unimportant for my apps (with the exception of latency differences, discussed in the next section). If you&rsquo;ll be deploying something at scale, that&rsquo;s not an assumption you should make. These databases all have vastly different implementations; better to run some tests and see what the performance actually looks like for your use case.</p>
<p>As another disclosure I should mention that JUXT (the company behind XTDB) sponsors Biff.</p>
<h2 id="xtdb-vs.-postgres">XTDB vs.&nbsp;Postgres</h2>
<p>The main architectural difference to Postgres is that XTDB is immutable. When you submit a transaction, XTDB puts it into a log of some sort. That log could be stored in Kafka, the local filesystem, Postgres*, &hellip; the implementation is pluggable. Then you can have any number of XTDB nodes (JVM processes) which consume the transaction log and use it to create queryable indexes on the node&rsquo;s local filesystem (typically with RocksDB). These indexes let you query the database at any point in time: transactions that were indexed after the point you specify will be ignored. This architecture comes with several benefits and at least one downside you should be aware of.</p>
<p>\*And yes, this means that using XTDB &ldquo;instead&rdquo; of Postgres sometimes technically means using XTDB <em>on top of</em> Postgres.</p>
<p>First of all, since queries are served from local indexes, you get &ldquo;read replicas&rdquo;/caches for free. If your app is written in Clojure or another JVM language, it can run in the same process as the XTDB node, which means queries don&rsquo;t even require a network hop. That makes it easier to write HTTP endpoints with low response times, especially when there&rsquo;s complex logic involved. I&rsquo;ve found this particularly helpful in my own work building recommender systems, which are very read heavy.</p>
<p>You also have the option of running your XTDB nodes in dedicated processes and querying them over HTTP. You could still get some latency benefits from this setup if you put your app servers and XTDB nodes close together, e.g.&nbsp;with <a href="https://fly.io/">Fly</a>. (Speaking of Fly, these latency benefits are the same as what you get from <a href="https://fly.io/blog/introducing-litefs/">distributed SQLite</a>.)</p>
<p>Another benefit of immutability is that it&rsquo;s easy to recover data without going through a separate backup system, in the same way that it&rsquo;s easy to recover deleted code from old git commits. Even when you &ldquo;delete&rdquo; a record, you can still go back and see all the previous versions of that record and restore it if needed. When you do need to permanently delete data&mdash;e.g.&nbsp;when a user wants to delete their account&mdash;there is an &ldquo;evict&rdquo; operation.</p>
<p>In a similar vein, you also don&rsquo;t have to worry about what timestamps you might need to store. If your records have a <code>foo</code> column and you later decide you need a <code>foo_added_at</code> column, you can run a migration that inspects each record&rsquo;s history and backfills <code>foo_added_at</code> for existing records.</p>
<p>The number of times I&rsquo;ve actually <em>done</em> either of those things is only a few, but in the day-to-day I still benefit from peace of mind/not even having to think about it.</p>
<p>Finally, immutability can be handy for debugging. For example, your HTTP request logs can include the point in time at which the database was being queried within that request, which makes it trivial to rerun the queries and get the same results later on. In Clojure, I&rsquo;ll often save an incoming HTTP request to a var so I can manipulate it in the REPL; it&rsquo;s convenient that the request includes a snapshot of the database. Pure functions can take the database snapshot as a parameter and remain pure.</p>
<p>With all that being said, immutability isn&rsquo;t free. Reads may be trivial to horizontally scale, but your write performance in XTDB will be bottlenecked by how fast transactions can get onto the transaction log and be indexed by the XTDB nodes. It&rsquo;s effectively a single-writer system. As a solo developer I have no experience with hitting XTDB&rsquo;s scaling limits, so if you&rsquo;re worried about that, I&rsquo;ll refer you to <a href="https://www.xtdb.com/#request-demo">the XTDB team</a>. My impression is that you should be able to get pretty far with Kafka as the transaction log. &macr;\_(ツ)_/&macr;</p>
<h3 id="bitemporality">Bitemporality</h3>
<p>This is the whole reason XTDB exists, which makes it somewhat amusing that I haven&rsquo;t mentioned it until now. Although the &ldquo;time travel&rdquo; benefits of immutability (being able to query past snapshots of the database) can be useful for operations, doing time travel in your business logic is <a href="https://vvvvalvalval.github.io/posts/2017-07-08-Datomic-this-is-not-the-history-youre-looking-for.html">fraught with peril</a>. Suffice it to say that <a href="https://v1-docs.xtdb.com/concepts/bitemporality/">bitemporality</a> addresses that problem.</p>
<p>I haven&rsquo;t yet needed bitemporality in any of my apps. Fortunately, XTDB <a href="https://www.xtdb.com/blog/but-bitemporality-always-introduces-complexity">was designed</a> so that the bitemporality features stay out of the way until you need them. You can use XTDB as a general-purpose database, and if you ever find that you <em>would</em> benefit from bitemporality, it&rsquo;s there.</p>
<h3 id="quality-of-life">Quality of life</h3>
<p>Aside from the high-level architecture, I also find the combination of <a href="https://v1-docs.xtdb.com/language-reference/datalog-queries/#_edn_datalog">Datalog</a> and <a href="https://v1-docs.xtdb.com/language-reference/datalog-queries/#pull">pull expressions</a> to be more ergonomic than SQL. I will admit though that the majority of queries I do would be equally compact in either query language.</p>
<p>In addition, XTDB is quite flexible when it comes to data modeling&mdash;you don&rsquo;t need a separate table to model many-to-many relationships, for example. XTDB is also schemaless, or as I think of it, &ldquo;bring your own schema.&rdquo; Biff lets you define your schema with <a href="https://github.com/metosin/malli">Malli</a> and then ensures your transactions conform before passing them to XTDB. I like that I <a href="https://blog.datomic.com/2017/01/the-ten-rules-of-schema-growth.html">don&rsquo;t need migrations</a> just for adding new columns.</p>
<h2 id="xtdb-vs.-datomic">XTDB vs.&nbsp;Datomic</h2>
<p>Much of the previous section also applies to <a href="https://www.datomic.com/">Datomic</a>. It&rsquo;s an immutable database with support for Datalog queries and graph data modeling, although it isn&rsquo;t bitemporal.</p>
<p>I used Datomic throughout 2019 but switched to XTDB in 2020 (back when it was still called Crux). I didn&rsquo;t want to run my apps on AWS anymore (I <a href="https://www.lastweekinaws.com/blog/should-i-pick-digitalocean-or-aws-for-my-next-project/">switched to DigitalOcean</a>), so Datomic Cloud was no longer a good fit, and the $5k/year license fee for Datomic On-Prem was a nonstarter. On top of that, setting up Datomic was less convenient than it was for XTDB, since Datomic required two separate JVM processes (the transactor and a peer).</p>
<p>A lot has changed in the past few years! There are <a href="https://blog.datomic.com/2023/04/datomic-is-free.html">no more licensing fees</a>, and you can run single-process systems with <a href="https://docs.datomic.com/cloud/datomic-local.html">Datomic Local</a>. Datomic is much more feasible now for solo developers, and while I still prefer XTDB, you can&rsquo;t go wrong with either.</p>
<h3 id="main-factors">Main factors</h3>
<p>In my mind there are three big factors that might point you to one or the other:</p>
<ol type="1">
<li>If you know that bitemporality will be useful for you, consider XTDB.</li>
<li>If you&rsquo;re deploying on AWS and/or want tight integration with AWS, consider Datomic Cloud.</li>
<li>If you want first-class SQL support, then wait with bated breath for <a href="https://www.xtdb.com/v2">XTDB 2</a>. I will probably make SQL the default in Biff since lots of people are already familiar with it, and those who prefer Datalog can switch easily.</li>
</ol>
<p>If you&rsquo;re ambivalent about those points, then again, they&rsquo;re both good choices. Take them each for a spin and see what feels best. Below are a few minor factors I&rsquo;ve thought about myself.</p>
<h4 id="operation">Operation</h4>
<p>For a certain class of solo-developed app, XTDB is still slightly more convenient operations-wise (unless you&rsquo;re already planning to run on AWS, as mentioned above). You can deploy your app to a single VM and then use a managed Postgres instance as the storage backend&mdash;a nice setup for apps that are serious enough that you&rsquo;d be uncomfortable storing all your data on the filesystem, but still early-stage enough that keeping the number of moving parts to a minimum is helpful. Like a part-time business.</p>
<p>You can also <a href="https://docs.datomic.com/pro/overview/storage.html#sql-database">use managed Postgres</a> as the backend for Datomic Pro, but besides that you&rsquo;ll still have to run at least two separate JVM processes, probably on separate VMs: <a href="https://docs.datomic.com/pro/overview/architecture.html#transactors">the transactor</a> (which processes new transactions and sends them to the storage backend) and <a href="https://docs.datomic.com/pro/overview/architecture.html#peer-server">the peer</a> (which contains the query indexes and can run in the same JVM as your application, similar to an XTDB node). XTDB avoids the need for a separate transactor because transactions are submitted directly to the storage backend and all transaction processing is done subsequently on the XTDB nodes.</p>
<pre class="language-bash"><code>Transaction lifecycle
=====================
App server --&gt; storage backend --&gt; XTDB node/app server
App server --&gt; transactor --&gt; storage backend
                   |
                   --&gt; Datomic peer/app server</code></pre>
<h4 id="documents-vs.-datoms">Documents vs.&nbsp;datoms</h4>
<p>In Datomic the atomic unit of data is a &ldquo;datom&rdquo; (which is like the value for a single row and column, such as &ldquo;User 12&rsquo;s email address is hello@example.com.&rdquo; Transactions are mostly collections of datoms.</p>
<pre class="language-clojure"><code>{:db/id 1234
 :user/email "hello@example.com" ; &lt;-- that's a datom
 :user/name "bob"                ; &lt;-- there's another one
 :user/age 30}                   ; &lt;-- and another</code></pre>
<div id="cb2" class="sourceCode"></div>
<p>In XTDB, transactions operate on entire documents (similar to rows), and XTDB provides only a few relatively low-level operations. If you want to update a single attribute in a document, you have to resubmit the entire document.</p>
<pre class="language-clojure"><code>;; Old document
(def bob {:xt/id 1234
          :user/email "hello@example.com"
          :user/name "bob"
          :user/age 30}

(xt/submit-tx node
  [[:xtdb.api/put (assoc bob :user/email "different@example.com")]])</code></pre>
<div id="cb3" class="sourceCode"></div>
<p>And if you do that naively, there&rsquo;s a possibility that concurrent transactions will clobber each other.</p>
<pre class="language-clojure"><code>;; If we run this at the same time as the submit-tx call
;; above, the email address might end up as
;; hello@example.com instead of different@example.com.
(xt/submit-tx node
  [[:xtdb.api/put (assoc bob :user/age 31)]])</code></pre>
<div id="cb4" class="sourceCode"></div>
<p>You can fix that by using a combination of&nbsp;<a href="https://v1-docs.xtdb.com/language-reference/datalog-transactions/#match">match operations</a> and <a href="https://v1-docs.xtdb.com/language-reference/datalog-transactions/#transaction-functions">transaction functions</a>. Biff does this already via its <a href="https://biffweb.com/docs/reference/transactions/"><code>submit-tx</code> wrapper</a>; if you&rsquo;re not using Biff, you&rsquo;ll need to roll your own. Though I am planning to release Biff&rsquo;s <code>submit-tx</code> as a standalone library, hopefully soon&hellip;.</p>
<p>In any case, once you have something in place to handle concurrent XTDB transactions, I haven&rsquo;t found the datoms-vs-documents difference to be significant. They both get the job done.</p>
<h4 id="schema">Schema</h4>
<p>Datomic schema is baked into the database, similar to Postgres and other RDBMSs. To add new schema, you run a transaction. As mentioned above, XTDB leaves schema enforcement to you. So Biff uses <a href="https://github.com/metosin/malli">Malli</a> for schema definitions, and transactions are checked against the schema before they&rsquo;re submitted. Similar to the documents-vs-datoms issue above, there&rsquo;s a bit of extra setup involved if you&rsquo;re using XTDB (unless you&rsquo;re using it via Biff), but after that the difference is moot in my experience. I do really like being able to define schema with Malli.</p>
<p>One point possibly worth mentioning: in Biff&rsquo;s approach, schema lives in your code (i.e.&nbsp;in your apps&rsquo; memory), not in the database. The Datomic team views this as <a href="https://blog.datomic.com/2017/01/the-ten-rules-of-schema-growth.html">unequivocally a bad thing</a>. As a solo developer I&rsquo;ve found schema-in-code to be more convenient than Datomic&rsquo;s approach, but for more complex systems I could see myself wanting to store schema in the database&mdash;for example, if you have a database that&rsquo;s accessed by multiple services.</p>
<p>Even with XTDB, you could store your Malli schemas inside the database and query for them whenever you submit a transaction. Though you would still be relying on clients to do their own schema enforcement; XTDB won&rsquo;t do it at the database level.</p>
<h2 id="the-future">The future</h2>
<p>Regardless of which route you take, I&rsquo;m excited to see what happens in the world of immutable, Clojurey databases. Datomic being backed by a large company (Nubank) and XTDB getting <a href="https://www.xtdb.com/v2">first-class SQL</a> and more are both promising developments.</p>