(ns day8.re-frame-10x.view.event
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.view.components :as components]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :as units]
            [day8.re-frame-10x.inlined-deps.reagent.v0v10v0.reagent.core :as reagent]
            [day8.re-frame-10x.inlined-deps.reagent.v0v10v0.reagent.dom :as rdom]
            [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
            [zprint.core :as zp]
            [goog.string]
            [clojure.string :as str]
            [day8.re-frame-10x.utils.pretty-print-condensed :as pp]
            [day8.re-frame-10x.utils.utils :as utils])
  (:require-macros [day8.re-frame-10x.utils.macros :refer [with-cljs-devtools-prefs]]
                   [day8.re-frame-10x.utils.re-com :refer [handler-fn]]))

(def code-border (str "1px solid " common/white-background-border-color))


(def event-styles
  [:#--re-frame-10x--
   [:.event-panel
    {:padding "19px 19px 0px 0px"}]
   [:.bold {:font-weight "bold"}]
   [:.event-section]
   [:.event-section--header
    {:background-color common/navbar-tint-lighter
     :color            common/navbar-text-color
     :height           common/gs-19
     :padding          [[0 common/gs-12]]
     :overflow         "hidden"}]
   [:.event-section--data
    {:background-color "rgba(100, 255, 100, 0.08)"
     :padding-left     (units/px- common/gs-12 common/expansion-button-horizontal-padding)
     :overflow-x       "auto"
     :overflow-y       "hidden"}]])



;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function


(defn no-event-instructions
  []
  [rc/v-box
   :children [[rc/p {:style {:font-style "italic"}} "Code trace is not available for this event"]
              [:br]
              [rc/hyperlink-href
               :label "Instructions for enabling Event Code Tracing"
               :attr {:rel "noopener noreferrer"}
               :target "_blank"
               :href "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/EventCodeTracing.md"]]])


(defn code-header
  [code-execution-id line]
  (let [open?-path       [@(rf/subscribe [:epochs/current-epoch-id]) code-execution-id (:id line)]
        max-column-width @(rf/subscribe [:code/max-column-width])
        trace-id         code-execution-id
        open?            (get-in @(rf/subscribe [:code/code-open?]) open?-path)
        line-str         (pp/pr-str-truncated max-column-width (:form line))
        =>str            "=> "
        result-length    (- max-column-width (count =>str) (count line-str))]
    [rc/h-box
     :class "code-fragment__content"
     :size "1"
     :align :center
     :style {:border   code-border
             :overflow "hidden"
             :padding  "0px 6px"}
     :children [[rc/box
                 :width "17px"
                 :height "17px"
                 :class "noselect"
                 :style {:cursor "pointer"
                         :color  "#b0b2b4"}
                 :attr {:on-click (handler-fn (rf/dispatch [:code/set-code-visibility open?-path (not open?)]))}
                 :child [rc/box
                         :margin "auto"
                         :child [:span.arrow (if open? "▼" "▶")]]]
                [rc/h-box
                 :size "1"
                 :style {:overflow "hidden"}
                 :children [[rc/box
                             :style {:margin-left "2px"
                                     :white-space "nowrap"}
                             :child [:code line-str]]
                            [rc/box
                             :class "code-fragment__result"
                             :style {:flex        "1"
                                     :margin-left "8px"
                                     :white-space "nowrap"}
                             :child [:code =>str (when (pos? result-length)
                                                   (pp/pr-str-truncated result-length (:result line)))]]]]
                [rc/box
                 :class "code-fragment__button"
                 :attr {:title    "Copy to the clipboard, an expression that will return this form's value in the cljs repl"
                        :on-click (handler-fn (do (utils/copy-to-clipboard (pr-str (list 'day8.re-frame-10x/traced-result trace-id (:id line))))
                                                  (rf/dispatch [:code/repl-msg-state :start])))}
                 :child "repl"]]]))


(defn code-block
  [code-execution-id line]
  [rc/box
   :size "1"
   :style {:background-color "rgba(100, 255, 100, 0.08)"
           :border           code-border
           :margin-top       "-1px"
           :overflow-x       "auto"
           :overflow-y       "hidden"
           :padding          "0px 3px"}
   :child [components/simple-render (:result line) [@(rf/subscribe [:epochs/current-epoch-id]) code-execution-id (:id line)]]])

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
                    (= (count s') i-s') (conj v (count s)) ;; we have reached the end of both strings
                    (= (nth s i-s) (nth s' i-s')) 
                       (recur (conj v i-s) (inc i-s) (inc i-s')) ;; when we have a match save the index
                    :else (recur v (inc i-s) i-s')))]    ;; no match (whitespace) increment the index on the orignal string
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
                              (str/index-of (pr-str search-str))
                              (nth reindex))
              length (if (some? start)
                       (count (pr-str search-str))
                       1)
              end    (some->> start
                              (+ length)
                              (nth reindex))]
          [start end])))))

