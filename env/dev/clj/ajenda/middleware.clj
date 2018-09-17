(ns ajenda.middleware
  (:require [ajenda.middleware.common :refer [wrap-common]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-common)
      wrap-exceptions
      wrap-reload))
