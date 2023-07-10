(ns day8.re-frame-10x.components.data
  (:require
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core     :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
   [day8.re-frame-10x.components.re-com                        :as rc]
   [day8.re-frame-10x.styles                                   :as styles]))

(defclass tag-style
  []
  {:width         styles/gs-50
   :height        styles/gs-19
   :font-size     (px 10)
   :font-weight   :bold
   :border-radius styles/gs-2}
  [:span
   {:margin :auto}])

(defn tag [class label]
  [rc/box
   :class (str (tag-style) " " class)
   :child [:span label]])