(defn event-expression
  []
  (let [scroll-pos (atom {:top 0 :left 0})]
    (reagent/create-class
      {:component-will-update
       (fn event-expression-component-will-update [this]
         (let [node (rdom/dom-node this)]
           (reset! scroll-pos {:top (.-scrollTop node) :left (.-scrollLeft node)})))

       :component-did-update
       (fn event-expression-component-did-update [this]
         (let [node (rdom/dom-node this)]
           (set! (.-scrollTop node) (:top @scroll-pos))
           (set! (.-scrollLeft node) (:left @scroll-pos))))

       :display-name
       "event-expression"

       :reagent-render
       (fn
         []
         (let [highlighted-form @(rf/subscribe [:code/highlighted-form])
               form-str         @(rf/subscribe [:code/current-zprint-form])
               show-all-code?   @(rf/subscribe [:code/show-all-code?])
               [start-index end-index] (find-bounds form-str (:form highlighted-form) (:num-seen highlighted-form))
               before           (subs form-str 0 start-index)
               highlight        (subs form-str start-index end-index)
               after            (subs form-str end-index)]
           ; DC: We get lots of React errors if we don't force a creation of a new element when the highlight changes. Not really sure why...
           ;; Possibly relevant? https://stackoverflow.com/questions/21926083/failed-to-execute-removechild-on-node
           ^{:key (gensym)}
           [rc/box
            :style {:max-height       (when-not show-all-code? (str (* 10 17) "px")) ;; Add scrollbar after 10 lines
                    :overflow         "auto"
                    :border           code-border
                    :background-color common/white-background-color
                    :white-space      "pre"} ;; TODO: This is a quick fix for issue #270
            :attr {:on-double-click (handler-fn (rf/dispatch [:code/set-show-all-code? (not show-all-code?)]))}
            :child (if (some? highlighted-form)
                     [components/highlight {:language "clojure"}
                      (list ^{:key "before"} before
                            ^{:key "hl"} [:span.code-listing--highlighted highlight]
                            ^{:key "after"} after)]
                     [components/highlight {:language "clojure"}
                      form-str])]))})))


