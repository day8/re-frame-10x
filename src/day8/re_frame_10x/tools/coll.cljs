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

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks]]
  (if ks
    (if-let [nextmap (clojure.core/get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn get-in-with-lists-and-sets
  "cljs.core/get-in with support for index access of lists and sets"
  [m ks]
  (reduce
   (fn [ret k]
     (cond
       (or (list? ret) (instance? cljs.core/LazySeq ret))
       (nth ret k)
       (set? ret)
       (if (number? k)
         (nth (vec ret) k)
         (get ret k))
       (map? ret)
       (get ret k)
       :else
       (get ret k)))
   m
   ks))

(defn nodes-fewer-than?
  "Counts the nodes in a nested data structure, until the count reaches a limit.
  Returns nil if the limit is reached, or the count if it is not."
  [data limit]
  (let [children #(if (coll? %) (seq %) nil)
        rf (fn [ct _] (if (>= ct limit) (reduced nil) (inc ct)))
        result (reduce rf 0 (tree-seq coll? children data))]
    (when (number? result) result)))
