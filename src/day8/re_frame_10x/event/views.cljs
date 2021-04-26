(ns day8.re-frame-10x.event.views
  (:require-macros
    [day8.re-frame-10x.utils.re-com :refer [handler-fn]])
  (:require
    [clojure.string :as string]
    [re-highlight.core :as re-highlight]
    ["highlight.js/lib/languages/clojure"]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px ms]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom :as rdom]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.components :as components]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.utils.utils :as utils]
    [day8.re-frame-10x.event.subs :as event.subs]
    [day8.re-frame-10x.event.events :as event.events]
    [day8.re-frame-10x.view.cljs-devtools :as cljs-devtools]
    [day8.re-frame-10x.utils.pretty-print-condensed :as pp]))

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defn- re-seq-idx
  "Like re-seq but returns matches and indices"
  ([re s] (re-seq-idx re s 0))
  ([re s offset]  ;; copied from re-seq* impl https://github.com/clojure/clojurescript/blob/0efe8fede9e06b8e1aa2fcb3a1c70f66cad6392e/src/main/cljs/cljs/core.cljs#L10014
   (when-some [matches (.exec re s)]
     (let [match-str (aget matches 0)
           match-vals (if (== (.-length matches) 1)
                        match-str
                        (vec matches))
           match-index (.-index matches)]
       (cons [match-vals, (+ offset match-index)]
             (lazy-seq
               (let [post-idx (+ (.-index matches)
                                 (max 1 (.-length match-str)))]
                 (when (<= post-idx (.-length s))
                   (re-seq-idx re (subs s post-idx) (+ offset post-idx))))))))))

