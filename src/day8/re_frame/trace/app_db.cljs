(ns day8.re-frame.trace.app-db
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

(def config {:well-known-types #{"cljs.core/Keyword"}

             :render-bools     false
             :render-strings   false
             :render-numbers   false
             :render-keywords  false
             :render-symbols   false
             :render-instances false
             :render-types     false
             :render-functions false
             })

(declare jsonml->hiccup)

(defn data-structure [jsonml]
  (let [expanded? (r/atom false)]
    (fn [jsonml]
      [:span
        {:class (str/join " " ["re-frame-trace--object"
                               (when @expanded? "expanded")])}
        [:span {:class "toggle"
                :on-click #(swap! expanded? not)}
           [:button.expansion-button (if @expanded? "▼" "▶")]]
        (jsonml->hiccup (if @expanded?
                          (cljs-devtools/body-api-call
                            (.-object (get jsonml 1))
                            (.-config (get jsonml 1)))
                          (cljs-devtools/header-api-call
                            (.-object (get jsonml 1))
                            (.-config (get jsonml 1)))))])))

(defn jsonml->hiccup
  "JSONML is the format used by Chrome's Custom Object Formatters.
  The spec is at https://docs.google.com/document/d/1FTascZXT9cxfetuPRT2eXPQKXui4nWFivUnS_335T3U/preview.

  JSONML is pretty much Hiccup over JSON. Chrome's implementation of this can
  be found at https://cs.chromium.org/chromium/src/third_party/WebKit/Source/devtools/front_end/object_ui/CustomPreviewComponent.js
  "
  [jsonml]
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
                :on-click #(swap! expanded? not)}
           [:button.expansion-button (if @expanded? "▼ " "▶ ")]]
        (or title "data")
        [:div {:style {:margin-left 20}}
          (cond
            (and @expanded?
              (or (string? data)
                  (number? data)))  [:div {:style {:margin "10px 0"}} data]
            @expanded?              (jsonml->hiccup (cljs-devtools/header-api-call data config :extra)))]])))

(defn render-state  [data]
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
            [components/search-input {:placeholder ":path :into :app-state"
                                      :on-save (fn [path]
                                                 (if false ;; TODO check if path exists
                                                   (reset! input-error true)
                                                   (do
                                                     ; (reset! input-error false)
                                                     ;; TODO check if input already wrapped in braces
                                                     (swap! subtree-paths #(into #{(cljs.reader/read-string (str "[" path "]"))} %)))))
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
                             [subtree
                               (get-in @data path)
                               [:button.subtree-button {:on-click #(swap! subtree-paths disj path)}
                                 [:span.subtree-button-string
                                   (str path)]]]]])
                @subtree-paths))]
          [subtree @data [:span.label "app-db"]]]])))
