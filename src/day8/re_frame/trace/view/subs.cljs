(ns day8.re-frame.trace.view.subs
  (:require [re-frame.subs :as subs]
            ;[cljs.spec.alpha :as s]
            [day8.re-frame.trace.view.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

;(s/def ::query-v any?)
;(s/def ::dyn-v any?)
;(s/def ::query-cache-params (s/tuple ::query-v ::dyn-v))
;(s/def ::deref #(satisfies? IDeref %))
;(s/def ::query-cache (s/map-of ::query-cache-params ::deref))
;(assert (s/valid? ::query-cache (rc/deref-or-value-peek subs/query->reaction)))

(def copy (macros/slurp-macro "day8/re_frame/trace/images/copy.svg"))

(def pod-gap common/gs-19s)

(defn tag
  [type label]
  (let [color (case type
                :created   "#9b51e0"
                :destroyed "#f2994a"
                :re-run    "#219653"
                :not-run   "#bdbdbd"
                "black")]
    [rc/box
     :style {:color            "white"
             :background-color color
             :width            common/gs-50s
             :height           common/gs-19s
             :font-size        "10px"
             :font-weight      "bold"
             :border-radius    "3px"}
     :child  [:span {:style {:margin "auto"}} label]]))

(defn title-tag
  [type title label]
  [rc/v-box
   :align    :center
   :gap      "2px"
   :children [[:span {:style {:font-size "9px"}} title]
              [tag type label]]])

(defn panel-header []
  [rc/h-box
   :justify  :between
   :margin   "19px 0px"
   :align    :center
   :children [[rc/h-box
               :align    :center
               :gap      common/gs-19s
               :height   "48px"
               :padding  "0px 19px"
               :style    {:background-color "#fafbfc"
                          :border "1px solid #e3e9ed"
                          :border-radius "3px"}
               :children [[:span {:style {:color       "#828282"
                                          :font-size   "18px"
                                          :font-weight "lighter"}} "Summary:"]
                          [title-tag :created   "CREATED" 2]
                          [title-tag :re-run    "RE-RUN"  44]
                          [title-tag :destroyed "DESTROYED" 1]
                          [title-tag :not-run   "NOT-RUN" 12]]]
              [rc/h-box
               :align    :center
               :gap      common/gs-19s
               :height   "48px"
               :padding  "0px 19px"
               :style    {:background-color "#fafbfc"
                          :border "1px solid #e3e9ed"
                          :border-radius "3px"}
               :children [[rc/checkbox
                           :model     true
                           :label     [:span "Ignore unchanged" [:br] "layer 2 subs"]
                           :style     {:margin-top "6px"}
                           :on-change #()]]]]])

(defn render []
  []
  [rc/v-box
   :style    {:margin-right common/gs-19s}
   :children [[panel-header]
              ;[pod-section]
              [:div.panel-content-scrollable
               [:div.subtrees {:style {:margin "20px 0"}}
                (doall
                  (->> @subs/query->reaction
                       (sort-by (fn [me] (ffirst (key me))))
                       (map (fn [me]
                              (let [[query-v dyn-v :as inputs] (key me)]
                                ^{:key query-v}
                                [:div.subtree-wrapper {:style {:margin "10px 0"}}
                                 [:div.subtree
                                  [components/subscription-render
                                   (rc/deref-or-value-peek (val me))
                                   [:button.subtree-button {:on-click #(rf/dispatch [:app-db/remove-path (key me)])}
                                    [:span.subtree-button-string
                                     (prn-str (first (key me)))]]
                                   (into [:subs] query-v)]]]))
                            )))
                (do @re-frame.db/app-db
                    nil)]]
              [rc/gap-f :size pod-gap]]])

