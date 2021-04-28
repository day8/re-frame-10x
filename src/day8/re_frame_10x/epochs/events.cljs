(ns day8.re-frame-10x.epochs.events
  (:require
    [clojure.string :as string]
    [re-frame.core]
    [re-frame.db]
    [re-frame.trace]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.metamorphic :as metam]
    [day8.re-frame-10x.utils.utils :as utils]))

(defn log-trace? [trace]
  (let [render-operation? (or (= (:op-type trace) :render)
                              (= (:op-type trace) :componentWillUnmount))
        component-name    (get-in trace [:tags :component-name] "")]
    (if-not render-operation?
      true
      (not (string/includes? component-name "devtools outer")))))

(defn first-match-id
  [m]
  (-> m :match-info first :id))

(rf/reg-event-fx
  ::receive-new-traces
  [rf/trim-v]
  (fn [{:keys [db]} [new-traces]]
    (if-let [filtered-traces (->> (filter log-trace? new-traces)
                                  (sort-by :id))]
      (let [number-of-epochs-to-retain (get-in db [:settings :number-of-epochs])
            events-to-ignore           (->> (get-in db [:settings :ignored-events]) vals (map :event-id) set)
            previous-traces            (get-in db [:traces :all] [])
            parse-state                (get-in db [:epochs :parse-state] metam/initial-parse-state)
            {drop-re-frame :re-frame drop-reagent :reagent} (get-in db [:settings :low-level-trace])
            all-traces                 (reduce conj previous-traces filtered-traces)
            parse-state                (metam/parse-traces parse-state filtered-traces)
            ;; TODO:!!!!!!!!!!!!! We should be parsing everything else with the traces that span the newly matched
            ;; epochs, not the filtered-traces, as these are only partial.
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
            subscription-info          (metam/subscription-info (get-in db [:epochs :subscription-info] {}) filtered-traces (get-in db [:app-db :reagent-id]))
            sub-state                  (get-in db [:epochs :sub-state] metam/initial-sub-state)
            subscription-match-state   (metam/subscription-match-state sub-state all-traces new-matches)
            subscription-matches       (rest subscription-match-state)

            new-sub-state              (last subscription-match-state)
            timing                     (mapv (fn [match]
                                               (let [epoch-traces        (into []
                                                                               (comp
                                                                                 (utils/id-between-xf (:id (first match)) (:id (last match))))
                                                                               all-traces)
                                                     ;; TODO: handle case when there are no epoch-traces
                                                     start-of-epoch      (nth epoch-traces 0)
                                                     ;; TODO: optimise trace searching
                                                     event-handler-trace (first (filter metam/event-handler? epoch-traces))
                                                     dofx-trace          (first (filter metam/event-dofx? epoch-traces))
                                                     event-trace         (first (filter metam/event-run? epoch-traces))
                                                     finish-run          (or (first (filter metam/finish-run? epoch-traces))
                                                                             (utils/last-in-vec epoch-traces))]
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
                                           (filter metam/quiescent? filtered-traces))
            all-matches                (reduce conj previous-matches new-matches)
            retained-matches           (into [] (take-last number-of-epochs-to-retain all-matches))
            first-id-to-retain         (first-match-id (first retained-matches))
            retained-traces            (into [] (comp (drop-while #(< (:id %) first-id-to-retain))
                                                      (remove (fn [trace]
                                                                (or (when drop-reagent (metam/low-level-reagent-trace? trace))
                                                                    (when drop-re-frame (metam/low-level-re-frame-trace? trace)))))) all-traces)]
        {:db       (-> db
                       (assoc-in [:traces :all] retained-traces)
                       (update :epochs (fn [epochs]
                                         (let [selected-index (:selected-epoch-index epochs)
                                               selected-id    (:selected-epoch-id epochs)]
                                           (assoc epochs
                                             :matches retained-matches
                                             :matches-by-id (into {} (map (juxt first-match-id identity)) retained-matches)
                                             :match-ids (mapv first-match-id retained-matches)
                                             :parse-state parse-state
                                             :sub-state new-sub-state
                                             :subscription-info subscription-info
                                             ;; Reset selected epoch to the head of the list if we got a new event in.
                                             :selected-epoch-id (if (seq new-matches) (first-match-id (last retained-matches)) selected-id)
                                             :selected-epoch-index (if (seq new-matches) nil selected-index))))))
         :dispatch (when quiescent? [::quiescent])})
      ;; Else
      {:db db})))

;; TODO: this code is a bit messy, needs refactoring and cleaning up.
(rf/reg-event-fx
  ::previous
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    (if-some [selected-id (:selected-epoch-id db)]
      (let [match-ids         (:match-ids db)
            match-array-index (utils/find-index-in-vec (fn [x] (= selected-id x)) match-ids)
            new-id            (nth match-ids (dec match-array-index))]
        {:db       (assoc db :selected-epoch-id new-id)
         :dispatch [:snapshot/reset-current-epoch-app-db new-id]})
      (let [new-id (nth (:match-ids db)
                        (- (count (:match-ids db)) 2))]
        {:db       (assoc db :selected-epoch-id new-id)
         :dispatch [:snapshot/reset-current-epoch-app-db new-id]}))))


(rf/reg-event-fx
  ::next
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    (if-some [selected-id (:selected-epoch-id db)]
      (let [match-ids         (:match-ids db)
            match-array-index (utils/find-index-in-vec (fn [x] (= selected-id x)) match-ids)
            new-id            (nth match-ids (inc match-array-index))]
        {:db       (assoc db :selected-epoch-id new-id)
         :dispatch [:snapshot/reset-current-epoch-app-db new-id]})
      (let [new-id (utils/last-in-vec (:match-ids db))]
        {:db       (assoc db :selected-epoch-id new-id)
         :dispatch [:snapshot/reset-current-epoch-app-db new-id]}))))

(rf/reg-event-fx
  ::most-recent
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    {:db       (assoc db :selected-epoch-index nil
                         :selected-epoch-id nil)
     :dispatch [:snapshot/reset-current-epoch-app-db (utils/last-in-vec (:match-ids db))]}))

(rf/reg-event-fx
  ::load
  [(rf/path [:epochs]) rf/trim-v]
  (fn [{:keys [db]} [new-id]]
    {:db       (assoc db :selected-epoch-id new-id)
     :dispatch [:snapshot/reset-current-epoch-app-db new-id]}))

(rf/reg-event-db
  ::replay
  [(rf/path [:epochs])]
  (fn [epochs _]
    (let [selected-epoch-id (or (get epochs :selected-epoch-id)
                                (utils/last-in-vec (get epochs :match-ids)))
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
    (dissoc db :epochs :traces)))