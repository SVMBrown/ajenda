(ns ajenda.assets
  (:require
   [hiccup.page :refer [include-js include-css html5]]))

(defn ajenda-css []
  (include-css "/assets/fullcalendar/fullcalendar.min.css"))

(defn ajenda-js []
  [:span
   (include-js "/assets/jquery/jquery.min.js")
   (include-js "/assets/moment/moment.min.js")
   (include-js "/assets/fullcalendar/fullcalendar.min.js")])
