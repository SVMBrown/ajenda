(ns ajenda.util)

#?(:cljs

   (defn go-to-date [calendar-id date]
     "Calls fullCalendar gotoDate method expecting date in yyyy-mm-dd form."
     (let [calendar (js/$ calendar-id)]
       (.fullCalendar calendar "gotoDate" date))))