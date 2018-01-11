(ns day8.re-frame.trace.view.timing
  (:require [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(defn render []

  [rc/v-box
   :padding "12px 0px"
   :children [[rc/label :label "Total Epoch Time"]
              [rc/label :label (str @(rf/subscribe [:timing/total-epoch-time]) "ms")]
              [rc/label :label "Animation Frames"]
              [rc/label :label @(rf/subscribe [:timing/animation-frame-count])]
              [rc/label :label "Event time"]
              [rc/label :label (str @(rf/subscribe [:timing/event-processing-time]) "ms")]
              [rc/label :label "Render/Subscription time"]
              [rc/label :label (str @(rf/subscribe [:timing/render-time]) "ms")]]])
