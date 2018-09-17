(ns ajenda.middleware.common
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.webjars :refer [wrap-webjars]]))

(defn wrap-common [handler]
  (-> handler
      (wrap-webjars)
      (wrap-defaults site-defaults)))
