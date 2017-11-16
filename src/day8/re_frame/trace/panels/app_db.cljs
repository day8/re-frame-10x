(ns day8.re-frame.trace.panels.app-db
  (:require-macros [day8.re-frame.trace.utils.macros :refer [with-cljs-devtools-prefs]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.utils.localstorage :as localstorage]
            [day8.re-frame.trace.components.components :as components]
            [day8.re-frame.trace.utils.re-com :as re-com]
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
   :none-style                     "display: none"
   :index-tag                      [:span :none-style]

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

(defn cljs-devtools-has-body [& args]
  (apply make-devtools-api-call devtools.formatters.core/has-body-api-call args))

(defn get-object [jsonml]
  (.-object (get jsonml 1)))

(defn get-config [jsonml]
  (.-config (get jsonml 1)))

(defn data-structure [jsonml path]
  (let [expanded? (rf/subscribe [:app-db/node-expanded? path])]
    (fn [jsonml path]
      [:span
       {:class (str "re-frame-trace--object" (when @expanded? " expanded"))}
       [:span {:class    "toggle"
               :on-click #(rf/dispatch [:app-db/toggle-expansion path])}
        [:button.expansion-button (if @expanded? "▼" "▶")]]
       (if (and @expanded? (cljs-devtools-has-body (get-object jsonml) (get-config jsonml)))
         (jsonml->hiccup
           (cljs-devtools-body
             (get-object jsonml)
             (get-config jsonml))
           (conj path :body))
         (jsonml->hiccup
           (cljs-devtools-header
             (get-object jsonml)
             (get-config jsonml))
           (conj path :header)))])))

(defn jsonml->hiccup
  "JSONML is the format used by Chrome's Custom Object Formatters.
  The spec is at https://docs.google.com/document/d/1FTascZXT9cxfetuPRT2eXPQKXui4nWFivUnS_335T3U/preview.

  JSONML is pretty much Hiccup over JSON. Chrome's implementation of this can
  be found at https://cs.chromium.org/chromium/src/third_party/WebKit/Source/devtools/front_end/object_ui/CustomPreviewComponent.js
  "
  [jsonml path]
  (if (number? jsonml)
    jsonml
    (let [[tag-name attributes & children] jsonml
          tagnames #{"div" "span" "ol" "li" "table" "tr" "td"}]
      (cond
        (contains? tagnames tag-name) (into
                                        [(keyword tag-name) {:style (-> (js->clj attributes)
                                                                        (get "style")
                                                                        (string->css))}]
                                        (map-indexed (fn [i child] (jsonml->hiccup child (conj path i))))
                                        children)

        (= tag-name "object") [data-structure jsonml path]
        :else jsonml))))

(defn subtree [data title path]
  (let [expanded? (rf/subscribe [:app-db/node-expanded? path])]
    (fn [data]
      [:div
       {:class (str/join " " ["re-frame-trace--object"
                              (when @expanded? "expanded")])}
       [:span {:class    "toggle"
               :on-click #(rf/dispatch [:app-db/toggle-expansion path])}
        [:button.expansion-button (if @expanded? "▼ " "▶ ")]]
       (or title "data")
       [:div {:style {:margin-left 20}}
        (cond
          (and @expanded?
               (or (string? data)
                   (number? data))) [:div {:style {:margin "10px 0"}} data]
          @expanded? (jsonml->hiccup (cljs-devtools-header data) (conj path 0)))]])))

(defn render-state [data]
  (let [subtree-input (r/atom "")
        subtree-paths (rf/subscribe [:app-db/paths])
        search-string (rf/subscribe [:app-db/search-string])
        input-error   (r/atom false)]
    (fn []
      [:div {:style {:flex "1 1 auto" :display "flex" :flex-direction "column"}}
       [:div.panel-content-scrollable {:style {:margin-left "10px"}}
        [re-com/input-text
         :model search-string
         :on-change (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
         :on-submit #(rf/dispatch [:app-db/add-path %])
         :change-on-blur? false
         :placeholder ":path :into :app-db"]

        ;; TODO: check for input errors
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
                     [:button.subtree-button {:on-click #(rf/dispatch [:app-db/remove-path path])}
                      [:span.subtree-button-string
                       (str path)]]
                     [path]]]])
                @subtree-paths))]
        [:div {:style {:margin-bottom "20px"}}
         [subtree @data [:span.label "app-db"] [:app-db]]]]])))
