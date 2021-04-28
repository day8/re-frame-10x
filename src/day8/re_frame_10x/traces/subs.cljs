(ns day8.re-frame-10x.traces.subs
  (:require
    [clojure.string :as string]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.utils :as utils]
    [day8.re-frame-10x.metamorphic :as metam]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.settings.subs :as settings.subs]))

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
  ::queries
  :<- [::root]
  (fn [{:keys [queries]} _]
    queries))

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
  ::filtered-by-epoch-always
  :<- [::all]
  :<- [::epochs.subs/beginning-trace-id]
  :<- [::epochs.subs/ending-trace-id]
  (fn [[traces beginning ending] _]
    (into [] (utils/id-between-xf beginning ending) traces)))

(rf/reg-sub
  ::filtered-by-epoch
  :<- [::filter-by-selected-epoch?]
  :<- [::all]
  :<- [::filtered-by-epoch-always]
  (fn [[filter-by-selected-epoch? all filtered] _]
    (if-not filter-by-selected-epoch?
      all
      filtered)))

(rf/reg-sub
  ::filtered-by-namespace
  :<- [::filtered-by-epoch]
  :<- [::settings.subs/filtered-view-trace]
  (fn [[traces namespaces] _]
    (let [munged-namespaces (->> namespaces
                                 (map (comp munge :ns-str))
                                 (set))]
      (into []
            ;; Filter out view namespaces we don't care about.
            (remove
              (fn [trace] (and (metam/render? trace)
                               (contains? munged-namespaces (subs (:operation trace) 0 (string/last-index-of (:operation trace) "."))))))
            traces))))

(rf/reg-sub
  ::filtered-by-cached-subscriptions
  :<- [::filtered-by-namespace]
  (fn [traces _]
    ;; Remove cached subscriptions. Could add this back in as a setting later
    ;; but it's pretty low signal/noise 99% of the time.
    (remove (fn [trace] (and (= :sub/create (:op-trace trace))
                             (get-in trace [:tags :cached?])))
            traces)))

(rf/reg-sub
  ::filtered-by-categories
  :<- [::filtered-by-cached-subscriptions]
  :<- [::categories]
  (fn [[traces categories] _]
    (if-not (seq categories)
      traces
      (filter (fn [trace] (when (contains? categories (:op-type trace)) trace)) traces))))

(defn query->fn [query]
  (if (= :contains (:filter-type query))
    (fn [trace]
      (string/includes? (string/lower-case (str (:operation trace) " " (:op-type trace)))
                        (:query query)))
    (fn [trace]
      (< (:query query) (:duration trace)))))

(rf/reg-sub
  ::filtered-by-queries
  :<- [::filtered-by-categories]
  :<- [::queries]
  (fn [[traces queries] _]
    (if-not (seq queries)
      traces
      (filter (apply every-pred (map query->fn queries)) traces))))

(rf/reg-sub
  ::sorted
  :<- [::filtered-by-queries]
  (fn [traces _]
    (sort-by :id traces)))

(rf/reg-sub
  ::draft-query-type
  :<- [::root]
  (fn [{:keys [draft-query-type]} _]
    (or draft-query-type :contains)))

(rf/reg-sub
  ::draft-query
  :<- [::root]
  (fn [{:keys [draft-query]} _]
    draft-query))

(rf/reg-sub
  ::draft-query-error
  :<- [::root]
  (fn [{:keys [draft-query-error]} _]
    draft-query-error))