(ns day8.re-frame-10x.view.fx
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.view.components :as components]
            [zprint.core :as zp]
            [clojure.string :as str]))

(def code-hover-background-color "rgba(100, 100, 255, 0.08)")

(def fx-styles
  [:#--re-frame-10x--
   [:.code-panel
    #_{:padding-bottom common/gs-31}] ;; Leaving the empty def here for now
   ;; We rely on highlight JS for our background now.
   ;[:.code-listing
   ; {:background-color common/white-background-color
   ;  :padding          "5px"
   ;  :margin-right     "19px"}]
   [:.code-listing--highlighted
    {:font-weight      "bold"
     :background-color code-hover-background-color}]
   [:.code-fragment
    {:background-color common/white-background-color}
    [:.code-fragment__content
     {:height "19px"}]
    [:.code-fragment__result
     {:visibility "hidden"
      :color "#b4b4b4"}] ;; Was common/medium-gray
    [:&:hover
     {:background-color code-hover-background-color}
     [:.code-fragment__result
      {:visibility "visible"}]]
    [:.code-fragment__button
     {:display "none"
      :padding-left "6px"
      :margin-left  "6px"
      :border-left  "1px solid #cdd8df"
      :cursor       "pointer"
      :color        common/blue-modern-color}]
    [:&:hover
     [:.code-fragment__button
      {:display "grid"}]]
    ]])

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
     :class    "event-section--header app-db-path--header"
     :align    :center
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
