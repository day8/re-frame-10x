(ns day8.re-frame-10x.view.components
  (:require [clojure.string :as str]
            [goog.fx.dom :as fx]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [clojure.string :as str]
            [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.reagent.v0v7v0.reagent.core :as r]
            [devtools.prefs]
            [devtools.formatters.core]
            [cljsjs.react-highlight]
            [cljsjs.highlight.langs.clojure])
  (:require-macros [day8.re-frame-10x.utils.macros :refer [with-cljs-devtools-prefs]]))

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

(defn scroll! [el start end time]
  (.play (fx/Scroll. el (clj->js start) (clj->js end) time)))

(defn scrolled-to-end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))

(defn autoscroll-list [{:keys [class scroll?]} child]
  "Reagent component that enables scrolling for the elements of its child dom-node.
   Scrolling is only enabled if the list is scrolled to the end.
   Scrolling can be set as option for debugging purposes.
   Thanks to Martin Klepsch! Original code can be found here:
       https://gist.github.com/martinklepsch/440e6fd96714fac8c66d892e0be2aaa0"
  (let [node          (r/atom nil)
        should-scroll (r/atom true)]
    (r/create-class
      {:display-name "autoscroll-list"
       :component-did-mount
                     (fn [_]
                       (scroll! @node [0 (.-scrollTop @node)] [0 (.-scrollHeight @node)] 0))
       :component-will-update
                     (fn [_]
                       (reset! should-scroll (scrolled-to-end? @node 100)))
       :component-did-update
                     (fn [_]
                       (when (and scroll? @should-scroll)
                         (scroll! @node [0 (.-scrollTop @node)] [0 (.-scrollHeight @node)] 500)))
       :reagent-render
                     (fn [{:keys [class]} child]
                       [:div {:class class :ref (fn [dom-node]
                                                  (reset! node dom-node))}
                        child])})))

;; Data browser

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
   :min-expandable-sequable-count-for-well-known-types
   3

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
       {:class (str "re-frame-10x--object" (when @expanded? " expanded"))}
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
      [rc/v-box
       :children
       [[rc/h-box
         :align :center
         :class (str/join " " ["re-frame-10x--object"
                               (when @expanded? "expanded")])
         :children
         [[:span {:class    "toggle"
                  :on-click #(rf/dispatch [:app-db/toggle-expansion path])}
           [:button.expansion-button (if @expanded? "▼ " "▶ ")]]
          (or title "data")]]
        [rc/h-box
         :children [[:div {:style {:margin-left 20}}
                     (cond
                       (and @expanded?
                            (or (string? data)
                                (number? data)
                                (boolean? data)
                                (nil? data))) [:div {:style {:margin "10px 0"}} (prn-str data)]
                       @expanded? (jsonml->hiccup (cljs-devtools-header data) (conj path 0)))]]]]])))

(defn subscription-render [data title path]
  (let [expanded? (r/atom true) #_(rf/subscribe [:app-db/node-expanded? path])]
    (fn [data]
      [:div
       {:class (str/join " " ["re-frame-10x--object"
                              (when @expanded? "expanded")])}
       #_[:span {:class    "toggle"
                 :on-click #(rf/dispatch [:app-db/toggle-expansion path])}
          [:button.expansion-button (if @expanded? "▼ " "▶ ")]]
       (or title "data")
       [:div {:style {:margin-left 20}}
        (cond
          (and @expanded?
               (or (string? data)
                   (number? data)
                   (boolean? data)
                   (nil? data))) [:div {:style {:margin "10px 0"}} (prn-str data)]
          @expanded? (jsonml->hiccup (cljs-devtools-header data) (conj path 0)))]])))

(defn simple-render [data path & [class]]
  (let [expanded? (r/atom true) #_(rf/subscribe [:app-db/node-expanded? path])]
    (fn [data]
      [:div
       {:class (str/join " " ["re-frame-10x--object"
                              (when @expanded? "expanded")
                              class])}
       #_[:span {:class    "toggle"
                 :on-click #(rf/dispatch [:app-db/toggle-expansion path])}
          [:button.expansion-button (if @expanded? "▼ " "▶ ")]]
       [:div #_{:style {:margin-left 20}}
        (cond
          (and @expanded?
               (or (string? data)
                   (number? data)
                   (boolean? data)
                   (nil? data))) [:div {:style {:margin "10px 0"}} (prn-str data)]
          @expanded? (jsonml->hiccup (cljs-devtools-header data) (conj path 0)))]])))

(defn tag [class label]
  [rc/box
   :class (str "rft-tag noselect " class)
   :child [:span {:style {:margin "auto"}} label]])

(def highlight (r/adapt-react-class js/Highlight))
