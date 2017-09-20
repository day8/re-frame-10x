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

(defn data-input [{:keys [title on-save on-change on-stop]}]
  (let [val  (r/atom title)
        save #(let [v (-> @val str str/trim)]
                (when (pos? (count v))
                  (on-save v)))]
    (fn []
      [:input {:type        (cond (integer? "number")
                                  (string?  "text"))
               :value       @val
               :size        (if (zero? (count (str @val)))
                              1
                              (count (str @val)))
               :on-change   #(do (reset! val (-> % .-target .-value))
                                 (on-change %))
               :on-key-down #(case (.-which %)
                               13 (save)
                               nil)}])))

(defn editable
  [raw]
  (let [val (r/atom raw)]
    [:span {:class (str/join " " ["data-structure-editable"
                                  (when (string? raw) "string")])}
      [data-input {:title     raw
                   :on-save   #(js/console.log %)
                   :on-change #(reset! val (.. % -target -value))}]]))


(defn str->hiccup
  [string]
  (cond (= string "span")              :span
        (= string "style")             :style
        (= string ", ")                " "
        (str/starts-with? string "\"") [editable (str/replace string "\"" "")]
        :else                          string))

(declare jsonml->hiccup)

(defn data-structure
  [jsonml]
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

(defn jsonml->hiccup
  [jsonml]
  (cond
    (and (array? jsonml)
         (= "object" (first jsonml))) [data-structure jsonml]
    (array? jsonml)                   (mapv jsonml->hiccup jsonml)
    (object? jsonml)                  {:style (string->css (js->clj jsonml))}
    (string? jsonml)                  (str->hiccup jsonml)
    (integer? jsonml)                 [editable jsonml]))

(defn tab [data]
  [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
    [:div.panel-content-scrollable
      (jsonml->hiccup (cljs-devtools/header-api-call data))]])
