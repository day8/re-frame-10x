(ns day8.re-frame-10x.fx.views
  (:require
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [percent]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass defglobal]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.components.cljs-devtools :as cljs-devtools]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.settings.subs :as settings.subs]))

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defclass section-header-style
  [ambiance]
  {:composes      (styles/colors-1 ambiance)
   :padding-left  styles/gs-12
   :height        styles/gs-31
   :border-bottom (styles/border-1 ambiance)})

(defn section-header
  [title]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (section-header-style ambiance)
     :align    :center
     :children [[rc/label :label title]]]))

(defclass section-style
  [ambiance]
  {:composes (styles/frame-1 ambiance)})

(defn section [title data]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     :class    (section-style ambiance)
     :size     "1"
     :children
     [[section-header title]
      [cljs-devtools/simple-render data [title] (styles/section-data ambiance)]]]))

(defclass panel-style
  [ambiance]
  {:width         (percent 100)})

(defn panel []
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        event-trace @(rf/subscribe [::epochs.subs/selected-event-trace])]
    [rc/v-box
     :class    (panel-style ambiance)
     :gap      styles/gs-19s
     :children [[section "Coeffects"    (get-in event-trace [:tags :coeffects])]
                [section "Effects"      (get-in event-trace [:tags :effects])]
                [section "Interceptors" (get-in event-trace [:tags :interceptors])]
                [rc/gap-f :size "0px"]]]))
