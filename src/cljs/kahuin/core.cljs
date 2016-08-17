(ns kahuin.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [devtools.core :as devtools]
              [kahuin.handlers]
              [kahuin.subs]
              [kahuin.routes :as routes]
              [kahuin.views.core :as views]
              [kahuin.config :as config]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (devtools/install!)))

(defn mount-root []
  (reagent/render [views/header] (.getElementById js/document "header"))
  (reagent/render [#'views/main] (.getElementById js/document "app"))
  (reagent/render [views/footer] (.getElementById js/document "footer")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
