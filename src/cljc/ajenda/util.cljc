(ns ajenda.util)

#?(:cljs

   (defn go-to-date
     "Calls fullCalendar gotoDate method expecting date in yyyy-mm-dd form."
     [calendar-id date]
     (let [calendar (js/$ (str "#" calendar-id))]
       (.fullCalendar calendar "gotoDate" date))))