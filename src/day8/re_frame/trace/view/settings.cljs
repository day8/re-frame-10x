(ns day8.re-frame.trace.view.settings
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc :refer [css-join]]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
            [day8.re-frame.trace.common-styles :as common]))

(def comp-section-width "400px")
(def instruction--section-width "190px")
(def horizontal-gap common/gs-7s)
(def vertical-gap common/gs-12s)

(def settings-styles
  [:#--re-frame-trace--
   [:.settings
    {:background-color common/white-background-color}]
   ])


(defn closeable-text-box
  [& {:keys [model width on-close on-change]}]
  [rc/h-box
   :children [[rc/input-text
               :width     width
               :style     {:width   width ;; TODO: Not needed in standard re-com but caused by :all unset
                           :height  "25px"
                           :padding (css-join "0px" common/gs-5s)}
               :model     model
               :change-on-blur? false
               :on-change on-change]
              [rc/close-button
               :div-size    25
               :font-size   25
               :top-offset  -4
               :left-offset 10
               :on-click   on-close]]])

(defn explanation-text [children]
  [rc/v-box
   :width    instruction--section-width
   :gap      common/gs-19s
   :children children])

(defn settings-box
  "settings and explanation are both children of re-com boxes"
  [settings explanation min-height]
  [rc/h-box
   :gap        common/gs-19s
   :min-height min-height
   :align      :center
   :children   [[rc/v-box
                 :width      comp-section-width
                 :gap        vertical-gap
                 ;:align-self :center
                 :children   settings]
                [explanation-text explanation]]])

(def txt (r/atom "1"))

(defn render []
  [rc/v-box
   :style    {:margin-left      common/gs-12s ;; A bit of a hack, 19px already provided by parent, add 12 to get to 31 as requires by spec
              :margin-right     common/gs-19s}
   :children [(let [num-epochs @(rf/subscribe [:epochs/number-of-matches])
                    num-traces @(rf/subscribe [:traces/number-of-traces])]
                [settings-box
                 [[rc/h-box
                   :align :center
                   :gap      horizontal-gap
                   :children [[rc/label :label "Retain last"]
                              [rc/input-text
                               :width     common/gs-31s
                               :style     {:width      common/gs-31s ;; TODO: Not needed in standard re-com but caused by :all unset
                                           :height     "25px"
                                           :padding    (css-join "0px" common/gs-5s)}
                               :model     txt
                               :change-on-blur? false
                               :on-change #(reset! txt %)]
                              [rc/label :label "epochs"]
                              [rc/gap-f :size common/gs-31s]
                              [rc/button
                               :class "bm-muted-button app-db-panel-button"
                               :label [rc/v-box
                                       :align :center
                                       :children ["clear all epochs"]]
                               :on-click #(println "Clicked CLEAR")]]]]
                 [[:p num-epochs " epochs currently retained, involving " num-traces " traces."]]
                 common/gs-81s])

              [rc/line]
              [settings-box
               [[rc/h-box
                 :align :center
                 :gap      horizontal-gap
                 :children [[rc/label :label "Ignore epochs for:"]
                            [rc/button
                             :class "bm-muted-button app-db-panel-button"
                             :style {:width  common/gs-81s}
                             :label [rc/v-box
                                     :align :center
                                     :children ["+ event-id"]]
                             :on-click #(println "Add EVENT ID")]]]
                [rc/h-box
                 :align :center
                 :gap      horizontal-gap
                 :children [[closeable-text-box
                             :model     txt
                             :width     "212px"
                             :on-close  #(println "Clicked event-id")
                             :on-change #(reset! txt %)]]]]
               [[:p "All trace associated with these events will be ignored."]
                [:p "Useful if you want to ignore a periodic background polling event."]]
               common/gs-131s]

              [rc/line]
              [settings-box
               [[rc/h-box
                 :align :center
                 :gap      horizontal-gap
                 :children [[rc/label :label "Filter out trace for views in:"]
                            [rc/button
                             :class "bm-muted-button app-db-panel-button"
                             :style {:width  "100px"}
                             :label [rc/v-box
                                     :align :center
                                     :children ["+ namespace"]]
                             :on-click #(println "Clicked NAMESPACE")]]]
                [rc/h-box
                 :align :center
                 :gap      horizontal-gap
                 :children [[closeable-text-box
                             :model     txt
                             :width     "343px"
                             :on-close  #(println "Clicked namespace")
                             :on-change #(reset! txt %)]]]]
               [[:p "Sometimes you want to focus on just your own views, and the trace associated with library views is just noise."]
                [:p "Nominate one or more namespaces."]]
               common/gs-131s]

              [rc/line]
              [settings-box
               [[rc/label :label "Remove low level trace"]
                [rc/checkbox
                 :model     false
                 :label     "reagent internals"
                 :on-change #(rf/dispatch [:settings/low-level-trace :reagent %])]
                [rc/checkbox
                 :model     false
                 :label     "re-frame internals"
                 :on-change #(rf/dispatch [:settings/low-level-trace :re-frame %])]]
               [[:p "Most of the time, low level trace is noisy and you want it filtered out."]]
               common/gs-131s]

              [rc/line]
              [settings-box
               [[rc/button
                 :class "bm-muted-button app-db-panel-button"
                 :style {:width  "100px"}
                 :label [rc/v-box
                         :align :center
                         :children ["Factory Reset"]]
                 :on-click #()]]
               [""]
               common/gs-131s]]])
