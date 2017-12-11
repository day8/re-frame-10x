(ns day8.re-frame.trace.styles
  (:require-macros [day8.re-frame.trace.utils.macros :as macros])
  (:require [garden.core :as garden]
            [garden.units :refer [em px percent]]
            [garden.color :as color]
            [garden.selectors :as s]
            [day8.re-frame.trace.common-styles :as common]
            [day8.re-frame.trace.utils.re-com :as rc]))

(def background-blue common/background-blue)
(def background-gray common/background-gray)
(def background-gray-hint common/background-gray-hint)
(def dark-green common/dark-green)
(def dark-gold common/dark-gold)
(def dark-purple common/dark-purple)
(def dark-blue common/dark-blue)
(def dark-gray common/dark-gray)
(def dark-skyblue common/dark-skyblue)
(def medium-gray common/medium-gray)
(def light-purple common/light-purple)
(def light-blue common/light-blue)
(def light-gray common/light-gray)
(def yellow common/yellow)
(def text-color common/text-color)
(def text-color-muted common/text-color-muted)


(def css-reset
  [:#--re-frame-trace--
   {:all "initial"}
   [:* "*:before" "*:after"
    {:all "unset"}]


   ;; /*! abridged from normalize.css v7.0.0 | MIT License | github.com/necolas/normalize.css */
   {:line-height "1.15"
    :font-size   "12px"}
   [:h1 {:font-size "2em"
         :margin    "0.67em 0"}]
   [:div :nav :h1 :h2 :h3 :h4 :h5 :h6 {:display "block"}]
   [:pre {:font-family "monospace"
          :font-size   (em 1)}]

   ;; Text-level semantics
   [(s/a) (s/a s/visited) {:color         text-color
                           :border-bottom [[(px 1) "#333" "dotted"]]}]
   [(s/a s/hover) (s/a s/focus) {:border-bottom [[(px 1) "#666666" "solid"]]}]

   [:code {:font-family "monospace"
           :font-size   (em 1)}]
   [:small {:font-size (percent 80)}]
   [:sub :sup {:font-size      (percent 75)
               :line-height    0
               :position       "relative"
               :vertical-align "baseline"}]
   [:sub {:bottom (em -0.25)}]
   [:sup {:top (em -0.5)}]
   [:img {:border-style "none"}]
   [:option {:display "block"}]
   [:button :input :optgroup :select :textarea
    {:font-family ["\"courier new\"" "monospace"]
     :font-size   (percent 100)
     :padding     [[(px 3) (px 3) (px 1) (px 3)]]
     :border      [[(px 1) "solid" medium-gray]]}]
   [:button :input {:overflow "visible"}]
   [:button :select [(s/& s/focus) {:outline [[medium-gray "dotted" (px 1)]]}]]
   [:button
    (s/html (s/attr= "type" "button"))
    (s/attr= "type" "reset")
    (s/attr= "type" "submit")
    {:-webkit-appearance "button"}]

   [:button:-moz-focusring
    (s/attr= "type" "button")
    ;; Couldn't figure out // Restore the focus styles unset by the previous rule.
    {:outline "1px dotted ButtonText"}]

   [:textarea
    {:overflow "auto"}]
   ;; Skipping IE 10-

   [(s/attr= "type" "search") {:-webkit-appearance "textfield"
                               :outline-offset     (px -2)}]

   [:li {:display "block"}]
   [:button {:overflow               "visible"
             :border                 0
             :-webkit-font-smoothing "inherit"
             :letter-spacing         "inherit"
             :background             "none"
            #_ #_  :cursor                 "pointer"}]
   [:img {:max-width (percent 100)
          :height    "auto"
          :border    "0"}]

   [:table :thead :tbody :tfoot :tr :th :td
    {:display                           "block"
     :width                             "auto"
     :height                            "auto"
     :margin                            0
     :padding                           0
     :border                            "none"
     :border-collapse                   "collapse"
     :border-spacing                    0
     :border-color                      "inherit"
     :vertical-align                    "inherit"
     :text-align                        "left"
     :font-weight                       "inherit"
     :-webkit-border-horizontal-spacing 0
     :-webkit-border-vertical-spacing   0}]
   [:table {:display "table"}]
   [:th :td {:display "table-cell"
             :padding [[0 (px 5)]]}]
   [:tr {:display "table-row"}]
   [:thead {:display "table-header-group"}]
   [:tbody {:display "table-row-group"}]
   [:th :td {:display "table-cell"}]
   [:tr {:display "table-row"}]])

(def label-mixin {:color      text-color
                  :background background-gray-hint
                  :border     [[(px 1) "solid" light-gray]]
                  :font-size  (em 0.9)
                  :margin     [[(px 10) (px 5)]]})

(def panel-mixin {:padding-top    (px 20)
                  :margin         "0 10px"
                  :display        "flex"
                  :flex-direction "column"
                  :flex           "1 1 auto"
                  :overflow-x     "auto"
                  :overflow-y     "auto"
                  :z-index        1000})

(def re-frame-trace-styles
  [:#--re-frame-trace--
   {:background  "white"
    :font-family ["'courier new'" "monospace"]
    :color       text-color}

   [:.label label-mixin]

   [s/table {:width     (percent 100)
             :font-size (px 14)}]
   [s/tbody {:color text-color}]
   [s/thead {:font-weight "bold"}]
   [:tr
    [:th :td {:padding (px 6)}]
    [(s/th s/first-child) {:text-align "right"}]
    [(s/& ".trace--trace") {}]

    [(s/& ":nth-child(even)") {:background background-gray-hint}]

    [(s/& ".trace--sub-create")
     [".trace--op" {:color dark-green}]]
    [(s/& ".trace--sub-run")
     [".trace--op" {:color dark-purple}]]
    [(s/& ".trace--event")
     [".trace--op" {:color common/event-color}]]
    [(s/& ".trace--render")
     [".trace--op" {:color dark-skyblue}]]
    [(s/& ".trace--fsm-trigger")
     [".trace--op" {:color dark-blue}]]



    [(s/& ".trace--details")
     {:color text-color-muted}
     [(s/& ":hover")
      (s/& ":focus") {:color (color/darken text-color-muted 0.2)} ;; TODO: darken(color, 20);
      [".trace--details-icon:before" {:color   text-color
                                      :cursor  "pointer"
                                      :content "\"🖶\""}]]

     ["&:focus"
      [".trace--details-tags-text"
       {:border-left  [["1px" "dotted" medium-gray]]
        :padding-left (px 7)}]]]

    [:td
     ["&.trace--toggle"
      {:color      background-gray
       :padding    0
       :text-align "right"}
      ["button:focus"
       {:color   text-color
        :outline "none"}]]
     ["&.trace--op"
      {:color        text-color-muted
       :padding-left 0
       :white-space  "nowrap"}]
     ["&.trace--op-string"
      {:word-break "break-all"}]
     ["&.trace--details-tags"
      {:padding 0
       :cursor  "pointer"}
      [".trace--details-tags-text"
       {:padding       "8px 5px 8px 8px"
        :margin-bottom "5px"}]]
     ["&.trace--meta"
      {:color       text-color-muted
       :white-space "nowrap"
       :text-align  "right"}]
     [".op-string"
      {:cursor  "pointer"
       :padding "1px"}
      ["&:hover"
       {:border-bottom  [[(px 1) "dotted" light-purple]]
        :padding-bottom 0}]]]

    ["&:hover"
     {".trace--toggle"
      {:color text-color}}]
    ["th" "td"
     ["&:first-child" {:padding-left "7px"}]
     ["&:last-child" {:padding-right "7px"}]]]

   [:.button {:padding       "5px 5px 3px"
              :margin        "5px"
              :border-radius "2px"
            #_ #_   :cursor        "pointer"}]
   [:.text-button {:border-bottom "1px dotted #888"
                   :font-weight   "normal"}
    [(s/& s/focus) {:outline [[medium-gray "dotted" (px 1)]]}]]

   [:.icon-button {:font-size "10px"}]
   [:button.tab {}]
   [:.nav-icon
    {:width   "15px"
     :height  "15px"
     :cursor  "pointer"
     :padding "0 5px"
     :margin  "0 5px"}
    ["&.inactive"
     {:cursor "initial"}]]
   [:.tab
    {:background     "transparent"
     :border-radius  0
     :text-transform "uppercase"
     :font-family    "monospace"
     :letter-spacing "2px"
     :margin-bottom  0
     :padding-bottom "4px"
     :vertical-align "bottom"}]

   [:.tab.active
    {:background     "transparent"
     :border-bottom  [[(px 3) "solid" dark-gray]]
     :border-radius  0
     :padding-bottom (px 1)}]

   [:ul.filter-items :.subtrees
    {:list-style-type "none"
     :margin          "0 5px"}
    [:.subtree-button :.filter-item
     (merge {:display "inline-block"}
            label-mixin)
     [:.filter-item-string {:color      text-color
                            :background yellow}]
     [:.subtree-button-string {:color text-color}]]
    [:.subtree
     [:img {:opacity "0"}]
     [:&:hover
      [:img {"opacity" 1}]]]]

   [:.icon {:display      "inline-block"
            :width        (em 1)
            :height       (em 1)
            :stroke-width 0
            :stroke       "currentColor"
            :fill         "currentColor"}]
   [:.icon-remove {:margin-left (px 10)}]
   [:.filter {:box-shadow "-7px 15px 6px -15px rgba(0, 0, 0, 0.3)"
              :z-index    1001}
    [:.filter-control
     [:select {:border             "none"
               :border-bottom      [[(px 1) "solid" text-color-muted]]
               :background         "white"
               :display            "inline-block"
               :font-family        "'courier new', monospace"
               :font-size          (em 1)
               :padding            "2px 0 0 0"
               :-moz-appearance    "menulist"
               :-webkit-appearance "menulist"
               :appearance         "menulist"}]
     [:.filter-control-input {:border-bottom [[(px 1) "solid" text-color-muted]]

                              :display       "inline-block"}

      [(s/& ":before") {:display   "inline-block"
                        :color     text-color-muted
                        :content   "\"⚲\""
                        :transform "rotate(-45deg)"}]
      [:input {:border "none"}]]]]
   [:.filter-control-input
    {:display "flex"
     :flex    "0 0 auto"}]
   [:.nav {:background light-gray
           :color      text-color}]
   [(s/& :.external-window) {:display "flex"
                             :height  (percent 100)
                             :flex    "1 1 auto"}]
   [:.panel-content-top {}]
   [:.panel-content-scrollable panel-mixin]
   [:.epoch-panel panel-mixin]
   [:.tab-contents {:display        "flex"
                    :flex           "1 1 auto"
                    :flex-direction "column"}]
   [:.filter-control {:margin "10px 0 0 10px"}]
   [:.filter-items-count
    {:cursor "auto"}
    [(s/& ".active") {:background yellow}]]
   [:.filter-fields {:margin-top "10px"}]
   [:.filter-category {:display    "inline-block"
                       :background "#efeef1"
                       :cursor     "pointer"
                       :padding    "5px"
                       :margin     "5px"
                       :opacity    "0.3"}]
   [:.active {:opacity 1}]
   [:.re-frame-trace--object
    [:.toggle {:color       text-color-muted
               :cursor      "pointer"
               :line-height 1}]
    ["> span" {:vertical-align "text-top"}]]
   [:.host-closed {:font-size        (em 4)
                   :background-color (color/rgba 255 255 0 0.8)}]
   [:.expansion-button {:font-family    "sans-serif"
                        :width          (px 16)
                        :padding        "0 2px"
                        :vertical-align "middle"}]
   ])


(def panel-styles (apply garden/css [css-reset (into [:#--re-frame-trace--] rc/re-com-css) re-frame-trace-styles]))
;(def panel-styles (macros/slurp-macro "day8/re_frame/trace/main.css"))


(defn inject-style [document id style]
  (let [styles-el     (.getElementById document id)
        new-styles-el (.createElement document "style")]
    (.setAttribute new-styles-el "id" id)
    (.setAttribute new-styles-el "type" "text/css")
    (-> new-styles-el
        (.-innerHTML)
        (set! style))
    (if styles-el
      (-> styles-el
          (.-parentNode)
          (.replaceChild new-styles-el styles-el))
      (let []
        (.appendChild (.-head document) new-styles-el)
        new-styles-el))))

(defn inject-trace-styles [document]
  (inject-style document "--re-frame-trace-styles--" panel-styles))
