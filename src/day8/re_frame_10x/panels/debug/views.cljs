(ns day8.re-frame-10x.panels.debug.views
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
    [day8.re-frame-10x.components.re-com                          :as rc]
    [day8.re-frame-10x.tools.metamorphic                          :as metam]
    [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
    [day8.re-frame-10x.panels.subs.subs                           :as subs.subs]))

(defn render []
  [rc/v-box
   :size "1 1 auto"
   :gap "5px"
   :children
   [
    [rc/label :label (str "Number of epochs " (prn-str @(rf/subscribe [::epochs.subs/number-of-matches])))]
    [rc/label :label (str "Beginning trace " (prn-str @(rf/subscribe [::epochs.subs/beginning-trace-id])))]
    [rc/label :label (str "Ending " (prn-str @(rf/subscribe [::epochs.subs/ending-trace-id])))]
    [rc/label :label (str "Current epoch ID " (prn-str @(rf/subscribe [::epochs.subs/selected-epoch-id])))]

    [:h2 "Subscriptions"]
    [cljs-devtools/simple-render @(rf/subscribe [::subs.subs/current-epoch-sub-state]) ["debug-subs"]]
    [:h2 "pre epoch"]
    [cljs-devtools/simple-render @(rf/subscribe [::subs.subs/intra-epoch-subs]) ["pre-epoch-subs"]]
    [:h2 "match state"]
    [cljs-devtools/simple-render @(rf/subscribe [::epochs.subs/selected-match-state]) ["match-state"]]


    [rc/label :label "Epochs"]
    (let [current-match @(rf/subscribe [::epochs.subs/selected-match])]
      (for [match @(rf/subscribe [::epochs.subs/matches])
            :let [match-info (:match-info match)]]
        ^{:key (:id (first match-info))}
        [rc/v-box
         :style {:border      "1px solid black"
                 :font-weight (if (= current-match match-info)
                                "bold"
                                "normal")}
         :children (doall (map (fn [event] [rc/label :label (prn-str event)]) (metam/summarise-match match-info)))]))]])



