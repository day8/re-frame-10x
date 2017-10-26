(ns day8.re-frame.trace.data-previews
  (:require [clojure.string :as str]))


(defn ^string truncate-string
  "Truncate a string to length `n`.

  Removal occurs at `cut-from`, which may be :start, :end, or :middle.

  Truncation is indicated by `…` at start/end, or `...` at middle, for readability. "
  ([n string]
   (n :end string))
  ([n cut-from string]
   (let [c (count string)]
     (if (> c n)
       (case cut-from
         :start (str "…" (subs string (- c (dec n)) c))
         :end (str (subs string 0 (dec n)) "…")
         :middle (case n
                   1 "…"
                   2 (truncate-string n :start string)
                   3 (str (subs string 0 1) "…" (subs string (dec c) c))
                   (let [content-budget (- n 2)
                         per-side-budget (-> content-budget
                                             (/ 2)
                                             (js/Math.floor))]
                     ;; 100 - 9 = 91 / 2 = 45
                     ;; subs string 0
                     (str (subs string 0 (cond-> per-side-budget
                                                 (even? content-budget)
                                                 (dec)))
                          "..."
                          (subs string (- c per-side-budget) c)))))
       string))))

(comment
  (assert (= (truncate-string 5 :start "123456789") "…6789"))
  (assert (= (truncate-string 5 :end "123456789") "1234…"))

  ;; special case use of … for short :middle-truncated strings
  (assert (= (truncate-string 1 :middle "123456789") "…"))
  (assert (= (truncate-string 2 :middle "123456789") "…9"))
  (assert (= (truncate-string 3 :middle "123456789") "1…9"))

  (assert (= (truncate-string 4 :middle "123456789") "...9"))
  (assert (= (truncate-string 5 :middle "123456789") "1...9"))
  (assert (= (truncate-string 6 :middle "123456789") "1...89"))
  (assert (= (truncate-string 7 :middle "123456789") "12...89"))
  (assert (= (truncate-string 8 :middle "123456789") "12...789")))

(defn ^string segments-within [n segments]
  (loop [segments segments
         length 0
         out '()]
    (let [segment (peek segments)]
      (cond (empty? segments) out
            (<= (+ length (count segment))
                n)
            (recur (pop segments)
                   (+ length (count segment))
                   (cons segment out))
            :else out))))

(defn ^string truncate-named
  [n named]
  (let [the-ns (namespace named)
        the-name (name named)
        kw? (keyword? named)
        ns-prefix-size (if kw? 3 2)]
    (if (or (> (count the-name) (if the-ns (- n ns-prefix-size) n))
            (nil? the-ns))
      (let [prefix (cond-> (if kw? ":" "")
                           the-ns (str "…/"))]
        (str prefix
             (truncate-string (- n (count prefix)) :start the-name)))
      (let [end (str "/" the-name)
            prefix (if kw? ":" "")
            ns-budget (- n (count end) (count prefix))
            ns-string (some->> (segments-within ns-budget (str/split the-ns #"\."))
                               (seq)
                               (str/join "."))]
        (str prefix
             (when-not (= (count ns-string) (count the-ns))
               "…")
             ns-string

             end)))))

(defn ^string truncate [n location param]
  (if (satisfies? INamed param)
    (truncate-named n param)
    (truncate-string n location (str param))))

(comment

  (assert (= (truncate-named 1 :saskatoon) ":…"))
  (assert (= (truncate-named 2 :saskatoon) ":…"))
  (assert (= (truncate-named 3 :saskatoon) ":…n"))
  (assert (= (truncate-named 9 :saskatoon) ":…skatoon"))
  (assert (= (truncate-named 10 :saskatoon) ":saskatoon"))


  (assert (= (truncate-named 1 :city/saskatoon) ":…/…"))
  (assert (= (truncate-named 2 :city/saskatoon) ":…/…"))
  (assert (= (truncate-named 3 :city/saskatoon) ":…/…"))
  (assert (= (truncate-named 4 :city/saskatoon) ":…/…"))
  (assert (= (truncate-named 5 :city/saskatoon) ":…/…n"))
  (assert (= (truncate-named 11 :city/saskatoon) ":…/…skatoon"))
  (assert (= (truncate-named 12 :city/saskatoon) ":…/saskatoon"))
  (assert (= (truncate-named 13 :city/saskatoon) ":…/saskatoon"))
  (assert (= (truncate-named 14 :city/saskatoon) ":…/saskatoon"))
  (assert (= (truncate-named 15 :city/saskatoon) ":city/saskatoon"))
  (assert (= (truncate-named 16 :city/saskatoon) ":city/saskatoon"))

  (assert (= (truncate-named 8 'saskatoon) "…skatoon"))
  (assert (= (truncate-named 9 'saskatoon) "saskatoon"))
  (assert (= (truncate-named 10 'saskatoon) "saskatoon"))

  (assert (= (truncate-named 1 'city/saskatoon) "…/…"))
  (assert (= (truncate-named 2 'city/saskatoon) "…/…"))
  (assert (= (truncate-named 3 'city/saskatoon) "…/…"))
  (assert (= (truncate-named 4 'city/saskatoon) "…/…n"))
  (assert (= (truncate-named 10 'city/saskatoon) "…/…skatoon"))
  (assert (= (truncate-named 11 'city/saskatoon) "…/saskatoon"))
  (assert (= (truncate-named 12 'city/saskatoon) "…/saskatoon"))
  (assert (= (truncate-named 13 'city/saskatoon) "…/saskatoon"))
  (assert (= (truncate-named 14 'city/saskatoon) "city/saskatoon"))
  (assert (= (truncate-named 15 'city/saskatoon) "city/saskatoon")))

(defn str->namespaced-sym [s]
  (if (string? s)
    (let [name (second (re-find #"\.([^.]+)$" s))]
      (if name (symbol (subs s 0 (- (count s) (count name) 1))
                       name)
               (symbol s)))
    s))

(defn edges
  "Return left and right edges of a collection (eg. brackets plus prefixes), defaults to [< >]."
  [coll]
  (cond (map? coll) [\{ \}]
        (vector? coll) [\[ \]]
        (set? coll) ["#{" \}]
        (list? coll) ["(" ")"]
        :else ["<" ">"]))

(defn with-edges
  "Wrap `value` with edges of `coll`"
  [coll value]
  (let [[left right] (edges coll)]
    (str left value right)))

(defn preview-param
  "Render parameters in abbreviated form, showing content only for keywords/strings/symbols and entering vectors to a depth of 1."
  ([param] (preview-param 0 vector? 1 param))
  ([depth enter-pred max-depth param]
   (cond
     (satisfies? INamed param) (truncate-named 16 param)
     (string? param) (truncate-string 16 :middle param)
     (fn? param) (or (some-> (.-name param)
                             (str/replace #"(^.*\$)(.*)" "$2"))
                     "ƒ")
     (and (enter-pred param)
          (< depth max-depth)) (with-edges param
                                           (str/join ", " (mapv (partial preview-param (inc depth) enter-pred max-depth) param)))
     :else (with-edges param "…"))))