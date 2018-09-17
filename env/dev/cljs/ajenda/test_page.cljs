(ns ajenda.test-page
  (:require [ajenda.core :as ajenda]
            [reagent.core :as r]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to Ajenda Test Page"]
   [ajenda/calendar ajenda/events  #_{:events events-fn}]])

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
