(ns com.biffweb.theme.site.templates.post
  (:require [com.biffweb.theme.site.base :as base]
            [com.platypub.util :as common]
            [lambdaisland.hiccup :as-alias h]))

(defn render [{:keys [post site/timezone recaptcha/site-key] :as opts}]
  (base/base-html
   opts
   [:div.grow.flex.flex-col.bg-stone-200
    (base/navbar (assoc opts :class (if (:video (:tags post))
                                      "max-w-screen-lg"
                                      "max-w-prose")))
    [:div.mx-auto.sm:px-3.py-3.text-lg.flex-grow.w-full
     {:class (if (:video (:tags post))
               "max-w-screen-lg"
               "max-w-prose")}
     [:div.bg-white.p-4.markdown-body
      [:h1 (:title post)]
      [:p.text-sm
        [:a {:href "https://obryant.dev"} "Jacob O'Bryant"]
        " | "
        (common/format-date (:published post) "d MMM yyyy" timezone)]
      [:div.h-2]
      [:div [::h/unsafe-html (:html post)]]]]
    (base/subscribe-form-mild {:sitekey site-key
                               :show-disclosure true})]))
