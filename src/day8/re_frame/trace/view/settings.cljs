(ns day8.re-frame.trace.view.settings
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common]))

(defn explanation-text [children]
  [rc/v-box
   :width "150px"
   :gap common/gs-19s
   :children children])

(defn settings-box
  "settings and explanation are both children of re-com boxes"
  [settings explanation]
  [rc/h-box
   :justify :between
   :children [[rc/v-box
               :children settings]
              [explanation-text explanation]]])

(defn render []
  [rc/v-box
   :style {:padding-top common/gs-31s}
   :gap common/gs-19s
   :children
   [[settings-box
     [[rc/label :label "Retain last 10 epochs"]
      [:button "Clear All Epochs"]]
     [[:p "8 epochs currently retained, involving 10,425 traces."]]]

    [rc/line]

    [settings-box
     [[rc/label :label "Ignore epochs for:"]
      [:button "+ event-id"]]
     [[:p "All trace associated with these events will be ignored."]
      [:p "Useful if you want to ignore a periodic background polling event."]]]

    [rc/line]

    [settings-box
     [[rc/label :label "Filter out trace for views in "]
      [:button "+ namespace"]]
     [[:p "Sometimes you want to focus on just your own views, and the trace associated with library views is just noise."]
      [:p "Nominate one or more namespaces"]]]

    [rc/line]

    [settings-box
     [[rc/label :label "Remove low level trace"]
      [rc/checkbox :model false :on-change #(rf/dispatch [:settings/low-level-trace :reagent %]) :label "reagent internals"]
      [rc/checkbox :model false :on-change #(rf/dispatch [:settings/low-level-trace :re-frame %]) :label "re-frame internals"]]
     [[:p "Most of the time, low level trace is noisy and you want it filtered out."]]]

    [rc/line]

    [settings-box
     [[:button "Factory Reset"]]
     [[:p "Reset all settings (will refresh browser)."]]]

    ]])
