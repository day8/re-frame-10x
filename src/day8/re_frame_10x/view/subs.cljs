(ns day8.re-frame-10x.view.subs
  (:require
    [day8.re-frame-10x.view.app-db :refer [pod-gap pod-padding border-radius pod-border-edge
                                           pod-header-section]]
    [day8.re-frame-10x.utils.utils :as utils]
    [day8.re-frame-10x.view.cljs-devtools :as cljs-devtools]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [em px percent]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass defglobal]]
    [day8.re-frame-10x.utils.re-com :as rc :refer [css-join]]
    [day8.re-frame-10x.components :as components]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :as units]
    [day8.re-frame-10x.svgs :as svgs]
    [clojure.data]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color :as color])
  (:require-macros
    [day8.re-frame-10x.utils.re-com :refer [handler-fn]]))

;(s/def ::query-v any?)
;(s/def ::dyn-v any?)
;(s/def ::query-cache-params (s/tuple ::query-v ::dyn-v))
;(s/def ::deref #(satisfies? IDeref %))
;(s/def ::query-cache (s/map-of ::query-cache-params ::deref))
;(assert (s/valid? ::query-cache (rc/deref-or-value-peek subs/query->reaction)))




(def tag-types {:sub/create  {:long "CREATED" :short "C"}
                :sub/dispose {:long "DISPOSED" :short "D"}
                :sub/run     {:long "RUN" :short "R"}
                :sub/not-run {:long "NOT-RUN" :short "N"}
                nil          {:long "NIL" :short "NIL"}})

(defn long-tag-desc [type]
  (get-in tag-types [type :long] (str type)))

(defn short-tag-desc [type]
  (get-in tag-types [type :short] (str type)))


(defn sub-color
  [type]
  (case type
    :sub/create  styles/nord15
    :sub/dispose styles/nord12
    :sub/run     styles/nord14
    :sub/not-run styles/nord9
    styles/nord5))

(defclass sub-tag-style
  [ambiance type]
  {:color            :#fff
   :background-color (sub-color type)
   :border           [[(px 1) :solid (color/darken (sub-color type) 10)]]})

(defclass sub-tag-short-style
  [ambiance type]
  {:composes (sub-tag-style ambiance type)
   :width    styles/gs-19})

(defn sub-tag [type label]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [components/tag (sub-tag-style ambiance type) label]))

(defn short-sub-tag [type label]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [components/tag (sub-tag-short-style ambiance type) label]))

(defn title-tag [type title label]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     ;:class    "noselect"
     :align    :center
     :gap      styles/gs-2s
     :children [[:span {:style {:font-size "9px"}} title]
                [components/tag (sub-tag-style ambiance type) label]]]))

(defclass panel-header-style
  [ambiance]
  {:margin-bottom styles/gs-19
   :flex-flow [[:row :wrap]]})

(defclass summary-style
  [ambiance]
  {:composes (styles/frame-1 ambiance)
   #_:background-color (if (= :bright ambiance) styles/nord5 styles/nord0)
   #_:color            (if (= :bright ambiance) styles/nord0 styles/nord4)
   #_:border           [[(px 1) :solid styles/nord4]]
   #_:border-radius    styles/gs-2
   :padding          [[0 styles/gs-19]]})

