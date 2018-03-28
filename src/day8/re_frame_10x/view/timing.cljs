(ns day8.re-frame-10x.view.timing
  (:require [devtools.prefs]
            [devtools.formatters.core]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.utils.re-com :as rc :refer [css-join]]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.view.components :as components]))

(def timing-styles
  [:#--re-frame-10x--
   [:.timing-details
    {:margin-top   common/gs-31s
     :margin-right common/gs-19s}]
   [:.timing-details--line
    {:margin "1em 0"}]

   [:p :ol
    {:max-width "26em"}]
   [:ol
    {"-webkit-padding-start" "20px"}]
   [:li
    {:margin "0 0 1em 0"}]

   [".rft-tag__timing"
    {:background-color "#ECEDF0"
     :border           (str "1px solid " common/border-line-color)
     :font-weight      "normal"
     :font-size        "14px"
     :width            "63px"}]

   [".timing-elapsed-panel"
    {:padding "12px 12px 12px 0px"
     :margin  (css-join common/gs-7s "0px")}]
   [".timing-part-panel"
    (merge (common/panel-style "3px")
           {:padding  common/gs-12s
            :margin   (css-join common/gs-7s "0px")
            :overflow "hidden"})]
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
              [timing-tag (cond
                            (nil? time) "-"
                            (= time 0) (str "0ms")
                            (< time 0.1) (str "<0.1ms")
                            (< time 1) (str (.toFixed time 1) "ms")
                            (some? time) (str (js/Math.round time) "ms"))]]])

(defn render []
  (let [timing-data-available? @(rf/subscribe [:timing/data-available?])
        event-processing-time  @(rf/subscribe [:timing/event-processing-time])]
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
                               :href "https://github.com/Day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnderstandingTiming.md"]]]
                  [rc/h-box
                   :gap common/gs-12s
                   :class "timing-part-panel"
                   :align :center
                   :children [[rc/v-box
                               :align :center
                               :width common/gs-81s
                               :children [[:span "event"] [:span "processing"]]]
                              [timing-section "total" (:timing/event-total event-processing-time)]
                              [:span "="]
                              [timing-section "handler" (:timing/event-handler event-processing-time)]
                              [:span "+"]
                              [timing-section "effects" (:timing/event-effects event-processing-time)]
                              #_[:span "+"]
                              #_[timing-section "other int." (:timing/event-interceptors event-processing-time)]
                              [:span "+"]
                              [timing-section "misc" (:timing/event-misc event-processing-time)]]]
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
                         [[rc/v-box
                           :align :center
                           :width common/gs-81s
                           :children [[:span "animation"] [:span "frame"] [:span "#" frame]]]
                          [timing-section "total" (:timing/animation-frame-total frame-time)]
                          [:span "="]
                          [timing-section "subs" (:timing/animation-frame-subs frame-time)]
                          [:span "+"]
                          [timing-section "views" (:timing/animation-frame-render frame-time)]
                          [:span "+"]
                          [timing-section "react, etc" (:timing/animation-frame-misc frame-time)]]])))]]
      [rc/v-box
       :class "timing-details"
       :children [[:h1 "No timing data currently available."]]])))
