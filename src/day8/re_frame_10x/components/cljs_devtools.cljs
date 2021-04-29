(ns day8.re-frame-10x.components.cljs-devtools
  (:require-macros
    [day8.re-frame-10x.utils.macros :refer [with-cljs-devtools-prefs]])
  (:require
    [clojure.string :as string]
    [devtools.prefs]
    [devtools.formatters.core]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core   :as r]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.db   :as db]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core    :refer [style]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [em px percent]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core       :refer [defclass defglobal]]
    [day8.re-frame-10x.utils.re-com                               :as rc]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]
    [day8.re-frame-10x.settings.subs                              :as settings.subs]
    [day8.re-frame-10x.app-db.subs                                :as app-db.subs]
    [day8.re-frame-10x.app-db.events                              :as app-db.events]))

(def default-config @devtools.prefs/default-config)

(def base-config
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
                                                   :border-left [[(px 2) :solid :pink]]
                                                   :padding     [[0 styles/gs-5 0 styles/gs-5]]
                                                   :margin      [[(px 1 0 0 0)]]})
   :fn-header-style                        ""
   :fn-prefix-style                        ""
   :nil-style                              (style {:color styles/nord9})
   :keyword-style                          (style {:color styles/nord7})
   :integer-style                          (style {:color styles/nord15})
   :float-style                            (style {:color styles/nord15})
   :float-nan-style                        (style {:color styles/nord15})
   :float-infinity-style                   (style {:color styles/nord15})
   :string-style                           (style {:color styles/nord14})
   :symbol-style                           (style {:color styles/nord8})
   :bool-style                             (style {:color styles/nord10})
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
   :type-header-style                      (style {:color               :red
                                                   :padding             [[0 (px 2) 0 (px 2)]]
                                                   :-webkit-user-select :none
                                                   :border-radius       (px 2)})
   :type-name-style                        (style {:padding-right (px 4)})
   :type-basis-style                       (style {:margin-right (px 3)})
   :protocol-name-style                    (style {:position :relative})
   :fast-protocol-style                    (style {:color               :red
                                                   :position            :relative
                                                   :padding             [[0 (px 4)]]
                                                   :border-radius       (px 2)
                                                   :-webkit-user-select :none})
   :slow-protocol-style                    (style {:color               :red
                                                   :position            :relative
                                                   :padding             [[0 (px 4)]]
                                                   :border-radius       (px 2)
                                                   :-webkit-user-select :none})
   :protocol-more-style                    (style {:font-size (px 8)
                                                   :position  :relative})
   :protocol-ns-name-style                 (style {:color :red})
   :list-style                             ""
   :body-field-name-style                  (style {:color :red})
   :body-field-value-style                 (style {:margin-left (px 6)})
   :header-field-name-style                (style {:color :red})
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
   :instance-header-style                  (style {#_#_:position :relative}) ;; type-outline-style
   :expandable-wrapper-style               ""
   :standalone-type-style                  ""               ;; type-outline-style
   :instance-custom-printing-style         (style {;:position :relative
                                                   :padding  [[0 (px 2) 0 (px 4)]]})
   :instance-custom-printing-wrapper-style (style {:position      :relative
                                                   :border-radius (px 2)})
   :instance-type-header-style             (style {:color               :red
                                                   :padding             [[0 (px 2) 0 (px 2)]]
                                                   :-webkit-user-select :none
                                                   :border-radius       [[(px 2) 0 0 (px 2)]]})
   :instance-body-fields-table-style       (style {:border-spacing  0
                                                   :border-collapse :collapse
                                                   :margin-bottom   (px -2)
                                                   :display         :inline-block})
   :fields-header-style                    (style {:padding [[0 styles/gs-2]]})
   :protocol-method-name-style             (style {:margin-right (px 6)
                                                   :color        :red})
   :meta-wrapper-style                     (style {:box-shadow    [[0 0 0 (px 1) :red :inset]]
                                                   :margin-top    (px 1)
                                                   :border-radius (px 2)})
   :meta-reference-style                   (style {:background-color :pink
                                                   :border-radius    [[0 (px 2) (px 2) 0]]})
   :meta-style                             (style {:color               :red
                                                   :padding             [[0 (px 3)]]
                                                   :-webkit-user-select :none})
   :meta-body-style                        (style {:background-color           :red
                                                   :box-shadow                 [[0 0 0 (px 1) :red :inset]]
                                                   ;:position                   :relative
                                                   :top                        (px -1)
                                                   :padding                    [[(px 3) (px 12)]]
                                                   :border-bottom-right-radius (px 2)})
   :fn-ns-name-style                       (style {:color styles/nord7})
   :fn-name-style                          (style {:color        styles/nord8
                                                   :margin-right (px 2)})
   :fn-args-style                          (style {:color styles/nord9})
   :fn-multi-arity-args-indent-style       (style {:visiblity :hidden})
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
   :body-items-more-style                  (style {:background-color    :red
                                                   :min-width           styles/gs-50
                                                   :display             :inline-block
                                                   :color               :pink
                                                   :cursor              :pointer
                                                   :line-height         (px 14)
                                                   :font-size           (px 10)
                                                   :border-radius       (px 2)
                                                   :padding             [[0 (px 4) 0 (px 4)]]
                                                   :margin              [[(px 1) 0 0 0]]
                                                   :-webkit-user-select :none})
   :index-style                            (style {:min-width           styles/gs-50
                                                   :display             :inline-block
                                                   ;:text-align          :right
                                                   ;:vertical-align      :top
                                                   :background-color    styles/nord2
                                                   :color               styles/nord7
                                                   :opacity             0.5
                                                   :margin-right        styles/gs-2
                                                   :padding             [[0 styles/gs-2 0 styles/gs-2]]
                                                   :margin              [[(px 1) 0 0 0]]
                                                   :-webkit-user-select :none})
   :expanded-string-style                  (style {:padding       [[0 styles/gs-12 0 styles/gs-12]]
                                                   :color         styles/nord14
                                                   :white-space   :pre
                                                   :border        [[(px 1) :solid styles/nord3]]
                                                   :border-radius (px 1)
                                                   :margin        [[0 0 (px 2) 0]]})
   :default-envelope-style                 ""})





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

