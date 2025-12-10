(ns day8.re-frame-10x.panels.flow.subs
  (:require
   [clojure.data]
   [clojure.string :as string]
   [day8.re-frame-10x.panels.traces.subs                         :as traces.subs]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.core    :refer [defclass]]
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
   [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
   [day8.re-frame-10x.tools.string                               :as tools.string]
   [day8.re-frame-10x.fx.clipboard                               :as clipboard]) )

(rf/reg-sub ::root :-> :flow)

(rf/reg-sub
 ::all-flows
 :<- [::traces.subs/filtered-by-epoch-always]
 #(filterv (comp #{:flow} :op-type) %))

(rf/reg-sub ::filter-str :<- [::root] :-> :filter-str)

(rf/reg-sub
 ::flow-pins
 (constantly nil))

(rf/reg-sub
 ::visible-flows
 :<- [::all-flows]
 :<- [::filter-str]
 :<- [::flow-pins]
 (fn [[all-subs filter-str pins]]
   (let [compare-fn (fn [s1 s2]
                      (let [p1 (boolean (get-in pins [(:id s1) :pin?]))
                            p2 (boolean (get-in pins [(:id s2) :pin?]))]
                        (if (= p1 p2)
                          (compare (:path s1) (:path s2))
                          p1)))]
     (cond->> all-subs
       :do                    (sort compare-fn)
       (not-empty filter-str) (filter (fn [{:keys [operation id]}]
                                        (or (string/includes? (str operation) filter-str)
                                            (get-in pins [id :pin?]))))))))

(rf/reg-sub ::visible-flows-by-id
            :<- [::visible-flows]
            :-> (fn [flows] (zipmap (map :id flows) flows)))

(rf/reg-sub
 ::inputs-diff
 :<- [::visible-flows-by-id]
 (fn [flows [_ id]]
   (let [old (get-in flows [id :tags :id->old-in])
         new (get-in flows [id :tags :id->in])]
     (clojure.data/diff old new))))

(rf/reg-sub
 ::live-inputs-diff
 :<- [::visible-flows-by-id]
 (fn [flows [_ id]]
   (let [old (get-in flows [id :tags :id->old-live-in])
         new (get-in flows [id :tags :id->live-in])]
     (clojure.data/diff old new))))
