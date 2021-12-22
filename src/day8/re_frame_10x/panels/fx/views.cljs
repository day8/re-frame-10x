(ns day8.re-frame-10x.panels.fx.views
  (:require
    [day8.re-frame-10x.components.re-com                          :as rc]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [percent]]
    [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core       :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.components.buttons                         :as buttons]
    [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]
    [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]))

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
  [title data]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (section-header-style ambiance)
     :align    :center
     :children [[rc/label :label title]
                [rc/gap-f :size "1"]
                [rc/box
                 :style {:margin       "auto"
                         :margin-right "1px"}
                 :child
                 [buttons/icon {:icon [material/print]
                                :on-click #(js/console.log data)}]]]]))

(defclass section-style
  [ambiance]
  {:composes (styles/frame-1 ambiance)})

(defn section [title data]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     :class    (section-style ambiance)
     :size     "1"
     :children
     [[section-header title data]
      [cljs-devtools/simple-render data [title] (styles/section-data ambiance)]]]))

(defclass panel-style
  [_]
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
