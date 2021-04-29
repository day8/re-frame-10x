(ns day8.re-frame-10x.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [cljs.spec.alpha :as s]))

;;

(rf/reg-sub
  :global/unloading?
  (fn [db _]
    (get-in db [:global :unloading?])))

;;

(rf/reg-sub
  :snapshot/snapshot-root
  (fn [db _]
    (:snapshot db)))

(rf/reg-sub
  :snapshot/snapshot-ready?
  :<- [:snapshot/snapshot-root]
  (fn [snapshot _]
    (contains? snapshot :current-snapshot)))

;;

#_(rf/reg-sub
    :subs/root
    (fn [db _]
      (:subs db)))

(def string! (s/and string? #(not (empty? %))))

(s/def :sub/id string!)
(s/def :sub/reagent-id string!)
(s/def :sub/run-types #{:sub/create :sub/dispose :sub/run :sub/not-run})
(s/def :sub/order (s/nilable (s/coll-of :sub/run-types)))
(s/def :sub/layer (s/nilable pos-int?))
(s/def :sub/path-data any?)
(s/def :sub/path string!)
(s/def :sub/value any?)
(s/def :sub/previous-value any?)
(s/def :subs/view-panel-sub
  (s/keys :req-un [:sub/id :sub/reagent-id :sub/order :sub/layer :sub/path-data :sub/path]
          :opt-un [:sub/value :sub/previous-value]))
(s/def :subs/view-subs (s/coll-of :subs/view-panel-sub))

;;

(rf/reg-sub
  :errors/root
  (fn [db _]
    (:errors db)))

(rf/reg-sub
  :errors/popup-failed?
  :<- [:errors/root]
  (fn [errors _]
    (:popup-failed? errors)))

