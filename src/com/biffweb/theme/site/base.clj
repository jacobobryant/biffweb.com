(ns com.biffweb.theme.site.base
  (:require [com.platypub.util :as common]
            [lambdaisland.hiccup :as h]))

(def head
  [[:script {:src "https://unpkg.com/hyperscript.org@0.9.3"}]
   [:script {:src "https://www.google.com/recaptcha/api.js"
             :async "async"
             :defer "defer"}]
   [:link {:href "https://fonts.googleapis.com", :rel "preconnect"}]
   [:link {:crossorigin "crossorigin",
           :href "https://fonts.gstatic.com",
           :rel "preconnect"}]
   [:link {:href (apply str "https://fonts.googleapis.com/css2?display=swap"
                        (for [f ["Roboto+Slab:wght@700"
                                 "Roboto:wght@500"]]
                          (str "&family=" f)))
           :rel "stylesheet"}]
   [:link {:rel "stylesheet" :href "/css/prism.css"}]
   [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/4.0.0/github-markdown.min.css"}]
   [:script {:src "/js/prism.js"}]])

(defn base-html [ctx & body]
  (apply common/base-html (update ctx :base/head concat head) body))

(def hamburger-icon
  [:div.sm:hidden.cursor-pointer
   {:_ "on click toggle .hidden on #nav-menu"}
   (for [_ (range 3)]
     [:div.bg-white
      {:class "h-[4px] w-[30px] my-[6px]"}])])

(defn nav-options [{:keys [docs-href
                           hide-home]
                    :or {docs-href "/docs/"}}]
  (concat (when-not hide-home
            [["Home" "/"]])
          [["Docs" docs-href]
           ["News" "/newsletter/"]
           ["GitHub" "https://github.com/jacobobryant/biff"]]))

(defn navbar
  ([] (navbar {:class "max-w-prose"}))
  ([opts]
   [:div.py-4
    [:div {:class (conj
                   '[flex
                     max-sm:flex-col
                     items-center
                     gap-4
                     text-lg
                     px-3
                     justify-between
                     mx-auto]
                   (:class opts "max-w-prose"))}
     [:div [:a {:href "/"}
            [:img {:src "https://cdn.findka.com/biff-logo-new.svg"
                   :alt "Biff"
                   :class '["max-h-[30px]"
                            opacity-90]}]]]
     [:div.flex.gap-4.flex-wrap
      (for [[label href] (nav-options opts)]
        [:a {:class '[font-semibold
                      uppercase
                      border-b-2
                      text-stone-800
                      border-stone-700
                      hover:text-indigo-700
                      hover:border-indigo-700
                      text-sm
                      sm:pt-1]
             :href href}
         label])]]]))

(def errors
  {"invalid-email" "It looks like that email is invalid. Try a different one."
   "recaptcha-failed" "reCAPTCHA check failed. Try again."
   "unknown" "There was an unexpected error. Try again."})

(defn subscribe-form-mild [{:keys [show-disclosure sitekey big]}]
  [:div.flex.flex-col.items-center.text-center.px-3
   [:div.h-5]
   [:div.font-bold {:class (when big "text-lg")} "Sign up for Biff: The Newsletter"]
   [:div " Announcements, blog posts,  " [:span.italic "et cetera et cetera"] "."]
   [:div.h-5]
   [:script "function onSubscribe(token) { document.getElementById('recaptcha-form').submit(); }"]
   [:form#recaptcha-form.w-full.max-w-md
    {:action "/.netlify/functions/subscribe"
     :method "POST"}
    [:input {:type "hidden"
             :name "href"
             :_ "on load set my value to window.location.href"}]
    [:input {:type "hidden"
             :name "referrer"
             :_ "on load set my value to document.referrer"}]
    [:div.flex.flex-col.sm:flex-row.gap-2
     [:input {:class '[rounded
                       shadow
                       border-stone-300
                       focus:ring-indigo-700
                       focus:border-indigo-700
                       flex-grow
                       text-black]
              :type "email"
              :name "email"
              :placeholder "Enter your email"
              :_ (str "on load "
                      "make a URLSearchParams from window.location.search called p "
                      "then set my value to p.get('email')")}]
     [:button {:class '[bg-indigo-700
                        hover:bg-indigo-600
                        text-white
                        py-2
                        px-4
                        rounded
                        shadow
                        g-recaptcha]
               :data-sitekey sitekey
               :data-callback "onSubscribe"
               :data-action "subscribe"
               :type "submit"}
      "Subscribe"]]
    (for [[code explanation] errors]
      [:div.text-black.hidden.font-semibold.mt-1
       {:_ (str "on load if window.location.search.includes('error="
                code
                "') remove .hidden from me")}
       explanation])
    [:div.h-3]
    [:div.text-center
     [:a.text-black.hover:text-indigo-700.underline
      {:href "/feed.xml"} "RSS feed"]
     (when-not big
       [:<>
        [:span.w-4.inline-block " · "]
        [:a.text-black.hover:text-indigo-700.underline
         {:href "/newsletter/"} "Archive"]])]]
   [:div.h-12]
   (when show-disclosure
     [:div.sm:text-center.text-sm.leading-snug.opacity-75.w-full.mb-3
      (common/recaptcha-disclosure {:link-class "underline"})])])
