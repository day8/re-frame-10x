(ns day8.re-frame-10x.panels.traces.views
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px percent]]
    [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core       :refer [defclass]]
    [day8.re-frame-10x.components.buttons                         :as buttons]
    [day8.re-frame-10x.components.inputs                          :as inputs]
    [day8.re-frame-10x.components.re-com                          :as rc]
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
    [day8.re-frame-10x.panels.traces.events                       :as traces.events]
    [day8.re-frame-10x.panels.traces.subs                         :as traces.subs]
    [day8.re-frame-10x.navigation.epochs.events                   :as epochs.events]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]
    [day8.re-frame-10x.tools.pretty-print-condensed               :as pp]
    [clojure.string                                               :as string]
    [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color   :as color]))

(defclass selected-epoch-style
  [ambiance active?]
  {:composes    (styles/frame-2 ambiance)
   :height      styles/gs-31
   :margin-left styles/gs-12
   :padding     [[(px 1) styles/gs-5]]})

(defn selected-epoch
  []
  (let [ambiance                  @(rf/subscribe [::settings.subs/ambiance])
        filter-by-selected-epoch? @(rf/subscribe [::traces.subs/filter-by-selected-epoch?])
        model                     (if filter-by-selected-epoch? :epoch :all)]
    [rc/h-box
     :class    (selected-epoch-style ambiance filter-by-selected-epoch?)
     :align    :center
     :gap      styles/gs-12s
     :children
     [[inputs/radio-button
       {:label     "only this epoch"
        :model     model
        :value     :epoch
        :on-change #(rf/dispatch [::traces.events/set-filter-by-selected-epoch? (not filter-by-selected-epoch?)])}]
      [inputs/radio-button
       {:label     "all epochs"
        :model     model
        :value     :all
        :on-change #(rf/dispatch [::traces.events/set-filter-by-selected-epoch? (not filter-by-selected-epoch?)])}]]]))

(defn op-type->color
  [op-type]
  (let [lighten-amount 30]
    (case op-type
      :sub/create                  (color/lighten styles/nord14 lighten-amount)
      :sub/run                     (color/lighten styles/nord15 lighten-amount)
      :sub/dispose                 (color/lighten styles/nord11 lighten-amount)
      :event                       (color/lighten styles/nord12 lighten-amount)
      :render                      (color/lighten styles/nord8  lighten-amount)
      :re-frame.router/fsm-trigger (color/lighten styles/nord10 lighten-amount)
      nil)))

(defclass category-style
  [ambiance op-type checked?]
  {;:composes        (styles/control-2 ambiance active?)
   :font-size        (px 12)
   :background-color (op-type->color op-type)
   :border           [[(px 1) :solid (color/darken (op-type->color op-type) 15)]]
   :color            styles/nord1
   :cursor           :pointer
   :border-radius    styles/gs-2
   :display          :flex
   :margin-left      styles/gs-12
   :padding          [[(px 1) styles/gs-5]]}
  [:svg
   {:margin-right styles/gs-5}
   [:path
    {:fill (color/darken (op-type->color op-type) 20)}]]
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

(defclass draft-query-error-style
  [ambiance]
  {:color styles/nord11})

(defn draft-query
  []
  (let [ambiance           @(rf/subscribe [::settings.subs/ambiance])
        draft-query-type   @(rf/subscribe [::traces.subs/draft-query-type])
        draft-query        @(rf/subscribe [::traces.subs/draft-query])
        draft-query-error? @(rf/subscribe [::traces.subs/draft-query-error])]
    [rc/h-box
     :children
     [[:select {:value     draft-query-type
                :on-change #(rf/dispatch [::traces.events/set-draft-query-type
                                          (keyword (.. % -target -value))])}
       [:option {:value "contains"} "contains"]
       [:option {:value "slower-than"} "slower than"]]
      [inputs/search
       {:on-save     #(rf/dispatch [::traces.events/save-draft-query])
        :on-change   #(rf/dispatch [::traces.events/set-draft-query
                                    (.. % -target -value)])
        :placeholder "filter traces"}]
      (if draft-query-error?
        [rc/label
         :class (draft-query-error-style ambiance)
         :label "Please enter a valid number."])]]))

(defclass query-clear-button-style
  [ambiance]
  {:cursor :pointer})

(defn queries
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        queries  @(rf/subscribe [::traces.subs/queries])]
    [rc/v-box
     :gap      styles/gs-19s
     :children
     [[draft-query]
      (when (seq queries)
        [rc/h-box
         :gap      styles/gs-12s
         :children
         (into
           []
           (map
             (fn [{:keys [query type id]}]
               [rc/h-box
                :align    :center
                :children
                [[:span type ": " query (when (= :slower-than type) " ms")]
                 [rc/box
                  :attr  {:on-click #(rf/dispatch [::traces.events/remove-query {:id id}])}
                  :class (query-clear-button-style ambiance)
                  :child
                  [material/clear]]]])
             queries))])]]))


(defclass table-style
  [ambiance]
  {:composes   (styles/frame-1 ambiance)
   :width      (percent 100)
   :overflow   :hidden})

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
  {:color            styles/nord1
   :background-color (op-type->color op-type)})

(defclass clickable-table-cell-style
  [ambiance op-type]
  {:cursor :pointer}
  [:&:hover
   {:background-color (color/lighten (op-type->color op-type) 2)}])

(defclass table-row-expansion-style
  [ambiance]
  {:cursor :pointer}
  [:svg :path
   {:fill styles/nord1}]
  [:&:hover
   [:svg :path
    {:fill styles/nord3}]])

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
        :class (clickable-table-cell-style ambiance op-type)
        :width styles/gs-81s
        :attr  {:on-click
                (fn [ev]
                  (rf/dispatch [::traces.events/add-query {:query (name op-type) :type :contains}])
                  (.stopPropagation ev))}
        :child
        [:span (str op-type)]]
       [rc/h-box
        :size      "1"
        :class     (clickable-table-cell-style ambiance op-type)
        :attr      {:on-click
                    (fn [ev]
                      (rf/dispatch [::traces.events/add-query {:query (name op-name) :type :contains}])
                      (.stopPropagation ev))}
        :children
        [[:span (pp/truncate 80 :middle (pp/str->namespaced-sym op-name))]
         (when-let [[_ & params] (or (get tags :query-v)
                                     (get tags :event))]
           [:span
            (->> (map pp/pretty-condensed params)
                 (string/join ", ")
                 (pp/truncate-string :middle 40))])]]
       [rc/box
        :width styles/gs-81s
        :child
        (if debug?
          [:span (:reaction (:tags trace)) "/" id]
          [:span (.toFixed duration 1) " ms"])]
       [rc/box
        :align   :center
        :justify :center
        :size    styles/gs-31s
        :child
        [buttons/icon {:icon [material/print]
                       :on-click #(js/console.log tags)}]]]]
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

(defn table-header
  []
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        traces              @(rf/subscribe [::traces.subs/sorted])
        {:keys [show-all?]} @(rf/subscribe [::traces.subs/expansions])]
    [rc/h-box
     :height   styles/gs-31s
     :children
     [[rc/box
       :class   (table-header-expansion-style ambiance)
       :align   :center
       :justify :center
       :width   styles/gs-31s
       :attr    {:on-click #(rf/dispatch [::traces.events/toggle-expansions])}
       :child
       (if show-all?
         [material/unfold-less]
         [material/unfold-more])]
      [rc/box
       :class     (table-header-style ambiance)
       :align     :center
       :justify   :center
       :width     styles/gs-81s
       :height    styles/gs-31s
       :child
       [rc/label :label "operations"]]
      [rc/h-box
       :class     (table-header-style ambiance)
       :align     :center
       :justify   :center
       :size      "1"
       :children
       [[rc/label :label (str (count traces) " traces")]
        [rc/gap-f :size styles/gs-5s]
        [rc/label
         :class    (styles/hyperlink ambiance)
         :on-click #(rf/dispatch [::epochs.events/reset])
         :label    "clear"]]]
      [rc/box
       :class     (table-header-style ambiance)
       :align     :center
       :justify   :center
       :width     styles/gs-81s
       :child
       [rc/label :label "meta"]]
      [rc/box
       :width styles/gs-31s
       :class (table-header-style ambiance)]
      [rc/box
       :class (table-header-style ambiance)
       :width "17px" ;; y scrollbar width
       :child ""]]]))

(defclass table-body-style
  [ambiance]
  {:overflow-x :auto
   :overflow-y :scroll})

(defn table
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        traces   @(rf/subscribe [::traces.subs/sorted])]
    [rc/v-box
     :size     "1"
     :class    (table-style ambiance)
     :children
     [[table-header]
      [rc/v-box
       :size     "1"
       :class    (table-body-style ambiance)
       :children
       (into [] (->> traces (map (fn [trace] [table-row trace]))))]]]))

(defclass panel-style
  []
  {:margin-right styles/gs-5
   :overflow     :hidden})

(defn panel
  []
  [rc/v-box
   :size     "1"
   :class    (panel-style)
   :align    :start
   :gap      styles/gs-19s
   :children
   [[filters]
    [queries]
    [table]]])