(ns ^{:doc    "Utilities for pretty-printing abbreviated Clojure forms"
      :author "Matthew Huebert"}
day8.re-frame-10x.utils.pretty-print-condensed
  (:require [clojure.string :as str]))


(defn ^string truncate-string
  "Truncate a string to length `n`.

  Removal occurs at `cut-from`, which may be :start, :end, or :middle.

  Truncation is indicated by `…` at start/end, or `...` at middle, for readability. "
  ([n string]
   (truncate-string n :end string))
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
                   (let [content-budget  (- n 2)
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

(defn ^string truncate-segments
  ([s limit] (truncate-segments s limit #"^[^.]+\." "…"))
  ([s limit match trunc-prefix]
   (if (<= (count s) limit)
     s
     (let [rep (str/replace s match trunc-prefix)]
       (if (= rep s)
         trunc-prefix
         (recur (str/replace s match trunc-prefix)
                limit
                match
                trunc-prefix))))))


(comment
  (assert (= (truncate-segments "a.bcd" 1) "…"))
  (assert (= (truncate-segments "a.bcd" 2) "…"))
  (assert (= (truncate-segments "a.bcd" 3) "…"))
  (assert (= (truncate-segments "a.bcd" 4) "…bcd"))
  (assert (= (truncate-segments "a.bcd" 5) "a.bcd"))
  (assert (= (truncate-segments "a.bcd" 6) "a.bcd"))


  (assert (= (truncate-segments "a.b.c" 1) "…"))
  (assert (= (truncate-segments "a.b.c" 2) "…c"))
  (assert (= (truncate-segments "a.b.c" 3) "…c"))
  (assert (= (truncate-segments "a.b.c" 4) "…b.c"))
  (assert (= (truncate-segments "a.b.c" 5) "a.b.c"))
  (assert (= (truncate-segments "a.b.c" 6) "a.b.c")))

(defn ^string truncate-named
  "Truncates `named`, which must satisfy INamed protocol, to within `n`
   characters, cutting from beginning. Adds a `…` prefix to indicate where
   cutting has occurred."
  [n named]
  (let [the-ns         (namespace named)
        the-name       (name named)
        kw?            (keyword? named)
        ns-prefix-size (if kw? 3 2)]
    (if (or (> (count the-name) (if the-ns (- n ns-prefix-size) n))
            (nil? the-ns))
      (let [prefix (cond-> (if kw? ":" "")
                           the-ns (str "…/"))]
        (str prefix
             (truncate-string (- n (count prefix)) :start the-name)))
      (let [end       (str "/" the-name)
            prefix    (if kw? ":" "")
            ns-budget (- n (count end) (count prefix))
            ns-string (truncate-segments the-ns ns-budget)]
        (str prefix
             ns-string
             end)))))


(assert (= (truncate-named 12 :city/saskatoon)

           ":…/saskatoon"))

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

(defn ^string truncate [n location param]
  (if (satisfies? INamed param)
    (truncate-named n param)
    (truncate-string n location (str param))))

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
        (or (list? coll)
            (seq? coll)) ["(" ")"]
        :else ["<" ">"]))

(defn with-edges
  "Wrap `value` with edges of `coll`"
  [coll value]
  (let [[left right] (edges coll)]
    (str left value right)))

(defn pretty-condensed
  "Render form in abbreviated form, showing content only for keywords/strings/symbols and entering collections to a depth of 1."
  ([form] (pretty-condensed 0 coll? 1 form))
  ([depth enter-pred max-depth form]
   (cond
     (satisfies? INamed form) (truncate-named 16 form)
     (string? form) (truncate-string 16 :middle form)
     (fn? form) (or (some-> (.-name form)
                            (str/replace #"(^.*\$)(.*)" "$2"))
                    "ƒ")
     (number? form) (str form)
     (and (enter-pred form)
          (< depth max-depth)) (with-edges form
                                           (str/join ", " (mapv (partial pretty-condensed (inc depth) enter-pred max-depth) form)))
     :else (with-edges form "…"))))
