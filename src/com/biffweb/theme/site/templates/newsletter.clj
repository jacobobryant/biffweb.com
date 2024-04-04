(ns com.biffweb.theme.site.templates.newsletter
  (:require [com.biffweb.theme.site.base :as base]
            [com.platypub.util :as common]))

(defn render [{:keys [posts recaptcha/site-key site/timezone] :as opts}]
  (base/base-html
   (assoc opts :base/title "Biff: The Newsletter")
   [:div.bg-stone-200.h-full.flex-grow
    (base/navbar opts)
    [:div.h-4]
    (base/subscribe-form-mild {:bg :light
                               :sitekey site-key
                               :big true})
    [:div.max-w-prose.mx-auto.sm:px-3.text-lg
     (for [{:keys [title slug published description tags]} posts
           :when (not (:unlisted tags))]
       [:a.block.mb-6.bg-white.p-3.hover:bg-stone-100.cursor-pointer.text-base
        {:href (str "/p/" slug "/")}
        [:div {:class '[text-xs
                        uppercase
                        font-semibold
                        text-stone-500]}
         (common/format-date published "d MMM yyyy" timezone)]
        [:div.h-1]
        [:div.text-xl.font-bold title]
        [:div.h-1]
        [:div.text-stone-700 description]])]
    [:div.h-10]
    [:div.sm:text-center.text-sm.leading-snug.opacity-75.w-full.px-3
     (common/recaptcha-disclosure {:link-class "underline"})]
    [:div.h-3]]))
