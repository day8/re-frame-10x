(ns day8.re-frame.trace.view.timing
  (:require [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

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
    {:margin "0 0 1em 0"}]])

(defn render []
  (let [timing-data-available? @(rf/subscribe [:timing/data-available?])]
    (if timing-data-available?
      [rc/v-box
       :class "timing-details"
       :children [
                  [rc/h-box
                   :children
                   [[rc/label :label "Total"]
                    [rc/label :label (str @(rf/subscribe [:timing/total-epoch-time]) "ms")]
                    [rc/label :label "Event"]
                    [rc/label :label (str @(rf/subscribe [:timing/event-processing-time]) "ms")]
                    ]]
                  (for [frame (range 1 (inc @(rf/subscribe [:timing/animation-frame-count])))]
                    (list
                      ^{:key (str "af-line" frame)}
                      [rc/line :class "timing-details--line"]
                      ^{:key (str "af" frame)}
                      [rc/h-box
                       :children
                       [[rc/label :label (str "AF #" frame)]
                        "Subs"
                        "2ms"
                        "Views"
                        "2ms"


                        ]]))
                  ;[rc/label :label "Animation Frames"]
                  ;[rc/label :label "Render/Subscription time"]
                  ;[rc/label :label (str @(rf/subscribe [:timing/render-time]) "ms")]

                  [rc/line :class "timing-details--line"]

                  [rc/v-box
                   :children
                   [[rc/p "Be careful. There are two problems with these numbers:"]
                    [:ol
                     [:li "Accurately timing anything in the browser is a nightmare. One moment a given function takes 1ms and the next it takes 10ms, and you'll never know why. So bouncy."]
                     [:li "You're currently running the dev build, not the production build. So don't freak out too much. Yet."]]]]]]
      [rc/v-box
       :class "timing-details"
       :children [[:h1 "No timing data available currently."]]])))
