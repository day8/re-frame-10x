(ns day8.re-frame.trace.view.settings
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common]))

(defn render []
  [rc/v-box
   :gap common/gs-19s
   :children
   [[rc/label
     :label "Limits"
     :class "bm-heading-text"]

    [rc/label
     :label "Event Filters"
     :class "bm-heading-text"
     ]

    [rc/label
     :label "View Filters"
     :class "bm-heading-text"]

    [rc/label
     :label "Low Level Trace Filters"
     :class "bm-heading-text"]

    [rc/checkbox :model false :on-change #(rf/dispatch [:settings/low-level-trace :reagent %]) :label "reagent internals"]
    [rc/checkbox :model false :on-change #(rf/dispatch [:settings/low-level-trace :re-frame %]) :label "re-frame internals"]

    [rc/label
     :label "Reset"
     :class "bm-heading-text"]

    [:button {:on-click #(rf/dispatch [:settings/clear-epochs])} "Clear Epochs"]
    [:button {:on-click #(rf/dispatch [:settings/factory-reset])} "Factory Reset"]
    [:p "Will refresh page"]
    ]]
  )
