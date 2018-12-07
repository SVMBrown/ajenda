(ns ajenda.test-page
  (:require [ajenda.core :as ajenda]
            [reagent.core :as r]))

(defn make-draggable [event]
  (assoc event
    :z-index 999
    :revert true
    :revert-duration 0))

(defn make-events-draggable [events]
  (reduce-kv
    (fn [events k v]
      (assoc events k (make-draggable v)))
    {} events))

(def events (r/atom (make-events-draggable
                      {:foo
                       {:id    "event-1"
                        :title "Test 1"
                        :start (js/Date.)
                        :tip   "tip1"}
                       :bar
                       {:id    "event-2"
                        :title "Test 2"
                        :start (js/Date.parse "04 Sep 2018 00:12:00 GMT")
                        :tip   "tip1"}})))

(def event-details (r/atom nil))

(defn add-event! [events event]
  (println "adding event" (:id event))
  (swap! events assoc (:id event) (dissoc event :id)))

(defn event-map-to-list [events]
  (mapv (fn [[k v]] (assoc v :_id (name k))) events))

(defn mount-dialog [node]
  (fn [component]
    (when-let [dom-node (r/dom-node component)]
      (reset! node dom-node)
      (.showModal @node))))

(defn popover []
  (let [node (r/atom nil)]
    (r/create-class
      {:component-did-mount
       (mount-dialog node)
       :component-did-update
       (mount-dialog node)
       :render
       (fn []
         (when-let [{:keys [event save]} @event-details]
           [:dialog
            [:h2 "Hello"]
            [:p (str event)]
            [:button
             {:on-click #(do
                           (save (assoc event :start (js/Date.parse "04 Sep 2018 00:12:00 GMT")))
                           (reset! event-details nil)
                           (.close @node))}
             "save"]
            [:button
             {:on-click #(do
                           (reset! event-details nil)
                           (.close @node))}
             "close"]]))})))

(defn home-page [events]
  [:div
   [popover]
   [:button
    {:on-click #(swap! events assoc :baz {:id    "event-3"
                                          :title "Test 3"
                                          :start (js/Date.)
                                          :tip   "tip3"})}
    "add event"]
   [ajenda/calendar
    {:header          {:left   "prev, next today"
                       :center "title"
                       :right  "month,agendaWeek,agendaDay"}
     :editable        true
     :droppable       true
     :selectable      true
     :events          (fn [start end timezone callback]
                        (callback (event-map-to-list @events)))
     :event-render    (fn [event element view]
                        (.attr element "title" (:tip event)))
     :event-click     (fn [id view js-event save-cb]
                        (println "id" id)
                        (let [model-event (get @events id)]
                          (println "model event" model-event)
                          (reset! event-details {:event model-event :save save-cb})))
     #_#_:event-click-sync (fn [id view]
                             (let [model-event (get @events id)]
                               (println model-event)
                               model-event))
     :event-mouseover (fn [& args] #_(println "hello"))
     :event-drop      (fn [& args]
                        (println "dropped" args))
     :select          (fn [start end js-event sview cb]
                        (let [event (make-draggable
                                      {:id    (keyword (str (.getTime (js/Date.))))
                                       :title (str "Test " (rand-int 1000))
                                       :start start
                                       :end   end
                                       :tip   "tip1"})]
                          (add-event! events event)
                          (cb event)))
     :select-sync     (fn [start end view]
                        (let [event {:id    (keyword (str (.getTime (js/Date.))))
                                     :title (str "Test " (rand-int 1000))
                                     :start start
                                     :end   end
                                     :tip   "tip1"}]
                          (add-event! events event)
                          event))}]

   #_[:p (str @events)]])

(defn mount-root []
  (r/render [home-page events] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
