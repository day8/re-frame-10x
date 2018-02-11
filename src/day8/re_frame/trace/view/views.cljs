(ns day8.re-frame.trace.view.views
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.metamorphic :as metam]
            [day8.re-frame.trace.utils.re-com :as rc]))

(defn render
  []
  [:div
   (for [x @(rf/subscribe [:traces/current-event-traces])
         :when (and (some #(or (= "rx88" %)
                               (= "rx193" %))
                          (get-in x [:tags :input-signals]))
                   #_ (metam/render? x))]
     ^{:key (:id x)}
     [:pre {:style {:display "block" :border "1px solid black"}} (prn-str (metam/summarise-event x))])])
