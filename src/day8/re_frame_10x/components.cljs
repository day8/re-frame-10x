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
  [ambiance checked? disabled?]
  {:cursor :pointer}
  [:svg
   {:margin-right styles/gs-5}
   (when checked?
     [:path
      {:fill styles/nord7}])]
  [:&:hover
   {:color (if (= :bright ambiance) styles/nord1 styles/nord5)}
   [:svg :path
    {:fill styles/nord7}]])

(defn checkbox
  [{:keys [model on-change label disabled? class]}]
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        checked?    (deref-or-value model)
        disabled?   (deref-or-value disabled?)
        on-click-fn #(when (and on-change (not disabled?))
                       (on-change (not checked?)))]
    [rc/h-box
     :align    :center
     :class    (str (checkbox-style ambiance checked? disabled?) " " class)
     :attr     {:on-click on-click-fn}
     :children
     [(if checked?
        [material/check-box]
        [material/check-box-outline-blank])
      (when label
        [rc/label
         :label label])]]))

(defclass radio-button-style
  [ambiance disabled?]
  {:cursor (when-not disabled? :pointer)}
  [:svg
   {:margin-right styles/gs-5}])

(defn radio-button
  [{:keys [model value on-change label disabled? class]}]
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        model       (deref-or-value model)
        disabled?   (deref-or-value disabled?)
        on-click-fn #(when (and on-change (not disabled?))
                       (on-change value))]
    [rc/h-box
     :align    :center
     :class    (str (radio-button-style ambiance disabled?) " " class)
     :attr     {:on-click on-click-fn}
     :children
     [(if (= model value)
        [material/radio-button-checked]
        [material/radio-button-unchecked])
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
  [{:keys [icon label title on-click disabled? class] :as args}]
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