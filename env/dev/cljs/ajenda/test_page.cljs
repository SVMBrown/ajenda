(ns ajenda.test-page
  (:require [ajenda.core :as ajenda]
            [reagent.core :as r]))

(def events (r/atom [{:title "Test 1"
                      :start (js/Date.)
                      :tip   "tip1"}]))

(defn add-event! [events event]
  (swap! events conj event))

(defn home-page []
  [:div
   [:p (str @events)]
   [ajenda/calendar
    {:header       {:left   "prev, next today"
                    :center "title"
                    :right  "month,agendaWeek,agendaDay"}
     :selectable   true
     :events       (fn [start end timezone callback]
                     (callback @events))
     :event-render (fn [event element]
                     (.attr element "title" (.-tip event)))
     :event-click  (fn [event js-event view]
                     (js/console.log event)
                     event
                     nil)
     :select       (fn [start end event view]
                     (let [event {:title (str "Test " (rand-int 1000))
                                  :start start
                                  :end   end
                                  :tip   "tip1"}]
                       (add-event! events event)
                       event))}]])

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
