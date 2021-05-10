(ns day8.re-frame-10x.components.inputs
  (:require
    [clojure.string                                               :as string]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core   :as r]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core       :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color   :as color]
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
    [day8.re-frame-10x.components.re-com                          :as rc :refer [deref-or-value]]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]))

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
  [ambiance checked? disabled?]
  {:cursor (when-not disabled? :pointer)}
  [:svg
   {:margin-right styles/gs-5}
   (when checked?
     [:path
      {:fill (color/lighten styles/nord3 20)}])]
  [:&:hover
   [:svg :path
    {:fill (color/lighten styles/nord3 20)}]])

(defn radio-button
  [{:keys [model value on-change label disabled? class]}]
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        model       (deref-or-value model)
        checked?    (= model value)
        disabled?   (deref-or-value disabled?)
        on-click-fn #(when (and on-change (not disabled?))
                       (on-change value))]
    [rc/h-box
     :align    :center
     :class    (str (radio-button-style ambiance checked? disabled?) " " class)
     :attr     {:on-click on-click-fn}
     :children
     [(if checked?
        [material/radio-button-checked]
        [material/radio-button-unchecked])
      [rc/label
       :label label]]]))

(defclass search-style
  [ambiance]
  {:composes      (styles/colors-1 ambiance)
   :border-bottom (styles/border-2 ambiance)}
  [:input
   {:border :none
    :background-color (styles/background-color-1 ambiance)}
   [:&:focus-visible
    {:outline :none}]])

(defn search [{:keys [title placeholder on-save on-change on-stop]}]
  (let [ambiance (rf/subscribe [::settings.subs/ambiance])
        val  (r/atom title)
        save #(let [v (-> @val str string/trim)]
                (when (pos? (count v))
                  (on-save v)))]
    (fn []
      [rc/h-box
       :class    (search-style @ambiance)
       :align    :center
       :children
       [[material/search]
        [:input {:type        "text"
                 :value       @val
                 :auto-focus  true
                 :placeholder placeholder
                 :size        (if (> 20 (count (str @val)))
                                25
                                (count (str @val)))
                 :on-change   #(do (reset! val (-> % .-target .-value))
                                   (on-change %))
                 :on-key-down #(case (.-which %)
                                 13 (do
                                      (save)
                                      (reset! val ""))
                                 nil)}]]])))