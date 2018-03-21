(ns day8.re-frame-10x.view.fx
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.view.components :as components]
            [zprint.core :as zp]
            [clojure.string :as str]))

(def fx-styles
  [:#--re-frame-10x--
   [:.code-panel
    #_{:padding-bottom common/gs-31}] ;; Leaving the empty def here for now
   ;; We rely on highlight JS for our background now.
   #_[:.code-listing
    {:background-color common/white-background-color
     :padding          "5px"
     :margin-right     "19px"}]
   [:.code-listing--highlighted
    {:font-weight      "bold"
     :background-color "rgba(100, 100, 255, 0.08)"}]
   [:.code-fragment {:background-color common/white-background-color}
    [:&:hover {:background-color "rgba(100, 100, 255, 0.08)"}]]])

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

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
                [event-section "Interceptors" (get-in event-trace [:tags :interceptors])]
                [rc/gap-f :size "0px"]]]))
