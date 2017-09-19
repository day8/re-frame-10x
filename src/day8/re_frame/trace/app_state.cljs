(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.formatters.core :as cljs-devtools]

            [cljs.pprint :refer [pprint]]))

(defn string->css [css-string]
  (->> (str/split (get css-string "style") #";")
       (map #(str/split % #":"))
       (reduce (fn [acc [property value]]
                 (assoc acc (keyword property) value)) {})))

(defn str->hiccup
  [string]
  (cond (= string "span")   :span
        (= string "style")  :style
        (= string ", ")     " "
        :else               string))

(declare jsonml->hiccup)

(defn data-structure
  [jsonml]
  (let [expand? (r/atom true)]
    (fn [jsonml]
      [:span
        {:class (str/join " " ["re-frame-trace--object"
                               (when @expand? "expanded")])
         :on-click #(swap! expand? not)}
        (jsonml->hiccup (if @expand?
                          (cljs-devtools/body-api-call
                            (.-object (get jsonml 1))
                            (.-config (get jsonml 1)))
                          (cljs-devtools/header-api-call
                            (.-object (get jsonml 1))
                            (.-config (get jsonml 1)))))])))

(defn jsonml->hiccup
  [jsonml]
  (cond
    (and (array? jsonml)
         (= "object" (first jsonml))) [data-structure jsonml]
    (array? jsonml)                   (mapv jsonml->hiccup jsonml)
    (object? jsonml)                  {:style (string->css (js->clj jsonml))}
    (or (string? jsonml)
        (integer? jsonml))            (str->hiccup jsonml)))

(defn tab [data]
  [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
    [:div.panel-content-scrollable
      (jsonml->hiccup (cljs-devtools/header-api-call data))]])
