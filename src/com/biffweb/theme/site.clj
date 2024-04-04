(ns com.biffweb.theme.site
  (:require [clojure.java.io :as io]
            [com.platypub.util :as common]
            [com.biffweb.theme.site.templates.docs :as docs]
            [com.biffweb.theme.site.templates.landing :as landing]
            [com.biffweb.theme.site.templates.newsletter :as newsletter]
            [com.biffweb.theme.site.templates.not-found :as not-found]
            [com.biffweb.theme.site.templates.post :as post]
            [com.biffweb.theme.site.templates.page :as page]
            [com.biffweb.theme.site.templates.card :as card]
            [com.biffweb.theme.email :as email]))

(def custom-pages
  {"/" landing/render
   "/newsletter/" newsletter/render
   "/404.html" not-found/render})

(defn render [{:keys [dev] :as config}]
  (let [ctx (merge config (common/read-content config))
        {:keys [docs-href]} (docs/render-all! ctx)
        ctx (assoc ctx :docs-href docs-href)]
    (io/copy (io/file "resources/new-project.clj_") (io/file "public/new.clj"))
    (io/copy (io/file "resources/new-project.clj_") (io/file "public/new-project.clj"))
    (common/render-default! (merge ctx #::common{:custom-pages custom-pages
                                                 :render-post post/render
                                                 :render-page page/render
                                                 :render-card card/render
                                                 :render-email email/render}))))
