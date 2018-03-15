(ns day8.re-frame-10x.view.debug
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.view.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.metamorphic :as metam]))

(defn render []
  [rc/v-box
   :size "1 1 auto"
   :gap "5px"
   :children
   [
    [rc/label :label (str "Number of epochs " (prn-str @(rf/subscribe [:epochs/number-of-matches])))]
    [rc/label :label (str "Beginning trace " (prn-str @(rf/subscribe [:epochs/beginning-trace-id])))]
    [rc/label :label (str "Ending " (prn-str @(rf/subscribe [:epochs/ending-trace-id])))]
    [rc/label :label (str "Current epoch ID " (prn-str @(rf/subscribe [:epochs/current-epoch-id])))]

    [:h2 "Subscriptions"]
    [components/simple-render @(rf/subscribe [:subs/current-epoch-sub-state]) ["debug-subs"]]
    [:h2 "pre epoch"]
    [components/simple-render @(rf/subscribe [:subs/intra-epoch-subs]) ["pre-epoch-subs"]]
    [:h2 "match state"]
    [components/simple-render @(rf/subscribe [:epochs/current-match-state]) ["match-state"]]


    [rc/label :label "Epochs"]
    (let [current-match @(rf/subscribe [:epochs/current-match])]
      (for [match (:matches @(rf/subscribe [:epochs/epoch-root]))
            :let [match-info (:match-info match)]]
        ^{:key (:id (first match-info))}
        [rc/v-box
         :style {:border "1px solid black"
                 :font-weight (if (= current-match match-info)
                                "bold"
                                "normal")}
         :children (doall (map (fn [event] [rc/label :label (prn-str event)]) (metam/summarise-match match-info)))
         ]))
    ]]
  )
