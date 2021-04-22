;; TODO: remove parts entirely.
(ns day8.re-frame-10x.view.parts
  (:require
    [re-frame.registrar]
    [re-frame.events]
    [re-frame.subs]
    [re-frame.fx]
    [re-frame.cofx]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]))

(defn render-registered
  [kind]
  (let [ambiance @(rf/subscribe [:settings/ambiance])]
    [:ul
     {:class (styles/registrar ambiance)}
     (for [[k v] (sort-by key (get @re-frame.registrar/kind->id->handler kind))]
       ^{:key (str kind "|" k)}
       [:li [:pre (prn-str k)]])]))

(defn render []
  [rc/v-box
   :children [[:h1 "Events"]
              (render-registered re-frame.events/kind)
              [:h1 "Subscriptions"]
              (render-registered re-frame.subs/kind)
              [:h1 "FX"]
              (render-registered re-frame.fx/kind)
              [:h1 "co-fx"]
              (render-registered re-frame.cofx/kind)
              [rc/gap-f :size styles/gs-19s]]])
