(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.formatters.core :as cljs-devtools]
            [day8.re-frame.trace.localstorage :as localstorage]
            [day8.re-frame.trace.components :as components]))


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
  (let [expanded? (r/atom false)]
    (fn [jsonml]
      [:span
        {:class (str/join " " ["re-frame-trace--object"
                               (when @expanded? "expanded")])}
        [:span {:class "toggle"
                :style {:margin-left 1}
                :on-click #(swap! expanded? not)}
           [:button (if @expanded? "▼" "▶")]]
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

(defn subtree [data title]
  (let [expanded? (r/atom false)]
    (fn [data]
      [:div
        {:class (str/join " " ["re-frame-trace--object"
                               (when @expanded? "expanded")])}
        [:span {:class "toggle"
                :style {:margin-left 1}
                :on-click #(swap! expanded? not)}
           [:button (if @expanded? "▼ " "▶ ")]]
        (or title "data")
        [:div {:style {:margin-left 20}}
          (cond
            (and @expanded?
              (or (string? data)
                  (number? data)))  [:div {:style {:margin "10px 0"}} data]
            @expanded?              (jsonml->hiccup (cljs-devtools/header-api-call data)))]])))

(defn render-state  [data]
  (let [subtree-input  (r/atom "")
        last-valid-subtree-input (r/atom "")
        subtree-paths  (r/atom (localstorage/get "subtree-paths" #{}))
        input-error    (r/atom false)]
    (add-watch subtree-paths
               :update-localstorage
               (fn [_ _ _ new-state]
                 (localstorage/save! "subtree-paths" new-state)))
    (add-watch subtree-input
               :update-subtree-autocomplete
               (fn [_ _ prev-state new-state]
                 (when-let [preview (try (get-in @data (cljs.reader/read-string (str "[" @subtree-input "]")))
                                         (catch js/Error e nil))]
                   (reset! last-valid-subtree-input @subtree-input))))
    (fn []
      [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
        [:div.panel-content-scrollable {:style {:margin 10}}
          [:div.filter-control-input
            [components/search-input {:placeholder ":path :into :app-state"
                                      :on-save (fn [path]
                                                 (if false ;; TODO check if path exists
                                                   (reset! input-error true)
                                                   (do
                                                     ; (reset! input-error false)
                                                     ;; TODO check if input already wrapped in braces
                                                     (reset! subtree-input "")
                                                     (swap! subtree-paths #(into #{(cljs.reader/read-string (str "[" path "]"))} %)))))
                                      :on-change #(reset! subtree-input (.. % -target -value))}]]
                       ; (if @input-error
                       ;   [:div.input-error {:style {:color "red" :margin-top 5}}
                       ;    "Please enter a valid path."])]]
          (when (not (zero? (count @subtree-input)))
            [:div
             [:div {:style {:margin "10px 0"}}
               (when (coll? (get-in @data (cljs.reader/read-string (str "[" @last-valid-subtree-input "]"))))
                 (when-let [keys (keys (get-in @data (cljs.reader/read-string (str "[" @last-valid-subtree-input "]"))))]
                   (map (fn [key] [:button.label {:key (str key)}
                                    (str key)])
                        keys)))]
             [:div (str (get-in @data (cljs.reader/read-string (str "[" @last-valid-subtree-input "]"))))]])
          [:div.subtrees {:style {:margin "20px 0"}}
            (doall
              (map (fn [path]
                     ^{:key path}
                     [:div.subtree-wrapper {:style {:margin "10px 0"}}
                       [:div.subtree
                             [subtree
                               (get-in @data path)
                               [:button.subtree-button {:on-click #(swap! subtree-paths disj path)}
                                 [:span.subtree-button-string
                                   (str path)]]]]])
                @subtree-paths))]
          [subtree @data [:button.label "app-state"]]]])))
