(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.formatters.core :as cljs-devtools]))

(defn string->css [css-string]
  (->> (str/split (get css-string "style") #";")
       (map #(str/split % #":"))
       (reduce (fn [acc [property value]]
                 (assoc acc (keyword property) value)) {})))

(declare jsonml->hiccup)

(defn data-structure [jsonml]
  (let [expanded? (r/atom true)]
    (fn [jsonml]
      [:span
        {:class (str/join " " ["re-frame-trace--object"
                               (when @expanded? "expanded")])}
        [:span {:class "toggle"
                :on-click #(swap! expanded? not)}
           (if @expanded? "▼" "▶")]
        (jsonml->hiccup (if @expanded?
                          (cljs-devtools/body-api-call
                            (.-object (get jsonml 1))
                            (.-config (get jsonml 1)))
                          (cljs-devtools/header-api-call
                            (.-object (get jsonml 1))
                            (.-config (get jsonml 1)))))])))

(defn jsonml->hiccup [jsonml]
  (if (number? jsonml)
    jsonml
    (let [[head & args] jsonml
          tagnames #{"span" "ol" "li" "div"}]
      (cond
        (contains? tagnames head)   (let [[style & children] args]
                                      [(keyword head) {:style (string->css (js->clj style))}
                                       (into [:div {:style {:display "inline-block"}}]
                                             (mapv jsonml->hiccup children))])
        (= head "object")           [data-structure jsonml]
        (= jsonml ", ")             " "
        :else jsonml))))


(defn render-state  [data]
  [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
    [:div.panel-content-scrollable
     (jsonml->hiccup (cljs-devtools/header-api-call @data))]])
