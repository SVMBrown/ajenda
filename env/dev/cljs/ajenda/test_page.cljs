(ns ajenda.test-page
  (:require [ajenda.core :as ajenda]
            [reagent.core :as r]))

(def events (r/atom {:foo
                     {:title    "Test 1"
                      :start    (js/Date.)
                      :tip      "tip1"}
                     :bar
                     {:title    "Test 2"
                      :start    (js/Date.parse "04 Sep 2018 00:12:00 GMT")
                      :tip      "tip1"}}))

(defn add-event! [events event]
  (swap! events assoc (:id event) event))

(defn event-map-to-list [events]
  (mapv (fn [[k v]] (assoc v :id (name k))) events))

(defn home-page []
  (r/with-let [event-popover? (r/atom false)]
    [:div
     [ajenda/calendar
      {:header          {:left   "prev, next today"
                         :center "title"
                         :right  "month,agendaWeek,agendaDay"}
       :selectable      true
       :events          (fn [start end timezone callback]
                          (callback (event-map-to-list @events)))
       :event-render    (fn [event element]
                          (println event)
                          (js/console.log element)
                          (.attr element "title" (:tip event)))
       :event-click     (fn [event view]
                          (let [model-event (get @events (:id event))]
                            model-event)
                          #_(assoc event :start (js/Date.parse "04 Sep 2018 00:12:00 GMT")))
       :event-mouseover (fn [& args] #_(println "hello"))
       :select          (fn [start end event view]
                          (let [event {:title    (str "Test " (rand-int 1000))
                                       :start    start
                                       :end      end
                                       :tip      "tip1"}]
                            (add-event! events event)
                            event))}]

     #_[:p (str @events)]]))

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
