(ns day8.re-frame-10x.view.parts
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [re-frame.registrar]
            [re-frame.events]
            [re-frame.subs]
            [re-frame.fx]
            [re-frame.cofx]
            [day8.re-frame-10x.common-styles :as common]))

(defn render-registered [kind]
  (for [[k v] (sort-by key (get @re-frame.registrar/kind->id->handler kind))]
    ^{:key (str kind "|" k)}
    [:pre {:style {:border       "1px black solid"
                   :padding      "10px"
                   :margin-right "10px"}} (prn-str k)]))

(defn render-subs [kind]
  (for [[k v] (sort-by key (get @re-frame.registrar/kind->id->handler kind))]
    ^{:key (str kind "|" k)}
    [:pre {:style {:border       "1px black solid"
                   :padding      "10px"
                   :margin-right "10px"}}
     (prn-str k)]))

(defn render []
  [rc/v-box
   :children [[:h1 "Events"]
              (render-registered re-frame.events/kind)
              [:h1 "Subscriptions"]
              (render-subs re-frame.subs/kind)
              [:h1 "FX"]
              (render-registered re-frame.fx/kind)
              [:h1 "co-fx"]
              (render-registered re-frame.cofx/kind)
              [rc/gap-f :size common/gs-19s]]])
