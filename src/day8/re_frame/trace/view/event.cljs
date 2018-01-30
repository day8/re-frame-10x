(ns day8.re-frame.trace.view.event
  (:require [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.view.components :as components]
            [day8.re-frame.trace.common-styles :as common]
            [mranderson047.garden.v1v3v3.garden.units :as units]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf])
  (:require-macros [day8.re-frame.trace.utils.macros :refer [with-cljs-devtools-prefs]]))

(def pod-border-color "#daddde")
(def pod-border-edge (str "1px solid " pod-border-color))
(def border-radius "3px")

(def event-styles
  [:#--re-frame-trace--
   [:.event-panel
    {:padding "39px 19px 0px 0px"}]
   [:.event-section]
   [:.event-section--header
    {:background-color common/navbar-tint-lighter
     :color            common/navbar-text-color
     :height           common/gs-19
     :font-size        "14px"
     :padding          [[0 common/gs-12]]
     }]
   [:.event-section--data
    {:background-color "rgba(100, 255, 100, 0.08)"
     :padding-left     (units/px- common/gs-12 common/expansion-button-horizontal-padding)
     :overflow-x "auto"}]
   ])

(defn event-section [title data]
  [rc/v-box
   :class "event-section"
   :children
   [[rc/h-box
     :class "event-section--header app-db-path--header"
     :align :center
     :children [[:h2 title]]]

    [components/simple-render data [title] "event-section--data app-db-path--pod-border"]]])

(defn render []
  (let [event-trace @(rf/subscribe [:epochs/current-event-trace])]
    [rc/v-box
     :class "event-panel"
     :gap common/gs-19s
     :children [[event-section "Coeffects" (get-in event-trace [:tags :coeffects])]
                [event-section "Effects" (get-in event-trace [:tags :effects])]
                [event-section "Interceptors" (get-in event-trace [:tags :interceptors])]]]))
