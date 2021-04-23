(ns day8.re-frame-10x.event.views
  (:require-macros
    [day8.re-frame-10x.utils.re-com :refer [handler-fn]])
  (:require
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom :as rdom]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [re-highlight.core :as re-highlight]
    ["highlight.js/lib/languages/clojure"]
    [clojure.string :as string]))

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
               highlighted-form    @(rf/subscribe [:code/highlighted-form])
               form-str            @(rf/subscribe [:code/current-zprint-form])
               show-all-code?      @(rf/subscribe [:code/show-all-code?])
               [start-index end-index] (find-bounds form-str (:form highlighted-form) (:num-seen highlighted-form))
               before              (subs form-str 0 start-index)
               highlight           (subs form-str start-index end-index)
               after               (subs form-str end-index)]
           ; DC: We get lots of React errors if we don't force a creation of a new element when the highlight changes. Not really sure why...
           ;; Possibly relevant? https://stackoverflow.com/questions/21926083/failed-to-execute-removechild-on-node
           ^{:key (gensym)}
           [rc/box
            :class (code-style ambiance syntax-color-scheme show-all-code?)
            :attr  {:on-double-click (handler-fn (rf/dispatch [:code/set-show-all-code? (not show-all-code?)]))}
            :child (if (some? highlighted-form)
                     [re-highlight/highlight {:language "clojure"}
                      (list ^{:key "before"} before
                            ^{:key "hl"} [:span.code-listing--highlighted highlight]
                            ^{:key "after"} after)]
                     [re-highlight/highlight {:language "clojure"}
                      form-str])]))})))

(defn panel
  []
  (let [epoch-id @(rf/subscribe [::epochs.subs/selected-epoch-id])]
    ;; Create a new id on each panel because Reagent can throw an exception if
    ;; the data provided in successive renders is sufficiently different.
    ^{:key epoch-id}
    [rc/v-box
     :size     "1"
     :gap      styles/gs-19s
     :children
     [[code]]]))
