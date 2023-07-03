(ns day8.re-frame-10x.panels.event.views
  (:require-macros
   [day8.re-frame-10x.components.re-com                          :refer [handler-fn]])
  (:require
   ["react"                                                      :as react]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px ms]]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core       :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.core   :as r]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.components.buttons                         :as buttons]
   [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
   [day8.re-frame-10x.components.hyperlinks                      :as hyperlinks]
   [day8.re-frame-10x.components.inputs                          :as inputs]
   [day8.re-frame-10x.components.re-com                          :as rc]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
   [day8.re-frame-10x.panels.event.events                        :as event.events]
   [day8.re-frame-10x.panels.event.subs                          :as event.subs]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
   [day8.re-frame-10x.fx.clipboard                               :as clipboard]
   [day8.re-frame-10x.tools.pretty-print-condensed               :as pp]
   [day8.re-frame-10x.tools.highlight-hiccup                     :refer [str->hiccup]]))

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defclass code-style
  [ambiance syntax-color-scheme show-all-code?]
  {:composes      (styles/hljs ambiance syntax-color-scheme)
   :max-height    (when-not show-all-code? (px (* 10 17)))  ;; Add scrollbar after 10 lines
   :padding       styles/gs-5
   :overflow      :auto
   :white-space   :pre
   :margin-right  styles/gs-5}) ;; TODO: This is a quick fix for issue #270

(defclass hljs-error-style
  [ambiance syntax-color-scheme]
  {:background-color styles/nord11
   :color styles/nord4
   :border-radius "3px"
   :padding styles/gs-12s
   :margin-right styles/gs-31s}
  [:p
   {:margin-top "0px"}])

(defn code
  []
  (let [scroll-pos (atom {:top 0 :left 0})
        ref        (react/createRef)]
    (r/create-class
     {:display-name "code"

      :get-snapshot-before-update
      (fn code-get-snapshot-before-update
        [this _ _]
        (let [node (.-current ref)]
          (reset! scroll-pos {:top (.-scrollTop node) :left (.-scrollLeft node)})))

      :component-did-update
      (fn code-component-did-update
        [this]
        (let [node (.-current ref)]
          (set! (.-scrollTop node) (:top @scroll-pos))
          (set! (.-scrollLeft node) (:left @scroll-pos))))

      :reagent-render
      (fn
        []
        (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
              syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])
              highlighted-form    @(rf/subscribe [::event.subs/highlighted-form])
              form-str            @(rf/subscribe [::event.subs/zprint-form-for-epoch])
              show-all-code?      @(rf/subscribe [::event.subs/show-all-code?])
              [start-idx end-idx] @(rf/subscribe [::event.subs/highlighted-form-bounds])
              before              (subs form-str 0 start-idx)
              highlight           (subs form-str start-idx end-idx)
              after               (subs form-str end-idx)]
          ^{:key (gensym)}
          [rc/box
           :class (code-style ambiance syntax-color-scheme show-all-code?)
           :attr  {:ref             ref
                   :on-double-click (handler-fn (rf/dispatch [::event.events/set-show-all-code? (not show-all-code?)]))}
           :child [str->hiccup form-str]]))})))

(defclass clipboard-notification-style
  [_]
  {:opacity            0
   :color              styles/nord6
   :background-color   styles/nord12
   :padding            [[0 styles/gs-5]]
   :white-space        :nowrap
   :overflow           :hidden
   :animation-duration (ms 5000)
   :animation-name     :fade-clipboard-msg-re-frame-10x})

(defn clipboard-notification
  []
  (let [ambiance       @(rf/subscribe [::settings.subs/ambiance])
        repl-msg-state @(rf/subscribe [::event.subs/repl-msg-state])]
    (when (get #{:running :re-running} repl-msg-state)
      [:div
       {:class            (clipboard-notification-style ambiance)
        :on-animation-end #(rf/dispatch [::event.events/repl-msg-state :end])}
       "Clipboard now contains text for pasting into your REPL"])))

(defclass copy-button-style
  [ambiance]
  {:background-color (if (= :bright ambiance) styles/nord-ghost-white styles/nord1)
   :border-top :none
   :border-bottom :none
   :border-right :none})

(defclass controls-style
  [_]
  {:margin-right styles/gs-5})

(defn controls
  []
  (let [ambiance         @(rf/subscribe [::settings.subs/ambiance])
        execution-order? @(rf/subscribe [::event.subs/execution-order?])]
    [rc/h-box
     :class    (controls-style ambiance)
     :align    :center
     :children
     [[inputs/checkbox
       {:model     execution-order?
        :on-change (handler-fn (rf/dispatch [::event.events/set-execution-order (not execution-order?)]))
        :label     "show trace in execution order"}]
      [rc/box
       :size  "1"
       :child ""]
      [clipboard-notification]
      [rc/box
       :size  "1"
       :child ""]
      [buttons/icon
       {:icon     [material/content-copy]
        :label    "requires"
        :title    "Copy to the clipboard, the require form to set things up for the \"repl\" links below"
        ;; Doing this in a list would be nicer, but doesn't let us use ' as it will be expanded before we can create the string.
        :on-click #(do (clipboard/copy! "(require '[day8.re-frame-10x])")
                       (rf/dispatch [::event.events/repl-msg-state :start]))}]
      [rc/gap-f :size styles/gs-7s]
      [hyperlinks/info "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UsingTheRepl.md"]]]))

(defclass indent-block-style
  [ambiance first?]
  {:composes    (styles/colors-2 ambiance)
   :border-left [[(px 1) :solid (if (= :bright ambiance) styles/nord4 styles/nord3)]]
   :border-top  (when first? [[(px 1) :solid (if (= :bright ambiance) styles/nord4 styles/nord3)]])})

(defn indent-block
  [indent-level first?]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :children
     (into []
           (for [_ (range indent-level)]
             [rc/box
              :width styles/gs-12s
              :class (indent-block-style ambiance first?)
              :child ""]))]))

