(ns day8.re-frame-10x.metamorphic)

(defn id-between-xf
  ;; Copied here because I got undeclared Var warnings from figwheel when requiring a CLJC utils ns.
  "Returns a transducer that filters for :id between beginning and ending. Inclusive on both ends."
  [beginning ending]
  (filter #(<= beginning (:id %) ending)))

;; What starts an epoch?

;;; idle -> dispatch -> running
;;; running -> dispatch -> handling new event

;; What ends an epoch?

;;; the start of a new epoch
;;; a Reagent animation frame ending AND nothing else being scheduled

;; Slight wrinkles

;;; Any renders that run between epochs deserve their own epoch really.
;;; Dispatch-sync's

;;;

;
;(defn add-event-from-idle? [event history pattern-sequence pattern]
;  #_(println @history event)
;
;  (and (= :re-frame.router/fsm-trigger (:op-type event))
;       (= (:operation event)
;          [:idle :add-event])))
;
;(defn event-run? [event history pattern-sequence pattern]
;  (= :event (:op-type event)))
;
;(defn epoch-started? [event history pattern-sequence pattern]
;  (or (add-event-from-idle? event history pattern-sequence pattern)
;      (and (event-run? event history pattern-sequence pattern)
;           (empty? @history))))
;
(defn fsm-trigger? [event]
  (= :re-frame.router/fsm-trigger (:op-type event)))
;
;(defn redispatched-event? [event history pattern-sequence pattern]
;  (and (fsm-trigger? event)
;       (= (:operation event)
;          [:running :add-event])))
;
;(defn router-scheduled? [event history pattern-sequence pattern]
;  (and (fsm-trigger? event)
;       (= (:operation event)
;          [:running :finish-run])
;       (= :running (get-in event [:tags :current-state]))
;       (= :scheduled (get-in event [:tags :new-state]))))
;
;(defn router-finished? [event history pattern-sequence pattern]
;  (and (fsm-trigger? event)
;       (= (:operation event)
;          [:running :finish-run])
;       (= :running (get-in event [:tags :current-state]))
;       (= :idle (get-in event [:tags :new-state]))))
;
;(defn quiescent? [event _ _ _]
;  (= :reagent/quiescent (:op-type event)))
;
;(defn epoch-ended? [event history pattern-sequence pattern]
;  (or (quiescent? event history pattern-sequence pattern)
;      (epoch-started? event history pattern-sequence pattern)))
;

(defn elapsed-time [ev1 ev2]
  (let [start-of-epoch (:start ev1)
        end-of-epoch   (:end ev2)]
    (when (and (some? start-of-epoch) (some? end-of-epoch))
      (- end-of-epoch start-of-epoch))))

(defn run-queue? [event]
  (and (fsm-trigger? event)
       (= (:operation event)
          [:scheduled :run-queue])))

(defn request-animation-frame? [event]
  (= :raf (:op-type event)))

(defn request-animation-frame-end? [event]
  (= :raf-end (:op-type event)))

(defn summarise-event [ev]
  (-> ev
      (dissoc :start :duration :end :child-of)
      (update :tags dissoc :app-db-before :app-db-after :effects :coeffects :interceptors)))


(defn summarise-match [match]
  (map summarise-event match))
;
(defn beginning-id [match]
  (:id (first match)))

(defn ending-id [match]
  (:id (last match)))
;
;(defn parse-traces-metam
;  "Returns a metamorphic runtime"
;  [traces]
;  (let [runtime (-> (m/new-pattern-sequence "simple traces")
;                    (m/begin "new-epoch-started" epoch-started?)
;                    #_(m/followed-by "run-queue" run-queue? {:optional? true})
;                    ;(m/followed-by "event-run" event-run?)
;                    #_(m/followed-by "router-finished" router-finished?)
;                    ;(m/followed-by "raf" request-animation-frame?)
;                    ;(m/followed-by "raf-end" request-animation-frame-end?)
;                    (m/followed-by "epoch-ended" epoch-ended?)
;                    (rt/initialize-runtime))
;        rt      (reduce rt/evaluate-event runtime traces)]
;    #_(println "Count"
;               (count (:matches rt))
;               (map count (:matches rt)))
;    #_(map summarise-match (:matches rt))
;    rt))

;;;;;;

;; TODO: this needs to be included too as a starting point.
(defn add-event-from-idle? [event]
  (and (= :re-frame.router/fsm-trigger (:op-type event))
       (= (:operation event)
          [:idle :add-event])))

(defn subscription? [trace]
  (and (= "sub" (namespace (:op-type trace)))
       ;; TODO: should we remove cached checks?
       (not (get-in trace [:tags :cached?]))))

(defn subscription-created? [trace]
  (and (= :sub/create (:op-type trace))
       (not (get-in trace [:tags :cached?]))))

(defn subscription-re-run? [trace]
  (= :sub/run (:op-type trace)))

(defn subscription-destroyed? [trace]
  (= :sub/dispose (:op-type trace)))

(defn subscription-not-run? [trace]
  false)

(defn low-level-re-frame-trace?
  "Is this part of re-frame internals?"
  [trace]
  (case (:op-type trace)
    (:re-frame.router/fsm-trigger) true
    false))

(defn low-level-reagent-trace?
  "Is this part of reagent internals?"
  [trace]
  (= :componentWillUnmount (:op-type trace)))

(defn render? [trace]
  (= :render (:op-type trace)))

(defn unchanged-l2-subscription? [sub]
  (and (get-in sub [:sub/traits :unchanged?])
       (not-any? #(= :sub/dispose %) (:order sub))
       (= 2 (get sub :layer))))


(defn finish-run?
  "Marks the end of event processing running."
  [event]
  (and (fsm-trigger? event)
       (= (:operation event)
          [:running :finish-run])))

(defn event-run? [event]
  (= :event (:op-type event)))

(defn event-handler? [trace]
  (= :event/handler (:op-type trace)))

(defn event-dofx? [trace]
  (= :event/do-fx (:op-type trace)))

(defn start-of-epoch?
  "Detects the start of a re-frame epoch

  Normally an epoch would always start with the queue being run, but with a dispatch-sync, the event is run directly."
  [event]
  (or (run-queue? event)
      (event-run? event)))

(defn start-of-epoch-and-prev-end?
  "Detects that a new epoch has started and that the previous one ended on the previous event.

  If multiple events are dispatched while processing the first event, each one is considered its
  own epoch."
  [event state]
  (or (run-queue? event)
      ;; An event ran, and the previous event was not
      ;; a run-queue.
      (and (event-run? event)
           (not (run-queue? (:previous-event state))))))

(defn quiescent? [event]
  (= :reagent/quiescent (:op-type event)))

(def initial-parse-state
  {:current-match  nil
   :previous-event nil
   :partitions     []})

(def initial-sub-state
  {:last-matched-id 0
   :pre-epoch-state {}
   :reaction-state  {}})

(defn parse-traces [parse-state traces]
  (reduce
    (fn [state event]
      (let [current-match  (:current-match state)
            previous-event (:previous-event state)
            no-match?      (nil? current-match)]
        (-> (cond

              ;; No current match yet, check if this is the start of an epoch
              no-match?
              (if (start-of-epoch? event)
                (assoc state :current-match [event])
                state)

              ;; We are in an epoch match, and reagent has gone to a quiescent state
              (quiescent? event)
              (-> state
                  (update :partitions conj (conj current-match event))
                  (assoc :current-match nil))

              ;; We are in an epoch match, and we have started a new epoch
              ;; The previously seen event was the last event of the old epoch,
              ;; and we need to start a new one from this event.
              (start-of-epoch-and-prev-end? event state)
              (-> state
                  (update :partitions conj (conj current-match previous-event))
                  (assoc :current-match [event]))

              (event-run? event)
              (update state :current-match conj event)


              :else
              state
              ;; Add a timeout/warning if a match goes on for more than a second?

              )
            (assoc :previous-event event))))
    parse-state
    traces))

(defn matched-event [match]
  (->> match
       (filter event-run?)
       (first)))

(defn app-db-after [event-trace]
  (get-in event-trace [:tags :app-db-after]))

(defn app-db-before [event-trace]
  (get-in event-trace [:tags :app-db-before]))

(defn subscription-info
  "Collect information about the subscription that we'd like
  to know, like its layer."
  [initial-state filtered-traces app-db-id]
  (->> filtered-traces
       (filter subscription-re-run?)
       (reduce (fn [state trace]
                 ;; Can we take any shortcuts by assuming that a sub with
                 ;; multiple input signals is a layer 3? I don't *think* so because
                 ;; one of those input signals could be a naughty subscription to app-db
                 ;; directly.
                 ;; If we knew when subscription handlers were loaded/reloaded then
                 ;; we could avoid doing most of this work, and only check the input
                 ;; signals if we hadn't seen it before, or it had been reloaded.
                 (assoc-in state
                           [(:operation trace) :layer]
                           ;; If any of the input signals are app-db, it is a layer 2 sub, else 3
                           (if (some #(= app-db-id %) (get-in trace [:tags :input-signals]))
                             2
                             3)))
               initial-state)))

(defn reset-sub-state
  "Remove information about the subscription that is transient and specific to a single
  phase."
  [state]
  (into {}
        (comp
          ;; Remove disposed subscriptions
          (filter (fn [me] (when-not (:disposed? (val me)) me)))
          ;; Remove transient state
          (map (fn [[k v]]
                 [k (dissoc v :order :created? :run? :disposed? :previous-value :sub/traits)])))
        state))



(defn process-sub-traces
  [initial-state traces]
  (let [first-pass  (reduce (fn [init-state trace]
                              (let [tags        (get trace :tags)
                                    reaction-id (:reaction tags)
                                    state       (-> init-state
                                                    (update-in [reaction-id :order] (fnil conj []) (:op-type trace))
                                                    ;; In a perfect world we could provide this only in the :sub/create branch, but we have
                                                    ;; zombie reactions roaming the DOM, so we re-add it on every trace in case a sub was
                                                    ;; disposed of previously (and removed from the sub state).
                                                    (assoc-in [reaction-id :subscription] (:query-v tags)))
                                    new-state
                                                (case (:op-type trace)
                                                  :sub/create (-> state
                                                                  (assoc-in [reaction-id :created?] true)
                                                                  (assoc-in [reaction-id :subscription] (:query-v tags)))
                                                  :sub/run (update state reaction-id (fn [sub-state]
                                                                                       ;; TODO: should we keep track of subscriptions that have been disposed
                                                                                       ;; so we can detect zombies?

                                                                                       ;; TODO: this should only update once per phase, even if a sub runs multiple times
                                                                                       (-> (if (contains? sub-state :value)
                                                                                             (assoc sub-state :previous-value (:value sub-state))
                                                                                             sub-state)
                                                                                           (assoc :run? true
                                                                                                  :value (:value tags)))))
                                                  :sub/dispose (assoc-in state [reaction-id :disposed?] true))]
                                (when-not (contains? (get new-state reaction-id) :subscription)
                                  #?(:cljs (js/console.warn trace (get new-state reaction-id))))


                                new-state))
                            initial-state
                            traces)
        second-pass (reduce (fn [all-state [sub-id sub-state]]
                              ;; TODO: integrate this into the first pass for efficiency
                              (if (and (contains? sub-state :previous-value)
                                       (contains? sub-state :value)
                                       (= (:previous-value sub-state) (:value sub-state)))
                                (assoc-in all-state [sub-id :sub/traits :unchanged?] true)
                                all-state))
                            first-pass
                            first-pass)]
    second-pass))

(defn subscription-match-state
  "Build up the state of re-frame's running subscriptions over each matched epoch.
  Returns initial state as first item in list"
  [sub-state filtered-traces new-matches]
  ;; For each match that we process there are two phases:
  ;; - Phase 1: Update and collect the state of the subscriptions that have trace between the last epoch
  ;;   and the new epoch. These traces can be from hover state, local ratom events, figwheel re-renders
  ;;   or rft resetting app-db. Most of the time these traces are noisy and not interesting, but we do
  ;;   need to use them to maintain our subscription state. We also want to show them down below the main
  ;;   subs, in case someone is wondering where they went. If there are no sub traces to process then
  ;;   this phase doesn't run.
  ;; - Phase 2: Update and collect the state of the subscription traces within an epoch. These subscription
  ;;   traces are going to be changing because of app-db changes.
  ;; We collect and present the state for both phases for consumption in the subs panel

  ;; If you look closely at the state of the subscriptions, and the traces they derive from, you will
  ;; come to a disturbing realisation: Disposed reactions are resurrected and continue to be run.
  ;; This is tracked in https://github.com/reagent-project/reagent/pull/270.

  ;#?(:cljs (js/console.log "New matches?" (not (empty? new-matches))))
  (reductions (fn [state match]
                (let [previous-id      (:last-matched-id state)
                      first-match-id   (:id (first match))
                      last-match-id    (:id (last match))
                      pre-epoch-traces (into []
                                             (comp
                                               (id-between-xf (inc previous-id)
                                                              (dec first-match-id))
                                               (filter subscription?))
                                             filtered-traces)
                      epoch-traces     (into []
                                             (comp
                                               (id-between-xf first-match-id last-match-id)
                                               (filter subscription?))
                                             filtered-traces)
                      reaction-state   (:reaction-state state)
                      pre-epoch-state  (-> reaction-state
                                           (reset-sub-state)
                                           (process-sub-traces pre-epoch-traces))
                      epoch-state      (-> pre-epoch-state
                                           (reset-sub-state)
                                           (process-sub-traces epoch-traces))]
                  {:pre-epoch-state     pre-epoch-state
                   :reaction-state      epoch-state
                   :first-matched-id    first-match-id
                   :last-matched-id     last-match-id
                   :previous-matched-id previous-id}))
              sub-state
              new-matches))