(defn panel-header []
  (let [ambiance                  @(rf/subscribe [::settings.subs/ambiance])
        created-count             (rf/subscribe [:subs/created-count])
        re-run-count              (rf/subscribe [:subs/re-run-count])
        destroyed-count           (rf/subscribe [:subs/destroyed-count])
        not-run-count             (rf/subscribe [:subs/not-run-count])
        ignore-unchanged-l2-subs? (rf/subscribe [:subs/ignore-unchanged-l2-subs?])
        ignore-unchanged-l2-count (rf/subscribe [:subs/unchanged-l2-subs-count])]
    [rc/h-box
     :class    (panel-header-style ambiance)
     :justify  :between
     :align    :center
     :children [[rc/h-box
                 :align :center
                 :gap     styles/gs-19s
                 :height  styles/gs-50s
                 :class   (summary-style ambiance)
                 :children [[:span {:style {:font-size   "18px"
                                            :font-weight "lighter"}}
                             "Summary:"]
                            [title-tag :sub/create (long-tag-desc :sub/create) @created-count]
                            [title-tag :sub/run (long-tag-desc :sub/run) @re-run-count]
                            [title-tag :sub/dispose (long-tag-desc :sub/dispose) @destroyed-count]
                            [title-tag :sub/not-run (long-tag-desc :sub/not-run) @not-run-count]]]
                [rc/h-box
                 :align   :center
                 :gap     styles/gs-19s
                 :height  styles/gs-50s
                 :class   (summary-style ambiance)
                 :children [[rc/checkbox
                             :model ignore-unchanged-l2-subs?
                             :label [:span
                                     "Ignore " [:b {:style {:font-weight "700"}} @ignore-unchanged-l2-count] " unchanged" [:br]
                                     [rc/link {:label (str "layer 2 " (utils/pluralize- @ignore-unchanged-l2-count "sub"))
                                               :href  "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md"}]]
                             :style {:margin-top "6px"}
                             :on-change #(rf/dispatch [:subs/ignore-unchanged-l2-subs? %])]]]]]))


(defn pod-header [{:keys [id layer path open? diff? pin? run-times order]}]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class (styles/section-header ambiance)
     #_#_:class (str "app-db-path--header"
                     (when pin? " subscription-pinned"))
     :align :center
     :height styles/gs-31s
     :children [[pod-header-section
                 :children [[rc/box
                             :width styles/gs-31s
                             :height styles/gs-31s
                             :class "noselect"
                             :style {:cursor "pointer"}
                             :attr {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                                    :on-click (handler-fn (rf/dispatch [:subs/open-pod? id (not open?)]))}
                             :child [rc/box
                                     :margin "auto"
                                     :child [:span.arrow
                                             (if open?
                                               [material/arrow-drop-down :fill "#6EC0E6"]
                                               [material/arrow-right :fill "#6EC0E6"])]]]]]

                #_[rc/box
                   ;:width "64px"                                ;; (100-36)px from box above
                   :child [sub-tag (first order) (short-tag-desc (first order))]]

                ;; TODO: report if a sub was run multiple times
                #_(when run-times
                    [:span "Warning: run " run-times " times"])
                ;; TODO: label
                [rc/h-box
                 :class    (styles/path-header-style ambiance)
                 :size     "auto"
                 :style    {:height       styles/gs-31s
                            :border-right pod-border-edge}
                 :align    :center
                 :children [[rc/label
                             :label path]
                            #_[rc/input-text
                               :class     (styles/path-text-input-style ambiance)
                               :width     "100%"
                               :model     path
                               :disabled? true]]]

                [pod-header-section
                 :min-width styles/gs-50s
                 :children  [[rc/label :label (str id)]]]

                [pod-header-section
                 :min-width "106px"                           ;; styles/gs-131s - (2 * 12px padding) - (1px border)
                 :gap       styles/gs-12s
                 :style     {:padding "0px 12px"}
                 :children  (into []
                                  (comp
                                    (take 3)
                                    (map (fn [o] [short-sub-tag o (short-tag-desc o)])))
                                  order)]

                [pod-header-section
                 :width    styles/gs-31s
                 :children [[rc/box
                             :style {:width            styles/gs-19s
                                     :height           styles/gs-19s
                                     :border           pod-border-edge
                                     :border-radius    "50%"
                                     :margin           "auto"
                                     :background-color "white"}
                             :child (if (some? layer)
                                      [:div {:style {:margin "auto"}} layer]
                                      [components/hyperlink-info
                                       "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md#why-do-i-sometimes-see-layer--when-viewing-a-subscription"])]]]

                [pod-header-section
                 :width styles/gs-50s
                 :attr {:on-click (handler-fn (rf/dispatch [:subs/set-pinned id (not pin?)]))}
                 :children [[rc/box
                             :style {:margin "auto"}
                             :child [rc/checkbox
                                     :model pin?
                                     :label ""
                                     :style {:margin-left "6px"
                                             :margin-top  "1px"}
                                     :on-change #(rf/dispatch [:subs/set-pinned id (not pin?)])]]]]

                [pod-header-section
                 :width styles/gs-50s
                 :attr {:on-click (handler-fn (rf/dispatch [:subs/set-diff-visibility id (not diff?)]))}
                 :last? true
                 :children [[rc/box
                             :style {:margin "auto"}
                             :child [rc/checkbox
                                     :model diff?
                                     :label ""
                                     :style {:margin-left "6px"
                                             :margin-top  "1px"}
                                     :on-change #(rf/dispatch [:subs/set-diff-visibility id (not diff?)])]]]]]]))


(def no-prev-value-msg [:p {:style {:font-style "italic"}} "No previous value exists to diff"])
(def unchanged-value-msg [:p {:style {:font-style "italic"}} "Subscription value is unchanged"])
(def sub-not-run-msg [:p {:style {:font-style "italic"}} "Subscription not run, so no diff is available"])
(def not-run-yet-msg [rc/label :style {:font-style "italic"} :label "Subscription not run yet, so no value is available"])


(defn pod [{:keys [id layer path open? diff?] :as pod-info}]
  (let [render-diff?    (and open? diff?)
        value?          (contains? pod-info :value)
        previous-value? (contains? pod-info :previous-value)]
    (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
      [rc/v-box
       :style {:margin-bottom pod-gap
               :margin-right  "1px"}
       :children [[pod-header pod-info]
                  [rc/v-box
                   :class (when open? "app-db-path--pod-border")
                   :children [(when open?
                                (let [main-value (:value pod-info)
                                      #_(cond value? (:value pod-info)
                                              previous-value? (:previous-value pod-info)
                                              :else nil)]
                                  [rc/v-box
                                   :class "data-viewer"
                                   :style {:margin     (css-join pod-padding pod-padding "0px" pod-padding)
                                           :overflow-x "auto"
                                           :overflow-y "hidden"}
                                   :children [(if (or value? #_previous-value?)
                                                [cljs-devtools/simple-render
                                                 main-value
                                                 ["sub-path" path]]
                                                not-run-yet-msg)]]))

                              (when render-diff?
                                (let [diffable?        (and value? previous-value?)
                                      not-run?         (= (:order pod-info) [:sub/not-run])
                                      previous-value   (:previous-value pod-info)
                                      value            (:value pod-info)
                                      unchanged-value? (get-in pod-info [:sub/traits :unchanged?] false)
                                      [diff-before diff-after _] (clojure.data/diff previous-value value)]
                                  [rc/v-box
                                   :children [[rc/v-box
                                               :class "app-db-path--link"
                                               :style {:background-color styles/nord0}
                                               :justify :end
                                               :children [[rc/hyperlink-href
                                                           ;:class  "app-db-path--label"
                                                           :label "ONLY BEFORE"
                                                           :style {:margin-left styles/gs-7s}
                                                           :attr {:rel "noopener noreferrer"}
                                                           :target "_blank"
                                                           :href utils/diff-link]]]
                                              [rc/v-box
                                               :class  (styles/pod-data ambiance)
                                               ;:class "data-viewer data-viewer--top-rule"
                                               :style {:overflow-x "auto"
                                                       :overflow-y "hidden"}
                                               :children [(cond
                                                            not-run? sub-not-run-msg
                                                            unchanged-value? unchanged-value-msg
                                                            diffable? [cljs-devtools/simple-render
                                                                       diff-before
                                                                       ["app-db-diff" path]]
                                                            :else no-prev-value-msg)]]
                                              [rc/v-box
                                               :class "app-db-path--link"
                                               :style {:background-color styles/nord0}
                                               :justify :end
                                               :children [[rc/hyperlink-href
                                                           ;:class  "app-db-path--label"
                                                           :label "ONLY AFTER"
                                                           :style {:margin-left      styles/gs-7s
                                                                   :background-color styles/nord0}
                                                           :attr {:rel "noopener noreferrer"}
                                                           :target "_blank"
                                                           :href utils/diff-link]]]
                                              [rc/v-box
                                               :class "data-viewer data-viewer--top-rule"
                                               :style {:overflow-x "auto"
                                                       :overflow-y "hidden"}
                                               :children [(cond
                                                            not-run? sub-not-run-msg
                                                            unchanged-value? unchanged-value-msg
                                                            diffable? [cljs-devtools/simple-render
                                                                       diff-after
                                                                       ["app-db-diff" path]]
                                                            :else no-prev-value-msg)]]]]))
                              (when open?
                                [rc/gap-f :size pod-padding])]]]])))


(defn no-pods []
  [rc/h-box
   :margin (css-join "0px 0px 0px" styles/gs-19s)
   :gap styles/gs-7s
   :align :start
   :align-self :start
   :children [[rc/label :label "There are no subscriptions to show"]]])


(defn pod-header-column-titles
  []
  [rc/h-box
   :height styles/gs-19s
   :align :center
   :style {:margin-right "1px"}
   :children [[rc/box
               :size "1"
               :child ""]
              [rc/box
               :width "132px"                               ;; styles/gs-131s + 1 border
               :justify :center
               :child [rc/label :style {:font-size "9px"} :label "ACTIVITY"]]
              [rc/box
               :width "32px"                                ;; styles/gs-31s + 1 border
               :justify :center
               :child [rc/label :style {:font-size "9px"} :label "LAYER"]]
              [rc/box
               :width "51px"                                ;;  50px + 1 border
               :justify :center
               :child [rc/label :style {:font-size "9px"} :label "PINNED"]]
              [rc/box
               :width "51px"                                ;;  50px + 1 border
               :justify :center
               :child [rc/label :style {:font-size "9px"} :label "DIFFS"]]
              [rc/gap-f :size "6px"]]])                     ;; Add extra space to look better when there is/aren't scrollbars


(defn pod-section []
  (let [visible-subs     @(rf/subscribe [:subs/visible-subs])
        intra-epoch-subs @(rf/subscribe [:subs/intra-epoch-subs])
        sub-expansions   @(rf/subscribe [:subs/sub-expansions])
        sub-pins         @(rf/subscribe [:subs/sub-pins])
        all-subs         (if @(rf/subscribe [::settings.subs/debug?])
                           (cons {:path [:subs/current-epoch-sub-state] :id "debug" :value @(rf/subscribe [:subs/current-epoch-sub-state])} visible-subs)
                           visible-subs)]
    [rc/v-box
     :size "1"
     :style {:overflow-y "auto"}
     :children [(if (empty? all-subs)
                  [no-pods]
                  [pod-header-column-titles])
                [rc/v-box
                 :size "auto"
                 :style {:overflow-x "hidden"
                         :overflow-y "auto"}
                 :children [(for [p all-subs]
                              ^{:key (:id p)}
                              [pod (merge p
                                          (get sub-expansions (:id p))
                                          (get sub-pins (:id p)))])
                            (when (seq intra-epoch-subs)
                              (list
                                ^{:key "intra-epoch-line"}
                                [rc/line :size "5px"
                                 :style {:margin "19px 0px"}]
                                ^{:key "intra-epoch-title"}
                                [:h2 {:class "bm-heading-text"
                                      :style {:margin "19px 0px"}}
                                 [rc/link
                                  {:href  "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/IntraEpoch.md"
                                   :label "Intra-Epoch Subscriptions"}]]
                                (for [p intra-epoch-subs]
                                  ^{:key (:id p)}
                                  [pod (merge p (get sub-expansions (:id p)))])))]]]]))


(defn filter-section []
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        filter-str (rf/subscribe [:subs/filter-str])]
    [:div
     {:class (styles/filter-style ambiance)}
     [:input
      {:type        "text"
       :value       @filter-str
       :placeholder "filter" ;; TODO italtic same as app-db
       :on-change   #(rf/dispatch [:subs/set-filter
                                   (-> % .-target .-value)])}]]))

(defclass panel-style
  [ambiance]
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
