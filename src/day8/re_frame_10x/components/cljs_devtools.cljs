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
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
    [day8.re-frame-10x.fx.clipboard                               :as clipboard]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core   :as r]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom    :as dom]
    [day8.re-frame-10x.tools.reader.edn                           :as reader.edn])
  (:import
    [goog.dom TagName]))

(def default-config @devtools.prefs/default-config)

(defn base-config
  [ambiance syntax-color-scheme]
  {:index-tag                              [:span :none-style]
   :min-expandable-sequable-count-for-well-known-types
                                           3
   ; Hide the index spans on the left hand of collections. Shows how many elements in a collection.
   :none-style                             (style {:display :none})

   ; Our JSON renderer does not have hierarchy depth limit,
   ; See https://github.com/binaryage/cljs-devtools/blob/master/src/lib/devtools/formatters/budgeting.cljs
   :initial-hierarchy-depth-budget         false

   :header-style                           (style {:white-space :nowrap})
   :expandable-style                       (style {:white-space  :nowrap
                                                   :padding-left styles/gs-2})
   :expandable-inner-style                 (style {:margin-left (px -2)})
   :item-style                             (style {:display     :inline-block
                                                   :white-space :nowrap
                                                   :border-left [[(px 2) :solid :#000]]
                                                   :padding     [[0 styles/gs-5 0 styles/gs-5]]
                                                   :margin      [[(px 1 0 0 0)]]})
   :fn-header-style                        ""
   :fn-prefix-style                        ""
   :nil-style                              (style {:color (styles/syntax-color ambiance syntax-color-scheme :nil)})
   :keyword-style                          (style {:color (styles/syntax-color ambiance syntax-color-scheme :keyword)})
   :integer-style                          (style {:color (styles/syntax-color ambiance syntax-color-scheme :integer)})
   :float-style                            (style {:color (styles/syntax-color ambiance syntax-color-scheme :float)})
   :float-nan-style                        (style {:color (styles/syntax-color ambiance syntax-color-scheme :float-nan)})
   :float-infinity-style                   (style {:color (styles/syntax-color ambiance syntax-color-scheme :float-infinity)})
   :string-style                           (style {:color (styles/syntax-color ambiance syntax-color-scheme :string)})
   :symbol-style                           (style {:color (styles/syntax-color ambiance syntax-color-scheme :symbol)})
   :bool-style                             (style {:color (styles/syntax-color ambiance syntax-color-scheme :bool)})
   :native-reference-wrapper-style         (style {:position :relative
                                                   :display  :inline-block})
   :native-reference-style                 (style {:padding  [[0 (px 3)]]
                                                   :margin   [[(px -4) 0 (px -2)]]
                                                   :position :relative
                                                   :top      (px 1)})
   :type-wrapper-style                     (style {:position      :relative
                                                   :padding-left  (px 1)
                                                   :border-radius (px 2)})
   :type-ref-style                         (style {:position :relative})
   :type-header-style                      (style {:color               (styles/syntax-color ambiance syntax-color-scheme :type-text)
                                                   :padding             [[0 (px 2) 0 (px 2)]]
                                                   :-webkit-user-select :none
                                                   :border-radius       (px 2)})
   :type-name-style                        (style {:padding-right (px 4)})
   :type-basis-style                       (style {:margin-right (px 3)})
   :protocol-name-style                    (style {:position :relative})
   :fast-protocol-style                    (style {:color               (styles/syntax-color ambiance syntax-color-scheme :fast-protocol)
                                                   :position            :relative
                                                   :padding             [[0 (px 4)]]
                                                   :border-radius       (px 2)
                                                   :-webkit-user-select :none})
   :slow-protocol-style                    (style {:color               (styles/syntax-color ambiance syntax-color-scheme :slow-protocol)
                                                   :position            :relative
                                                   :padding             [[0 (px 4)]]
                                                   :border-radius       (px 2)
                                                   :-webkit-user-select :none})
   :protocol-more-style                    (style {:font-size (px 8)
                                                   :position  :relative})
   :protocol-ns-name-style                 (style {:color (styles/syntax-color ambiance syntax-color-scheme :ns)})
   :list-style                             ""
   :body-field-name-style                  (style {:color (styles/syntax-color ambiance syntax-color-scheme :field)})
   :body-field-value-style                 (style {:margin-left (px 6)})
   :header-field-name-style                (style {:color (styles/syntax-color ambiance syntax-color-scheme :field)})
   :body-field-td1-style                   (style {:vertical-align :top
                                                   :padding        0
                                                   :padding-right  (px 4)})
   :body-field-td2-style                   (style {:vertical-align :top
                                                   :padding        0})
   :body-field-td3-style                   (style {:vertical-align :top
                                                   :padding        0})
   ;type-outline-style (css (str "box-shadow: 0px 0px 0px 1px " (named-color :type 0.5) " inset;")
   ;     "margin-top: 1px;"
   ;     "border-radius: 2px;")
   ;:instance-header-style                  (style {#_#_:position :relative}) ;; type-outline-style
   ;:expandable-wrapper-style               ""
   ;:standalone-type-style                  ""               ;; type-outline-style
   :instance-custom-printing-style         (style {:position :relative
                                                   :padding  [[0 (px 2) 0 (px 4)]]})
   :instance-custom-printing-wrapper-style (style {:position      :relative
                                                   :border-radius (px 2)})
   :instance-type-header-style             (style {:color               (styles/syntax-color ambiance syntax-color-scheme :type-text)
                                                   :padding             [[0 (px 2) 0 (px 2)]]
                                                   :-webkit-user-select :none
                                                   :border-radius       [[(px 2) 0 0 (px 2)]]})
   :instance-body-fields-table-style       (style {:border-spacing  0
                                                   :border-collapse :collapse
                                                   :margin-bottom   (px -2)
                                                   :display         :inline-block})
   :fields-header-style                    (style {:padding [[0 styles/gs-2]]})
   :protocol-method-name-style             (style {:margin-right (px 6)
                                                   :color        (styles/syntax-color ambiance syntax-color-scheme :protocol)})
   :meta-wrapper-style                     (style {:box-shadow    [[0 0 0 (px 1) (styles/syntax-color ambiance syntax-color-scheme :meta) :inset]]
                                                   :margin-top    (px 1)
                                                   :border-radius (px 2)})
   :meta-reference-style                   (style {:background-color (styles/syntax-color ambiance syntax-color-scheme :meta)
                                                   :border-radius    [[0 (px 2) (px 2) 0]]})
   :meta-style                             (style {:color               (styles/syntax-color ambiance syntax-color-scheme :meta-text)
                                                   :padding             [[0 (px 3)]]
                                                   :-webkit-user-select :none})
   :meta-body-style                        (style {:background-color           (styles/syntax-color ambiance syntax-color-scheme :meta)
                                                   :box-shadow                 [[0 0 0 (px 1) (styles/syntax-color ambiance syntax-color-scheme :meta) :inset]]
                                                   :position                   :relative
                                                   :top                        (px -1)
                                                   :padding                    [[(px 3) (px 12)]]
                                                   :border-bottom-right-radius (px 2)})
   :fn-ns-name-style                       (style {:color (styles/syntax-color ambiance syntax-color-scheme :ns)})
   :fn-name-style                          (style {:color        (styles/syntax-color ambiance syntax-color-scheme :fn)
                                                   :margin-right (px 2)})
   :fn-args-style                          (style {:color (styles/syntax-color ambiance syntax-color-scheme :fn-args)})
   :fn-multi-arity-args-indent-style       (style {:visibility :hidden})
   :standard-ol-style                      (style {:list-style-type :none
                                                   :padding-left    0
                                                   :margin-bottom   0
                                                   :margin-left     0})
   :standard-ol-no-margin-style            (style {:list-style-type :none
                                                   :padding-left    0
                                                   :margin-top      0
                                                   :margin-bottom   0
                                                   :margin-left     0})
   :standard-li-style                      (style {:margin-left 0
                                                   :min-height  (px 14)})
   :standard-li-no-margin-style            (style {:margin-left 0
                                                   :min-height  (px 14)})
   :aligned-li-style                       (style {:margin-left 0
                                                   :min-height  (px 14)})
   :body-items-more-style                  (style {:background-color    (styles/syntax-color ambiance syntax-color-scheme :more-background)
                                                   :min-width           styles/gs-50
                                                   :display             :inline-block
                                                   :color               (styles/syntax-color ambiance syntax-color-scheme :more)
                                                   :cursor              :pointer
                                                   :line-height         (px 14)
                                                   :font-size           (px 10)
                                                   :border-radius       (px 2)
                                                   :padding             [[0 (px 4) 0 (px 4)]]
                                                   :margin              [[(px 1) 0 0 0]]
                                                   :-webkit-user-select :none})
   :index-style                            (style {:min-width           styles/gs-50
                                                   :display             :inline-block
                                                   :text-align          :right
                                                   :vertical-align      :text-top
                                                   :background-color    (styles/syntax-color ambiance syntax-color-scheme :index-background)
                                                   :color               (styles/syntax-color ambiance syntax-color-scheme :index)
                                                   :opacity             0.5
                                                   :margin-right        styles/gs-2
                                                   :padding             [[0 styles/gs-2 0 styles/gs-2]]
                                                   :margin              [[(px 1) 0 0 0]]
                                                   :-webkit-user-select :none})
   :expanded-string-style                  (style {:padding          [[0 styles/gs-12 0 styles/gs-12]]
                                                   :color            (styles/syntax-color ambiance syntax-color-scheme :string)
                                                   :white-space      :pre
                                                   :border           [[(px 1) :solid (styles/syntax-color ambiance syntax-color-scheme :expanded-string-border)]]
                                                   :border-radius    (px 1)
                                                   :margin           [[0 0 (px 2) 0]]
                                                   :background-color (styles/syntax-color ambiance syntax-color-scheme :expanded-string-background)})
   :default-envelope-style                 ""
   ;; Setting this prevents https://github.com/day8/re-frame-10x/issues/321
   :max-number-body-items                  10000})





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
  (merge default-config (base-config :bright :cljs-devtools) #_bright-ambiance-config))

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

(defn data-structure-with-path-annotations [_ indexed-path devtools-path {:keys [update-path-fn object] :as opts}]
  (let [expanded? (rf/subscribe [::app-db.subs/node-expanded? indexed-path])]
    (fn [jsonml indexed-path]
      [:span
       {:class (jsonml-style)}
       [:span {:class    (toggle-style :bright)
               :on-click #(rf/dispatch [::app-db.events/toggle-expansion indexed-path])}
        [:button
         (if @expanded?
           [material/arrow-drop-down]
           [material/arrow-right])]]
       (if (and @expanded? (has-body (get-object jsonml) (get-config jsonml)))
         (jsonml->hiccup-with-path-annotations
           (body
             (get-object jsonml)
             (get-config jsonml)
             {:render-paths? true})
           (conj indexed-path :body)
           devtools-path
           opts)
         (jsonml->hiccup-with-path-annotations
           (header
             (get-object jsonml)
             (get-config jsonml))
           (conj indexed-path :header)
           devtools-path
           opts))])))

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
  [jsonml indexed-path devtools-path {:keys [update-path-fn object click-listener menu-listener] :as opts}]
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

        (= tag-name "object") [data-structure-with-path-annotations jsonml indexed-path devtools-path opts]
        (= tag-name "annotation") (let [index         (-> attributes
                                                          (js->clj :keywordize-keys true)
                                                          :path
                                                          last)
                                        devtools-path (if index (conj devtools-path index) devtools-path)
                                        id            (-> (random-uuid) str)
                                        child-element (nth children 0 nil)
                                        child-value   (when (instance? js/Array child-element)
                                                        (nth child-element 2 nil))]
                                    ;; add menu only to strings, numbers and keywords
                                    (if (or (string? child-value)
                                            (number? child-value)
                                            (keyword? child-value))
                                      [:> (r/create-class
                                            {:component-did-mount (fn [component]
                                                                    (let [component (dom/dom-node component)]
                                                                      (goog.events/listen component "contextmenu" (partial menu-listener object))
                                                                      (goog.events/listen component "click" click-listener)))
                                             :reagent-render      (fn []
                                                                    (into [:span {:id        id
                                                                                  :class     "path-annotation"
                                                                                  :data-path (str devtools-path)}]
                                                                          (map-indexed (fn [i child] (jsonml->hiccup-with-path-annotations child (conj indexed-path i) devtools-path opts)) children)))})]
                                      (into [:span {}]
                                            (map-indexed (fn [i child] (jsonml->hiccup-with-path-annotations child (conj indexed-path i) devtools-path opts)) children))))
        :else jsonml))))

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
  [data path & [class]]
  [rc/box
   :size  "1"
   :class (str (jsonml-style) " " class)
   :child
   (if (prn-str-render? data)
     (prn-str-render data)
     (jsonml->hiccup (header data nil) (conj path 0)))])

(def popup-menus (atom {}))                                 ;; stores all the current rendered menus to prevent re-rendering the same menu twice
(def event-log (atom (list )))                              ;;stores a history of the events, treated as a stack

;; `html-element` is the html element that has received the right click
;; `data` is the clj data from db that is passed to devtools
;; `path` is the current path at the point where the popup is clicked in `data`
;; `viewing path` is the path that is filled in the input box (if any) when the popup is opening
(defn build-popup
  [data path viewing-path html-element]
  (if-let [rendered? (get @popup-menus (.-id html-element))]
    (.setVisible rendered? true)                            ;; we have already rendered the menu, proceed to display it
    (let [popup-menu       (goog.ui.PopupMenu.)
          js-menu-style    (-> #js {:text-align "center"
                                    :padding    "10px 0"
                                    :border     "1px solid black"}
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
          x-pos            (+ (.-left element-rect) (.-scrollX js/window))
          y-pos            (+ (.-top element-rect) (.-scrollY js/window))]
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
        (.showAt x-pos y-pos)
        (.render html-element))
      (goog.object.forEach
        goog.ui.Component.EventType
        (fn [type]
          (goog.events.listen
            popup-menu
            type
            (fn [e] (cond
                      (= (.-type e) "hide")
                      (when-not (or (empty? @event-log) (= (peek @event-log) "unhighlight"))
                        ;; if the last event registered is 'unhighlight' then we should close the dialog
                        ;; `unhighlight` is dispatched when the mouse leaves a menu item
                        ;; if the mouse icon would scroll over another item in the menu, a `highlight` event would
                        ;; overwrite the last `unhighlight`
                        (.preventDefault e))

                      (= (.-type e) "action")
                      (let [class-names (-> e .-target .getExtraClassNames js->clj)]
                        (swap! event-log conj "action")
                        (cond
                          (some (fn [class-name] (= class-name "copy-object")) class-names)
                          (let [path-obj (reader.edn/read-string-maybe path)
                                nested?  (when viewing-path
                                           (count viewing-path))
                                path-obj (if nested?
                                           (subvec path-obj nested?) ;; both path and viewing-path are from the root point of view
                                           path-obj)
                                object   (get-in data path-obj)]
                            ;; note we can't copy nil objects
                            (if (or object (and (not (nil? object)) (= object false)))
                              (clipboard/copy! object)
                              (js/console.error "Could not copy!")))

                          (some (fn [class-name] (= class-name "copy-path")) class-names)
                          (clipboard/copy! path)

                          (some (fn [class-name] (= class-name "copy-repl")) class-names)
                          (clipboard/copy! (str "(simple-render-with-path-annotations " data " " ["app-db-path" path] {} ")"))))

                      :else
                      (swap! event-log conj (.-type e)))))))
      (swap! popup-menus assoc (.-id html-element) popup-menu))))

