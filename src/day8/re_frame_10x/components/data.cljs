(ns day8.re-frame-10x.components.data
  (:require
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.core     :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
   [day8.re-frame-10x.components.re-com                        :as rc]
   [day8.re-frame-10x.styles                                   :as styles]))

(defn tag [{:keys [class style label]}]
  [rc/box
   :style style
   :class (str "data-tag " class)
   :child [:span label]])
