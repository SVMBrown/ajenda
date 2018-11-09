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

(defn calendar-node [view]
  (-> view .-calendar .-el))

(defn ->camel-case [s]
  (string/replace s #"-{1,}\b." #(when-let [c (last %)] (.toUpperCase c))))

(defn events-handler [calendar f sync?]
  (if sync?
    (fn [start end timezone callback]
      (callback (clj->js (f start end timezone))))
    (fn [start end timezone callback]
      (f start end timezone #(callback (clj->js %))))))

(defn parse-event [calendar-event]
  (some-> (js->clj-keywordized calendar-event)
          (rename-keys {:_id :id})
          (update :id keyword)))

(defn unparse-event [model-event]
  (some-> (rename-keys model-event {:id :_id})
          (clj->js)))

(defn save-event [calendar id model-event event]
  (if event
    (do
      (doseq [[k v] (select-keys model-event [:start :end :tip :title :description])]
        (goog.object/set event (name k) (clj->js v)))
      (.fullCalendar calendar "updateEvent" event))
    (.fullCalendar calendar "removeEvents" id)))

(defn wrap-event-click
  "removes the event from calendar when the click handler returns nil"
  [calendar f sync?]
  (fn [event js-event view]
    (let [id (.-_id event)]
      (if sync?
        (save-event calendar id (f (keyword id) js-event view) event)
        (f (keyword id) view (fn [model-event] (save-event calendar id model-event event)))))))

(defn wrap-event-render
  "calls the select function and paints the event with the result"
  [calendar f sync?]
  (fn [event element view]
    (f (parse-event event) element view)))

(defn wrap-mouseover [calendar f sync?]
  (fn [event js-event view]
    (f (parse-event event) js-event view)))

(defn wrap-select [calendar f sync?]
  (if sync?
    (fn [start end js-event view]
      (when-let [event (f start end js-event view)]
        (.fullCalendar calendar "renderEvent" (unparse-event event))))
    (fn [start end js-event view]
      (f start end js-event view
         (fn [event]
           (when event
             (.fullCalendar calendar "renderEvent" (unparse-event event))))))))

(defn wrap-event-drop [calendar f sync?]
  (if sync?
    (fn [event delta revert-fn js-event ui view]
      (when-not (f (parse-event event) delta js-event ui view)
        (revert-fn)))
    (fn [event delta revert-fn js-event ui view]
      (f (parse-event event) delta js-event ui view revert-fn))))

(defn wrap-event-default [calendar f sync?]
  (fn [event js-event ui view]
    (f (parse-event event) js-event ui view)))

(defn camel-case-event-keys [opts]
  (postwalk
    (fn [node]
      (if (keyword? node)
        (keyword (->camel-case (name node)))
        node))
    opts))

(def event-wrappers
  {"event-drop"         wrap-event-drop
   "event-drag-start"   wrap-event-default
   "event-drag-stop"    wrap-event-default
   "event-resize-start" wrap-event-default
   "event-resize-stop"  wrap-event-default
   "event-resize"       wrap-event-default
   "event-click"        wrap-event-click
   "event-mouseover"    wrap-mouseover
   "event-render"       wrap-event-render
   "select"             wrap-select
   "events"             events-handler})

(defn wrap-event [calendar [k handler]]
  (let [key-name (name k)
        event-id (string/replace key-name #"-sync$" "")
        sync?    (string/ends-with? key-name "-sync")
        wrapper  (get event-wrappers event-id)]
    (if wrapper
      {(keyword event-id) (wrapper calendar handler sync?)}
      {k handler})))

(defn parse-opts [calendar opts]
  (-> (reduce
        (fn [wrapped-opts event]
          (merge wrapped-opts (wrap-event calendar event)))
        {}
        opts)
      (camel-case-event-keys)
      (clj->js)))

(defn calendar [opts]
  (r/create-class
    {:display-name           "calendar"
     :component-did-mount    (fn [this]
                               (let [calendar ($ this)]
                                 (.fullCalendar calendar (parse-opts calendar opts))))
     :component-did-update   (fn [this _]
                               (-> this $ (.fullCalendar "render")))
     :component-will-unmount (fn [this]
                               (-> this $ (.fullCalendar "destroy")))
     :reagent-render         (fn [_] [:div.calendar])}))
