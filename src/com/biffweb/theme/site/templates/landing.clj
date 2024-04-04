(ns com.biffweb.theme.site.templates.landing
  (:require [com.biffweb.theme.site.base :as base]
            [com.biffweb.theme.icons :as icons]
            [com.platypub.util :as common]))

(def sponsors
  [{:img "https://avatars.githubusercontent.com/u/4767299?v=4"
    :url "https://github.com/jeffp42ker"}
   {:img "https://avatars.githubusercontent.com/u/53870456?v=4"
    :url "https://github.com/john-shaffer"}
   {:img "https://avatars.githubusercontent.com/u/23649855?s=200&v=4"
    :url "https://github.com/Flexiana"}
   {:img "https://avatars.githubusercontent.com/u/19023?v=4"
    :url "https://github.com/tbrooke"}
   {:img "https://avatars.githubusercontent.com/u/54562307?v=4"
    :url "https://github.com/loganrios"}
   {:img "https://avatars.githubusercontent.com/u/5285452?v=4"
    :url "https://github.com/teodorlu"}
   {:img "https://avatars.githubusercontent.com/u/9289130?v=4"
    :url "https://github.com/wuuei"}
   {:img "https://avatars.githubusercontent.com/u/193480?v=4"
    :url "https://github.com/laheadle"}])

(def info-blocks
  [{:icon "database-regular"
    :title "XTDB"
    :content (str "Bring immutability to your database, not just your code. "
                  "Biff adds schema enforcement with Malli.")}
   {:icon "code"
    :title "htmx"
    :content (str "Create rich, interactive UIs without leaving the backend. "
                  "Throw in a dash of _hyperscript for light client-side scripting.")}
   {:icon "lock-regular"
    :title "Authentication"
    :content "Passwordless, email-based authentication, with support for magic link and one-time passcode flows."}
   {:icon "server-regular"
    :title "Ready to deploy"
    :content "Biff comes with code for provisioning an Ubuntu VPS, or you can deploy an Uberjar with Docker."}
   {:icon "terminal"
    :title "REPL made easy"
    :content [:<> "Changes are evaluated whenever you save a file. "
              "Connect to a production REPL and develop your whole app on the fly."]}
   {:icon "file-lines-regular"
    :title "Meticulously documented"
    :content (str "Get started with the tutorial, delve into the reference docs, "
                  "or tinker with the starter project and inspect the doc strings.")}])

