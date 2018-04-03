(ns day8.re-frame-10x.utils.utils)

(def diff-link "https://github.com/Day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/Diffs.md")

(defn last-in-vec
  "Get the last element in the vector"
  [v]
  (nth v (dec (count v))))

(defn find-all-indexes-in-vec
  "Gets the index of all items in vec that match the predicate"
  [pred v]
  (keep-indexed #(when (pred %2) %1) v))

(defn find-index-in-vec
  "Gets the index of the first item in vec that matches the predicate"
  [pred v]
  (first (find-all-indexes-in-vec pred v)))

(defn id-between-xf
  "Returns a transducer that filters for :id between beginning and ending."
  [beginning ending]
  (filter #(<= beginning (:id %) ending)))

(defn spy
  ([x]
   (js/console.log x)
   x)
  ([label x]
   (js/console.log label x)
   x))

(defn pluralize
  "Return a pluralized phrase, appending an s to the singular form if no plural is provided.
  For example:
     (pluralize 5 \"month\") => \"5 months\"
     (pluralize 1 \"month\") => \"1 month\"
     (pluralize 1 \"radius\" \"radii\") => \"1 radius\"
     (pluralize 9 \"radius\" \"radii\") => \"9 radii\"
     From https://github.com/flatland/useful/blob/194950/src/flatland/useful/string.clj#L25-L33"
  [num singular & [plural]]
  (str num " " (if (= 1 num) singular (or plural (str singular "s")))))

(defn pluralize-
  "Same as pluralize, but doesn't prepend the number to the pluralized string."
  [num singular & [plural]]
  (if (= 1 num)
    singular
    (or plural (str singular "s"))))

(defn copy-to-clipboard
  [text]
  (let [el (.createElement js/document "textarea")]
    (set! (.-value el) text)
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (.appendChild (.-body js/document) el)
    (.select el)
    (.execCommand js/document "copy")
    (.removeChild (.-body js/document) el)))
