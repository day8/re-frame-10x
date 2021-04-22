(ns day8.re-frame-10x.view.timing
  (:require
    [devtools.prefs]
    [devtools.formatters.core]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [em px percent]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass defglobal]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.re-com :as rc :refer [css-join]]
    [day8.re-frame-10x.view.components :as components]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.timing.subs :as timing.subs]))

#_(defglobal timing-styles
    [:#--re-frame-10x--
     [:.timing-details
      {:margin-top   styles/gs-31s
       :margin-right styles/gs-19s}]
     [:.timing-details--line
      {:margin "1em 0"}]

     [:p :ol
      {:max-width "26em"}]
     [:ol
      {"-webkit-padding-start" "20px"}]
     [:li
      {:margin "0 0 1em 0"}]

     [".rft-tag__timing"
      {:background-color "#ECEDF0"
       :border           (str "1px solid " styles/border-line-color)
       :font-weight      "normal"
       :font-size        "14px"
       :width            "63px"}]

     [".timing-elapsed-panel"
      {:padding "12px 12px 12px 0px"
       :margin  (css-join styles/gs-7s "0px")}]])

(defclass timing-tag-style
  [ambiance]
  {:background-color (if (= :bright ambiance) styles/nord5 styles/nord1)
   :color            (if (= :bright ambiance) styles/nord1 styles/nord5)
   :border           [[(px 1) :solid styles/nord4]]})

(defn timing-tag [label]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [components/tag (timing-tag-style ambiance) label]))

(defn timing-section
  [label time]
  [rc/v-box
   :align :center
   :gap "3px"
   ;; TODO: detect <1 ms timing here, to distinguish between none at all, and rounding to 0.
   :children [[rc/label :class "bm-textbox-label" :label label]
              [timing-tag (cond
                            (nil? time) "-"
                            (= time 0) (str "0ms")
                            (< time 0.1) (str "<0.1ms")
                            (< time 1) (str (.toFixed time 1) "ms")
                            (some? time) (str (js/Math.round time) "ms"))]]])

(defclass timing-part-style
  [ambiance]
  {:background-color (if (= :bright ambiance) styles/nord4 styles/nord0)
   :color            (if (= :bright ambiance) styles/nord0 styles/nord6)
   :border           [[(px 1) :solid styles/nord5]]
   :border-radius    styles/gs-2
   :padding          styles/gs-12
   :margin           [[styles/gs-7 0]]
   :overflow         :hidden})

(defn render []
  (let [ambiance               @(rf/subscribe [::settings.subs/ambiance])
        timing-data-available? @(rf/subscribe [::timing.subs/data-available?])
        event-processing-time  @(rf/subscribe [::timing.subs/event-processing-time])]
    (if timing-data-available?
      [rc/v-box
       :class "timing-details"
       :children [[rc/h-box
                   :class (timing-part-style ambiance)
                   :gap   styles/gs-12s
                   :align :end
                   :children [[rc/box
                               :justify :center
                               :width   styles/gs-81s
                               :child   [:span "elapsed"]]
                              [timing-section "" @(rf/subscribe [::timing.subs/total-epoch-time])]
                              [rc/hyperlink-href
                               :label  "guide me to greatness"
                               :class  (styles/hyperlink ambiance)
                               ;:style  {:margin-left styles/gs-19s}
                               :attr   {:rel "noopener noreferrer"}
                               :target "_blank"
                               :href   "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnderstandingTiming.md"]]]
                  [rc/h-box
                   :gap      styles/gs-12s
                   :class    (timing-part-style ambiance)
                   :align    :center
                   :children [[rc/v-box
                               :align :center
                               :width styles/gs-81s
                               :children [[:span "event"] [:span "processing"]]]
                              [timing-section "total" (:timing/event-total event-processing-time)]
                              [:span "="]
                              [timing-section "handler" (:timing/event-handler event-processing-time)]
                              [:span "+"]
                              [timing-section "effects" (:timing/event-effects event-processing-time)]
                              #_[:span "+"]
                              #_[timing-section "other int." (:timing/event-interceptors event-processing-time)]
                              [:span "+"]
                              [timing-section "misc" (:timing/event-misc event-processing-time)]]]
                  (doall
                    (for [frame (range 1 (inc @(rf/subscribe [::timing.subs/animation-frame-count])))
                          :let [frame-time @(rf/subscribe [::timing.subs/animation-frame-time frame])]]
                      (list
                        ;^{:key (str "af-line" frame)}
                        ;[rc/line :class "timing-details--line"]
                        ^{:key (str "af" frame)}
                        [rc/h-box
                         :align :center
                         :class (timing-part-style ambiance)
                         #_#_:class "timing-part-panel"
                         :gap styles/gs-12s
                         :children
                         [[rc/v-box
                           :align :center
                           :width styles/gs-81s
                           :children [[:span "animation"] [:span "frame"] [:span "#" frame]]]
                          [timing-section "total" (:timing/animation-frame-total frame-time)]
                          [:span "="]
                          [timing-section "subs" (:timing/animation-frame-subs frame-time)]
                          [:span "+"]
                          [timing-section "views" (:timing/animation-frame-render frame-time)]
                          [:span "+"]
                          [timing-section "react, etc" (:timing/animation-frame-misc frame-time)]]])))]]
      [rc/v-box
       :class "timing-details"
       :children [[:h1 "No timing data currently available."]]])))
