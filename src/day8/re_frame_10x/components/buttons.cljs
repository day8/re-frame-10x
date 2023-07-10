(ns day8.re-frame-10x.components.buttons
  (:require
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core  :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
   [day8.re-frame-10x.components.re-com                        :as rc]
   [day8.re-frame-10x.material                                 :as material]
   [day8.re-frame-10x.styles                                   :as styles]))

(defclass icon-style
  [disabled?]
  {:cursor           (if disabled? :default :pointer)
   :border-radius    (px 3)
   :background-color (if disabled? styles/nord2 styles/nord5)
   :border           [[(px 1) :solid (if disabled? styles/nord1 styles/nord4)]]
   :padding          styles/gs-2s
   :font-weight      400}
  [:svg :path
   {:fill styles/nord0}]
  (when-not disabled?
    [:&:hover
     {:background-color styles/nord6}
     [:svg :path
      {:fill styles/nord1}]]))

(defn icon
  [{:keys [icon label title on-click disabled? class]}]
  (let [disabled? (rc/deref-or-value disabled?)]
    [rc/button
     :class    (str (icon-style disabled?) " " class)
     :attr     {:title title}
     :label    [rc/h-box
                :align    :center
                :justify  :center
                :children [icon
                           (when label
                             [:<>
                              [rc/gap-f :size styles/gs-2s]
                              label])]]
     :on-click #(when-not disabled? (on-click))]))

(defclass expansion-style
  []
  {:cursor :pointer}
  [:svg :path
   {:fill styles/nord0}]
  [:&:hover
   [:svg :path
    {:fill styles/nord1}]])

(defn expansion
  [{:keys [open? size]}]
  [rc/box
   :class (expansion-style)
   :child (if open?
            [material/arrow-drop-down {:size size}]
            [material/arrow-right {:size size}])])