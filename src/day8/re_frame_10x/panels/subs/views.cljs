(ns day8.re-frame-10x.panels.subs.views
  (:require-macros
    [day8.re-frame-10x.components.re-com                          :refer [handler-fn]])
  (:require
    [clojure.data]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core    :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px percent]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color   :as color]
    [day8.re-frame-10x.components.buttons                         :as buttons]
    [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
    [day8.re-frame-10x.components.data                            :as data]
    [day8.re-frame-10x.components.hyperlinks                      :as hyperlinks]
    [day8.re-frame-10x.components.inputs                          :as inputs]
    [day8.re-frame-10x.components.re-com                          :as rc :refer [css-join]]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]
    [day8.re-frame-10x.panels.app-db.views                        :as app-db.views :refer [pod-gap pod-padding pod-border-edge
                                                                                           pod-header-section]]
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
    [day8.re-frame-10x.panels.subs.events                         :as subs.events]
    [day8.re-frame-10x.panels.subs.subs                           :as subs.subs]
    [day8.re-frame-10x.tools.string                               :as tools.string]))

(def tag-types
  {:sub/create  {:long "CREATED"  :short "C"}
   :sub/dispose {:long "DISPOSED" :short "D"}
   :sub/run     {:long "RUN"      :short "R"}
   :sub/not-run {:long "NOT-RUN"  :short "N"}
   nil          {:long "NIL"      :short "NIL"}})

(defn long-tag-desc [type]
  (get-in tag-types [type :long] (str type)))

(defn short-tag-desc [type]
  (get-in tag-types [type :short] (str type)))

(defn sub-type->color
  [type]
  (case type
    :sub/create  styles/nord15
    :sub/dispose styles/nord12
    :sub/run     styles/nord14
    :sub/not-run styles/nord9
    styles/nord5))

(defclass sub-tag-style
  [_ type]
  {:color            :#fff
   :background-color (sub-type->color type)
   :border           [[(px 1) :solid (color/darken (sub-type->color type) 10)]]})

(defclass sub-tag-short-style
  [ambiance type]
  {:composes (sub-tag-style ambiance type)
   :width    styles/gs-19})

(defn short-sub-tag [type label]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [data/tag (sub-tag-short-style ambiance type) label]))

(defn title-tag [type title label]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     :align    :center
     :gap      styles/gs-2s
     :children
     [[:span {:style {:font-size "9px"}} title]
      [data/tag (sub-tag-style ambiance type) label]]]))

(defclass panel-header-style
  [_]
  {:margin-bottom styles/gs-19
   :flex-flow     [[:row :wrap]]})

(defclass summary-style
  [ambiance]
  {:composes (styles/frame-1 ambiance)
   :padding  [[0 styles/gs-19]]})

