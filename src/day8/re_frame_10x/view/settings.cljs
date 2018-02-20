(ns day8.re-frame-10x.view.settings
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.utils.re-com :as rc :refer [css-join]]
            [day8.re-frame-10x.common-styles :as common]
            [mranderson047.garden.v1v3v3.garden.units :as units]
            [mranderson047.garden.v1v3v3.garden.compiler :refer [render-css]]))

(def comp-section-width "400px")
(def instruction--section-width "190px")
(def horizontal-gap common/gs-7s)
(def vertical-gap common/gs-12s)
(def settings-box-vertical-padding common/gs-7)
(def settings-box-padding (css-join "7px" "0"))
(def settings-box-81 (render-css (units/px- common/gs-81 (units/px* 2 settings-box-vertical-padding))))
(def settings-box-131 (render-css (units/px- common/gs-131 (units/px* 2 settings-box-vertical-padding))))

(def settings-styles
  [:#--re-frame-10x--
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
   :padding    settings-box-padding
   :align      :center
   :children   [[rc/v-box
                 :width      comp-section-width
                 :gap        vertical-gap
                 ;:align-self :center
                 :children   settings]
                [explanation-text explanation]]])

(defn render []
  [rc/v-box
   :style    {:margin-left      common/gs-12s ;; A bit of a hack, 19px already provided by parent, add 12 to get to 31 as requires by spec
              :margin-right     common/gs-19s}
   :children [(let [num-epochs @(rf/subscribe [:epochs/number-of-matches])
                    num-traces @(rf/subscribe [:traces/number-of-traces])
                    epochs-to-retain (rf/subscribe [:settings/number-of-retained-epochs])]

                [settings-box
                 [[rc/h-box
                   :align    :center
                   :gap      horizontal-gap
                   :children [[rc/label :label "Retain last"]
                              [rc/input-text
                               :width     common/gs-31s
                               :style     {:width      "35px" ;; TODO: Not needed in standard re-com but caused by :all unset
                                           :height     "25px"
                                           :padding    (css-join "0px" common/gs-5s)}
                               :model     epochs-to-retain
                               :change-on-blur? true
                               :on-change #(rf/dispatch [:settings/set-number-of-retained-epochs %])]
                              [rc/label :label "epochs"]
                              [rc/gap-f :size common/gs-31s]
                              [rc/button
                               :class "bm-muted-button app-db-panel-button"
                               :label [rc/v-box
                                       :align :center
                                       :children ["clear all epochs"]]
                               :on-click #(rf/dispatch [:epochs/reset])]]]]
                 [[:p num-epochs " epochs currently retained, involving " num-traces " traces."]]
                 settings-box-81])

              [rc/line]
              (let [follows-events? @(rf/subscribe [:settings/app-db-follows-events?])]
                [settings-box
                 [[rc/checkbox
                   :model follows-events?
                   :label "sync app-db with epoch navigation"
                   :on-change #(rf/dispatch [:settings/app-db-follows-events? %])]]
                 [[:p "When you navigate to an epoch, update app-db to match. Causes UI to \"time travel\"."]]
                 settings-box-81])

              [rc/line]
              [settings-box
               [[rc/h-box
                 :align    :center
                 :gap      horizontal-gap
                 :children [[rc/label :label "Ignore epochs for:"]
                            [rc/button
                             :class "bm-muted-button app-db-panel-button"
                             :style {:width  common/gs-81s}
                             :label [rc/v-box
                                     :align :center
                                     :children ["+ event-id"]]
                             :on-click #(rf/dispatch [:settings/add-ignored-event])]]]
                [rc/v-box
                 :width      comp-section-width
                 :gap        vertical-gap
                 :children   (for [item @(rf/subscribe [:settings/ignored-events])
                                   :let [id (:id item)]]
                               ^{:key id}
                               [closeable-text-box
                                :model     (:event-str item)
                                :width     "212px"
                                :on-close  #(rf/dispatch [:settings/remove-ignored-event id])
                                :on-change #(rf/dispatch [:settings/update-ignored-event id %])])]]
               [[:p "All trace associated with these events will be ignored."]
                [:p "Useful if you want to ignore a periodic background polling event."]]
               settings-box-131]

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
                             :on-click #(rf/dispatch [:settings/add-filtered-view-trace])]]]
                [rc/v-box
                 :width      comp-section-width
                 :gap        vertical-gap
                 :children   (for [item @(rf/subscribe [:settings/filtered-view-trace])
                                   :let [id (:id item)]]
                               ^{:key id}
                               [closeable-text-box
                                :model     (:ns-str item)
                                :width     "343px"
                                :on-close  #(rf/dispatch [:settings/remove-filtered-view-trace id])
                                :on-change #(rf/dispatch [:settings/update-filtered-view-trace id %])])]]
               [[:p "Sometimes you want to focus on your own views, and the trace associated with library views is just noise."]
                [:p "Nominate one or more namespaces."]]
               settings-box-131]

              [rc/line]
              (let [low-level-trace @(rf/subscribe [:settings/low-level-trace])]
                [settings-box
                [[rc/label :label "Remove low level trace"]
                 [rc/checkbox
                  :model (:reagent low-level-trace)
                  :label "reagent internals"
                  :on-change #(rf/dispatch [:settings/low-level-trace :reagent %])]
                 [rc/checkbox
                  :model (:re-frame low-level-trace)
                  :label "re-frame internals"
                  :on-change #(rf/dispatch [:settings/low-level-trace :re-frame %])]]
                [[:p "Most of the time, low level trace is noisy and you want it filtered out."]]
                settings-box-131])

              [rc/line]
              [settings-box
               [[rc/button
                 :class "bm-muted-button app-db-panel-button"
                 :style {:width  "100px"}
                 :label [rc/v-box
                         :align :center
                         :children ["Factory Reset"]]
                 :on-click #(rf/dispatch [:settings/factory-reset])]]
               [""]
               settings-box-81]]])
