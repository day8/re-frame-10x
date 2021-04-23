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
    [day8.re-frame-10x.view.components :as components]))

(defclass category-style
  [ambiance active?]
  {:composes      (styles/control-2 ambiance active?)
   :display       :flex
   :align-items   :center
   :margin-left   styles/gs-12
   :padding       [[(px 2) styles/gs-12]]}
  [:svg
   {:margin-right styles/gs-5}])


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
     :class    (category-style ambiance filter-by-selected-epoch?)
     :align    :center
     :attr     {:on-click #(rf/dispatch [::traces.events/set-filter-by-selected-epoch? (not filter-by-selected-epoch?)])}
     :children
     [(if filter-by-selected-epoch?
        [material/check-box]
        [material/check-box-outline-blank])
      ;; TODO: radio button, [ ] only this epoch [ ] all epochs
      [rc/label
       :label "only this epoch"]]]))

(defn category
  [{:keys [label keys]}]
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        categories @(rf/subscribe [::traces.subs/categories])
        active?    (contains? categories (first keys))]
    [:li {:class    (category-style ambiance active?)
          :on-click #(rf/dispatch [::traces.events/toggle-categories keys])}
     (if active?
       [material/check-box]
       [material/check-box-outline-blank])
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
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]))

(defclass table-style
  [ambiance]
  {:composes   (styles/frame-1 ambiance)
   :width      (percent 100)
   :margin-top styles/gs-19})

(defclass table-header-expansion-style
  [ambiance]
  {:composes  (styles/colors-2 ambiance)
   :cursor    :pointer})

(defclass table-header-style
  [ambiance]
  {:composes    (styles/colors-2 ambiance)
   :border-left [[(px 1) :solid (if (= :bright ambiance) styles/nord4 styles/nord3)]]})

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
       (->> visible-traces
            (map-indexed
              (fn [index {:keys [op-type id operation tags duration] :as trace}]
                [rc/h-box
                 :children
                 [[rc/box
                   :width styles/gs-31s
                   :child [components/expansion-button
                           {:open true}]]]]))))]))

(defn panel
  []
  [rc/v-box
   :size     "1"
   :align    :start
   :children
   [[filters]
    [table]]])