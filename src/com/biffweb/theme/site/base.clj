(ns com.biffweb.theme.site.base
  (:require [com.platypub.util :as common]))

(def head
  [[:script {:src "/vendor/unpkg.com/hyperscript.org@0.9.3.js"}]
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
   [:link {:rel "stylesheet" :href "/vendor/cdnjs.cloudflare.com/ajax/libs/github-markdown-css/4.0.0/github-markdown.min.css"}]
   [:script {:src "/js/prism.js"}]])

(def biff-2-docs-href
  "https://github.com/jacobobryant/biff/blob/master/README.md")

(def legacy-docs-banner
  [:div.bg-yellow-300.text-yellow-950
   [:div.mx-auto.max-w-screen-xl.px-3.py-2.text-center.text-sm.font-semibold
    "You are viewing the legacy documentation for Biff 1.x. View the "
    [:a.underline.hover:text-yellow-700
     {:href biff-2-docs-href}
     "Biff 2 documentation"] "."]])

(defn base-html [ctx & body]
  (apply common/base-html (update ctx :base/head concat head) body))

(defn nav-options [{:keys [hide-home]}]
  (concat (when-not hide-home
            [["Home" "/"]])
          [["Docs" biff-2-docs-href]
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

(defn subscribe-form-mild [{:keys [show-disclosure sitekey big dev]}]
  [:<>
   [:iframe {:title "Subscribe to Biff: The Newsletter"
             :src "https://platypub.com/subscribe/AaDkA0Aoc8GXtHano9ZaZw"
             :scrolling "no"
             :style "display:block;width:100%;height:260px;border:0;overflow:hidden"}]
   [:script
    (str "(function(){var frame=document.currentScript.previousElementSibling;"
         "var origin=new URL(frame.src).origin;"
         "window.addEventListener('message',function(event){"
         "if(event.source===frame.contentWindow&&event.origin===origin&&"
         "event.data&&event.data.type==='platypub:resize'&&"
         "Number.isFinite(event.data.height)){"
         "frame.style.height=Math.max(1,event.data.height)+'px';"
         "}});})();")]])
