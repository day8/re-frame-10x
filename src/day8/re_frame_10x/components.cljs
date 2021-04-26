(ns day8.re-frame-10x.components
  (:require
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.utils.re-com :refer [deref-or-value]]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]))

(defclass checkbox-style
  [ambiance active? disabled?]
  {:composes (styles/control-2 ambiance active?)})

(defn checkbox
  [{:keys [model on-change label disabled? class]}]
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        model       (deref-or-value model)
        disabled?   (deref-or-value disabled?)
        callback-fn #(when (and on-change (not disabled?))
                       (on-change (not model)))]
    [rc/h-box
     :class    (str (checkbox-style ambiance model disabled?) " " class)
     :attr     {:on-click callback-fn}
     :children
     [(if model
        [material/check-box]
        [material/check-box-outline-blank])
      [rc/label
       :label label]]]))

(defn hyperlink-info
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

(defclass icon-button-style
  [ambiance disabled?]
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

(defn icon-button
  [{:keys [icon label title on-click disabled? class]}]
  (let [ambiance  @(rf/subscribe [::settings.subs/ambiance])
        disabled? (rc/deref-or-value disabled?)]
    [rc/button
     :class    (str (icon-button-style ambiance disabled?) " " class)
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


(defclass expansion-button-style
  [ambiance]
  {:cursor :pointer}
  [:svg :path
   {:fill styles/nord0}]
  [:&:hover
   [:svg :path
    {:fill styles/nord1}]])

(defn expansion-button
  [{:keys [open? size]}]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/box
     :class (expansion-button-style ambiance)
     :child (if open?
              [material/arrow-drop-down :size size]
              [material/arrow-right :size size])]))