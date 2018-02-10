(ns day8.re-frame.trace.view.timing
  (:require [devtools.prefs]
            [devtools.formatters.core]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common]
            [day8.re-frame.trace.view.components :as components]))

(def timing-styles
  [:#--re-frame-trace--
   [:.timing-details
    {:margin-top common/gs-31s}]
   [:.timing-details--line
    {:margin "1em 0"}]

   [:p :ol
    {:max-width "26em"}]
   [:ol
    {"-webkit-padding-start" "20px"}]
   [:li
    {:margin "0 0 1em 0"}]

   [".rft-tag__timing"
    {:background-color common/disabled-background-color
     :border           (str "1px solid " common/border-line-color)
     :font-weight      "normal"
     :font-size        "14px"}]

   [".timing-elapsed-panel"
    {:padding "12px"
     :margin  common/gs-7s}]
   [".timing-part-panel"
    (merge (common/panel-style "3px")
           {:padding "12px"
            :margin  common/gs-7s})]
   ])

(defn timing-tag [label]
  [components/tag "rft-tag__timing" label])

(defn timing-section
  [label time]
  [rc/v-box
   :align :center
   :gap "3px"
   ;; TODO: detect <1 ms timing here, to distinguish between none at all, and rounding to 0.
   :children [[rc/label :class "bm-textbox-label" :label label]
              [timing-tag (str (js/Math.round time) "ms")]]])

(defn render []
  (let [timing-data-available? @(rf/subscribe [:timing/data-available?])]
    (if timing-data-available?
      [rc/v-box
       :class "timing-details"
       :children [[rc/h-box
                   :class "timing-elapsed-panel"
                   :align :end
                   :children [[timing-section "elapsed" @(rf/subscribe [:timing/total-epoch-time])]
                              [rc/hyperlink-href
                               :label "guide me to greatness"
                               :style {:margin-left common/gs-19s}
                               :attr {:rel "noopener noreferrer"}
                               :target "_blank"
                               :href "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/UnderstandingTiming.md"]]]
                  [rc/h-box
                   :gap common/gs-12s
                   :class "timing-part-panel"
                   :children
                   [[:p "event" [:br] "processing"]
                    [timing-section "event" @(rf/subscribe [:timing/event-processing-time])]
                    ;;; TODO: calculate handler and effects timing separately
                    [timing-section "handler" -1]
                    [timing-section "effects" -1]

                    ]]
                  (doall
                    (for [frame (range 1 (inc @(rf/subscribe [:timing/animation-frame-count])))
                          :let [frame-time @(rf/subscribe [:timing/animation-frame-time frame])]]
                      (list
                        ;^{:key (str "af-line" frame)}
                        ;[rc/line :class "timing-details--line"]
                        ^{:key (str "af" frame)}
                        [rc/h-box
                         :align :center
                         :class "timing-part-panel"
                         :gap common/gs-12s
                         :children
                         [[:p "Animation" [:br] "frame" [:br] (str "#" frame)]
                          [timing-section "total" (:timing/animation-frame-total frame-time)]
                          [:span "="]
                          [timing-section "subs" (:timing/animation-frame-subs frame-time)]
                          [:span "+"]
                          [timing-section "views" (:timing/animation-frame-render frame-time)]
                          [:span "+"]
                          [timing-section "react, etc" (:timing/animation-frame-misc frame-time)]]])))]]
      [rc/v-box
       :class "timing-details"
       :children [[:h1 "No timing data available currently."]]])))
