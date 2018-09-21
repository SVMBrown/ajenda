(ns ajenda.core
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :as string]
    [clojure.walk :refer [postwalk]]
    [reagent.core :as r]
    [reagent.dom.server :refer [render-to-static-markup]]))

(defn $ [this]
  (-> this r/dom-node js/$))

(defn js->clj-keywordized [v]
  (js->clj v :keywordize-keys true))

(defn ->camel-case [s]
  (string/replace s #"-{1,}\b." #(when-let [c (last %)] (.toUpperCase c))))

(defn events-handler [f]
  (fn [start end timezone callback]
    (f start end timezone #(callback (clj->js %)))))

(defn parse-event [calendar-event]
  (some-> (js->clj-keywordized calendar-event)
          (rename-keys {:_id :id})
          (update :id keyword)))

(defn unparse-event [model-event]
  (some-> (rename-keys model-event {:id :_id})
          (clj->js)))

(defn save-event [calendar id event]
  (if event
    (do
      (doseq [[k v] (select-keys event [:start :end :tip :title])]
        (goog.object/set event (name k) (clj->js v)))
      (.fullCalendar calendar "updateEvent" event))
    (.fullCalendar calendar "removeEvents" id)))

(defn wrap-event-click
  "removes the event from calendar when the click handler returns nil"
  [f calendar]
  (when f
    (fn [event js-event view]
      (let [id (.-_id event)]
        (save-event calendar id (f id view))))))

(defn wrap-event-click-async
  "removes the event from calendar when the click handler returns nil"
  [f calendar]
  (when f
    (fn [event js-event view]
      (let [id (.-_id event)]
        (f id view (fn [event] (save-event calendar id event)))))))

(defn wrap-event-render
  "calls the select function and paints the event with the result"
  [f]
  (fn [event element view]
    (f (parse-event event) element view)))

(defn wrap-mouseover [f]
  (fn [event js-event view]
    (f (parse-event event) view)))

(defn wrap-select [f calendar]
  (fn [start end js-event view]
    (when-let [event (f start end view)]
      (println "new event:" event)
      (js/console.log (unparse-event event))
      (.fullCalendar calendar "renderEvent" (unparse-event event)))))

(defn camel-case-event-keys [opts]
  (postwalk
    (fn [node]
      (if (keyword? node)
        (keyword (->camel-case (name node)))
        node))
    opts))

;todo handle having either eventClick or eventClickSync
(defn parse-opts [calendar {:keys [event-click event-click-async] :as opts}]
  (-> (camel-case-event-keys opts)
      (assoc :eventClick
             (if event-click
               (wrap-event-click event-click calendar)
               (wrap-event-click-async event-click-async calendar)))
      (dissoc :eventClickSync)
      (update :eventMouseover wrap-mouseover)
      (update :eventRender wrap-event-render)
      (update :select wrap-select calendar)
      (update :events events-handler)
      (clj->js)))

(defn calendar [opts]
  (let []
    (r/create-class
      {:display-name           "calendar"
       :component-did-mount    (fn [this]
                                 (let [calendar ($ this)]
                                   (.fullCalendar calendar (parse-opts calendar opts))))
       :component-did-update   (fn [this _]
                                 (-> this $ (.fullCalendar "render")))
       :component-will-unmount (fn [this]
                                 (-> this $ (.fullCalendar "destroy")))
       :reagent-render         (fn [_] [:div.calendar])})))
