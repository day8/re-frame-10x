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
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core    :refer [style]]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px]]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core    :refer [defclass]]
   [day8.re-frame-10x.components.re-com                          :as rc]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.panels.app-db.events                       :as app-db.events]
   [day8.re-frame-10x.panels.app-db.subs                         :as app-db.subs]
   [day8.re-frame-10x.fx.clipboard                               :as clipboard]
   [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core   :as r]
   [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom    :as dom]
   [day8.re-frame-10x.tools.coll                                 :as tools.coll]
   [day8.re-frame-10x.tools.datafy                               :as tools.datafy]
   [day8.re-frame-10x.tools.reader.edn                           :as reader.edn]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs])
  (:import
   [goog.dom TagName]))

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
  (.-object (get jsonml 1)))

(defn get-config [jsonml]
  (.-config (get jsonml 1)))

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

(defn data-structure-with-path-annotations [_ _ _ {:keys [path-id]}]
  (let [expand-all?   (rf/subscribe [::app-db.subs/expand-all? path-id]) ;; default is nil, false means that we have collapsed the app-db
        ;; true means we have expanded the whole db
        render-paths? (rf/subscribe [::app-db.subs/data-path-annotations?])]
    (fn [jsonml indexed-path devtools-path opts]
      (let [expanded?  (rf/subscribe [::app-db.subs/node-expanded? indexed-path])
            show-body? (and (has-body (get-object jsonml) (get-config jsonml))
                            (cond
                              @expand-all? true
                              (and @expanded? (not= @expand-all? false)) true))]
        [:span
         {:class (jsonml-style)}
         [:span {:class    (toggle-style :bright)
                 :on-click #(do (rf/dispatch [::app-db.events/toggle-expansion indexed-path])
                                (rf/dispatch [::app-db.events/set-expand-all? path-id nil]))}
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

  JSONML is pretty much Hiccup over JSON. Chrome's implementation of this can
  be found at https://cs.chromium.org/chromium/src/third_party/WebKit/Source/devtools/front_end/object_ui/CustomPreviewComponent.js
  "
  [jsonml indexed-path devtools-path {:keys [click-listener middle-click-listener menu-listener] :as opts}] ;;indexed-path
  ;; is updated on every html element such as `tagnames` while devtools-path is updated only when we encounter an
  ;; element that contains the `:path` attribute.
  (if (number? jsonml)
    jsonml
    (let [[tag-name attributes & children] jsonml
          tagnames #{"div" "span" "ol" "li" "table" "tr" "td"}]
      (cond
        (contains? tagnames tag-name) (into
                                       [(keyword tag-name) {:style (-> (js->clj attributes)
                                                                       (get "style")
                                                                       (string->css))}]
                                       (map-indexed (fn [i child] (jsonml->hiccup-with-path-annotations child (conj indexed-path i) devtools-path opts)))
                                       children)

        (= tag-name "object")         [data-structure-with-path-annotations jsonml indexed-path devtools-path opts]
        (= tag-name "annotation")     (let [jsonml-path-index       (-> attributes
                                                                        (js->clj :keywordize-keys true)
                                                                        :path
                                                                        last) ;;index of the current element in the immediate parent
                                            absolute-devtools-path  (if jsonml-path-index
                                                                      (conj devtools-path jsonml-path-index)
                                                                      devtools-path) ;; path of the current visible db from root node view
                                            element-id              (str (random-uuid))
                                            child-element           (nth children 0 nil)
                                            child-value             (when (instance? js/Array child-element)
                                                                      (nth child-element 2 nil))]
                                        ;; add menu only to strings, numbers and keywords
                                        (if (or (string? child-value)
                                                (number? child-value)
                                                (keyword? child-value))
                                          [:> (r/create-class
                                               {:component-did-mount (fn [component]
                                                                       (let [component (dom/dom-node component)]
                                                                         (goog.events/listen component "contextmenu" menu-listener)
                                                                         (goog.events/listen component "dblclick" click-listener)
                                                                         (goog.events/listen component "mousedown" middle-click-listener)))
                                                :reagent-render      (fn []
                                                                       (into [:span {:id        element-id
                                                                                     :class     "path-annotation"
                                                                                     :data-path (str absolute-devtools-path)}]
                                                                             (map-indexed (fn [i child] (jsonml->hiccup-with-path-annotations child (conj indexed-path i) absolute-devtools-path opts)) children)))})]
                                          (into [:span {}]
                                                (map-indexed (fn [i child] (jsonml->hiccup-with-path-annotations child (conj indexed-path i) absolute-devtools-path opts)) children))))
        :else                         jsonml))))

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
        data                  (cond->> data sort? tools.datafy/deep-sorted-map)
        data                  (tools.datafy/alias-namespaces data ns->alias)]
    [rc/box
     :size  "1"
     :class (str (jsonml-style) " " class)
     :child
     (if (prn-str-render? data)
       (prn-str-render data)
       (jsonml->hiccup (header data nil) (conj path 0)))]))

(def event-log (atom '()))                                  ;;stores a history of the events, treated as a stack

;; `html-element` is the html element that has received the right click
;; `app-db` is the full app db
;; `path` is the current path at the point where the popup is clicked in `data`
;; `html-target`, optional, is the element which the menus will be rendered in
(defn build-popup
  [app-db path indexed-path html-element offset-x offset-y & [html-target]]
  (let [popup-menu       (goog.ui.PopupMenu.)
          js-menu-style    (-> #js {:text-align "center"
                                    :padding    "10px"
                                    :border     "1px solid #b9bdc6"}
                               (goog.style.toStyleAttribute))
          create-menu-item (fn [menu-text]
                             (-> (goog.dom.createDom
                                  TagName.DIV
                                  #js {}
                                  (goog.dom.createDom TagName.SPAN #js {} menu-text))
                                 (doto (.setAttribute "style" js-menu-style))
                                 goog.ui.MenuItem.))
          copy-path-item   (create-menu-item "Copy path")
          copy-obj-item    (create-menu-item "Copy object")
          copy-repl-item   (create-menu-item "Copy REPL command")
          element-rect     (.getBoundingClientRect html-element)
          target-rect      (when html-target (.getBoundingClientRect html-target))
          target-x-offset  (when target-rect (+ (.-left target-rect) (.-scrollX js/window)))
          element-x-pos    (+ (.-left element-rect) (.-scrollX js/window))
          ;; element-x-pos is relative to window, so we remove offset of element we're rendering in below
          menu-x-pos       (+ offset-x
                              (if target-x-offset
                                (- element-x-pos target-x-offset)
                                element-x-pos))
          menu-y-pos       (+ offset-y (.-top element-rect) (.-scrollY js/window))]
      (doto copy-path-item
        (.addClassName "copy-path")
        (.addClassName "10x-menu-item"))
      (doto copy-obj-item
        (.addClassName "copy-object")
        (.addClassName "10x-menu-item"))
      (doto copy-repl-item
        (.addClassName "copy-repl")
        (.addClassName "10x-menu-item"))
      (doto popup-menu
        (.addItem copy-path-item)
        (.addItem copy-obj-item)
        (.addItem copy-repl-item)
        (.showAt menu-x-pos menu-y-pos)
        (.render (or html-target html-element)))            ;;if menu target is not supplied we render on clicked element
      (goog.object.forEach
       goog.ui.Component.EventType
       (fn [type]
         (goog.events.listen
          popup-menu
          type
          (fn [e]
            (cond
              (= (.-type e) "hide")
              (when (= (peek @event-log) "highlight")
                  ;; if the last event registered is 'highlight' then we should not close the dialog
                  ;; `highlight` event is dispatched right before `action`. Action would not be dispatched
                  ;; if the preceding `highlight` closes the dialog
                (.preventDefault e))

                ;; `action` is thrown after hide
                ;; `action` is thrown before unhighlight -> hide -> leave
              (= (.-type e) "action")
              (let [class-names (-> e .-target .getExtraClassNames js->clj)
                    object      (tools.coll/get-in-with-lists-and-sets app-db path)]
                (swap! event-log conj "action")
                (cond
                  (some (fn [class-name] (= class-name "copy-object")) class-names)
                  (if (or object (= object false))
                    (clipboard/copy! object)              ;; note we can't copy nil objects
                    (js/console.error "Could not copy!"))

                  (some (fn [class-name] (= class-name "copy-path")) class-names)
                  (clipboard/copy! path)

                  (some (fn [class-name] (= class-name "copy-repl")) class-names)
                  (clipboard/copy! (str "(simple-render-with-path-annotations " app-db " " ["app-db-path" indexed-path] {} ")"))))

              :else
              (swap! event-log conj (.-type e)))))))))

(defn simple-render-with-path-annotations
  [data indexed-path {:keys [object update-path-fn sort?] :as opts} & [class]]
  (let [render-paths?         (rf/subscribe [::app-db.subs/data-path-annotations?])
        open-new-inspectors?  @(rf/subscribe [::settings.subs/open-new-inspectors?])
        ns->alias             @(rf/subscribe [::settings.subs/ns->alias])
        data                  (cond->> data sort? tools.datafy/deep-sorted-map)
        data                  (tools.datafy/alias-namespaces data ns->alias)
        input-field-path      (second indexed-path)              ;;path typed in input-box
        shadow-root           (-> (.getElementById js/document "--re-frame-10x--") ;;main shadow-root html component
                                  .-shadowRoot
                                  .-children)
        root-div              (-> (filter (fn [element]     ;; root re-frame-10x parent div
                                            (= (.-tagName element) "DIV")) shadow-root)
                                  first)
        menu-html-target      (when root-div
                                (.-firstChild root-div))
        menu-html-target      (when (= (.-childElementCount menu-html-target) 2) ;; we will render menus on this element
                                (.-lastChild menu-html-target))
        ;; triggered during `contextmenu` event when a path annotation is right-clicked
        menu-listener         (fn [event]
                                ;; at this stage `data` might have changed
                                ;; we have to rely on `current-data` alias `obj`
                                (let [target   (-> event .-target .-parentElement)
                                      path     (.getAttribute target "data-path")
                                      path-obj (reader.edn/read-string-maybe path)
                                      offset-x (.-offsetX event)
                                      offset-y (.-offsetY event)]
                                  (.preventDefault event)
                                  (build-popup object path-obj indexed-path target offset-x offset-y menu-html-target)))
        ;; triggered during `click` event when a path annotation is clicked
        click-listener        (fn [event]
                                (let [target (-> event .-target .-parentElement)
                                      path   (.getAttribute target "data-path")
                                      btn    (.-button event)]
                                  (when (= btn 0)           ;;left click btn
                                    (rf/dispatch (conj update-path-fn path)))))
        ;; triggered during `mousedown` event when an element is clicked.
        middle-click-listener (fn [event]
                                (let [target (-> event .-target .-parentElement)
                                      path   (.getAttribute target "data-path")
                                      btn    (.-button event)]
                                  (.preventDefault event)
                                  (when (= btn 1)           ;;middle click btn
                                    (rf/dispatch [::app-db.events/create-path-and-skip-to path open-new-inspectors?]))))]
    [rc/box
     :size "1"
     :class (str (jsonml-style) " " class)
     :child
     (if (prn-str-render? data)
       (prn-str-render data)
       (jsonml->hiccup-with-path-annotations
        (header data nil {:render-paths? @render-paths?})
        (conj indexed-path 0)
        (or input-field-path [])
        (assoc opts
               :click-listener        click-listener
               :middle-click-listener middle-click-listener
               :menu-listener         menu-listener)))]))
