(ns com.biffweb.theme.site.templates.docs
  (:require [com.biffweb.theme.site.base :as base]
            [com.platypub.util :as common]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [lambdaisland.hiccup :as h]))

(defn join [sep xs]
  (rest (mapcat vector (repeat sep) xs)))

(defn nav-href [doc]
  (if (nil? (:html doc))
    (:href (first (:children doc)))
    (:path doc)))

(defn nav-doc [segment->doc]
  (->> segment->doc
       (sort-by (comp :sort-key val))
       (mapv (fn [[_ doc]]
               (let [doc (update doc :children nav-doc)]
                 (merge {:title (some doc [:short-title :title])
                         :href (nav-href doc)
                         :has-content (boolean (not-empty (:html doc)))}
                        (some->> (not-empty (:children doc))
                                 (hash-map :children))))))))

(defn doc-nav-data [docs]
  (let [nav-data (->> docs
                      (reduce (fn [nav doc]
                                (update-in nav
                                           (join :children (:segments doc))
                                           merge
                                           doc))
                              {})
                      nav-doc)
        nodes (->> (tree-seq :children :children {:children nav-data})
                   (filterv :has-content))
        href->siblings (into {} (map (fn [prev cur next]
                                       [(:href cur) {:next next :prev prev}])
                                     (concat [nil nil] nodes)
                                     (concat [nil] nodes [nil])
                                     (concat nodes [nil nil])))
        nav-data (walk/postwalk
                  (fn [node]
                    (if-let [siblings (and (map? node) (href->siblings (:href node)))]
                      (merge node siblings)
                      node))
                  nav-data)]
    nav-data))

