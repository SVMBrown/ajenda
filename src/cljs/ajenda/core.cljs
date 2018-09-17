(ns ajenda.core
  (:require [reagent.core :as r]))

(defn $ [this]
  (-> this
      r/dom-node
      js/$))

(def events (r/atom (vec (repeatedly
                        10
                        (fn []
                          (let [start (+ (- (* 1000 60 60 24 30) (rand-int (* 1000 60 60 24 60))) (.valueOf (.now js/Date)))
                                end  (+ start (rand-int (* 1000 60 60 12)))
                                id (random-uuid)]
                            {:id id
                             :title (str id)
                             :start (js/moment start)
                             :end (js/moment end)}))))))

(def opts (r/atom {:view "agendaWeek"
                   :date (.valueOf (.now js/Date))}))

(defn events-fn [cal-start cal-end timezone callback]
  (callback (clj->js
             (filterv
              (fn [{:keys [start end]}]
                (or (< (.valueOf cal-start) start (.valueOf cal-end))
                    (< (.valueOf cal-start) end (.valueOf cal-end))))
              @events))))

(def cal-opts
  {:events events-fn
   #_#_  :defaultView "agendaWeek"
   :header {:left "month,agendaWeek,agendaDay"
            :center "title"}
   :height "auto"
   :editable true
   :eventDrop (fn [event delta revert parent-ev ui-obj view]
                (let [event (js->clj event :keywordize-keys true)]
                  (swap! events #(mapv (fn [{:keys [id] :as e}] (if (= id (:id event)) event e)) %))))
   :eventResize (fn [event delta revert parent-ev ui-obj view]
                  (let [event (js->clj event :keywordize-keys true)]
                    (swap! events #(mapv (fn [{:keys [id] :as e}] (if (= id (:id event)) event e)) %))))
   :dayClick (fn [date parent-ev view]
               (println "Clicked: " (.format date)))})

(defn format-cal-opts [opts]
  (-> opts
      (or {})
      (clj->js)))

(defn parse-cal-opts [this opts]
  (if (true? (:skip-parse opts))
    (format-cal-opts opts)
    (-> opts
        (update :dayClick (fn [orig-fn]
                            (fn [d p v]
                              (orig-fn d p v))))
        (format-cal-opts))))

;; -------------------------
;; Components

(defn get-track
  [track-fn & dereffables]
  (apply r/track
         (comp #(apply track-fn %)
               #(mapv deref %&))
         dereffables))

(defn get-track!
  [track-fn & dereffables]
  (apply r/track!
         (comp #(apply track-fn %)
               #(mapv deref %&))
         dereffables))

(defn register-track!
  [registry-atom track-fn & dereffables]
  (let [tracker (apply get-track! track-fn dereffables)]
    (swap! registry-atom (fnil conj #{}) tracker)
    tracker))

(defn dispose-all!
  [registry-atom]
  (doseq [tracker @registry-atom]
    (r/dispose! tracker))
  (reset! registry-atom #{}))

(defn calendar [events opts {:keys [set-view set-date]}]
  (let [view (get-track :view opts)
        track-registry (atom #{})
        track! (partial register-track! track-registry)]
      (r/create-class
       {:display-name "calendar"
        :component-did-mount (fn [this]
                               (.fullCalendar
                                ($ this)
                                {:viewRender (fn [new-view element]
                                               (set-view (aget new-view "name"))
                                               (when-let [new-date (.fullCalendar ($ this) "getDate")]
                                                 (set-date new-date)))}))
        :component-did-update (fn [this _]
                                (println "Calendar did update")
                                (-> this
                                    $
                                    (.fullCalendar "render")))
        :component-will-unmount (fn [this]
                                  (dispose-all! track-registry)
                                  (-> this
                                      $
                                      (.fullCalendar "destroy")))
        :reagent-render
        (fn [_]
          [:div.calendar])})))
