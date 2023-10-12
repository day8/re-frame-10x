(ns day8.re-frame-10x.navigation.epochs.events
  (:require
   [re-frame.core]
   [re-frame.db]
   [re-frame.trace]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.fx.debounce                                :as debounce]
   [day8.re-frame-10x.fx.scroll                                  :as scroll]
   [day8.re-frame-10x.tools.metamorphic                          :as metam]
   [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

(defn first-match-id
  [m]
  (-> m :match-info first :id))

(rf/reg-event-fx
 ::scroll-into-view-debounced
 [rf/trim-v]
 (fn [_ [js-dom]]
   {:fx [[::debounce/dispatch {:key   ::scroll-into-view
                               :event [::scroll-into-view js-dom]
                               :delay 128}]]}))

(rf/reg-event-fx
 ::scroll-into-view
 [rf/trim-v]
 (fn [_ [js-dom]]
   {:fx [[::scroll/into-view {:js-dom js-dom}]]}))

(rf/reg-event-fx
 ::receive-new-traces
 [rf/trim-v]
 (fn [{:keys [db]} [new-traces]]
   (if-let [sorted-traces (sort-by :id new-traces)]
     (let [number-of-epochs-to-retain (get-in db [:settings :number-of-epochs])
           events-to-ignore           (->> (get-in db [:settings :ignored-events]) vals (map :event-id) set)
           previous-traces            (get-in db [:traces :all] [])
           parse-state                (get-in db [:epochs :parse-state] metam/initial-parse-state)
           {drop-re-frame :re-frame drop-reagent :reagent} (get-in db [:settings :low-level-trace])
           all-traces                 (into previous-traces sorted-traces)
           parse-state                (metam/parse-traces parse-state sorted-traces)
           new-matches                (:partitions parse-state)
           previous-matches           (get-in db [:epochs :matches] [])
           parse-state                (assoc parse-state :partitions []) ;; Remove matches we know about
           new-matches                (remove (fn [match]
                                                (let [event (get-in (metam/matched-event match) [:tags :event])]
                                                  (contains? events-to-ignore (first event)))) new-matches)
            ;; subscription-info is calculated separately from subscription-match-state because they serve different purposes:
            ;; - subscription-info collects all the data that we know about the subscription itself, like its layer, inputs and other
            ;;   things that are defined as part of the reg-sub.
            ;; - subscription-match-state collects all the data that we know about the state of specific instances of subscriptions
            ;;   like its reagent id, when it was created, run, disposed, what values it returned, e.t.c.
           subscription-info          (metam/subscription-info (get-in db [:epochs :subscription-info] {}) sorted-traces (get-in db [:app-db :reagent-id]))
           sub-state                  (get-in db [:epochs :sub-state] metam/initial-sub-state)
           subscription-match-state   (metam/subscription-match-state sub-state all-traces new-matches)
           subscription-matches       (rest subscription-match-state)

           new-sub-state              (last subscription-match-state)
           timing                     (mapv (fn [match]
                                              (let [epoch-traces        (into []
                                                                              (comp
                                                                               (tools.coll/id-between-xf (:id (first match)) (:id (last match))))
                                                                              all-traces)
                                                     ;; TODO: handle case when there are no epoch-traces
                                                    start-of-epoch      (nth epoch-traces 0)
                                                     ;; TODO: optimise trace searching
                                                    event-handler-trace (first (filter metam/event-handler? epoch-traces))
                                                    dofx-trace          (first (filter metam/event-dofx? epoch-traces))
                                                    event-trace         (first (filter metam/event-run? epoch-traces))
                                                    finish-run          (or (first (filter metam/finish-run? epoch-traces))
                                                                            (tools.coll/last-in-vec epoch-traces))]
                                                {:re-frame/event-run-time     (metam/elapsed-time start-of-epoch finish-run)
                                                 :re-frame/event-time         (:duration event-trace)
                                                 :re-frame/event-handler-time (:duration event-handler-trace)
                                                 :re-frame/event-dofx-time    (:duration dofx-trace)}))
                                            new-matches)

           new-matches                (map (fn [match sub-match t] {:match-info match
                                                                    :sub-state  sub-match
                                                                    :timing     t})
                                           new-matches subscription-matches timing)
            ;; If there are new matches found, then by definition, a quiescent trace must have been received
            ;; However in cases where we reset the db in a replay, we won't get an event match.
            ;; We short circuit here to avoid iterating over the traces when it's unnecessary.
           quiescent?                 (or (seq new-matches)
                                          (filter metam/quiescent? sorted-traces))
           all-matches                (into previous-matches new-matches)
           retained-matches           (into [] (take-last number-of-epochs-to-retain all-matches))
           first-id-to-retain         (first-match-id (first retained-matches))
           retained-traces            (into [] (comp (drop-while #(< (:id %) first-id-to-retain))
                                                     (remove (fn [trace]
                                                               (or (when drop-reagent (metam/low-level-reagent-trace? trace))
                                                                   (when drop-re-frame (metam/low-level-re-frame-trace? trace)))))) all-traces)
           match-ids (mapv first-match-id retained-matches)
            ;; Select the latest event when it arrives, unless the user has selected an older one.
           select-latest? (and (seq new-matches)
                               (or (empty? previous-matches)
                                   (-> db :epochs :selected-epoch-id #{(first-match-id (last previous-matches))})))]
       {:db       (-> db
                      (assoc-in [:traces :all] retained-traces)
                      (update :epochs assoc
                              :matches retained-matches
                              :match-ids match-ids
                              :matches-by-id (zipmap match-ids retained-matches)
                              :parse-state parse-state
                              :sub-state new-sub-state
                              :subscription-info subscription-info)
                      (cond-> select-latest? (assoc-in [:epochs :selected-epoch-id] (last match-ids))))
        :dispatch (when quiescent? [::quiescent])})
      ;; Else
     {:db db})))

(rf/reg-event-fx
 ::previous
 [(rf/path [:epochs])]
 (fn [{{:keys [match-ids selected-epoch-id] :as db} :db} _]
   (let [new-id (->> match-ids (take-while (complement #{selected-epoch-id})) last)]
     {:db       (assoc db :selected-epoch-id new-id)
      :dispatch [::reset-current-epoch-app-db new-id]})))

(rf/reg-event-fx
 ::next
 [(rf/path [:epochs])]
 (fn [{{:keys [match-ids selected-epoch-id] :as db} :db} _]
   (let [new-id (if-not selected-epoch-id
                  (tools.coll/last-in-vec match-ids)
                  (->> match-ids (drop-while (complement #{selected-epoch-id})) (take 2) last))]
     {:db       (assoc db :selected-epoch-id new-id)
      :dispatch [::reset-current-epoch-app-db new-id]})))

(rf/reg-event-fx
 ::most-recent
 [(rf/path [:epochs])]
 (fn [{:keys [db]} _]
   (let [new-id (tools.coll/last-in-vec (:match-ids db))]
     {:db       (assoc db :selected-epoch-id new-id)
      :dispatch [::reset-current-epoch-app-db new-id]})))

(rf/reg-event-fx
 ::load
 [(rf/path [:epochs]) rf/trim-v]
 (fn [{:keys [db]} [new-id]]
   {:db       (assoc db :selected-epoch-id new-id)
    :dispatch [::reset-current-epoch-app-db new-id]}))

(rf/reg-event-db
 ::replay
 [(rf/path [:epochs])]
 (fn [epochs _]
   (let [selected-epoch-id (or (get epochs :selected-epoch-id)
                               (tools.coll/last-in-vec (get epochs :match-ids)))
         event-trace      (-> (get-in epochs [:matches-by-id selected-epoch-id :match-info])
                              (metam/matched-event))
         app-db-before    (metam/app-db-before event-trace)
         event            (get-in event-trace [:tags :event])]
     (reset! re-frame.db/app-db app-db-before)
      ;; Wait for quiescence
     (assoc epochs :replay event))))

(rf/reg-event-db
 ::quiescent
 [(rf/path [:epochs])]
 (fn [db _]
   (if-some [event-to-replay (:replay db)]
     (do (re-frame.core/dispatch event-to-replay)
         (dissoc db :replay))
     db)))

(rf/reg-event-db
 ::reset
 (fn [db]
   (re-frame.trace/reset-tracing!)
   (-> db
       (dissoc :epochs)
       (tools.coll/dissoc-in [:traces :all]))))

(rf/reg-event-db
 ::reset-current-epoch-app-db
 [rf/trim-v]
 (fn [db [new-id]]
   (when (get-in db [:settings :app-db-follows-events?])
     (let [epochs   (:epochs db)
           match-id (or new-id
                         ;; new-id may be nil when we call this event from :settings/play
                        (tools.coll/last-in-vec (get epochs :match-ids)))
           match    (get-in epochs [:matches-by-id match-id])
           event    (metam/matched-event (:match-info match))]
        ;; Don't mess up the users app if there is a problem getting app-db-after.
       (when-some [new-db (metam/app-db-after event)]
         (reset! re-frame.db/app-db new-db))))
   db))

(rf/reg-event-db
 ::set-filter
 [(rf/path [:epochs :filter-str]) rf/trim-v]
 (fn [_ [filter-str]]
   filter-str))
