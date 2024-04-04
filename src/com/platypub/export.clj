(ns com.platypub.export
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clj-yaml.core :as yaml]))

(defn format-date-local [date]
  (.format (doto (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss a")
             (.setTimeZone (java.util.TimeZone/getTimeZone "America/Los_Angeles"))) date))

(comment

  (let [all-docs (->> (file-seq (io/file "/home/jacob/dev/platypub/dbdump/"))
                      (filterv #(.isFile %))
                      (mapv (comp edn/read-string slurp)))
        items (filterv :item/user all-docs)
        site-id->root {#uuid "1227c578-6578-45da-9250-8941140e7fe1" "content/"
                       #uuid "a5fc5e04-5d99-49b3-9d93-2cb38d75c846" "../ldsrationalism.com/content/"
                       #uuid "60b8cc19-2ede-4e84-be2f-463b3b9b2404" "../tfos.co/content/"
                       #uuid "267727c4-c4b6-499a-a407-1129a129557d" "../jacobobryant.com/content/"}
        posts (filterv :item.custom.com.platypub.post/slug all-docs)
        pages (filterv :item.custom.com.platypub.page/path all-docs)]
    (io/make-parents "export/_")
    (doseq [{:item.custom.com.platypub.page/keys [path]
             :item.custom.com.platypub.post/keys [title
                                                  draft
                                                  tags
                                                  description
                                                  image
                                                  html]
             :as item} pages
            :let [file-path (str (get site-id->root (first (:item/sites item)) "none")
                                 "/pages/" (str/replace (or (not-empty path) (:xt/id item))
                                                        #"/$"
                                                        "") ".md")]]
      (io/make-parents file-path)
      (spit file-path (str "---\n"
                           (yaml/generate-string
                            (into {}
                                  (remove (fn [[_ v]]
                                            (nil? v)))
                                  {:title (not-empty title)
                                   :path (not-empty path)
                                   :draft (when draft true)
                                   :tags (not-empty tags)
                                   :description (not-empty description)
                                   :image (not-empty image)})
                            {:dumper-options {:flow-style :block}})
                           "---\n"
                           "\n"
                           html)))
    (doseq [{:item.custom.com.platypub.post/keys [slug
                                                  title
                                                  short-title
                                                  draft
                                                  published-at
                                                  tags
                                                  description
                                                  image
                                                  canonical
                                                  comments-url
                                                  html]
             :as item} posts
            :let [file-path (str (get site-id->root (first (:item/sites item)) "none")
                                 "/posts/" (or (not-empty slug) (:xt/id item)) ".md")]]
      (io/make-parents file-path)
      (spit file-path (str "---\n"
                           (yaml/generate-string
                            (into {}
                                  (remove (fn [[_ v]]
                                            (nil? v)))
                                  {:title (not-empty title)
                                   :short-title (not-empty short-title)
                                   :slug (not-empty slug)
                                   :draft (when draft true)
                                   :published (some-> published-at format-date-local)
                                   :tags (not-empty tags)
                                   :description (not-empty description)
                                   :image (not-empty image)
                                   :canonical (not-empty canonical)
                                   :comments-url (not-empty comments-url)})
                            {:dumper-options {:flow-style :block}})
                           "---\n"
                           "\n"
                           html))))

  
  )
