(ns ajenda.middleware
  (:require [ajenda.middleware.common :refer [wrap-common]]))

(defn wrap-middleware [handler]
  (wrap-common handler))
