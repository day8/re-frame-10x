(ns day8.re-frame-10x.components
  (:require
    [goog.fx.dom :as fx]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.utils.re-com :refer [deref-or-value]]
    [day8.re-frame-10x.panels.settings.subs :as settings.subs]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [clojure.string :as string]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color :as color]))

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

(defn popout-button
  [external-window?]
  (when-not external-window?
    [icon-button
     {:icon     [material/open-in-new]
      :title    "Pop out"
      :on-click #(rf/dispatch-sync [:global/launch-external])}]))

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

;; --- OLD ---

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

(defn scroll! [el start end time]
  (.play (fx/Scroll. el (clj->js start) (clj->js end) time)))

(defn scrolled-to-end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))


(defn reset-wrapping [css-string]
  (string/replace css-string #"white-space:nowrap;" ""))


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
   :child [:span #_{:style {:color  styles/nord4
                            :margin "auto"}} label]])