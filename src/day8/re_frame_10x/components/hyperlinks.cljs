(ns day8.re-frame-10x.components.hyperlinks
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px percent]]
    [day8.re-frame-10x.components.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.panels.settings.subs :as settings.subs]
    [day8.re-frame-10x.material :as material]))


(defclass hyperlink-info
  [ambiance]
  {#_#_:background-color nord0
   :border-radius (percent 50)}
  [:svg ;; TODO: no border; fill the question.
   {:background-color styles/nord0
    :width (px 18)
    :height (px 18)
    :border-radius (percent 50)}]
  [:svg :path
   {:fill styles/nord4}]
  [:&:hover
   [:svg :path
    {:fill styles/nord5}]])

(defn info
  [url]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/hyperlink-href
     :class (styles/hyperlink-info ambiance)
     :label [rc/box
             :justify :center
             :align   :center
             :child   [material/help]]
     :attr   {:rel "noopener noreferrer"}
     :target "_blank"
     :href   url]))