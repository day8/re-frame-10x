(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.formatters.core :as cljs-devtools]
            [day8.re-frame.trace.localstorage :as localstorage]))

;; TODO move search-input into components ns

(defn search-input [{:keys [title placeholder on-save on-change on-stop]}]
  (let [val  (r/atom title)
        save #(let [v (-> @val str str/trim)]
                (when (pos? (count v))
                  (on-save v)))]
    (fn []
      [:input {:type        "text"
               :value       @val
               :auto-focus  true
               :placeholder placeholder
               :size        (if (> 20 (count (str @val)))
                              25
                              (count (str @val)))
               :on-change   #(do (reset! val (-> % .-target .-value))
                                 (on-change %))
               :on-key-down #(case (.-which %)
                               13 (do
                                    (save)
                                    (reset! val ""))
                               nil)}])))

(defn string->css [css-string]
  "This function converts jsonml css-strings to valid css maps for hiccup.
  Example: 'margin-left:0px;min-height:14px;' converts to
           {:margin-left '0px', :min-height '14px'}"

  (->> (str/split css-string #";")
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
    (let [[head & args]             jsonml
          tagnames                  #{"div" "span" "ol" "li" "table" "tr" "td"}]
      (cond
        (contains? tagnames head)   (let [[style & children] args]
                                      (into
                                        [(keyword head) {:style (-> (js->clj style)
                                                                    (get "style")
                                                                    (string->css))}]
                                        (map jsonml->hiccup children)))

        (= head "object")           [data-structure jsonml]
        (= jsonml ", ")             " "
        :else jsonml))))

(defn render-state [data]
  (let [subtree-input  (r/atom "")
        subtree-paths  (r/atom (localstorage/get "subtree-paths" #{}))
        input-error    (r/atom false)]
    (add-watch subtree-paths
               :update-localstorage
               (fn [_ _ _ new-state]
                 (localstorage/save! "subtree-paths" new-state)))
    (fn []
      [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
        [:div.panel-content-scrollable {:style {:margin 10}}
          [:div.filter-control-input
            [search-input {:placeholder ":path :into :app-state"
                           :on-save (fn [path]
                                      (if false ;; TODO check if path exists in app-state
                                        (reset! input-error true)
                                        (do
                                          ; (reset! input-error false)
                                          ;; TODO check if input already wrapped in braces
                                          (swap! subtree-paths conj (cljs.reader/read-string (str "[" path "]"))))))
                           :on-change #(reset! subtree-input (.. % -target -value))}]]
            ; (if @input-error
            ;   [:div.input-error {:style {:color "red" :margin-top 5}}
            ;    "Please enter a valid path."])]]

          [:div.subtrees {:style {:margin "20px 0"}}
            (doall
              (map (fn [path]
                     ^{:key path}
                     [:div.subtree-wrapper {:style {:margin "10px 0"}}
                       [:div.subtree
                         [:button.subtree-button {:on-click #(swap! subtree-paths disj path)}
                           [:span.subtree-button-string
                             (str path)]]
                         [:div {:style {:margin-top 10
                                        :margin-left 10
                                        :padding-bottom 10}}
                           (if-let [jsonml (cljs-devtools/header-api-call (get-in @data path))]
                             (jsonml->hiccup jsonml)
                             (get-in @data path))]]])
                @subtree-paths))]

          [:div [:h1 {:style {:font-size 20
                              :margin-top 30
                              :display "block"}}
                  "app-state"]]
          [:div {:style {:margin-top 10}} (jsonml->hiccup (cljs-devtools/header-api-call @data))]]])))
