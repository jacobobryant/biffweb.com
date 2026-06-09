(ns com.biffweb.tasks
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [com.biffweb.theme.site.templates.card :as card]
            [com.platypub.cli :as platypub-cli]
            [com.platypub.util :as util]
            [lambdaisland.hiccup :as h]))

(def card-width 1202)
(def card-height 620)

(defn- run-command! [& args]
  (let [{:keys [exit out err]} (apply sh/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str "Command failed: " (str/join " " args))
                      {:args args
                       :exit exit
                       :out out
                       :err err})))
    {:out out
     :err err}))

(defn- build-css! []
  (run-command! "npx" "tailwindcss"
                "-c" "resources/tailwind.config.js"
                "-i" "resources/tailwind.css"
                "-o" "public/css/main.css"
                "--minify"))

(defn- browser-spec []
  (or (some (fn [name]
              (when-let [path (fs/which name)]
                {:engine :firefox
                 :path (str path)}))
            ["firefox"])
      (some (fn [name]
              (when-let [path (fs/which name)]
                {:engine :chromium
                 :path (str path)}))
            ["google-chrome" "google-chrome-stable" "chromium" "chromium-browser"])
      (throw (ex-info "Couldn't find Firefox or Chromium on PATH." {}))))

(defn- card-html [post css]
  (h/render
   [:html {:lang "en-US"}
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:href "https://fonts.googleapis.com" :rel "preconnect"}]
     [:link {:crossorigin "crossorigin"
             :href "https://fonts.gstatic.com"
             :rel "preconnect"}]
     [:link {:href (apply str "https://fonts.googleapis.com/css2?display=swap"
                          (for [f ["Roboto+Slab:wght@700"
                                   "Roboto:wght@500"]]
                            (str "&family=" f)))
             :rel "stylesheet"}]
     [:style (str "html,body{margin:0;padding:0;width:" card-width "px;height:" card-height "px;overflow:hidden;background:#1c1917;}"
                  "#card{margin:0 !important;}"
                  css)]]
    [:body
     (card/card-body {:post post})]]))

(defn- output-path [post-path]
  (let [file (io/file "resources/public/cards"
                      (str (fs/file-name (fs/strip-ext post-path)) ".png"))]
    {:display (str file)
     :absolute (.getAbsolutePath file)}))

(defn- read-post [post-path]
  (let [file (io/file post-path)]
    (when-not (.isFile file)
      (throw (ex-info (str "Post not found: " post-path) {:path post-path})))
    (util/read-md-file (util/read-config) file)))

(defn- temp-card-file []
  (.getAbsolutePath (java.io.File/createTempFile "biff-card-" ".html")))

(defn- screenshot-command [{:keys [engine path]} html-path output]
  (case engine
    :firefox [path
              "--headless"
              "--window-size" (str card-width "," card-height)
              "--screenshot" output
              (str "file://" html-path)]
    :chromium [path
               "--headless"
               "--disable-gpu"
               (str "--window-size=" card-width "," card-height)
               (str "--screenshot=" output)
               (str "file://" html-path)]))

(defn card
  "Generates a social card image for the given post.

   clj -M:run card [post path]

   Example:

     clj -M:run card content/posts/2026/core.md"
  [post-path]
  (build-css!)
  (let [post (read-post post-path)
        {:keys [display absolute]} (output-path post-path)
        css (slurp "public/css/main.css")
        html (card-html post css)
        browser-config (browser-spec)
        html-path (temp-card-file)]
    (fs/create-dirs (fs/parent absolute))
    (spit html-path html)
    (try
      (apply run-command! (screenshot-command browser-config html-path absolute))
      (when-not (fs/exists? absolute)
        (throw (ex-info (str "Screenshot command succeeded but didn't create " display)
                        {:output absolute})))
      (println display)
      (finally
        (fs/delete-if-exists html-path)))))

(def tasks
  (assoc platypub-cli/tasks
         "card" #'card))