(defn api-call [api-fn ambiance & args]
  (let [config   (merge default-config
                        #_base-config
                        #_(if (= ambiance :bright)
                            bright-ambiance-config
                            dark-ambiance-config))]
    (with-cljs-devtools-prefs config (apply api-fn args))))

(defn header [ambiance & args]
  (apply api-call devtools.formatters.core/header-api-call ambiance args))

(defn body [ambiance & args]
  (apply api-call devtools.formatters.core/body-api-call ambiance args))

(defn has-body [ambiance & args]
  (apply api-call devtools.formatters.core/has-body-api-call ambiance args))

(defn get-object [jsonml]
  (.-object (get jsonml 1)))

(defn get-config [jsonml]
  (.-config (get jsonml 1)))

(declare jsonml->hiccup)

(defclass jsonml-style
  [ambiance]
  {:display        :flex
   :flex-direction :row}
  #_["> span"
     {:vertical-align :text-top}]
  [:li
   {:margin 0}])

(defclass toggle-style
  [ambiance]
  {:cursor      :pointer
   :display     :flex
   :align-self  :center
   #_#_:line-height 1}
  [:button
   {:cursor :pointer}]
  [:svg :path
   {:fill (if (= ambiance :bright) styles/nord0 styles/nord5)}])

(defn data-structure [jsonml path]
  (let [ambiance  (rf/subscribe [::settings.subs/ambiance])
        expanded? (rf/subscribe [::app-db.subs/node-expanded? path])]
    (fn [jsonml path]
      [:span
       {:class (jsonml-style @ambiance)}
       [:span {:class    (toggle-style @ambiance)
               :on-click #(rf/dispatch [::app-db.events/toggle-expansion path])}
        [:button
         (if @expanded?
           [material/arrow-drop-down]
           [material/arrow-right])]]
       (if (and @expanded? (has-body @ambiance (get-object jsonml) (get-config jsonml)))
         (jsonml->hiccup
           (body
             @ambiance
             (get-object jsonml)
             (get-config jsonml))
           (conj path :body))
         (jsonml->hiccup
           (header
             @ambiance
             (get-object jsonml)
             (get-config jsonml))
           (conj path :header)))])))

(defn string->css [css-string]
  "This function converts jsonml css-strings to valid css maps for hiccup.
  Example: 'margin-left:0px;min-height:14px;' converts to
           {:margin-left '0px', :min-height '14px'}"

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
        :else jsonml))))

(defn prn-str-render?
  [data]
  (or (string? data)
      (instance? js/RegExp data)
      (number? data)
      (boolean? data)
      (nil? data)))

(defclass prn-str-render-style
  [ambiance syntax-color-scheme]
  {:padding          styles/gs-5
   :background-color (styles/syntax-color ambiance syntax-color-scheme :signature-background)
   :color            (styles/syntax-color ambiance syntax-color-scheme :bool)})

(defn prn-str-render
  [data]
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])]
    [:div {:class (prn-str-render-style ambiance syntax-color-scheme)}
     (prn-str data)]))

(defn simple-render [data path & [class]]
  (let [ambiance (rf/subscribe [::settings.subs/ambiance])]
    (fn [data]
      [rc/box
       :size  "1"
       :class (str (jsonml-style @ambiance) " " class)
       :child
       (if (prn-str-render? data)
         (prn-str-render data)
         (jsonml->hiccup (header @ambiance data) (conj path 0)))])))