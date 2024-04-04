(ns com.biffweb.theme.site.templates.card
  (:require [com.biffweb.theme.site.base :as base]))

(defn render [{:keys [site post] :as opts}]
  (base/base-html
   opts
   [:div.mx-auto.border.border-black.bg-stone-900
    {:style {:width "1202px"
             :height "620px"}}
    [:div.flex.flex-col.justify-center.h-full.p-12
     [:div [:img {:src "https://cdn.findka.com/biff-logo-new-light.svg"
                  :alt "Logo"
                  :style {:max-height "60px"}
                  :class '[opacity-90]}]]
     [:div.grow]
     [:h1.font-bold.leading-tight.opacity-90.text-white
      {:class "text-[6rem]"}
      (:title post)]
     [:div.grow]]]))