(defn collapse-whitespace-and-index
  "given a string argument `s` it will return a vector of two values:
     - a modified version of `s`, call it s'
     - a vector of indexes, v
   s' will be a copy of s in which all consecutive whitespace is collapsed to one whitespace
   v  will be a vector of index for characters in s' back to the original s
   For example:
      (collapse-whitespace-and-index \"a b  c\")
   will return
       [\"a b c\" [0 1 2 3 5]]     ;; notice that the 4 is not there
   "
  [s]
  (let [s' (clojure.string/replace s #"\s+" " ") ;; generate a new string with whitespace replaced
        v (loop [v []     ;; Build up an index between the string with and without whitespace
                 i-s 0
                 i-s' 0]
            (cond
              (= (count s') i-s')           (conj v (count s)) ;; we have reached the end of both strings
              (= (nth s i-s) (nth s' i-s')) (recur (conj v i-s) (inc i-s) (inc i-s')) ;; when we have a match save the index
              :else                         (recur v (inc i-s) i-s')))]    ;; no match (whitespace) increment the index on the orignal string
    [s' v]))

(defn find-bounds
  "Try and find the bounds of the form we are searching for. Uses some heuristics to
  try and avoid matching partial forms, e.g. 'default-|weeks| for the form 'weeks."
  [form-str search-str num-seen]
  (if (nil? search-str)
    [0 0]  ;; on mouse out etc
    (let [[form-str reindex]   (collapse-whitespace-and-index form-str) ;; match without whitespace
          esc-str    (goog.string.regExpEscape search-str)
          regex      (str "(\\s|\\(|\\[|\\{)" "(" esc-str ")(\\s|\\)|\\]|\\})")
          re         (re-pattern regex)
          results    (re-seq-idx re form-str)]
      ;; (js/console.log "FIND-BOUNDS" form-str  regex reindex results)
      (if (and search-str num-seen (seq results) (>= (count results)  num-seen))
        (let [result                              (nth results (dec num-seen))
              [[_ pre-match matched-form] index]  result
              index                               (+ index (count pre-match))
              start                               (nth reindex index)
              stop                                (nth reindex (+ index (count matched-form)))]
          [start stop])
        ;; If the regex fails, fall back to string index just in case.
        (let [start  (some->> form-str
                              (string/index-of (pr-str search-str))
                              (nth reindex))
              length (if (some? start)
                       (count (pr-str search-str))
                       1)
              end    (some->> start
                              (+ length)
                              (nth reindex))]
          [start end])))))

(defclass code-style
  [ambiance syntax-color-scheme show-all-code?]
  {:composes      (styles/hljs ambiance syntax-color-scheme)
   :max-height    (when-not show-all-code? (px (* 10 17)))  ;; Add scrollbar after 10 lines
   :overflow      :auto
   :white-space   :pre}) ;; TODO: This is a quick fix for issue #270

(defn code
  []
  (let [scroll-pos (atom {:top 0 :left 0})]
    (r/create-class
      {:display-name "code"

       :get-snapshot-before-update
                     (fn code-get-snapshot-before-update
                       [this old-argv new-argv]
                       (let [node (rdom/dom-node this)]
                         (reset! scroll-pos {:top (.-scrollTop node) :left (.-scrollLeft node)})))

       :component-did-update
                     (fn code-component-did-update
                       [this]
                       (let [node (rdom/dom-node this)]
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
                             [start-index end-index] (find-bounds form-str (:form highlighted-form) (:num-seen highlighted-form))
                             before              (subs form-str 0 start-index)
                             highlight           (subs form-str start-index end-index)
                             after               (subs form-str end-index)]
                         ; DC: We get lots of React errors if we don't force a creation of a new element when the highlight changes. Not really sure why...
                         ;; Possibly relevant? https://stackoverflow.com/questions/21926083/failed-to-execute-removechild-on-node
                         ^{:key (gensym)}
                         [rc/box
                          :class (code-style ambiance syntax-color-scheme show-all-code?)
                          :attr {:on-double-click (handler-fn (rf/dispatch [::event.events/set-show-all-code? (not show-all-code?)]))}
                          :child (if (some? highlighted-form)
                                   [re-highlight/highlight {:language "clojure"}
                                    (list ^{:key "before"} before
                                          ^{:key "hl"} [:span.code-listing--highlighted highlight]
                                          ^{:key "after"} after)]
                                   [re-highlight/highlight {:language "clojure"}
                                    form-str])]))})))

(defclass clipboard-notification-style
  [ambiance]
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

(defn controls
  []
  (let [execution-order? @(rf/subscribe [::event.subs/execution-order?])]
    [rc/h-box
     :children
     [[components/checkbox
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
      [components/icon-button
       {:icon     [material/content-copy]
        :label    "requires"
        :title    "Copy to the clipboard, the require form to set things up for the \"repl\" links below"
        ;; Doing this in a list would be nicer, but doesn't let us use ' as it will be expanded before we can create the string.
        :on-click #(do (utils/copy-to-clipboard "(require '[day8.re-frame-10x])")
                       (rf/dispatch [::event.events/repl-msg-state :start]))}]
      [rc/gap-f :size styles/gs-7s]
      [components/hyperlink-info "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UsingTheRepl.md"]]]))

(defclass indent-block-style
  [ambiance first?]
  (merge
    {:composes (styles/colors-1 ambiance)
     :border-left [[(px 1) :solid (if (= :bright ambiance) styles/nord4 styles/nord3)]]}
    (when first?
      {:border-top [[(px 1) :solid (if (= :bright ambiance) styles/nord4 styles/nord3)]]})))

(defn indent-block
  [indent-level first?]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :children
     (into []
           (for [i (range indent-level)]
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
       [components/expansion-button
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
      [components/icon-button
       {:icon     [material/content-copy {:size "14px"}]
        :title    "Copy to the clipboard, an expression that will return this form's value in the cljs repl"
        :on-click (handler-fn (do (utils/copy-to-clipboard (pr-str (list 'day8.re-frame-10x/traced-result trace-id frag)))
                                  (rf/dispatch [::event.events/repl-msg-state :start])))}]]]))

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
     [cljs-devtools/simple-render result [@(rf/subscribe [::epochs.subs/selected-epoch-id]) trace-id id]]]))

(defclass fragment-style
  [ambiance]
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

(defn fragments-style
  [ambiance]
  {:overflow-y :auto})

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

