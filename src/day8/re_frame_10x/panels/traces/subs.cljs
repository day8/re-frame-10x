(ns day8.re-frame-10x.panels.traces.subs
  (:require
   [clojure.string                                               :as string]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.tools.metamorphic                          :as metam]
   [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
   [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

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
 ::draft-query
 :<- [::root]
 (fn [{:keys [draft-query]} _]
   draft-query))

(rf/reg-sub
 ::draft-query-type
 :<- [::root]
 (fn [{:keys [draft-query-type]} _]
   (or draft-query-type :contains)))

(rf/reg-sub
 ::draft-query-error
 :<- [::root]
 (fn [{:keys [draft-query-error]} _]
   draft-query-error))

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
   (into [] (tools.coll/id-between-xf beginning ending) traces)))

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
   (filter (fn [trace] (when (contains? categories (:op-type trace)) trace)) traces)))

(defn query->fn [trace query]
  (cond
    (= :contains (:type query))
    (string/includes? (string/lower-case (str (:operation trace) " " (:op-type trace)))
                      (:query query))
    (= :contains-not (:type query))
    (not (string/includes? (string/lower-case (str (:operation trace) " " (:op-type trace)))
                           (:query query)))
    :else
    (< (:query query) (:duration trace))))

(rf/reg-sub
 ::filtered-by-queries
 :<- [::filtered-by-categories]
 :<- [::queries]
 :<- [::draft-query]
 :<- [::draft-query-type]
 (fn [[traces queries draft-query draft-query-type] _]
   (let [queries (if-not (empty? draft-query) (conj queries {:type draft-query-type :query draft-query})
                         queries)]
     (if-not (seq queries)
       traces
        ;; loop over traces, retain traces that match any of the queries
       (reduce (fn [ret trace]
                 (if (some #(query->fn trace %) queries)
                   (conj ret trace)
                   ret)) [] traces)))))

(rf/reg-sub
 ::sorted
 :<- [::filtered-by-queries]
 (fn [traces _]
   (sort-by :id traces)))

(rf/reg-sub
 ::filtered-by-ignored-events
 :<- [::sorted]
 :<- [::settings.subs/ignored-events]
 (fn [[traces ignored-events] _]
   (let [ignored-events (->> ignored-events (map :event-id) set)]
      ;; loop over traces and find events, then remove any events in `ignored events`
     (remove (fn [trace]
               (let [event?     (= :event (:op-type trace))
                     event-name (when event? (first (get-in trace [:tags :event])))]
                 (contains? ignored-events event-name))) traces))))
