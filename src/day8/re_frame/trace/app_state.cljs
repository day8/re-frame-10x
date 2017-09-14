(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.formatters.core :as cljs-devtools]

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
    [:div  {:class (str (namespace-css "collection") " " (namespace-css (css-munge (type-string data))))}]
    [:span {:class (str (namespace-css "primative") " " (namespace-css (css-munge (type-string data))))} (str data)]))

(defn jsonml-style
  [style-map]
  ; {:style (get style-map "style")}
  {:style {:background "rgba(0,0,0,0.1)"}})

(defn str->hiccup
  [string]
  ; (println string)
  (cond (= string "span")  :span
        (= string "style") :style
        ; (= string "}")     nil
        ; (= string "{")     nil
        ; (= string " ")     nil
        ; (= string ", ")    nil
        :else              string))


(defn crawl
  [data]
  (if (coll? data)
    (into (view data) (mapv crawl data))
    (view data)))

(defn jsonml->hiccup
  [data]
  (cond
    (vector? data) (mapv jsonml->hiccup data)
    (map? data)    (jsonml-style data)
    :else          (str->hiccup data)))

(defn tab [data]
  [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
    [:div.panel-content-scrollable
      (jsonml->hiccup (js->clj (cljs-devtools/header-api-call data)))]])
      ; (crawl data)]])
