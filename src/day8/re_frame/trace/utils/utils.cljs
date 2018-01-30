(ns day8.re-frame.trace.utils.utils)

(def diff-link "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/Diffs.md")

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
