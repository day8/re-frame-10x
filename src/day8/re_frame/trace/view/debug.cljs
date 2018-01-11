(ns day8.re-frame.trace.view.debug
  (:require [day8.re-frame.trace.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.metamorphic :as metam]))

(defn render []
  [rc/v-box
   :gap "5px"
   :children
   [
    [rc/label :label (str "Number of epochs " (prn-str @(rf/subscribe [:epochs/number-of-matches])))]
    [rc/label :label (str "Beginning trace " (prn-str @(rf/subscribe [:epochs/beginning-trace-id])))]
    [rc/label :label (str "Ending " (prn-str @(rf/subscribe [:epochs/ending-trace-id])))]

    [rc/label :label "Epochs"]
    (for [match (:matches @(rf/subscribe [:epochs/epoch-root]))]
      ^{:key (:id (first match))}
      [rc/v-box
       :style {:border "1px solid black"}
       :children (doall (map (fn [event] [rc/label :label (prn-str event)]) (metam/summarise-match match)))
       ])
    ]]
  )
