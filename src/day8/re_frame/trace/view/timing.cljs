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
    {:background-color common/white-background-color
     :margin-top       common/gs-31s
     :padding          common/gs-19}]
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

   [".timing-part-panel"
    (merge (common/panel-style "3px")
           {:padding "12px"
            :margin common/gs-7s})]
   ])

(defn timing-tag [label]
  [components/tag "rft-tag__timing" label])

(defn timing-section
  [label time]
  [rc/v-box
   :align :center
   :gap "3px"
   :children [[rc/label :class "bm-textbox-label" :label label]
              [timing-tag (str time "ms")]]])

(defn render []
  (let [timing-data-available? @(rf/subscribe [:timing/data-available?])]
    (if timing-data-available?
      [rc/v-box
       :class "timing-details"
       :children [
                  [rc/h-box
                   :gap common/gs-12s
                   :class "timing-part-panel"
                   :children
                   [[timing-section "total" @(rf/subscribe [:timing/total-epoch-time])]
                    [timing-section "event" @(rf/subscribe [:timing/event-processing-time])]
                    ]]
                  (doall
                    (for [frame (range 1 (inc @(rf/subscribe [:timing/animation-frame-count])))
                          :let [frame-time (rf/subscribe [:timing/animation-frame-time frame])]]
                      (list
                        ;^{:key (str "af-line" frame)}
                        ;[rc/line :class "timing-details--line"]
                        ^{:key (str "af" frame)}
                        [rc/h-box
                         :align :center
                         :class "timing-part-panel"
                         :gap "25px"
                         :children
                         [[rc/label :label (str "Animation frame #" frame)]
                          [timing-section "total" @frame-time]
                          #_[timing-section "subs" 2]
                          #_[timing-section "views" 3]]])))

                  [rc/line :class "timing-details--line"]

                  [rc/v-box
                   :children
                   [[rc/p "Be careful. There are two problems with these numbers:"]
                    [:ol
                     [:li "Accurately timing anything in the browser is a nightmare. One moment a given function takes 1ms and the next it takes 10ms, and you'll never know why. So bouncy."]
                     [:li "You're currently running the dev build, not the production build. So don't freak out too much. Yet."]]
                    [rc/hyperlink-href
                     :label "Timing documentation"
                     :style {:margin-left common/gs-7s}
                     :attr {:rel "noopener noreferrer"}
                     :target "_blank"
                     :href "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/UnderstandingTiming.md"]]]]]
      [rc/v-box
       :class "timing-details"
       :children [[:h1 "No timing data available currently."]]])))
