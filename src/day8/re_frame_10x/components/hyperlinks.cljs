(ns day8.re-frame-10x.components.hyperlinks
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core    :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [percent]]
   [day8.re-frame-10x.components.re-com                          :as rc]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]))

(defclass info-style
  [ambiance]
  {:composes      (styles/colors-2 ambiance)
   :border-radius (percent 50)}
  [:svg :path
   {:fill styles/nord0}]
  [:&:hover
   [:svg :path
    {:fill styles/nord3}]])

(defn info
  [url]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/hyperlink-href
     :class (info-style ambiance)
     :label [rc/box
             :justify :center
             :align   :center
             :child   [material/help-outline]]
     :attr   {:rel "noopener noreferrer"}
     :target "_blank"
     :href   url]))
