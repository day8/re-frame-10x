(ns day8.re-frame-10x.preload.dev
  (:require
   [day8.re-frame-10x                                                :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core     :as rf]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom.client :as rdc]
   [day8.re-frame-10x.events                                         :as events]
   shadow.resource)
  (:require-macros [day8.re-frame-10x.components.re-com :refer [inline-resource]]))

(re-frame-10x/patch!)

(rf/dispatch-sync [::events/init re-frame-10x/project-config])

(rf/clear-subscription-cache!)

#_(defonce react-root (rdc/create-root (js/document.getElementById "--re-frame-10x-dev--")))

(defn root-component []
  [:div {:id "re-frame-10x"}
   [:style (shadow.resource/inline "day8/re_frame_10x/style.css")]
   [re-frame-10x/devtools-outer
    {:panel-type :inline
     :debug?     re-frame-10x/debug?}]])

#_(defn render []
    (rdc/render react-root [root-component]))
