(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]))

(defn classname
  [obj]
  (str/replace (pr-str (type obj)) #"\.|/" "-"))

(defn view
  [data]
  (if (string? data)
    [:span.data-string data]
    [:div {:class (classname data)}]))

(defn crawl
  [data]
  (if (coll? data)
    (into (view data) (mapv crawl data))
    (view data)))

(defn tab [data]
  (println (crawl data))
  (crawl data))
