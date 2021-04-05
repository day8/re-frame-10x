(ns day8.re-frame-10x.styles
  (:require
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core       :as garden]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.compiler   :refer [render-css]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units      :refer [em px percent]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color      :as color :refer [rgb rgba as-hsla opacify transparentize]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.selectors  :as s]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.stylesheet :refer [at-keyframes]] ;;(at-import at-media at-keyframes)
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core          :refer [defclass defglobal]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.runtime       :as spade.runtime])
  (:require-macros
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.selectors  :refer [defpseudoclass
                                                                             defpseudoelement]]))

;; ====
;; https://www.nordtheme.com/docs/colors-and-palettes
;; ===

;; Polar Night
(def nord0 "#2E3440")
(def nord1 "#3B4252")
(def nord2 "#434C5E")
(def nord3 "#4C566A")

;; Snow Storm
(def nord4 "#D8DEE9")
(def nord5 "#E5E9F0")
(def nord6 "#ECEFF4")

;; Frost
(def nord7 "#8FBCBB")
(def nord8 "#88C0D0")
(def nord9 "#81A1C1")
(def nord10 "#5E81AC")

;; Aurora
(def nord11 "#BF616A")
(def nord12 "#D08770")
(def nord13 "#EBCB8B")
(def nord14 "#A3BE8C")
(def nord15 "#B48EAD")

;; Extras
(def nord-ghost-white "#F8F9FB")

;; Golden section, base 50
(def gs-2 (px 2))
(def gs-5 (px 5))
(def gs-7 (px 7))
(def gs-12 (px 12))
(def gs-19 (px 19))
(def gs-31 (px 31))
(def gs-50 (px 50))
(def gs-81 (px 81))
(def gs-131 (px 131))

(def gs-2s (render-css gs-2))
(def gs-5s (render-css gs-5))
(def gs-7s (render-css gs-7))
(def gs-12s (render-css gs-12))
(def gs-19s (render-css gs-19))
(def gs-31s (render-css gs-31))
(def gs-50s (render-css gs-50))
(def gs-81s (render-css gs-81))
(def gs-131s (render-css gs-131))

(def font-stack ["\"Segoe UI\"" "Roboto" "Helvetica" "sans-serif"])

(defpseudoclass -moz-focusring)
(defpseudoelement -moz-focus-inner)

;; ===
;; Base Classes
;; ===