(def current-data (atom nil))

(defn simple-render-with-path-annotations
  [data indexed-path {:keys [update-path-fn sort?] :as opts} & [class]]
  (when (not= @current-data data)
    (reset! current-data data))
  (let [data            (if (and sort? (map? data)) (into (sorted-map) data) data)
        devtools-path   (second indexed-path)
        ;; triggered during `contextmenu` event when a path annotation is right clicked
        menu-listener   (fn [obj event]
                          ;; at this stage `data` might have changed
                          ;; we have to rely on `current-data` alias `obj`
                          (let [target (-> event .-target .-parentElement)
                                path   (.getAttribute target "data-path")]
                            (.preventDefault event)
                            (build-popup @obj path devtools-path target)))
        ;; triggered during `click` event when a path annotation is clicked
        click-listener  (fn [event]
                          (let [target (-> event .-target .-parentElement)
                                path   (.getAttribute target "data-path")
                                btn    (.-button event)]
                            (when (= btn 0)                 ;;left click btn
                              (rf/dispatch (conj update-path-fn path)))))]
    [rc/box
     :size "1"
     :class (str (jsonml-style) " " class)
     :child
     (if (prn-str-render? data)
       (prn-str-render data)
       (jsonml->hiccup-with-path-annotations
         (header data nil {:render-paths? true})
         (conj indexed-path 0)
         (or devtools-path [])
         (assoc opts
           :object current-data
           :click-listener click-listener
           :menu-listener menu-listener)))]))