(defn repl-msg-area
  []
  (let [repl-msg-state @(rf/subscribe [:code/repl-msg-state])]
    (when (get #{:running :re-running} repl-msg-state)
      ^{:key (gensym)}
      [:div
       {:style            {:opacity            "0"
                           :color              "white"
                           :background-color   "green"
                           :padding            "0px 4px"
                           :white-space        "nowrap"
                           :overflow           "hidden"
                           :animation-duration "5000ms"
                           :margin-right       "5px"
                           :animation-name     "fade-clipboard-msg-re-frame-10x"}
        :on-animation-end #(rf/dispatch [:code/repl-msg-state :end])}
       "Clipboard now contains text for pasting into the REPL"])))


(defn repl-section
  []
  (let [execution-order?    @(rf/subscribe [:code/execution-order?])]
   [rc/h-box
    :height "23px"
    :align :end
    :style {:margin-bottom "2px"}
    :children [[rc/checkbox
                :model execution-order?
                :label "show trace in execution order"
                :on-change (handler-fn (rf/dispatch [:code/set-execution-order (not execution-order?)]))] 
               [rc/box
                :size "1"
                :child ""]
               [repl-msg-area]
               [rc/box
                :size "1"
                :child ""]
               [rc/hyperlink
                :label "repl requires"
                :style {:margin-right common/gs-7s}
                :attr {:title "Copy to the clipboard, the require form to set things up for the \"repl\" links below"}
               ;; Doing this in a list would be nicer, but doesn't let us use ' as it will be expanded before we can create the string.
                :on-click #(do (utils/copy-to-clipboard "(require '[day8.re-frame-10x])")
                               (rf/dispatch [:code/repl-msg-state :start]))]
               [rc/hyperlink-info "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UsingTheRepl.md"]]]))


(defn indent-block
  [indent-level first?]
  [rc/h-box
   :children (doall
               (for [num (range indent-level)]
                 [rc/box
                  :width "12px"
                  :style {:background-color common/standard-background-color
                          :border-top       (when first? code-border)
                          :border-left      code-border}
                  :child ""]))])


(defn event-fragments
  [unordered-fragments code-exec-id]
  (let [code-open?       @(rf/subscribe [:code/code-open?])
        max-frags        50
        execution-order? @(rf/subscribe [:code/execution-order?])
        fragments        (if execution-order?
                           unordered-fragments 
                           (sort-by :syntax-order unordered-fragments))]
    [rc/v-box
     :size "1"
     :style {:overflow-y "auto"}
     :children [(doall
                  (for [frag (take max-frags fragments)]
                    (let [id     (:id frag)
                          first? (zero? id)]
                      ^{:key id}
                      [rc/v-box
                       :class "code-fragment"
                       :style {:margin-top (when-not first? "-1px")}
                       :attr {:on-mouse-enter (handler-fn (rf/dispatch [:code/hover-form frag]))
                              :on-mouse-leave (handler-fn (rf/dispatch [:code/exit-hover-form frag]))}
                       :children [[rc/h-box
                                   :children [[indent-block (:indent-level frag) first?]
                                              [code-header code-exec-id frag]]]
                                  (when (get-in code-open? [@(rf/subscribe [:epochs/current-epoch-id]) code-exec-id id])
                                    [rc/h-box
                                     :children [[indent-block (:indent-level frag) false]
                                                [code-block code-exec-id frag id]]])]])))
                (when (> (count fragments) max-frags)
                  [rc/label
                   :style {:margin-top common/gs-5s
                           :font-style "italic"}
                   :label (str "(only showing first " max-frags " of " (count fragments) " traces)")])]]))


(defn event-code
  []
  (let [code-traces    @(rf/subscribe [:code/current-code])
        code-execution (first code-traces)                  ;; Ignore multiple code executions for now
        #_#_debug? @(rf/subscribe [:settings/debug?])
        #_#_highlighted-form (rf/subscribe [:code/highlighted-form])]
    (if-not code-execution
      [no-event-instructions]
      [rc/v-box
       :size "1 1 auto"
       :class "code-panel"
       :children [#_(when debug? [:pre "Hover " (subs (pr-str @highlighted-form) 0 50) "\n"])
                  [event-expression]
                  [repl-section]
                  [event-fragments (->> (:code code-execution)
                                        (remove (fn [line] (fn? (:result line)))))
                   (:trace-id code-execution)]]])))


(defn render []
  (let [epoch-id @(rf/subscribe [:epochs/current-epoch-id])]
    ;; Create a new id on each panel because Reagent can throw an exception if
    ;; the data provided in successive renders is sufficiently different.
    ^{:key epoch-id}
    [rc/v-box
     :size "1"
     :class "event-panel"
     :gap common/gs-19s
     :children [[event-code]
                [rc/gap-f :size "0px"]]]))
