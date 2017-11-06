(ns day8.re-frame.trace.panels.app-db
  (:require-macros [day8.re-frame.trace.utils.macros :refer [with-cljs-devtools-prefs]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.utils.localstorage :as localstorage]
            [day8.re-frame.trace.components.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))

(defn string->css [css-string]
  "This function converts jsonml css-strings to valid css maps for hiccup.
  Example: 'margin-left:0px;min-height:14px;' converts to
           {:margin-left '0px', :min-height '14px'}"

  (->> (str/split css-string #";")
       (map #(str/split % #":"))
       (reduce (fn [acc [property value]]
                 (assoc acc (keyword property) value)) {})))

(declare jsonml->hiccup)

(def default-cljs-devtools-prefs @devtools.prefs/default-config)

(defn reset-wrapping [css-string]
  (str/replace css-string #"white-space:nowrap;" ""))

(def customized-cljs-devtools-prefs
  {; Override some cljs-devtools default styles.

   ; The goal here is to make default styles more flexible and wrap at the edge of our panel (we don't want horizontal
   ; scrolling). Technically we want to remove all 'white-space:no-wrap'.
   ; See https://github.com/binaryage/cljs-devtools/blob/master/src/lib/devtools/defaults.cljs
   ;; Commented out as this causes some other issues too.
   ;:header-style (reset-wrapping (:header-style default-cljs-devtools-prefs))
   ;:expandable-style (reset-wrapping (:expandable-style default-cljs-devtools-prefs))
   ;:item-style (reset-wrapping (:item-style default-cljs-devtools-prefs))

   ; Hide the index spans on the left hand of collections. Shows how many elements in a collection.
   :none-style   "display: none"
   :index-tag    [:span :none-style]

   ; Our JSON renderer does not have hierarchy depth limit,
   ; See https://github.com/binaryage/cljs-devtools/blob/master/src/lib/devtools/formatters/budgeting.cljs
   :initial-hierarchy-depth-budget false})

(def effective-cljs-devtools-prefs (merge default-cljs-devtools-prefs customized-cljs-devtools-prefs))

(defn make-devtools-api-call [api-fn & args]
  (with-cljs-devtools-prefs effective-cljs-devtools-prefs
    (apply api-fn args)))

(defn cljs-devtools-header [& args]
  (apply make-devtools-api-call devtools.formatters.core/header-api-call args))

(defn cljs-devtools-body [& args]
  (apply make-devtools-api-call devtools.formatters.core/body-api-call args))

(defn data-structure [jsonml]
  (let [expanded? (r/atom false)]
    (fn [jsonml]
      [:span
       {:class (str/join " " ["re-frame-trace--object"
                              (when @expanded? "expanded")])}
       [:span {:class    "toggle"
               :on-click #(swap! expanded? not)}
        [:button.expansion-button (if @expanded? "▼" "▶")]]
       (jsonml->hiccup (if @expanded?
                         (cljs-devtools-body
                           (.-object (get jsonml 1))
                           (.-config (get jsonml 1)))
                         (cljs-devtools-header
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
    (let [[head & args] jsonml
          tagnames #{"div" "span" "ol" "li" "table" "tr" "td"}]
      (cond
        (contains? tagnames head) (let [[style & children] args]
                                    (into
                                      [(keyword head) {:style (-> (js->clj style)
                                                                  (get "style")
                                                                  (string->css))}]
                                      (map jsonml->hiccup children)))

        (= head "object") [data-structure jsonml]
        (= jsonml ", ") " "
        :else jsonml))))

(defn subtree [data title]
  (let [expanded? (r/atom false)]
    (fn [data]
      [:div
       {:class (str/join " " ["re-frame-trace--object"
                              (when @expanded? "expanded")])}
       [:span {:class    "toggle"
               :on-click #(swap! expanded? not)}
        [:button.expansion-button (if @expanded? "▼ " "▶ ")]]
       (or title "data")
       [:div {:style {:margin-left 20}}
        (cond
          (and @expanded?
               (or (string? data)
                   (number? data))) [:div {:style {:margin "10px 0"}} data]
          @expanded? (jsonml->hiccup (cljs-devtools-header data)))]])))

(defn render-state [data]
  (let [subtree-input (r/atom "")
        subtree-paths (rf/subscribe [:app-db/paths])
        input-error   (r/atom false)]
    (fn []
      [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
       [:div.panel-content-scrollable {:style {:margin 10}}
        [:div.filter-control-input
         [components/search-input {:placeholder ":path :into :app-state"
                                   :on-save     (fn [path]
                                                  (if false ;; TODO check if path exists
                                                    (reset! input-error true)
                                                    (do
                                                      ; (reset! input-error false)
                                                      ;; TODO check if input already wrapped in braces
                                                      (rf/dispatch [:app-db/paths (into #{(cljs.reader/read-string (str "[" path "]"))} @subtree-paths)])
                                                      #_(swap! subtree-paths #(into #{(cljs.reader/read-string (str "[" path "]"))} %)))))
                                   :on-change   #(reset! subtree-input (.. % -target -value))}]]
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
