(ns day8.re-frame.trace.panels.subs
  (:require [re-frame.subs :as subs]
            [day8.re-frame.trace.utils.re-com :as rc]
            ;[cljs.spec.alpha :as s]
            [day8.re-frame.trace.components.data-browser :as data-browser]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))

;(s/def ::query-v any?)
;(s/def ::dyn-v any?)
;(s/def ::query-cache-params (s/tuple ::query-v ::dyn-v))
;(s/def ::deref #(satisfies? IDeref %))
;(s/def ::query-cache (s/map-of ::query-cache-params ::deref))
;(assert (s/valid? ::query-cache (rc/deref-or-value-peek subs/query->reaction)))




(defn subs-panel []
  []
  [:div {:style {:flex "1 1 auto" :display "flex" :flex-direction "column"}}
   [:div.panel-content-scrollable {:style {:margin-left "10px"}}
    [:div.subtrees {:style {:margin "20px 0"}}
     (doall
       (->> @subs/query->reaction
            (sort)
            (map (fn [me]
                   (let [[query-v dyn-v :as inputs] (key me)]
                     ^{:key query-v}
                     [:div.subtree-wrapper {:style {:margin "10px 0"}}
                      [:div.subtree
                       [data-browser/subscription-render
                        (rc/deref-or-value-peek (val me))
                        [:button.subtree-button {:on-click #(rf/dispatch [:app-db/remove-path (key me)])}
                         [:span.subtree-button-string
                          (prn-str (first (key me)))]]
                        (into [:subs] query-v)]]]))
                 )))
     (do @re-frame.db/app-db
         nil)]]])

