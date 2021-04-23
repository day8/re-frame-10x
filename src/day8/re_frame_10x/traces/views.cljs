(ns day8.re-frame-10x.traces.views
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.traces.events :as traces.events]
    [day8.re-frame-10x.traces.subs :as traces.subs]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.styles :as styles]))

(defclass category-style
  [ambiance active?]
  {:composes      (styles/control-2 ambiance)
   :display       :flex
   :align-items   :center
   :margin-left   styles/gs-12
   :padding       [[(px 2) styles/gs-12]]}
  [:svg
   {:margin-right styles/gs-5}])

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
  {:composes    (styles/frame-1 ambiance)
   :list-style  :none
   :display     :flex
   :align-items :center
   :height      styles/gs-31
   :padding     [[0 styles/gs-12]]})

(defn categories
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        options  [{:label "events"
                   :keys #{:event}}
                  {:label "subscriptions"
                   :keys  #{:sub/run :sub/create :sub/dispose}}
                  {:label "reagent"
                   :keys  #{:render}}
                  {:label "internals"
                   :keys  #{:re-frame.router/fsm-trigger :componentWillUnmount}}]]
    (into
      [:ul {:class (categories-style ambiance)}
       [rc/label :label "show:"]]
      (for [m options]
        [category m]))))

(defn panel
  []
  [rc/v-box
   :size     "1"
   :align    :start
   :children
   [[categories]]])