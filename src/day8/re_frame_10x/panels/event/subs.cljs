(ns day8.re-frame-10x.panels.event.subs
  (:require
   [zprint.core                                                  :as zp]
   [clojure.string                                               :as string]
   [goog.string                                                  :as gstring]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
   [day8.re-frame-10x.panels.settings.subs                              :as settings.subs]
   [day8.re-frame-10x.panels.traces.subs                                :as traces.subs]))

(rf/reg-sub
 ::root
 (fn [{:keys [code]} _]
   code))

(rf/reg-sub
 ::code-for-epoch
 :<- [::traces.subs/filtered-by-epoch]
 (fn [traces _]
   (->> traces
        (keep-indexed
         (fn [i trace]
           (when-some [code (get-in trace [:tags :code])]
             {:id       i
              :trace-id (:id trace)
              :title    (pr-str (:op-type trace))
              :code     (->> code (map-indexed (fn [i code] (assoc code :id i))) vec) ;; Add index
              :form     (get-in trace [:tags :form])})))
        (first)))) ;; Ignore multiple code executions for now

(rf/reg-sub
 ::code-for-epoch-exists?
 :<- [::code-for-epoch]
 (fn [code _]
   (boolean code)))

(rf/reg-sub
 ::fragments-for-epoch
 :<- [::code-for-epoch]
 :<- [::execution-order?]
 (fn [[{:keys [code]} execution-order?] _]
   (let [unordered-fragments (remove (fn [line] (fn? (:result line))) code)]
     (if execution-order?
       unordered-fragments
       (sort-by :syntax-order unordered-fragments)))))

(rf/reg-sub
 ::trace-id-for-epoch
 :<- [::code-for-epoch]
 (fn [{:keys [trace-id]} _]
   trace-id))

(rf/reg-sub
 ::form-for-epoch
 :<- [::code-for-epoch]
 (fn [{:keys [form]} _]
   form))

(rf/reg-sub
 ::zprint-form-for-epoch
 :<- [::form-for-epoch]
 (fn [form _]
   (zp/zprint-str form)))

(rf/reg-sub
 ::execution-order?
 :<- [::root]
 (fn [code _]
   (get code :execution-order? true)))

(rf/reg-sub
 ::code-open?
 :<- [::root]
 (fn [{:keys [code-open?]} _]
   code-open?))

(rf/reg-sub
 ::highlighted-form
 :<- [::root]
 (fn [{:keys [highlighted-form]} _]
   highlighted-form))

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
          esc-str    (gstring/regExpEscape search-str)
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

(rf/reg-sub
 ::highlighted-form-bounds
 :<- [::highlighted-form]
 :<- [::form-for-epoch]
 (fn [[highlighted-form form] _]
   (find-bounds (str form)
                (:form highlighted-form)
                (:num-seen highlighted-form))))

(rf/reg-sub
 ::show-all-code?
 :<- [::root]
 (fn [{:keys [show-all-code?]} _]
   show-all-code?))

(rf/reg-sub
 ::repl-msg-state
 :<- [::root]
 (fn [{:keys [repl-msg-state]} _]
   repl-msg-state))

;; [IJ] TODO: This should not be a subscription:
(def canvas (js/document.createElement "canvas"))

(rf/reg-sub
 ::single-character-width
 (fn [_ _]
   (let [context (.getContext canvas "2d")]
     (set! (.-font context) "monospace 1em")
     (.-width (.measureText context "T")))))

(rf/reg-sub
 ::max-column-width
 :<- [::settings.subs/window-width-rounded 100]
 :<- [::single-character-width]
  ;; It seems like it would be possible to do something smarter responding to panel sizing,
  ;; but that introduces a lot of jank, so we just set to maximum possible window width.
 (fn [[window-width char-width] _]
   (Math/ceil (/ window-width
                 char-width))))