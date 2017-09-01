(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]

            [cljs.pprint :refer [pprint]]))


(defn css-munge
  [string]
  (str/replace string #"\.|/" "-"))

(defn namespace-css
  [classname]
  (str "re-frame-trace--" classname))

(defn type-string
  [obj]
  (cond
    (number? obj)    "number"
    (boolean? obj)   "boolean"
    (string? obj)    "string"
    (nil? obj)       "nil"
    (keyword? obj)   "keyword"
    (symbol? obj)    "symbol"
    :else (pr-str (type obj))))

(defn view
  [data]
  (if (coll? data)
    [:div {:class (str (namespace-css "collection") " " (namespace-css (css-munge (type-string data))))}]
    [:span {:class (namespace-css (css-munge (type-string data)))} (str data)]))

(defn crawl
  [data]
  (if (coll? data)
    (into (view data) (mapv crawl data))
    (view data)))

(defn tab [data]
  (pprint data)
  (pprint (crawl data))
  [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
    [:div.panel-content-scrollable
     (crawl data)]])
