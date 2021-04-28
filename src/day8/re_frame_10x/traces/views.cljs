(ns day8.re-frame-10x.traces.views
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px percent]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.traces.events :as traces.events]
    [day8.re-frame-10x.traces.subs :as traces.subs]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.components :as components]
    [day8.re-frame-10x.utils.pretty-print-condensed :as pp]
    [clojure.string :as string]
    [day8.re-frame-10x.view.cljs-devtools :as cljs-devtools]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color :as color]))

(defclass selected-epoch-style
  [ambiance active?]
  {:composes (styles/control-2 ambiance active?)
   :height   styles/gs-31
   :margin-left styles/gs-12
   :padding  [[0 styles/gs-12]]})

(defn selected-epoch
  []
  (let [ambiance                  @(rf/subscribe [::settings.subs/ambiance])
        filter-by-selected-epoch? @(rf/subscribe [::traces.subs/filter-by-selected-epoch?])]
    [rc/h-box
     ;:class    (category-style ambiance filter-by-selected-epoch?)
     :align    :center
     :attr     {:on-click #(rf/dispatch [::traces.events/set-filter-by-selected-epoch? (not filter-by-selected-epoch?)])}
     :children
     [[components/radio-button
       {:label     "only this epoch"
        :model     :epoch
        :value     :epoch
        :on-change #()}]
      [components/radio-button
       {:label     "all epochs"
        :model     :epoch
        :value     :all
        :on-change #()}]]]))

(defn op-type->color
  [op-type]
  (case op-type
    :sub/create                  styles/nord14
    :sub/run                     styles/nord15
    :sub/dispose                 styles/nord11
    :event                       styles/nord12
    :render                      styles/nord8
    :re-frame.router/fsm-trigger styles/nord10
    nil))

(defclass category-style
  [ambiance op-type checked?]
  {;:composes        (styles/control-2 ambiance active?)
   :font-size        (px 12)
   :background-color (op-type->color op-type)
   :border           [[(px 1) :solid (color/darken (op-type->color op-type) 15)]]
   :color            :#fff
   :cursor           :pointer
   :border-radius    styles/gs-2
   :display          :flex
   :align-items      :center
   :margin-left      styles/gs-12
   :padding          [[(px 1) styles/gs-12]]}
  [:svg
   {:margin-right styles/gs-5}
   [:path
    {:fill :#fff}]]
  [:&:hover
   {:background-color (color/lighten (op-type->color op-type) 5)}])

(defn category
  [{:keys [label keys]}]
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        categories @(rf/subscribe [::traces.subs/categories])
        checked?   (contains? categories (first keys))]
    [:li {:class    (category-style ambiance (first keys) checked?)
          :on-click #(rf/dispatch [::traces.events/toggle-categories keys])}
     (if checked?
       [material/check-box
        {:size styles/gs-19s}]
       [material/check-box-outline-blank
        {:size styles/gs-19s}])
     [rc/label
      :label label]]))

(defclass categories-style
  [ambiance]
  {;:composes    (styles/frame-1 ambiance)
   :list-style  :none
   :display     :flex
   :align-items :center
   :height      styles/gs-31
   :margin      0
   :padding     0})

(defn filters
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        options  [{:label "events"
                   :keys #{:event}}
                  {:label "subs"
                   :keys  #{:sub/run :sub/create :sub/dispose}}
                  {:label "reagent"
                   :keys  #{:render}}
                  {:label "internals"
                   :keys  #{:re-frame.router/fsm-trigger :componentWillUnmount}}]]
    [rc/h-box
     :align    :center
     :children
     [
      (into
        [:ul {:class (categories-style ambiance)}
         [rc/label :label "show:"]]
        (for [m options]
          [category m]))
      [selected-epoch]]]))


(defn queries
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :children
     [[:select
       [:option {:value "contains"} "contains"]
       [:option {:value "slower-than"} "slower than"]]
      [material/search]
      #_[components/search-input
         {:on-save     #()
          :on-change   #()
          :placeholder "filter traces"}]]]))


(defclass table-style
  [ambiance]
  {:composes   (styles/frame-1 ambiance)
   :width      (percent 100)})

(defclass table-header-expansion-style
  [ambiance]
  {:composes  (styles/colors-2 ambiance)
   :cursor    :pointer})

(defclass table-header-style
  [ambiance]
  {:composes    (styles/colors-2 ambiance)
   :border-left [[(px 1) :solid (if (= :bright ambiance) styles/nord4 styles/nord3)]]})

(defclass table-row-style
  [ambiance op-type]
  {:color            :#fff
   :background-color (case op-type
                       :sub/create                  styles/nord14
                       :sub/run                     styles/nord15
                       :sub/dispose                 styles/nord11
                       :event                       styles/nord12
                       :render                      styles/nord8
                       :re-frame.router/fsm-trigger styles/nord10
                       nil)})

(defclass table-row-expansion-style
  [ambiance]
  {:cursor :pointer}
  [:svg :path
   {:fill :#fff}]
  [:&:hover
   [:svg :path
    {:fill styles/nord6}]])

(defclass table-row-expanded-style
  [ambiance syntax-color-scheme]
  {:background-color (styles/syntax-color ambiance syntax-color-scheme :signature-background)})

(defn table-row
  [{:keys [op-type id operation tags duration] :as trace}]
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])
        debug?              @(rf/subscribe [::settings.subs/debug?])
        expansions          @(rf/subscribe [::traces.subs/expansions])
        expanded?           (get-in expansions [:overrides id] (:show-all? expansions))
        op-name             (if (vector? operation)
                              (second operation)
                              operation)]
    [:<>
     [rc/h-box
      :class    (table-row-style ambiance op-type)
      :height   styles/gs-19s
      :children
      [[rc/box
        :width   styles/gs-31s
        :class   (table-row-expansion-style ambiance)
        :attr    {:on-click #(rf/dispatch [::traces.events/toggle-expansion id])}
        :justify :center
        :child
        (if expanded?
          [material/arrow-drop-down]
          [material/arrow-right])]
       [rc/box
        :size  "1"
        :attr  {:on-click
                (fn [ev]
                  (rf/dispatch [::traces.events/add-query (name op-type) :contains])
                  (.stopPropagation ev))}
        :child
        [:span (str op-type)]]
       [rc/h-box
        :size "1"
        :attr {:on-click
               (fn [ev]
                 (rf/dispatch [::traces.events/add-query (name op-name) :contains])
                 (.stopPropagation ev))}
        :children
        [[:span (pp/truncate 20 :middle (pp/str->namespaced-sym op-name))]
         (when-let [[_ & params] (or (get tags :query-v)
                                     (get tags :event))]
           [:span
            (->> (map pp/pretty-condensed params)
                 (string/join ", ")
                 (pp/truncate-string :middle 40))])]]
       [rc/box
        :size "1"
        :child
        (if debug?
          [:span (:reaction (:tags trace)) "/" id]
          [:span (.toFixed duration 1) " ms"])]]]
     (when expanded?
       [rc/h-box
        :class    (table-row-expanded-style ambiance syntax-color-scheme)
        :children
        [[rc/box
          :width styles/gs-31s
          :child ""]
         [rc/box
          :size  "1"
          :child
          [cljs-devtools/simple-render tags []]]]])]))

(defn table
  []
  (let [ambiance       @(rf/subscribe [::settings.subs/ambiance])
        visible-traces @(rf/subscribe [::traces.subs/sorted])
        expansions     @(rf/subscribe [::traces.subs/expansions])]
    [rc/v-box
     :size     "1"
     :class    (table-style ambiance)
     :children
     (into
       [[rc/h-box
         :size     "1"
         :children
         [[rc/box
           :class   (table-header-expansion-style ambiance)
           :align   :center
           :justify :center
           :width   styles/gs-31s
           :attr    {:on-click #(rf/dispatch [::traces.events/toggle-expansions])}
           :child   (if (:show-all? expansions)
                      [material/unfold-less]
                      [material/unfold-more])]
          [rc/box
           :class   (table-header-style ambiance)
           :align   :center
           :justify :center
           :size   "1"
           :height styles/gs-31s
           :child  [rc/label :label "operations"]]
          [rc/box
           :class   (table-header-style ambiance)
           :align   :center
           :justify :center
           :size  "1"
           :child [rc/label :label (str (count visible-traces) " traces")]]
          [rc/box
           :class   (table-header-style ambiance)
           :align   :center
           :justify :center
           :size  "1"
           :child [rc/label :label "meta"]]]]]
       (->> visible-traces (map (fn [trace] [table-row trace]))))]))

(defclass panel-style
  []
  {:margin-right styles/gs-5})

(defn panel
  []
  [rc/v-box
   :class    (panel-style)
   :size     "1"
   :align    :start
   :gap      styles/gs-19s
   :children
   [[filters]
    [queries]
    [table]]])