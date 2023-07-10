(ns day8.re-frame-10x.tools.datafy
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

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
  (walk/postwalk #(cond->> % (map? %) (into (sorted-map))) m))
