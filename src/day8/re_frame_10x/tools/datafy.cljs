(ns day8.re-frame-10x.tools.datafy
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.loggers :refer [console]]
            [arrangement.core :as order]))

(defn keyboard-event [e]
  {:key (.-key e)
   :altKey (.-altKey e)
   :ctrlKey (.-ctrlKey e)
   :metaKey (.-metaKey e)
   :shiftKey (.-shiftKey e)})

(def mod-key->str {:metaKey "Meta"
                   :ctrlKey "Ctrl"
                   :altKey "Alt"
                   :shiftKey "Shift"})

(def mod-key->order {:metaKey 1
                     :ctrlKey 2
                     :altKey 3
                     :shiftKey 4})

(defn keyboard-event->str [v]
  (let [{key-str :key :as m} (cond-> v (not (map? v)) keyboard-event)
        mods (->> m
                  (filter (comp true? val))
                  (map key)
                  (sort-by mod-key->order)
                  (mapv mod-key->str))]
    (str/join "-" (conj mods key-str))))

(defn deep-sorted-map [m]
  (walk/postwalk
   #(cond->> %
      (map? %) (into (sorted-map-by order/rank)))
   m))

(defn alias [k ns->alias]
  (if-let [a (get ns->alias (namespace k))]
    (keyword (str ":" a) (name k))
    k))

(defn alias-namespaces [m ns->alias]
  (->> m
       (walk/postwalk
        #(cond-> %
           (keyword? %) (alias ns->alias)))))

(defn pr-str-safe [value]
  (pr-str-with-opts [value] {:flush-on-newline true
                             :readably true
                             :meta false
                             :print-length nil}))
