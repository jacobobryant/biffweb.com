(ns com.biffweb.theme.site.templates.not-found
  (:require [com.biffweb.theme.site.base :as base]))

(defn render [opts]
  (base/base-html
   (assoc opts :base/title "Page not found")
   [:div.grow.flex.flex-col.bg-stone-200
    (base/navbar opts)
    [:div.mx-auto.sm:px-3.py-3.text-lg.flex-grow.w-full.max-w-prose
     [:div.markdown-body.bg-white.p-4
      [:h2 "Page not found"]
      [:p "Try the " [:a {:href "/"} "home page"] " instead."]]]]))

