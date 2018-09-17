(ns ^:figwheel-no-load ajenda.dev
  (:require
    [ajenda.test-page :as test-page]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(test-page/init!)
