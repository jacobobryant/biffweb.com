(ns com.biffweb.theme.email
  (:require [lambdaisland.hiccup :as h]
            [clojure.java.io :as io]))

(defn centered [& body]
  [:table
   {:border "0",
    :cellpadding "0",
    :align "center",
    :cellspacing "0"
    :style {:width "100%"
            :max-width "600px"}}
   [:tr
    [:td
     [:table
      {:width "100%", :cellspacing "", :cellpadding "0", :border "0"}
      [:tr
       [:td body]]]]]])

(defn space [px]
  [:div {:style {:height (str px "px")}}])

(defn render [{:keys [site/url post list/mailing-address] list-title :list/title}]
  [:html
   [:head
    [:title (:title post)]
    [:style (slurp (io/resource "email.css"))]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
    [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]]
   [:body {:style {:background-color "#e7e5e4"}}
    (centered
     [:a {:href url
          :style {:text-decoration "none"}}
      [:div
       {:style {:padding "1rem"}}
       [:img {:src "https://cdn.findka.com/biff/biff-logo-email.png"
              :style {:max-height "30px"
                      :display "block"
                      :margin "0 auto"}
              :alt list-title}]]]
     (space 12)
     [:div {:style {:padding "16px"
                    :background-color "white"}}
      [:div.markdown-body
       [:div [::h/unsafe-html (:html post)]]
       (space 32)
       [:p {:style {:font-size "85%"}}
        mailing-address ". "
        [:a {:href "%mailing_list_unsubscribe_url%"} "Unsubscribe"] "."]]]
     (space 32))]])
