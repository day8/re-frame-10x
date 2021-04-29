(ns day8.re-frame-10x.panels.settings.views
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :as units :refer [px]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.compiler :refer [render-css]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass defglobal]]
    [day8.re-frame-10x.panels.traces.subs :as traces.subs]
    [day8.re-frame-10x.utils.re-com :as rc :refer [css-join]]
    [day8.re-frame-10x.components :as components]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.panels.settings.subs :as settings.subs]
    [day8.re-frame-10x.epochs.events :as epochs.events]
    [day8.re-frame-10x.panels.settings.events :as settings.events]))

(def comp-section-width "400px")
(def instruction--section-width "190px")
(def horizontal-gap styles/gs-7s)
(def vertical-gap styles/gs-12s)
(def settings-box-vertical-padding styles/gs-7)
(def settings-box-padding (css-join "7px" "0"))
(def settings-box-81 (render-css (units/px- styles/gs-81 (units/px* 2 settings-box-vertical-padding))))
(def settings-box-131 (render-css (units/px- styles/gs-131 (units/px* 2 settings-box-vertical-padding))))

(defn done-button
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [components/icon-button
     {:class    (styles/done-button ambiance)
      :icon     [material/check-circle-outline]
      :label    "Done"
      :on-click #(rf/dispatch [::settings.events/toggle])}]))

(defn right-buttons
  [external-window?]
  [rc/h-box
   :gap      styles/gs-12s
   :style    {:margin-right styles/gs-19s}
   :children [[done-button]
              [components/popout-button external-window?]]])

(defclass navigation-style
  [ambiance]
  {:composes (styles/navigation-border-bottom ambiance)}
  [:.rc-label
   {:padding-left styles/gs-19s}])

(defn navigation
  [external-window?]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (navigation-style ambiance)
     :align    :center
     :justify  :between
     :height   styles/gs-31s
     :gap      styles/gs-19s
     :children [[rc/label :label "Settings"]
                [right-buttons external-window?]]]))


(defn closeable-text-box
  [& {:keys [model width on-close on-change]}]
  [rc/h-box
   :children [[rc/input-text
               :width          width
               :style          {:width   width                       ;; TODO: Not needed in standard re-com but caused by :all unset
                                :height  "25px"
                                :padding (css-join "0px" styles/gs-5s)}
               :model           model
               :change-on-blur? false
               :on-change       on-change]
              [rc/close-button
               :div-size    25
               :font-size   25
               :top-offset  -4
               :left-offset 10
               :on-click    on-close]]])

(defn explanation-text [children]
  [rc/v-box
   :width    instruction--section-width
   :gap      styles/gs-19s
   :children children])

(defn settings-box
  "settings and explanation are both children of re-com boxes"
  [settings explanation min-height]
  [rc/h-box
   :gap        styles/gs-19s
   :min-height min-height
   :padding    settings-box-padding
   :align      :center
   :children   [[rc/v-box
                 :width    comp-section-width
                 :gap      vertical-gap
                 :children settings]
                [explanation-text explanation]]])

(defn render []
  [rc/v-box
   :style {:margin-left  styles/gs-12s                      ;; A bit of a hack, 19px already provided by parent, add 12 to get to 31 as requires by spec
           :margin-right styles/gs-19s}
   :children [(let [ambiance         @(rf/subscribe [::settings.subs/ambiance])
                    num-epochs       @(rf/subscribe [::epochs.subs/number-of-matches])
                    num-traces       @(rf/subscribe [::traces.subs/count])
                    epochs-to-retain (rf/subscribe [::settings.subs/number-of-retained-epochs])]

                [settings-box
                 [[rc/h-box
                   :align :center
                   :gap horizontal-gap
                   :children [[rc/label :label "Retain last"]
                              [rc/input-text
                               :width styles/gs-31s
                               :style {:width   "35px"      ;; TODO: Not needed in standard re-com but caused by :all unset
                                       :height  "25px"
                                       :padding (css-join "0px" styles/gs-5s)}
                               :model epochs-to-retain
                               :change-on-blur? true
                               :on-change #(rf/dispatch [:settings/set-number-of-retained-epochs %])]
                              [rc/label :label "epochs"]
                              [rc/gap-f :size styles/gs-31s]
                              [rc/button
                               :class (styles/button ambiance)
                               :label [rc/v-box
                                       :align :center
                                       :children ["clear all epochs"]]
                               :on-click #(rf/dispatch [::epochs.events/reset])]]]]
                 [[:p num-epochs " epochs currently retained, involving " num-traces " traces."]]
                 settings-box-81])

              [rc/line]
              (let [follows-events? @(rf/subscribe [::settings.subs/app-db-follows-events?])]
                [settings-box
                 [[rc/checkbox
                   :model follows-events?
                   :label "sync app-db with epoch navigation"
                   :on-change #(rf/dispatch [:settings/app-db-follows-events? %])]]
                 [[:p "When you navigate to an epoch, update app-db to match. Causes UI to \"time travel\"."]]
                 settings-box-81])

              [rc/line]
              (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
                [settings-box
                 [[rc/h-box
                   :align :center
                   :gap horizontal-gap
                   :children [[rc/label :label "Ignore epochs for:"]
                              [rc/button
                               :class (styles/button ambiance)
                               :style {:width styles/gs-81s}
                               :label [rc/v-box
                                       :align :center
                                       :children ["+ event-id"]]
                               :on-click #(rf/dispatch [:settings/add-ignored-event])]]]
                  [rc/v-box
                   :width comp-section-width
                   :gap vertical-gap
                   :children (for [item @(rf/subscribe [::settings.subs/ignored-events])
                                   :let [id (:id item)]]
                               ^{:key id}
                               [closeable-text-box
                                :model (:event-str item)
                                :width "212px"
                                :on-close #(rf/dispatch [:settings/remove-ignored-event id])
                                :on-change #(rf/dispatch [:settings/update-ignored-event id %])])]]
                 [[:p "All trace associated with these events will be ignored."]
                  [:p "Useful if you want to ignore a periodic background polling event."]]
                 settings-box-131])

              [rc/line]
              (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
                [settings-box
                 [[rc/h-box
                   :align :center
                   :gap horizontal-gap
                   :children [[rc/label :label "Filter out trace for views in:"]
                              [rc/button
                               :class (styles/button ambiance)
                               :style {:width "100px"}
                               :label [rc/v-box
                                       :align :center
                                       :children ["+ namespace"]]
                               :on-click #(rf/dispatch [:settings/add-filtered-view-trace])]]]
                  [rc/v-box
                   :width comp-section-width
                   :gap vertical-gap
                   :children (for [item @(rf/subscribe [::settings.subs/filtered-view-trace])
                                   :let [id (:id item)]]
                               ^{:key id}
                               [closeable-text-box
                                :model (:ns-str item)
                                :width "343px"
                                :on-close #(rf/dispatch [:settings/remove-filtered-view-trace id])
                                :on-change #(rf/dispatch [:settings/update-filtered-view-trace id %])])]]
                 [[:p "Sometimes you want to focus on your own views, and the trace associated with library views is just noise."]
                  [:p "Nominate one or more namespaces."]]
                 settings-box-131])

              [rc/line]
              (let [low-level-trace @(rf/subscribe [::settings.subs/low-level-trace])]
                [settings-box
                 [[rc/label :label "Remove low level trace"]
                  [rc/checkbox
                   :model (:reagent low-level-trace)
                   :label "reagent internals"
                   :on-change #(rf/dispatch [::settings.subs/low-level-trace :reagent %])]
                  [rc/checkbox
                   :model (:re-frame low-level-trace)
                   :label "re-frame internals"
                   :on-change #(rf/dispatch [::settings.subs/low-level-trace :re-frame %])]]
                 [[:p "Most of the time, low level trace is noisy and you want it filtered out."]]
                 settings-box-131])

              [rc/line]
              (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
                [settings-box
                 [[rc/radio-button
                   :model     ambiance
                   :label     "dark"
                   :value     :dark
                   :on-change #(rf/dispatch [::settings.events/set-ambiance %])]
                  [rc/radio-button
                   :model     ambiance
                   :label     "bright"
                   :value     :bright
                   :on-change #(rf/dispatch [::settings.events/set-ambiance %])]]])

              [rc/line]
              (let [syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])]
                [settings-box
                 [[rc/radio-button
                   :model     syntax-color-scheme
                   :label     "cljs-devtools"
                   :value     :cljs-devtools
                   :on-change #(rf/dispatch [::settings.events/set-syntax-color-scheme %])]
                  [rc/radio-button
                   :model     syntax-color-scheme
                   :label     "nord"
                   :value     :nord
                   :on-change #(rf/dispatch [::settings.events/set-syntax-color-scheme %])]]])

              [rc/line]
              (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
                [settings-box
                 [[rc/button
                   :class (styles/button ambiance)
                   :style {:width "100px"}
                   :label [rc/v-box
                           :align :center
                           :children ["Factory Reset"]]
                   :on-click #(rf/dispatch [::settings.events/factory-reset])]]
                 [""]
                 settings-box-81])]])
