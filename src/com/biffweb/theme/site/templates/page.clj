(ns com.biffweb.theme.site.templates.page
  (:require [com.biffweb.theme.site.base :as base]
            [lambdaisland.hiccup :as h]))

(defn render [{:keys [page] :as opts}]
  (base/base-html
   opts
   [:div.grow.flex.flex-col.bg-stone-200
    (base/navbar opts)
    [:div.mx-auto.sm:px-3.py-3.text-lg.flex-grow.w-full.max-w-prose
     [:div.markdown-body.bg-white.p-4
      [::h/unsafe-html (:html page)]]]]))