(defn background-color-0
  [ambiance]
  (if (= ambiance :bright) :#fff nord1))

(defclass colors-0
  [ambiance]
  {:background-color (background-color-0 ambiance)})

(defn background-color-1
  [ambiance]
  (if (= :bright ambiance) nord-ghost-white nord0))

(defn color-1
  [ambiance]
  (if (= :bright ambiance) nord3 nord4))

(defclass colors-1
  [ambiance]
  {:background-color (background-color-1 ambiance)
   :color            (color-1 ambiance)})

(defn border-1
  [ambiance]
  [[(px 1) :solid (if (= :bright ambiance) nord4 nord3)]])

(defclass frame-1
  [ambiance]
  {:composes      (colors-1 ambiance)
   :border        (border-1 ambiance)
   :border-radius gs-2})

(defn background-color-2
  [ambiance]
  (if (= :bright ambiance) nord6 nord1))

(defn color-2
  [ambiance]
  (if (= :bright ambiance) nord2 nord4))

(defclass colors-2
  [ambiance]
  {:background-color (background-color-2 ambiance)
   :color            (color-2 ambiance)}
  [:svg :path
   {:fill (color-2 ambiance)}])

(defn border-2
  [ambiance]
  [[(px 1) :solid (if (= :bright ambiance) nord4 nord3)]])

(defclass frame-2
  [ambiance]
  {:composes      (colors-2 ambiance)
   :border        (border-2 ambiance)
   :border-radius gs-2})

(defclass colors-uncommon
  [ambiance]
  {:color            :#fff
   :background-color nord15})

(defclass frame-uncommon
  [ambiance]
  {:composes      (colors-uncommon ambiance)
   :border        [[(px 1) :solid (color/darken nord15 10)]]
   :border-radius gs-2})

(defclass control-2
  [ambiance active?]
  {:composes (frame-2 ambiance)
   :cursor   :pointer}
  (when active?
    [:svg :path
     {:fill nord7}])
  [:&:hover
   {:background-color (if (= :bright ambiance) nord5 nord2)
    :color            (if (= :bright ambiance) nord1 nord5)}
   [:svg :path
    {:fill nord7}]])

(defclass hyperlink
  [ambiance]
  {:color nord9}
  [:&:hover
   {:text-decoration :underline}])

(defn syntax-color
  [ambiance syntax-color-scheme key]
  (let [signature (if (= :cljs-devtools)
                    (rgba 100 255 100 1)
                    (if (= :bright ambiance)
                      nord-ghost-white
                      nord0))]
    (case key
      :base-text-color      (if (= :cljs-devtools syntax-color-scheme)
                              (rgb 0 0 0)
                              (if (= :bright ambiance) nord0 nord6))
      :signature-background  (transparentize signature 0.92)
      :type                 (if (= :cljs-devtools syntax-color-scheme)
                              (rgb 0 160 220)
                              nord7)

      :type-text            (if (= :cljs-devtools syntax-color-scheme)
                              (rgb 238 238 355)
                              :pink)
      :field                (syntax-color ambiance syntax-color-scheme :type)
      :basis                (syntax-color ambiance syntax-color-scheme :type)
      :meta (rgb 255 102 0)
      :meta-text (rgb 238 238 238)
      :protocol (rgb 41 59 163)
      :method (rgb 41 59 163) ;; protocol
      :ns (rgb 150 150 150)
      :native (rgb 255 0 255)
      :fn (rgb 30 130 30)
      :lambda (rgb 30 130 30) ;; fn
      :fn-args (rgb 170 130 20)
      :custom-printing (rgb 255 255 200)
      :circular-ref (rgb 255 0 0)
      :nil (rgb 128 128 128)
      :keyword (rgb  136 19 145)
      :integer (rgb 28 0 207)
      :float (rgb 28 136 207)
      :float-nan (rgb 213 60 27)
      :float-infinity (rgb 28 80 207)
      :string (if (= :cljs-devtools syntax-color-scheme)
                (rgb 196 26 22) nord14)
      :expanded-string (rgb 255 100 100)
      :symbol (if (= :cljs-devtools syntax-color-scheme)
                (rgb 0 0 0) nord8)
      :bool (rgb 0 153 153)
      :fast-protocol (rgb 255 255 170)
      :slow-protocol (rgb 238 238 238)
      :more (rgb 255 255 255)
      :more-background (rgb 153 153 153)
      :index (rgb 0 0 0)
      :index-background (rgb 221 221 221)
      :field-spacer (rgb 204 204 204)
      :native-reference-background (rgb 255 255 255)
      :body-border (rgba 60 90 60 0.1)
      :expanded-string-background (rgba 255 100 100 0.98) ;;expanded-string 0.4
      :expanded-string-border (rgba 255 100 100 0.6) ;; expanded-string 0.4
      :custom-printing-background (rgb 255 255 200) ;; custom-printing
      nil)))

(defclass code
  [ambiance syntax-color-scheme]
  {:background-color (syntax-color ambiance syntax-color-scheme :signature-background)
   :border           [[(px 1) :solid (color/darken (syntax-color ambiance syntax-color-scheme :signature-background) 50)]]})

(defclass hljs
  [ambiance syntax-color-scheme]
  {:composes         (code ambiance syntax-color-scheme)
   :border-radius    gs-2
   :font-weight      400}
  [:.code-listing--highlighted
   {:background  (if (= :bright ambiance) nord13 nord2)}]
  [:.hljs-symbol
   {:color (syntax-color ambiance syntax-color-scheme :keyword)}]
  [:.hljs-type
   {:color (syntax-color ambiance syntax-color-scheme :type)}]
  [:.hljs-string
   {:color (syntax-color ambiance syntax-color-scheme :string)}]
  [:.hljs-name
   {:color (syntax-color ambiance syntax-color-scheme :fn)}]
  [:.hljs-literal
   {:color (syntax-color ambiance syntax-color-scheme :bool)}]
  [:.hljs-number
   {:color (syntax-color ambiance syntax-color-scheme :float)}])













(defclass unset
  []
  {:all :initial}
  [:h1 :h2 :h3 :h4 :h5 :h6
   :div :p
   :pre :code
   :a :b :i :span
   :li :ol :ul
   :br
   :img
   :table :tbody :td :tfoot :th :thead :tr
   :button :input :option :select
   {:all :unset}
   [:&:before :&:after
    {:all :unset}]])

;; /*! abridged from normalize.css v8.0.1 | MIT License | github.com/necolas/normalize.css */
(defclass normalize
  []
  {:line-height              (em 1.15)
   :-webkit-text-size-adjust (percent 100)}

  [:h1
   {:font-size (em 2)
    :margin    [[(em 0.67) 0]]}]

  [:pre
   :code
   {:font-family [:monospace :monospace]
    :font-size   (em 1)}]

  [:b
   {:font-weight :bolder}]

  [:button
   :input
   :select
   :textarea
   {:font-family font-stack
    :font-size   (percent 100)
    :line-height 1.15
    :margin      0}]

  [:button
   :input
   {:overflow :visible}]

  [:button
   :select
   {:text-transform :none}]

  [:button
   (s/html (s/attr= :type :button))
   (s/attr= :type :reset)
   (s/attr= :type :submit)
   {:-webkit-appearance :button}]

  [((s/button) -moz-focus-inner)
   ((s/attr= :type :button) -moz-focus-inner)
   ((s/attr= :type :reset) -moz-focus-inner)
   ((s/attr= :type :submit) -moz-focus-inner)
   {:border-style :none
    :padding      0}]

  [((s/button) -moz-focusring)
   ((s/attr= :type :button) -moz-focusring)
   ((s/attr= :type :reset) -moz-focusring)
   ((s/attr= :type :submit) -moz-focusring)
   {:outline [[(px 1) :dotted :ButtonText]]}]

  [:textarea
   {:overflow :auto}])

;; nord0 nord6 background when not syntax
;; nord1 nord4 background prominent/focused

(defclass background
  [ambiance]
  {:background-color (if (= ambiance :bright) :#fff nord0)})

(defclass navigation
  [ambiance]
  {:background-color nord0}
  [:.rc-label
   {:color       nord5
    :font-weight :bold
    :font-size   (px 14)}])

(defclass navigation-border-top
  [ambiance]
  {:composes   (navigation ambiance)
   #_#_:border-top [[gs-2 :solid nord1]]})

(defclass navigation-border-bottom
  [ambiance]
  {:composes      (navigation ambiance)
   #_#_:border-bottom [[gs-2 :solid nord1]]})

(defclass button
  [ambiance]
  {:cursor           :pointer
   :background-color (if (= :bright ambiance) nord5 nord0)
   :color            (if (= :bright ambiance) nord0 nord5)
   ;; TODO: why doesn't this default to 400?
   :font-weight      400
   :border-radius    (px 2)
   :border           [[(px 1) :solid nord4]]
   :padding          [[gs-2 gs-5 gs-2 gs-5]]}
  [:svg :path
   {:fill (if (= :bright ambiance) nord0 nord5)}]
  [:&:hover
   {:background-color (if (= :bright ambiance) nord6 nord2)
    :color            (if (= :bright ambiance) nord2 nord6)}
   [:svg :path
    {:fill (if (= :bright ambiance) nord2 nord6)}]])

(defclass hyperlink-info
  [ambiance]
  {#_#_:background-color nord0
   :border-radius "50%"}
  [:svg ;; TODO: no border; fill the question.
   {:background-color nord0
    :width "18px"
    :height "18px"
    :border-radius "50%"}]
  [:svg :path
   {:fill nord4}]
  [:&:hover
   [:svg :path
    {:fill nord5}]])



;[:.hljs-built_in
; :.hljs-type
; {:color nord7}]
  ;["::selection"
  ; {:background (if (= :bright ambiance) nord5 nord2)}]
  ;[:.hljs
  ; {:display    :block
  ;  :overflow-x :auto
  ;  :padding    (em 0.5)}]
  ;
  ;[:.hljs
  ; :.hljs-subst
  ; {:color (if (= :bright ambiance) nord0 nord4)}]
  ;[:.hljs-selector.tag
  ; {:color nord9}]
  ;[:.hljs-selector-id
  ; {:color nord7
  ;  :font-weight :bold}]
  ;[:.hljs-selector-class
  ; {:color nord7}]
  ;[:.hljs-selector-attr
  ; {:color nord7}]
  ;[:.hljs-selector-pseudo
  ; {:color nord8}]
  ;[:.hljs-addition
  ; {:color nord14}]
  ;[:.hljs-deletion
  ; {:color nord11}]

  ;[:.hljs-class
  ; {:color nord7}]
  ;[:.hljs-function
  ; {:color nord8}
  ; ["> .hljs-title"
  ;  {:color nord8}]]
  ;[:.hljs-keyword
  ; :.hljs-literal
  ; {:color nord9}]
  ;[:.hljs-symbol
  ; {:color nord13}]
  ;[:.hljs-number
  ; {:color nord15}]
  ;[:.hljs-regexp
  ; {:color nord13}]

  ;[:.hljs-params
  ; {:color nord4}]
  ;[:.hljs-comment
  ; {:color nord3}]
  ;[:.hljs-doctag
  ; {:color nord7}]
  ;[:.hljs-meta
  ; :.hljs-meta-keyword
  ; {:color nord10}]
  ;[:.hljs-meta-string
  ; {:color nord14}]
  ;[:.hljs-attr
  ; {:color nord7}]
  ;[:.hljs-attribute
  ; {:color nord4}]
  ;[:.hljs-builtin-name
  ; {:color nord9}]
  ;[:.hljs-name
  ; {:color nord8}]
  ;[:.hljs-section
  ; {:color nord8}]
  ;[:.hljs-tag
  ; {:color nord9}]
  ;[:.hljs-variable
  ; {:color nord4}]
  ;[:.hljs-template-variable
  ; {:color nord4}]
  ;[:.hljs-template-tag
  ; {:color nord10}])

(defclass section-header
  [ambiance]
  (let [[foreground background border] (if (= :bright ambiance)
                                         [nord0 nord5 nord4]
                                         [nord6 nord1 nord3])]
    {:background-color background
     :color            foreground
     #_#_:padding-left     gs-12s ;; TODO: this conflicts between fx and subs; need padding for fx but no padding for subs.
     :border           [[(px 1) :solid border]]
     :height           gs-31
     :font-size        (px 14)
     ;:font-weight      :bold ;; TODO same as above
     :overflow         :hidden}))


(defclass path-header-style
  [ambiance]
  {:background-color (if (= :bright ambiance) nord5 nord0)
   :color            (if (= :bright ambiance) nord0 nord5)
   :margin           (px 3)})

(defclass path-text-input-style
  [ambiance]
  {:background-color (background-color-1 ambiance)
   :height           (px 25)
   :width            "-webkit-fill-available" ;; This took a bit of finding!
   :padding          [[0 gs-7]]
   :border           :none}
  [:&:focus
   {:outline-color nord6}]
  ["&::placeholder"
   {:color nord3
    :font-style :italic
    :font-weight 300}])

(defclass pod-border
  [ambiance]
  {:border-left      [[(px 1) :solid nord4]]
   :border-right     [[(px 1) :solid nord4]]
   :border-bottom    [[(px 1) :solid nord4]]})

(defclass pod-data
  [ambiance]
  {:background-color (background-color-1 ambiance)
   :padding          [[0 gs-2]]
   :min-width        (px 100)})

(defclass section-data
  [ambiance]
  {:background-color (syntax-color ambiance :cljs-devtools :signature-background)
   :padding     [[gs-5 gs-12]]})

(defclass app-db-inspector-link
  [ambiance]
  {}
  [:a
   {:font-size (px 11)
    :color nord7}])

(defclass filter-style
  [ambiance]
  {:box-shadow [[(px -7) (px 15) (px 6) (px -15) (rgba 0 0 0 0.3)]]
   :z-index 1001}
  [:input
   [(s/& ":before")
    {:display   :inline-block
     :color     nord0
     :content   "\"⚲\""
     :transform "rotate(-45deg)"}]])

(defclass code-fragment
  [ambiance]
  {:color             (if (= :bright ambiance) nord0 nord4)
   :background-color  (background-color-1 ambiance)}


  [:.re-frame-10x-code-fragment-content
   {:border   [[(px 1) :solid nord4]]}]
  [:.re-frame-10x-code-fragment-button
   {:display      :none
    :padding-left gs-7
    :margin-left  gs-7
    :border-left  [[(px 1) :solid nord4]]
    :cursor       :pointer
    :color        nord7}
   [:&:hover
    {:display :grid}]])

(defclass registrar
  [ambiance]
  {:background-color (if (= :bright ambiance) nord5 nord0)
   :color            nord7
   :list-style      :none
   :border          [[(px 1) :solid nord4]]
   :padding         0}
  [:pre
   {:padding      gs-12
    :margin-right gs-12}])

(defclass no-select
  [ambiance]
  {:-webkit-touch-callout :none
   :-webkit-user-select   :none
   :-khtml-user-select    :none
   :-moz-user-select      :none
   :-ms-user-select       :none
   :user-select           :none})

(defclass category-filters
  [ambiance]
  {:list-style :none
   :display    :flex
   :align-items :center
   :padding    0
   :color      (if (= :bright ambiance) nord0 nord4)}
  [:li
   {:display          :flex
    :align-items      :center
    :margin-left      gs-12
    :cursor           :pointer
    :background-color (if (= :bright ambiance) nord5 nord1)
    :border-radius    gs-2
    :border           [[(px 1) :solid nord4]]
    :padding          [[gs-5 gs-12]]}
   [:svg
    {:margin-right gs-5}]
   [:&.active
    :&:hover
    {:background-color (if (= :bright ambiance) nord6 nord2)
     :color            (if (= :bright ambiance) nord1 nord5)}
    [:svg :path
     {:fill nord7}]]])

(defclass trace-filter-fields
  [ambiance]
  {}
  [:select
   :input
   {:border :none
    :background-color (if (= :bright ambiance) nord6 nord1)
    :color            (if (= :bright ambiance) nord0 nord6)}]
  [:.search
   [:input
    {:margin-left gs-12}]
   [:&::before
    {:content "\"⚲\""
     :transform "rotate(-45deg)"}]])

(defclass done-button
  [ambiance]
  {:background-color nord13}
  [:&:hover
   {:background-color nord13}])

(defclass trace-item
  [ambiance op-type]
  {:color (case op-type
            :sub/create  nord14
            :sub/run     nord15
            :sub/dispose nord11
            :event       nord12
            :render      nord8
            :re-frame.router/fsm-trigger nord10
            nil)})

(defclass trace-table
  [ambiance]
  {:background-color nord4
   :border-radius    gs-2}
  [:thead
   {:background-color nord5}])

(defglobal at-keyframes-styles
  (let [slide? false]
    [(at-keyframes :pulse-previous-re-frame-10x
                   [:from (merge {:color "white"}
                                 (when slide? :left "-100%"))]
                   [:to (when slide? {:left  "0%"})])
     (at-keyframes :pulse-next-re-frame-10x
                   [:from (merge {:color "white"}
                                 (when slide? {:left "100%"}))]
                   [:to (when slide? {:left "0%"})])
     (at-keyframes :fade-clipboard-msg-re-frame-10x
                   [:0% {:margin-left "100px"}]
                   [:5% {:margin-left "0px"
                         :opacity     "1"}]
                   [:90% {:opacity "1"}])]))

(defn inject-popup-style!
  [document id css]
  (let [head (.-head document)
        element (doto (js/document.createElement "style")
                  (.setAttribute "spade-id" (str id)))]
    (.appendChild head element)
    (set! (.-innerHTML element) css)))

(defn inject-popup-styles!
  [document]
  ;; [IJ] TODO: run! might be more appropriate for side effects...
  (reduce-kv
    (fn [_ _ {:keys [id source] :as f}]
      (inject-popup-style! document id source))
    nil
    @spade.runtime/*injected*)
  ;; [IJ] TODO: injected new/state update styles for popups:
  (add-watch spade.runtime/*injected* :injected
             (fn [k a old-state new-state])))
