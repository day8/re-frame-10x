(ns day8.re-frame-10x.tools.coll)

(defn last-in-vec
  "Get the last element in the vector. Returns nil if v is empty"
  [v]
  (let [num (count v)]
    (if (zero? num)
      nil
      (nth v (dec num)))))

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