---
description: Simple hack to include a `Set-Cookie` header with GET requests by making
a background POST request.
slug: how-to-set-cookies-on-get
title: How to set cookies on GET requests with Biff
published: TODO
author: Marcelina Hołub
content-type: html
---

<p>
Biff, like many Clojure web frameworks, has certain restrictions on GET request responses. Specifically, it typically expects GET handlers to return HTML or JSON, not Ring response maps. This can make it challenging to set cookies on GET requests.
</p>
<p>Here's a simple general approach to overcome this limitation. 
Note that you can also try using the official <a href="https://ring-clojure.github.io/ring/ring.middleware.cookies.html">Ring Cookies middleware</a>, but I haven't tested that.</p>

<ol type="1">
  <li>First, create a middleware that will set an appropriate key in the <code>ctx</code> map of your GET handlers:</li>
<pre class="language-clojure"><code>
(defn create-cookie-middleware
  [{:keys [cookie-getter cookie-name cookie-setter]}]
  (fn [handler]
    (fn [ctx]
    ;; if cookie-getter determines that a condition
    ;; for setting a cookie is met, update the context map 
    ;; with the cookie data
      (if (cookie-getter ctx)
        (-> ctx
            (assoc :set-cookie true)
            (assoc-in [:cookie-data cookie-name] (cookie-setter ctx))
            handler)
        (handler ctx)))))

    ;; For example:
(def user-cookie-middleware
  (create-cookie-middleware
   {:cookie-getter (fn [ctx] (nil? (get-in ctx [:cookies "user-id" :value])))
    :cookie-name "user-id"
    :cookie-setter (fn [ctx] {
                    :value (str (random-uuid))
                    :max-age (str (* 365 86400))
                    :domain (last (clojure.string/split (:biff/base-url ctx) #"/") 2)
                    :path "/"
                    :secure "true"
                    :same-site "Lax"
                    :http-only "true"}))
    </code></pre>

<li>If you don't need that much flexibility and only need to set one cookie, you can write a simpler middleware like this one:</li>
<pre class="language-clojure"><code>
;; example: check if user_id cookie is present
(defn get-user-id [cookies]
(parse-uuid (get-in cookies ["user_id" :value]))
  )

(defn need-to-set-cookie? [ctx]
(seq (get-user-id (:cookies ctx)))

(defn wrap-set-cookie
[handler]
  (fn [ctx]
    (let [new-ctx (if (need-to-set-cookie? ctx)
  ;; :set-cookie prop will be passed to handler
                    (assoc ctx :set-cookie true)
                    ctx)]
      (handler new-ctx))))
      </code></pre>

  <li>Secondly, we will need to create a handler that will return a response
      with a <code>Set-Cookie</code> header</li>
      <pre class="language-clojure">
        <code>
;; helper function to build a Set-Cookie header string
(defn cookie->header-string [name value]
(str name "=" value "; Path=/; HttpOnly; SameSite=Lax"))

(defn set-cookie-handler [ctx]
  ;; here we read the data from context key. 
  ;; Using the simpler approach, you'd most likely use a let form that 
  ;; binds a cookie value to the output of a function that generates a cookie map
  (if-let [cookie-data (:cookie-data ctx)]
    {:status 200
     :headers {"Set-Cookie" (map (fn [[name value]]
                                   (cookie->header-string name value))
                                ;; cookie-data is a map of cookie name string
                                ;; to cookie value map
                                 cookie-data)}}
    ;; or just log the error if the cookie is not critical to your site's operation
    {:status 500
     :body "No cookie data provided"}))
          </code>
  </pre>
<li>Next, update your routes to include the newly created middleware and set-cookie-handler.
            We will use the user-cookie-middleware function that builds upon
            create-cookie-middleware: </li>
<pre class="language-clojure">
            <code>
(def module 
            {:routes ["" {:middleware [user-cookie-middleware
  ;; ...          
  ]}
["/" {:get home}]
["/set-cookie" {:post set-cookie-handler}]
  ]}
  )
</code>
        </pre>

<li>Finally, add the hidden self-submitting form to your page template:</li>
  <pre class="language-clojure">
    <code>
      (defn home [ctx]
(page ctx
    ;; put this somewhere after your first tag
(when (:set-user-cookie ctx)
      (biff/form
      {;; must be a root-level path, otherwise the cookie will get an incorrect path
                          :hx-post "/set-cookie"
                          :hx-trigger "load"
                          :hx-swap "none"
                          }))
            )
    </code>
  </pre>
</ol>

<h2>Summary</h2>
<p>One potential issue I've encountered while implementing this in my application
was the fact that upon the first load, the CSRF token would be missing in the request,
but then be included in it upon the second request.</p>
<p>And of course, this approach will not work without JavaScript.</p>
<p>In such case, the most approachable solution is restructuring your application so that the cookie is 
included in the response to an "active" form submission.</p>
<p>This can however pose challenges with incorrect <code>Path</code> attributes of the cookies if you're not careful.</p>
<p>Alternatively, you can manipulate the session storage,</p>
<p>or set up some clever redirection round-trip, but this could easily lead to slower page load or redirection loops.</p>

<p>You can further enhance this approach by such steps as:</p>
<ul>
  <li>Adding more parameters to the cookie factory, like <code>Max-Age</code>,
  <code>HttpOnly</code> etc.</li>
  <li>Creating a higher-order function to group multiple cookie factories if you 
  need to set multiple cookies at the same time in a neat way.</li>
</ul>
