---
description: 'Jeremy Taylor drove in today''s pair session, and he fixed a bug in Platypub where users would get an Internal Server Error if they tried to create a new site or newsletter without first adding API keys for Netlify or Mailgun, respectively (issue #19).'
tags:
- biff
- video
slug: bpp-1
title: 'Biff Pair Programming #1'
image: https://platypub.sfo3.cdn.digitaloceanspaces.com/e7a9fb1e-e7ce-4ddc-a6d5-433dc1789b15
published: 2022-07-07T17:23:53 PM
content-type: html
---

<div style="padding: 56.25% 0 0 0; position: relative;"><iframe style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;" title="Platypub Pair Programming #1" src="https://player.vimeo.com/video/727866989?h=e189564aa0&amp;badge=0&amp;autopause=0&amp;player_id=0&amp;app_id=58479" frameborder="0" allowfullscreen="allowfullscreen"></iframe></div>
<p><em><br>This is the first in a series of Biff Pair Programming Sessions<sup>&reg;</sup>, in which we tackle an issue in an open-source Biff project for the sake of education, camaraderie, and glory.</em></p>
<p>Jeremy Taylor drove in today's pair session, and he fixed a bug in <a href="https://github.com/jacobobryant/platypub">Platypub</a> where users would get an Internal Server Error if they tried to create a new site or newsletter without first adding API keys for Netlify or Mailgun, respectively (<a href="https://github.com/jacobobryant/platypub/issues/19">issue #19</a>).</p>
<ul>
<li>0:00 &ndash; Introduction</li>
<li>3:50 &ndash; Running Platypub</li>
<li>9:00 &ndash; Selecting an issue: showing a better error message if you haven't set API keys</li>
<li>11:00 &ndash; Start coding</li>
<li>15:00 &ndash; Tangent: how one should access the system map</li>
<li>19:00 &ndash; Resume coding</li>
<li>22:05 &ndash; Doing a system refresh. I think we screwed something up here because we had to restart the JVM before eval-ing worked again.</li>
<li>32:05 &ndash; Start working on an error message</li>
<li>34:18 &ndash; Jeremy has a burst of insight and realizes we can just disable the button instead of showing an error message after the fact</li>
<li>40:45 &ndash; "New newsletter" button is now disabled properly, now we do the same for "New site"</li>
<li>41:50 &ndash; You can hear my wife in the background</li>
<li>43:30 &ndash; Throw in a little CSS</li>
<li>47:40 &ndash; Victory is ours. Jeremy submits the pull request.</li>
</ul>
<p><em>The next session is scheduled for <a href="https://calndr.link/event/jcRxgJXIb2">July 21st at 16:30 UTC</a>.</em></p>