(defn assoc-nav-siblings [docs nav-data]
  (let [nodes (->> (tree-seq :children :children {:children nav-data})
                   (filterv :has-content))
        href->siblings (into {} (map (fn [prev cur next]
                                       [(:href cur) {:next next :prev prev}])
                                     (concat [nil nil] nodes)
                                     (concat [nil] nodes [nil])
                                     (concat nodes [nil nil])))]
    (map #(merge % (href->siblings (:path %))) docs)))

(defn sidebar-left [{:keys [doc-nav-data base/path]}]
  [:div
   [:div.w-fit.sticky.top-0.sm:whitespace-nowrap
    [:div.h-3]
    (for [{:keys [href title children has-content]} doc-nav-data
          :let [children (when (some #(= path (:href %)) children)
                           children)]]
      (list
       [:div.pb-3
        {:class (if (and has-content (= path href))
                  "text-indigo-600 font-bold"
                  "font-semibold")}
        [:a.hover:underline {:href href} title]]
       (for [{:keys [href title]} children]
         [:div.border-l-2.px-3.py-1.text-sm
          {:class (if (= path href)
                    "border-indigo-600 text-indigo-700 font-semibold"
                    "border-stone-50")}
          [:a.hover:underline {:href href} title]])
       (when (not-empty children)
         [:div.h-3])))]])

(def chevron-left
  [:svg.w-3.opacity-50 {:xmlns "http://www.w3.org/2000/svg", :viewbox "0 0 384 512"}
   [:path {:d "M41.4 233.4c-12.5 12.5-12.5 32.8 0 45.3l192 192c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L109.3 256 278.6 86.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0l-192 192z"}]])

(def chevron-right
  [:svg.w-3.opacity-50 {:xmlns "http://www.w3.org/2000/svg", :viewbox "0 0 384 512"}
   [:path {:d "M342.6 233.4c12.5 12.5 12.5 32.8 0 45.3l-192 192c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3L274.7 256 105.4 86.6c-12.5-12.5-12.5-32.8 0-45.3s32.8-12.5 45.3 0l192 192z"}]])

(defn render-api [{:keys [api-sections doc] :as opts}]
  (list
   [:p "All functions are located in the " [:code "com.biffweb"] " namespace."]

   (for [{:keys [arglists doc line name]} (api-sections (:api-section doc))
         :when (not-empty doc)]
     (list
      [:div.flex.items-baseline
       [:h3 {:id name} name]
       [:div.flex-grow]
       [:a.text-sm {:href (str "https://github.com/jacobobryant/biff/blob/master/src/com/biffweb.clj#L"
                               line)}
        "View source"]]
      (when (not-empty arglists)
        [:pre.bg-white {:style {:padding "0"}}
         [:code
          (for [arglist arglists]
            (with-out-str
             (pprint (concat [name] arglist))))]])
      [:pre.text-sm
       [:code.language-plaintext (str/replace doc #"(?s)\n  " "\n")]]))))

(defn render [{:keys [doc recaptcha/site-key] :as opts}]
  (let [width-class "max-w-[900px]"]
    (base/base-html
     (assoc opts
            :base/title (str (:title doc) " | Biff")
            :post doc)
     [:div.grow.flex.flex-col.bg-stone-200
      (base/navbar (assoc opts :class width-class))
      [:div.mx-auto.sm:px-3.py-3.flex-grow.w-full
       {:class width-class}
       [:div.flex.sm:gap-x-4
        [:div.max-sm:hidden (sidebar-left opts)]
        [:div.flex.flex-col.min-w-0
         [:div.min-w-0.w-full.bg-white.p-4.pb-0.mb-4.sm:mb-8
          [:div.markdown-body
           [:h1 (:title doc)]
           (if (:api-section doc)
             (render-api opts)
             [:div [::h/unsafe-html (:html doc)]])
           [:div.h-10]
           [:div.text-center.italic.text-sm
            "Have a question? Join the #biff channel on "
            [:a {:href "http://clojurians.net" :target "_blank"} "Clojurians Slack"]
            ", or "
            [:a {:href "https://github.com/jacobobryant/biff/discussions" :target "_blank"} "ask on GitHub"]
            "."]]
          [:div.h-2]
          [:hr]
          [:div.flex.my-5.items-end
           (when-some [prev (:prev doc)]
             (list chevron-left
                   [:div.w-2]
                   [:div.leading-none
                    [:div.text-sm.text-gray-600 "Prev"]
                    [:div.h-1]
                    [:a.font-bold.text-lg.leading-none.hover:underline
                     {:href (:href prev)}
                     (:title prev)]]))
           [:div.flex-grow]
           (when-some [next (:next doc)]
             (list [:div.leading-none.text-right
                    [:div.text-sm.text-gray-600 "Next"]
                    [:div.h-1]
                    [:a.font-bold.text-lg.leading-none.hover:underline
                     {:href (:href next)}
                     (:title next)]]
                   [:div.w-2]
                   chevron-right))]]
         [:div.sm:hidden.px-4 (sidebar-left opts)]
         [:div.sm:hidden.h-10]
         (base/subscribe-form-mild {:sitekey site-key
                                    :show-disclosure true})]]]])))

(defn embolden [{:keys [html] :as doc}]
  (cond-> doc
    html (update :html str/replace #"%%(.*)%%"
                 (fn [[_ text]]
                   (str "<span class=\"codeblock-highlight\">" text "</span>")))))

(defn render-all! [{:keys [docs] :as ctx}]
  (let [docs (for [{:keys [path] :as doc} docs
                   :let [new-path (-> (str "/" path)
                                      (str/replace #"\d\d-" "")
                                      (str/replace #"\.md$" "/"))]]
               (merge doc {:path new-path
                           :sort-key path
                           :segments (drop 2 (str/split new-path #"/"))}))
        nav-data (doc-nav-data docs)
        docs (->> (assoc-nav-siblings docs nav-data)
                  (mapv embolden))
        api-sections (->> (edn/read-string (slurp "resources/api.edn"))
                          (sort-by :line)
                          (group-by :section))
        ctx (assoc ctx :doc-nav-data nav-data :api-sections api-sections :docs-href (:href (first nav-data)))]
    (doseq [{:keys [path] :as doc} docs
            :when (not-empty (:html doc))]
      (common/render! path (render (assoc ctx :base/path path :doc doc))))
    {:docs-href (:href (first nav-data))}))