(defn render [opts]
  (base/base-html
   (assoc opts :base/title "Biff | Clojure web framework")
   [:div {:class '[text-white
                   flex
                   flex-col
                   grow]}
    [:div
     [:div.bg-black.relative
      [:div {:class '[absolute
                      inset-0
                      bg-center
                      bg-cover
                      opacity-50]
             :style {:background-image "url('https://cdn.findka.com/biff/space-blast-blue.jpg')"}}]
      [:div.bg-center.bg-cover.relative.z-10
       [:div.py-4
        [:div.flex.mx-auto.items-center.gap-4.text-lg.flex-wrap.px-3.max-w-screen-md
         [:a {:href "/"}
          [:img {:src "https://cdn.findka.com/biff-logo-new-light.svg"
                 :alt "Biff"
                 :style {:box-shadow "0 0 20px black"
                         :background-color "rgba(0, 0, 0, 0.5)"}
                 :class '["max-h-[25px]"
                          "sm:max-h-[30px]"
                          opacity-90]}]]
         [:div.flex-grow]
         (for [[label href] (base/nav-options (assoc opts :hide-home true))]
           [:a {:class '[font-semibold
                         uppercase
                         border-b-2
                         text-stone-100
                         border-stone-300
                         hover:text-indigo-300
                         hover:border-indigo-300
                         text-xs
                         sm:text-sm
                         pt-1]
                :style {:background-color "rgba(0, 0, 0, 0.5)"
                        :box-shadow "0 0 10px black"}
                :href href}
            label])]]

       [:div.py-16.flex.flex-col.items-center.flex-grow.bg-center.px-3
        [:h1 {:class '[font-bold
                       text-3xl
                       md:text-4xl
                       text-center
                       "max-w-[360px]"
                       sm:max-w-none
                       rounded-full
                       leading-tight]
              :style {:background-color "rgba(0, 0, 0, 0.5)"
                      :box-shadow "0 0 20px black"}}
         "Biff helps solo developers move fast."]
        [:div.h-7]
        [:a {:class '[text-center
                      py-2
                      px-8
                      rounded
                      font-semibold
                      text-lg
                      md:text-xl
                      roboto
                      font-medium
                      bg-gradient-to-br
                      from-indigo-700
                      to-blue-700
                      "from-30%"
                      hover:from-indigo-600
                      hover:to-blue-600
                      "hover:from-30%"]
             :href (:docs-href opts "/docs/")}
         "Get Started"]
        [:div.h-7]
        [:div {:class '[mx-auto
                        text-xl
                        md:text-2xl
                        text-center
                        max-w-xl
                        rounded-full]
               :style {:background-color "rgba(0, 0, 0, 0.5)"
                       :box-shadow "0 0 20px black"}}
         "Biff is a batteries-included web framework for Clojure. "
         "Launch new projects quickly without getting bogged down in complexity later."]]]]]

    [:div {:style {:min-height "100vh"}
           :class '[flex
                    flex-col]}
     [:div {:class '[py-12
                     bg-stone-200
                     z-10]}
      [:div.mx-auto.px-4
       [:div.text-lg.md:text-xl.text-center.mx-auto.text-black
        {:class "max-w-[520px]"}
        "Biff curates libraries and tools from across the ecosystem "
        "and composes them into one polished whole."]
       [:div.h-10]
       [:div {:class '[grid
                       sm:grid-cols-2
                       lg:grid-cols-3
                       gap-4
                       text-black
                       justify-center
                       "max-w-[67rem]"
                       mx-auto]}
        (for [{:keys [icon title content]} info-blocks]
          [:div.bg-white.p-3.shadow-md.border-l-4.border-indigo-700
           [:div.font-bold.text-stone-800
            (icons/base icon {:class '[w-4 h-4]})
            [:span.align-middle " " title]]
           [:div.h-1]
           [:div.text-stone-800 content]])]
       [:div.h-10]
       [:div {:class '["max-w-[34rem]"
                       mx-auto
                       md:text-lg
                       text-center
                       text-black]}
        [:span.font-bold.text-stone-800 "Strong defaults, weakly held."]
        " Biff is designed to be taken apart and modified, so it doesn't get in the way"
        " as your needs evolve."]]]

     [:div.pt-12.pb-16.bg-stone-900.grow.z-10
      [:div.px-4.max-w-screen-md.mx-auto.w-full.text-white
       [:div.text-2xl.font-bold.text-center "Sponsors"]
       [:div.h-5]
       [:div.flex.items-center.justify-center.flex-col.sm:flex-row
        [:a {:href "https://juxt.pro/"
             :target "_blank"}
         [:img.sm:mt-4
          {:style {:height "60px"}
           :src "/img/juxt-logo.svg"}]]
        [:div.h-8.w-12]
        [:a {:href "https://www.clojuriststogether.org/"
             :target "_blank"}
         [:img
          {:style {:height "80px"}
           :src "/img/clj-together-logo.svg"}]]]
       [:div.h-8]
       [:div.flex.gap-4.mx-auto.justify-center.flex-wrap
        (for [{:keys [img url]} sponsors]
          [:a {:href url :target "_blank"}
           [:img {:src (common/cached-img-url {:url img :w 160 :h 160})
                  :width "40px"
                  :height "40px"
                  :style {:border-radius "50%"}}]])]
       [:div.h-8]
       [:div.flex.justify-center
        [:a.bg-indigo-700.hover:bg-indigo-600.text-white.text-center.py-2.px-4.rounded.md:text-lg
         {:class '[shadow-inner
                   roboto
                   font-medium]
          :href "https://github.com/sponsors/jacobobryant"}
         "Support Biff"]]]]]]))
