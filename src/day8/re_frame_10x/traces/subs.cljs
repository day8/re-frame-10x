(ns day8.re-frame-10x.traces.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.utils :as utils]))

(rf/reg-sub
  ::root
  (fn [{:keys [traces]} _]
    traces))

(rf/reg-sub
  ::categories
  :<- [::root]
  (fn [{:keys [categories]} _]
    categories))

(rf/reg-sub
  ::expansions
  :<- [::root]
  (fn [{:keys [expansions]} _]
    expansions))

(rf/reg-sub
  ::all
  :<- [::root]
  (fn [{:keys [all]} _]
    all))

(rf/reg-sub
  ::count
  :<- [::all]
  (fn [traces _]
    (count traces)))

(rf/reg-sub
  ::filter-by-selected-epoch?
  :<- [::root]
  (fn [{:keys [filter-by-selected-epoch?]} _]
    filter-by-selected-epoch?))

(rf/reg-sub
  ::filtered-by-epoch
  :<- [::all]
  :<- [::filter-by-selected-epoch?]
  :<- [:epochs/beginning-trace-id] ;; TODO
  :<- [:epochs/ending-trace-id]
  (fn [[traces filter-by-selected-epoch? beginning ending]]
    (if-not filter-by-selected-epoch?
      traces
      (into []
            (utils/id-between-xf beginning ending) traces))))

(rf/reg-sub
  ::filtered-by-namespace
  :<- [::filtered-by-epoch])