(defclass fragment-header-style
  [ambiance first?]
  {:composes   (styles/frame-1 ambiance)
   :height     styles/gs-19
   :margin-top (when-not first? (px -1))}
  [:.code
   {:margin-left styles/gs-2
    :white-space :nowrap}]
  [:.result
   {:color       styles/nord10
    :flex        "1"
    :margin-left styles/gs-7
    :overflow    :hidden
    :white-space :nowrap
    :visibility  :hidden}]
  [:&:hover
   {:background-color (if (= :bright ambiance) styles/nord6 styles/nord2)}
   [:.result
    {:visibility :visible}]])

(defn fragment-header
  [{:keys [id form result] :as frag}]
  (let [ambiance         @(rf/subscribe [::settings.subs/ambiance])
        trace-id         @(rf/subscribe [::event.subs/trace-id-for-epoch])
        open?-path       [@(rf/subscribe [::epochs.subs/selected-epoch-id]) trace-id id]
        max-column-width @(rf/subscribe [::event.subs/max-column-width])
        open?            (get-in @(rf/subscribe [::event.subs/code-open?]) open?-path)
        line-str         (pp/pr-str-truncated max-column-width form)
        =>str            "=> "
        result-length    (- max-column-width (count =>str) (count line-str))
        first?           (zero? id)]
    [rc/h-box
     :class    (fragment-header-style ambiance first?)
     :size     "1"
     :align    :center
     :children
     [[rc/box
       :attr  {:on-click (handler-fn (rf/dispatch [::event.events/set-code-visibility open?-path (not open?)]))}
       :child
       [buttons/expansion
        {:open? open?
         :size  styles/gs-19s}]]
      [rc/h-box
       :size     "1"
       :children
       [[rc/box
         :class "code"
         :child
         [:code line-str]]
        [rc/box
         :class "result"
         :child
         [:code =>str (when (pos? result-length)
                        (pp/pr-str-truncated result-length result))]]]]
      [rc/box
       :width styles/gs-19s
       :child
       [buttons/icon
        {:class    (copy-button-style ambiance)
         :icon     [material/content-copy {:size "14px"}]
         :title    "Copy to the clipboard, an expression that will return this form's value in the cljs repl"
         :on-click (handler-fn (do (clipboard/copy! (pr-str (list 'day8.re-frame-10x/traced-result trace-id id)))
                                   (rf/dispatch [::event.events/repl-msg-state :start])))}]]]]))

(defclass fragment-body-style
  [ambiance syntax-color-scheme]
  {:composes   (styles/code ambiance syntax-color-scheme)
   :overflow-x :auto
   :overflow-y :hidden
   :padding    styles/gs-5})

(defn fragment-body
  [{:keys [id result]}]
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])
        trace-id            @(rf/subscribe [::event.subs/trace-id-for-epoch])]
    [rc/box
     :size  "1"
     :class (fragment-body-style ambiance syntax-color-scheme)
     :child
     [cljs-devtools/simple-render
      result
      [@(rf/subscribe [::epochs.subs/selected-epoch-id]) trace-id id]]]))

(defclass fragment-style
  [_]
  {})

(defn fragment
  [{:keys [id indent-level] :as frag}]
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        code-open? @(rf/subscribe [::event.subs/code-open?])
        trace-id   @(rf/subscribe [::event.subs/trace-id-for-epoch])
        first?     (zero? id)]
    [rc/v-box
     :class (fragment-style ambiance)
     :attr  {:on-mouse-enter (handler-fn (rf/dispatch [::event.events/hover-form frag]))
             :on-mouse-leave (handler-fn (rf/dispatch [::event.events/exit-hover-form frag]))}
     :children
     [[rc/h-box
       :children
       [[indent-block indent-level first?]
        [fragment-header frag]]]
      (when (get-in code-open? [@(rf/subscribe [::epochs.subs/selected-epoch-id]) trace-id id])
        [rc/h-box
         :children
         [[indent-block indent-level false]
          [fragment-body frag]]])]]))

(defclass fragments-style
  [_]
  {:overflow-y :auto
   :margin-right styles/gs-5})

(defn fragments
  []
  (let [ambiance      @(rf/subscribe [::settings.subs/ambiance])
        fragments     @(rf/subscribe [::event.subs/fragments-for-epoch])
        max-fragments 50]
    [rc/v-box
     :size     "1"
     :class    (fragments-style ambiance)
     :children
     [(into [:<>]
            (for [frag (take max-fragments fragments)]
              [fragment frag]))
      (when (> (count fragments) max-fragments)
        [rc/label
         :label (str "(only showing first " max-fragments " of " (count fragments) " traces)")])]]))

(defn instructions
  []
  [rc/v-box
   :children
   [[rc/p "Code trace is not available for this event"]
    [:br]
    [rc/hyperlink-href
     :label  "Instructions for enabling Event Code Tracing"
     :attr   {:rel "noopener noreferrer"}
     :target "_blank"
     :href   "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/EventCodeTracing.md"]]])

(defn panel
  []
  (let [epoch-id @(rf/subscribe [::epochs.subs/selected-epoch-id])
        exists?  @(rf/subscribe [::event.subs/code-for-epoch-exists?])]
    ;; Create a new id on each panel because Reagent can throw an exception if
    ;; the data provided in successive renders is sufficiently different.
    ^{:key epoch-id}
    [rc/v-box
     :size     "1"
     :gap      styles/gs-19s
     :children
     (if-not exists?
       [[instructions]]
       [[code]
        [controls]
        [fragments]])]))
