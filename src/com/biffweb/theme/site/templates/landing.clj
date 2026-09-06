(ns com.biffweb.theme.site.templates.landing
  (:require [com.biffweb.theme.site.base :as base]
            [com.biffweb.theme.icons :as icons]))

(def info-blocks
  [{:icon "database-regular"
    :title "SQLite"
    :content (str "The database so fast it should be illegal. "
                  "Now with support for more than 3 data types.")}
   {:icon "code"
    :title "Datastar"
    :content (str "Create interactive UIs without leaving the backend. "
                  "Great for simple and complex UIs.")}
   {:icon "lock-regular"
    :title "Authentication"
    :content "Email-based authentication with a default signin form."}
   {:icon "server-regular"
    :title "Deployment tooling"
    :content "Provision an Ubuntu VPS or deploy with an Uberjar/Docker container."}
   {:icon "terminal"
    :title "REPL made easy"
    :content (str "Changes are evaluated whenever you save a file. "
                  "Even an LLM can figure it out.")}
   {:icon "file-lines-regular"
    :title "Meticulously documented"
    :content "Hand-written and comprehensive."}])

(defn render [opts]
  (base/base-html
   (assoc opts :base/title "Biff | Clojure web framework")
   [:div {:class '[text-white
                   flex
                   flex-col
                   grow]}
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
            :href "https://github.com/jacobobryant/biff/blob/master/README.md"}
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
        "Biff is a full-stack web framework for Clojure. "
        "Launch new projects quickly without getting bogged down in complexity later."]]]]

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
          [:div.text-stone-800 content]])]]]

    [:div.pt-12.pb-16.bg-stone-900.grow.z-10
     [:div.px-4.max-w-screen-md.mx-auto.w-full.text-white
      [:div.text-2xl.font-bold.text-center "It's just libraries"]
      [:div.h-5]

      [:div {:class '["max-w-[34rem]"
                      mx-auto
                      text-lg
                      md:text-xl
                      text-center
                      text-stone-200

                      ]}
       " Biff is designed to be taken apart and modified, so it doesn't get in the way"
       " as your needs evolve."]
      [:div.h-8]
      [:div.flex.justify-center
       [:a.bg-indigo-700.hover:bg-indigo-600.text-white.text-center.py-2.px-4.rounded.md:text-lg
        {:class '[shadow-inner
                  roboto
                  font-medium]
         :href "https://github.com/jacobobryant/biff/blob/master/README.md"}
        "Get started"]]
      [:div.h-8]
      [:div.text-center [:a.underline {:href "/docs-v1"} "Biff 1.x documentation"]]]]]))
