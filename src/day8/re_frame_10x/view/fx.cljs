(ns day8.re-frame-10x.view.fx
  (:require
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass defglobal]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.view.cljs-devtools :as cljs-devtools]
    [day8.re-frame-10x.styles :as styles]))

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defn event-section [title data]
  (let [ambiance @(rf/subscribe [:settings/ambiance])]
    [rc/v-box
     :children
     [[rc/h-box
       :class    (styles/section-header ambiance)
       :align    :center
       :children [[rc/label :label title]]]
      [cljs-devtools/simple-render data [title] (styles/section-data ambiance)]]]))

(defn render []
  (let [event-trace @(rf/subscribe [:epochs/current-event-trace])]
    [rc/v-box
     :style    {:width "100%"}
     :gap      styles/gs-19s
     :children [[event-section "Coeffects"    (get-in event-trace [:tags :coeffects])]
                [event-section "Effects"      (get-in event-trace [:tags :effects])]
                [event-section "Interceptors" (get-in event-trace [:tags :interceptors])]
                [rc/gap-f :size "0px"]]]))
