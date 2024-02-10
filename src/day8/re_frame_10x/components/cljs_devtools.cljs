(ns day8.re-frame-10x.components.cljs-devtools
  (:require-macros
   [day8.re-frame-10x.components.cljs-devtools                   :refer [with-cljs-devtools-prefs]])
  (:require
   [clojure.string :as string]
   [devtools.prefs]
   [devtools.formatters.core]
   [goog.dom]
   [goog.events]
   [goog.style]
   [goog.ui.PopupMenu]
   [goog.ui.MenuItem]
   [goog.ui.Component]
   [goog.object]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core    :refer [style]]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px]]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.core    :refer [defclass]]
   [day8.re-frame-10x.components.re-com                          :as rc]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.panels.app-db.events                       :as app-db.events]
   [day8.re-frame-10x.panels.app-db.subs                         :as app-db.subs]
   [day8.re-frame-10x.tools.datafy                               :as tools.datafy]
   [day8.re-frame-10x.tools.reader.edn                           :as reader.edn]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]))

(def default-config @devtools.prefs/default-config)

(defn base-config
  []
  {; Hide index tags
   :index-tag                              [:span :none-style]
   :none-style                             (style {:display :none})

   ; Our JSON renderer does not have hierarchy depth limit,
   ; See https://github.com/binaryage/cljs-devtools/blob/master/src/lib/devtools/formatters/budgeting.cljs
   :initial-hierarchy-depth-budget         false
   :item-style                             (style {:display     :inline-block
                                                   :white-space :nowrap
                                                   :border-left [[(px 2) :solid :#000]]
                                                   :padding     [[0 styles/gs-5 0 styles/gs-5]]
                                                   :margin      [[(px 1 0 0 0)]]})})

(def body-style-base
  {:display          :inline-block
   :padding          [[styles/gs-2 styles/gs-12]]
   :border           [[(px 1) :solid styles/nord3]]
   :margin           (px 1)
   :margin-top       0})

(def dark-ambiance-config
  {:cljs-land-style (style {:background-color styles/nord0
                            :color            styles/nord6})
   :body-style      (style body-style-base {:background-color styles/nord0})})

(def bright-ambiance-config
  {:cljs-land-style (style {:background-color styles/nord6
                            :color            styles/nord0})
   :body-style      (style body-style-base {:background-color styles/nord6})})

;; This used to be in the api-call fn below. However, recalculating this on *every* render is expensive so moved
;; here as static def.
;; TODO: If we expose ambiance and/or syntax color scheme as settings will need to fix this, maybe by recalculating
;; at the time the setting is changed/loaded.
(def custom-config
  (merge default-config (base-config) #_bright-ambiance-config))

(defn header [value config & [{:keys [render-paths?]}]]
  (with-cljs-devtools-prefs
    (if render-paths?
      (merge custom-config {:render-path-annotations       true})
      custom-config)
    (devtools.formatters.core/header-api-call value config)))

(defn body [value config & [{:keys [render-paths?]}]]
  (with-cljs-devtools-prefs
    (if render-paths?
      (merge custom-config {:render-path-annotations       true})
      custom-config)
    (devtools.formatters.core/body-api-call value config)))

(defn has-body [value config]
  (with-cljs-devtools-prefs custom-config
    (devtools.formatters.core/has-body-api-call value config)))

(defn get-object [jsonml]
  (.-object ^js (get jsonml 1)))

(defn get-config [jsonml]
  (.-config ^js (get jsonml 1)))

(declare jsonml->hiccup)
(declare jsonml->hiccup-with-path-annotations)

(defclass jsonml-style
  []
  {:display          :inline
   :flex-direction   :row
   :background-color (styles/syntax-color :bright :cljs-devtools :signature-background)}
  ["> span"
   {:vertical-align :text-top}]
  [:li
   {:margin 0}])

(defclass toggle-style
  [ambiance]
  {:cursor      :pointer
   :display     :inline
   :align-self  :center
   #_#_:line-height 1}
  [:button
   {:cursor :pointer
    :background :none
    :border :none}]
  [:svg :path
   {:fill (if (= ambiance :bright) styles/nord0 styles/nord5)}])

(defn data-structure [_ path]
  (let [expanded? (rf/subscribe [::app-db.subs/node-expanded? path])]
    (fn [jsonml path]
      [:span
       {:class (jsonml-style)}
       [:span {:class    (toggle-style :bright)
               :on-click #(rf/dispatch [::app-db.events/toggle-expansion path])}
        [:button
         (if @expanded?
           [material/arrow-drop-down]
           [material/arrow-right])]]
       (if (and @expanded? (has-body (get-object jsonml) (get-config jsonml)))
         (jsonml->hiccup
          (body
           (get-object jsonml)
           (get-config jsonml))
          (conj path :body))
         (jsonml->hiccup
          (header
           (get-object jsonml)
           (get-config jsonml))
          (conj path :header)))])))

(defn data-structure-with-path-annotations [_ _ _ _]
  (let [render-paths? (rf/subscribe [::app-db.subs/data-path-annotations?])]
    (fn [jsonml indexed-path devtools-path {:keys [expand? path-id] :as opts}]
      (let [node-expanded?  @(rf/subscribe [::app-db.subs/node-expanded? indexed-path])
            show-body? (and (has-body (get-object jsonml) (get-config jsonml))
                            (or (and node-expanded? (not (nil? expand?)))
                                expand?))]
        [:span
         {:class (jsonml-style)}
         [:span {:class    (toggle-style :bright)
                 :on-click #(do (rf/dispatch [::app-db.events/toggle-expansion indexed-path])
                                (rf/dispatch [::app-db.events/expand {:id path-id :expand? false}]))}
          [:button
           (if show-body?
             [material/arrow-drop-down]
             [material/arrow-right])]]
         (if show-body?
           (jsonml->hiccup-with-path-annotations
            (body
             (get-object jsonml)
             (get-config jsonml)
             {:render-paths? @render-paths?})
            (conj indexed-path :body)
            devtools-path
            opts)
           (jsonml->hiccup-with-path-annotations
            (header
             (get-object jsonml)
             (get-config jsonml))
            (conj indexed-path :header)
            devtools-path
            opts))]))))

(defn string->css
  "This function converts jsonml css-strings to valid css maps for hiccup.
  Example: 'margin-left:0px;min-height:14px;' converts to
           {:margin-left '0px', :min-height '14px'}"
  [css-string]
  (->> (string/split css-string #";")
       (map #(string/split % #":"))
       (reduce (fn [acc [property value]]
                 (assoc acc (keyword property) value)) {})))

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
        (= tag-name "annotation") (into [:span {}]
                                        (map-indexed (fn [i child] (jsonml->hiccup child (conj path i))))
                                        children)
        :else jsonml))))

(defn jsonml->hiccup-with-path-annotations
  "JSONML is the format used by Chrome's Custom Object Formatters.
  The spec is at https://docs.google.com/document/d/1FTascZXT9cxfetuPRT2eXPQKXui4nWFivUnS_335T3U/preview.

  JSONML is pretty much Hiccup over JSON. Chrome's implementation of this can be found at
  https://cs.chromium.org/chromium/src/third_party/WebKit/Source/devtools/front_end/object_ui/CustomPreviewComponent.js
"
  [jsonml indexed-path devtools-path {:keys [click-listener middle-click-listener menu-listener]
                                      :as   opts}]
  ;; indexed-path is updated on every html element such as `tagnames`
  ;; while devtools-path is updated only when we encounter an element that contains the `:path` attribute.
  (if (number? jsonml)
    jsonml
    (let [[tag-name attributes & children] jsonml
          tagnames                         #{"div" "span" "ol" "li" "table" "tr" "td"}]
      (cond
        (contains? tagnames
                   tag-name) (into [(keyword tag-name) {:style (-> (js->clj attributes)
                                                                   (get "style")
                                                                   (string->css))}]
                                   (map-indexed (fn [i child] (jsonml->hiccup-with-path-annotations
                                                               child
                                                               (conj indexed-path i)
                                                               devtools-path
                                                               opts)))
                                   children)

        (= tag-name
           "object")     [data-structure-with-path-annotations jsonml indexed-path devtools-path opts]
        (= tag-name
           "annotation") (let [;;index of the current element in the immediate parent
                               jsonml-path-index      (-> attributes
                                                          (js->clj :keywordize-keys true)
                                                          :path
                                                          last)
                               ;; path of the current visible db from root node view
                               absolute-devtools-path (if jsonml-path-index
                                                        (conj devtools-path jsonml-path-index)
                                                        devtools-path)
                               element-id             (str (random-uuid))
                               child-element          (nth children 0 nil)
                               child-value            (when (instance? js/Array child-element)
                                                        (nth child-element 2 nil))
                               child-component        (fn [i child] (jsonml->hiccup-with-path-annotations
                                                                     child
                                                                     (conj indexed-path i)
                                                                     absolute-devtools-path
                                                                     opts))]
                           (into [:span (if-not (or (string? child-value)
                                                    (number? child-value)
                                                    (keyword? child-value))
                                          {}
                                          {:id        element-id
                                           :ref       #(when %
                                                         (doto %
                                                           (goog.events/listen "contextmenu" menu-listener)
                                                           (goog.events/listen "dblclick" click-listener)
                                                           (goog.events/listen "mousedown" middle-click-listener)))
                                           :class     "path-annotation"
                                           :data-path (str absolute-devtools-path)})]
                                 (map-indexed child-component children)))
        :else            jsonml))))

(defn prn-str-render?
  [data]
  (or (string? data)
      (instance? js/RegExp data)
      (number? data)
      (boolean? data)
      (nil? data)))

(defclass prn-str-render-style
  []
  {:background-color (styles/syntax-color :bright :cljs-devtools :signature-background)
   :color            (styles/syntax-color :bright :cljs-devtools :bool)})

(defn prn-str-render
  [data]
  [:div {:class (prn-str-render-style)}
   (prn-str data)])

(defn simple-render
  [data path & [{:keys [class sort?]}]]
  (let [ns->alias             @(rf/subscribe [::settings.subs/ns->alias])
        alias?                (and (seq ns->alias)
                                   @(rf/subscribe [::settings.subs/alias-namespaces?]))
        data                  (cond-> data
                                alias? (tools.datafy/alias-namespaces ns->alias)
                                sort? tools.datafy/deep-sorted-map)]
    [rc/box
     :size  "1"
     :class (str (jsonml-style) " " class)
     :child
     (if (prn-str-render? data)
       (prn-str-render data)
       (jsonml->hiccup (header data nil) (conj path 0)))]))

(defn simple-render-with-path-annotations
  [{:keys [data path path-id sort?] :as opts}]
  (let [render-paths?         @(rf/subscribe [::app-db.subs/data-path-annotations?])
        open-new-inspectors?  @(rf/subscribe [::settings.subs/open-new-inspectors?])
        ns->alias             @(rf/subscribe [::settings.subs/ns->alias])
        alias?                (and (seq ns->alias)
                                   @(rf/subscribe [::settings.subs/alias-namespaces?]))
        data                  (cond-> data
                                alias? (tools.datafy/alias-namespaces ns->alias)
                                sort? tools.datafy/deep-sorted-map)]
    [rc/box
     :size "1"
     :class (jsonml-style)
     :child
     (if (prn-str-render? data)
       (prn-str-render data)
       (jsonml->hiccup-with-path-annotations
        (header data nil {:render-paths? render-paths?})
        ["app-db-path" path]
        (or path [])
        (merge
         opts
         {:click-listener        #(when-let [path (some-> % .-target .-parentElement (.getAttribute "data-path"))]
                                    (when (= (.-button %) 0)
                                      (rf/dispatch [::app-db.events/update-path {:id path-id
                                                                                 :path-str path}])))
          :middle-click-listener #(when-let [target (some-> % .-target .-parentElement)]
                                    (let [path (.getAttribute target "data-path")
                                          btn  (.-button %)]
                                      (.preventDefault %)
                                      (when (= btn 1)
                                        (rf/dispatch
                                         [::app-db.events/create-path-and-skip-to path open-new-inspectors?]))))
          :menu-listener         #(do (.preventDefault %)
                                      (rf/dispatch
                                       [::app-db.events/open-popup-menu
                                        {:data data
                                         :mouse-position [(.-clientX %) (.-clientY %)]
                                         :path path
                                         :data-path (some-> %
                                                            .-target
                                                            .-parentElement
                                                            (.getAttribute "data-path")
                                                            reader.edn/read-string-maybe)}]))})))]))