(defn panel-header []
  (let [ambiance                  @(rf/subscribe [::settings.subs/ambiance])
        created-count             (rf/subscribe [::subs.subs/created-count])
        re-run-count              (rf/subscribe [::subs.subs/re-run-count])
        destroyed-count           (rf/subscribe [::subs.subs/destroyed-count])
        not-run-count             (rf/subscribe [::subs.subs/not-run-count])
        ignore-unchanged-l2-subs? (rf/subscribe [::subs.subs/ignore-unchanged-l2-subs?])
        ignore-unchanged-l2-count (rf/subscribe [::subs.subs/unchanged-l2-subs-count])]
    [rc/h-box
     :class    (panel-header-style ambiance)
     :justify  :between
     :align    :center
     :children
     [[rc/h-box
       :align   :center
       :gap     styles/gs-19s
       :height  styles/gs-50s
       :class   (summary-style ambiance)
       :children
       [[:span {:style {:font-size   "18px"
                        :font-weight "lighter"}}
         "Summary:"]
        [title-tag :sub/create  (long-tag-desc :sub/create)  @created-count]
        [title-tag :sub/run     (long-tag-desc :sub/run)     @re-run-count]
        [title-tag :sub/dispose (long-tag-desc :sub/dispose) @destroyed-count]
        [title-tag :sub/not-run (long-tag-desc :sub/not-run) @not-run-count]]]
      [rc/h-box
       :align    :center
       :gap      styles/gs-19s
       :height   styles/gs-50s
       :class    (summary-style ambiance)
       :children
       [[rc/checkbox
         :model ignore-unchanged-l2-subs?
         :label [:span
                 "Ignore " [:b {:style {:font-weight "700"}} @ignore-unchanged-l2-count] " unchanged" [:br]
                 [rc/hyperlink
                  :class (styles/hyperlink ambiance)
                  :label (str "layer 2 " (tools.string/pluralize- @ignore-unchanged-l2-count "sub"))
                  :href  "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md"]]
         :style {:margin-top "6px"}
         :on-change #(rf/dispatch [::subs.events/ignore-unchanged-l2-subs? %])]]]]]))

(defclass path-header-style
  [ambiance]
  {:background-color (styles/background-color-2 ambiance)
   :color            (styles/color-2 ambiance)
   :margin           styles/gs-2
   :height           styles/gs-31
   :border-right     [[(px 1) :solid styles/nord4]]
   ;; Fixes https://github.com/day8/re-frame-10x/issues/320
   :white-space      :nowrap})

(defclass layer-circle-style
  [_]
  {:width            styles/gs-19s
   :height           styles/gs-19s
   :border           pod-border-edge
   :border-radius    (percent 50)
   :margin           :auto
   :background-color :#fff})

(defn pod-header [{:keys [id layer path open? diff? pin? order value]}]
  ;; TODO: highlight when pin? is true
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (styles/section-header ambiance)
     :align    :center
     :height   styles/gs-31s
     :children
     [[pod-header-section
       :children
       [[rc/box
         :width  styles/gs-31s
         :height styles/gs-31s
         :class  "noselect"
         :style  {:cursor "pointer"}
         :attr   {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                  :on-click (handler-fn (rf/dispatch [::subs.events/open-pod? id (not open?)]))}
         :child
         [rc/box
          :margin "auto"
          :child
          [:span.arrow
           (if open?
             [material/arrow-drop-down]
             [material/arrow-right])]]]]]

      #_[rc/box
         ;:width "64px"                                ;; (100-36)px from box above
         :child [sub-tag (first order) (short-tag-desc (first order))]]

      ;; TODO: report if a sub was run multiple times
      #_(when run-times
          [:span "Warning: run " run-times " times"])
      [rc/h-box
       :class    (path-header-style ambiance)
       :size     "auto"
       :align    :center
       :children
       [[rc/label
         :label path]]]

      [pod-header-section
       :min-width styles/gs-50s
       :children
       [[rc/label :label (str id)]]]

      [pod-header-section
       :min-width "106px"                           ;; styles/gs-131s - (2 * 12px padding) - (1px border)
       :gap       styles/gs-12s
       :style     {:padding "0px 12px"}
       :children
       (into []
             (comp
               (take 3)
               (map (fn [o] [short-sub-tag o (short-tag-desc o)])))
             order)]

      [pod-header-section
       :width    styles/gs-31s
       :children
       [[rc/box
         :class (layer-circle-style ambiance)
         :child
         (if (some? layer)
           [:div {:style {:margin "auto"}} layer]
           [hyperlinks/info
            "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md#why-do-i-sometimes-see-layer--when-viewing-a-subscription"])]]]

      [pod-header-section
       :width styles/gs-50s
       :attr  {:on-click (handler-fn (rf/dispatch [::subs.events/set-pinned id (not pin?)]))}
       :children
       [[rc/box
         :style {:margin "auto"}
         :child
         [rc/checkbox
          :model pin?
          :label ""
          :style {:margin-left "6px"
                  :margin-top  "1px"}
          :on-change #(rf/dispatch [::subs.events/set-pinned id (not pin?)])]]]]

      [pod-header-section
       :width    styles/gs-50s
       :attr     {:on-click (handler-fn (rf/dispatch [::subs.events/set-diff-visibility id (not diff?)]))}
       :children
       [[rc/box
         :style {:margin "auto"}
         :child
         [rc/checkbox
          :model     diff?
          :label     ""
          :style     {:margin-left "6px"
                      :margin-top  "1px"}
          :on-change #(rf/dispatch [::subs.events/set-diff-visibility id (not diff?)])]]]]

      [pod-header-section
       :width    styles/gs-31s
       :attr     {:on-click #(js/console.log value)}
       :last?    true
       :children
       [[rc/box
         :style {:margin "auto"}
         :child
         [buttons/icon {:icon [material/print]
                        :on-click #()}]]]]]]))

(defclass sub-message-style
  []
  {:font-style :italic
   :padding    styles/gs-5})

(defn no-previous-value-message []
  [rc/label :class (sub-message-style) :label "No previous value exists to diff"])

(defn unchanged-value-message
  []
  [rc/label :class (sub-message-style) :label "Subscription value is unchanged"])

(defn sub-not-run-message
  []
  [rc/label :class (sub-message-style) :label "Subscription not run, so no diff is available"])

(defn not-run-yet-message
  []
  [rc/label :class (sub-message-style) :label "Subscription not run yet, so no value is available"])

(defclass sub-data-style
  [ambiance]
  {:overflow-x   :auto
   :overflow-y   :hidden
   :border-left  (styles/border-2 ambiance)
   :border-right (styles/border-2 ambiance)})

(defclass diff-header-style
  [ambiance]
  {:composes     (styles/colors-2 ambiance)
   :border-left  (styles/border-2 ambiance)
   :border-right (styles/border-2 ambiance)})

(defclass diff-data-style
  [ambiance]
  {:overflow-x   :auto
   :overflow-y   :hidden
   :border-left  (styles/border-2 ambiance)
   :border-right (styles/border-2 ambiance)})

(defn pod [{:keys [path open? diff?] :as pod-info}]
  (let [ambiance        @(rf/subscribe [::settings.subs/ambiance])
        render-diff?    (and open? diff?)
        value?          (contains? pod-info :value)
        previous-value? (contains? pod-info :previous-value)]
    [rc/v-box
     :style {:margin-bottom pod-gap
             :margin-right  "1px"}
     :children
     [[pod-header pod-info]
      [rc/v-box
       :children
       [(when open?
          (let [main-value (:value pod-info)
                #_(cond value? (:value pod-info)
                        previous-value? (:previous-value pod-info)
                        :else nil)]
            [rc/v-box
             :class    (sub-data-style ambiance)
             :children
             [(if value?  ;(or value? #_previous-value?)
                [cljs-devtools/simple-render
                 main-value
                 ["sub-path" path]]
                [not-run-yet-message])]]))

        (when render-diff?
          (let [diffable?        (and value? previous-value?)
                not-run?         (= (:order pod-info) [:sub/not-run])
                previous-value   (:previous-value pod-info)
                value            (:value pod-info)
                unchanged-value? (get-in pod-info [:sub/traits :unchanged?] false)
                [diff-before diff-after _] (clojure.data/diff previous-value value)]
            [rc/v-box
             :children
             [[rc/v-box
               :class    (diff-header-style ambiance)
               :justify  :end
               :children
               [[rc/hyperlink
                 :class  (styles/hyperlink ambiance)
                 :label  "ONLY BEFORE"
                 :style  {:margin-left styles/gs-7s}
                 :attr   {:rel "noopener noreferrer"}
                 :target "_blank"
                 :href   app-db.views/diff-url]]]
              [rc/v-box
               :class    (diff-data-style ambiance)
               :children
               [(cond
                  not-run?         [sub-not-run-message]
                  unchanged-value? [unchanged-value-message]
                  diffable?        [cljs-devtools/simple-render
                                    diff-before
                                    ["app-db-diff" path]]
                  :else            [no-previous-value-message])]]
              [rc/v-box
               :class    (diff-header-style ambiance)
               :justify  :end
               :children
               [[rc/hyperlink
                 :class  (styles/hyperlink ambiance)
                 :label  "ONLY AFTER"
                 :style  {:margin-left      styles/gs-7s}
                 :attr   {:rel "noopener noreferrer"}
                 :target "_blank"
                 :href   app-db.views/diff-url]]]
              [rc/v-box
               :class    (diff-data-style ambiance)
               :children
               [(cond
                  not-run?         [sub-not-run-message]
                  unchanged-value? [unchanged-value-message]
                  diffable?        [cljs-devtools/simple-render
                                    diff-after
                                    ["app-db-diff" path]]
                  :else            [no-previous-value-message])]]]]))
        (when open?
          [rc/gap-f :size pod-padding])]]]]))


(defn no-pods []
  [rc/h-box
   :margin     (css-join "0px 0px 0px" styles/gs-19s)
   :gap        styles/gs-7s
   :align      :start
   :align-self :start
   :children   [[rc/label :label "There are no subscriptions to show"]]])


(defclass column-title-label-style
  []
  {:font-size (px 9)})

(defn pod-header-column-titles
  []
  [rc/h-box
   :height   styles/gs-19s
   :align    :center
   :style    {:margin-right "1px"}
   :children [[rc/box
               :width styles/gs-31s
               :child ""]
              [rc/box
               :size    "1"
               :justify :center
               :child
               [rc/label :class (column-title-label-style) :label "ID"]]
              [rc/box
               :width styles/gs-50s
               :child ""]
              [rc/box
               :width   "132px"                               ;; styles/gs-131s + 1 border
               :justify :center
               :child
               [rc/label :class (column-title-label-style) :label "ACTIVITY"]]
              [rc/box
               :width   "32px"                                ;; styles/gs-31s + 1 border
               :justify :center
               :child
               [rc/label :class (column-title-label-style) :label "LAYER"]]
              [rc/box
               :width   "51px"                                ;;  50px + 1 border
               :justify :center
               :child
               [rc/label :class (column-title-label-style) :label "PINNED"]]
              [rc/box
               :width   "51px"                                ;;  50px + 1 border
               :justify :center
               :child
               [rc/label :class (column-title-label-style) :label "DIFFS"]]
              [rc/box
               :width   "32px"                                ;; styles/gs-31s + 1 border
               :justify :center
               :child
               [rc/label :class (column-title-label-style) :label ""]]
              [rc/gap-f :size "6px"]]])                     ;; Add extra space to look better when there is/aren't scrollbars

(defclass pod-section-style
  [_]
  {:overflow-y :auto})

(defn pod-section []
  (let [ambiance         @(rf/subscribe [::settings.subs/ambiance])
        visible-subs     @(rf/subscribe [::subs.subs/visible-subs])
        intra-epoch-subs @(rf/subscribe [::subs.subs/intra-epoch-subs])
        sub-expansions   @(rf/subscribe [::subs.subs/sub-expansions])
        sub-pins         @(rf/subscribe [::subs.subs/sub-pins])
        all-subs         (if @(rf/subscribe [::settings.subs/debug?])
                           (cons {:path [::subs.subs/current-epoch-sub-state] :id "debug" :value @(rf/subscribe [::subs.subs/current-epoch-sub-state])} visible-subs)
                           visible-subs)]
    [rc/v-box
     :size     "1"
     :class    (pod-section-style ambiance)
     :children
     [(if (empty? all-subs)
        [no-pods]
        [pod-header-column-titles])
      [rc/v-box
       :size "auto"
       :style {:overflow-x "hidden"
               :overflow-y "auto"}
       :children
       [(for [p all-subs]
          ^{:key (:id p)}
          [pod (merge p
                      (get sub-expansions (:id p))
                      (get sub-pins (:id p)))])
        (when (seq intra-epoch-subs)
          (list
            ^{:key "intra-epoch-line"}
            [rc/line :size styles/gs-2s
             :style {:margin "19px 0px"}]
            ^{:key "intra-epoch-title"}
            [:h2 {:class "bm-heading-text"
                  :style {:margin "19px 0px"}}
             [rc/hyperlink
              :class (styles/hyperlink ambiance)
              :href  "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/IntraEpoch.md"
              :label "Intra-Epoch Subscriptions"]]
            (for [p intra-epoch-subs]
              ^{:key (:id p)}
              [pod (merge p (get sub-expansions (:id p)))])))]]]]))


(defn filter-section []
  [inputs/search
   {:placeholder "filter subs"
    :on-change   #(rf/dispatch [::subs.events/set-filter
                                (-> % .-target .-value)])}])

(defclass panel-style
  [_]
  {:margin-right styles/gs-5
   :width        (percent 100)})

(defn panel []
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     :class    (panel-style ambiance)
     :size     "1"
     :children
     [[panel-header]
      [filter-section]
      [pod-section]
      [rc/gap-f :size styles/gs-19s]]]))
