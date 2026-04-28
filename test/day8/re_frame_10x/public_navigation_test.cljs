(ns day8.re-frame-10x.public-navigation-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [re-frame.db                                                            :as userland.re-frame.db]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core           :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.db             :as rf.db]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.registrar      :as rf.registrar]
   [day8.re-frame-10x.public                                               :as public]))

(deftest previous-epoch-identifier-is-stable
  (testing "previous-epoch resolves to the public namespaced string identifier"
    (is (= "day8.re-frame-10x.public/previous-epoch" public/previous-epoch))))

(deftest next-epoch-identifier-is-stable
  (testing "next-epoch resolves to the public namespaced string identifier"
    (is (= "day8.re-frame-10x.public/next-epoch" public/next-epoch))))

(deftest reset-epochs-identifier-is-stable
  (testing "reset-epochs resolves to the public namespaced string identifier"
    (is (= "day8.re-frame-10x.public/reset-epochs" public/reset-epochs))))

(deftest replay-epoch-identifier-is-stable
  (testing "replay-epoch resolves to the public namespaced string identifier"
    (is (= "day8.re-frame-10x.public/replay-epoch" public/replay-epoch))))

(deftest reset-app-db-event-identifier-is-stable
  (testing "reset-app-db-event resolves to the public namespaced string identifier"
    (is (= "day8.re-frame-10x.public/reset-app-db" public/reset-app-db-event))))

(defn- prime-app-db! [match-ids selected-epoch-id]
  (reset! rf.db/app-db
          {:epochs   {:match-ids         match-ids
                      :selected-epoch-id selected-epoch-id
                      :matches-by-id     (zipmap match-ids (repeat nil))}
           :settings {:app-db-follows-events? false}}))

(deftest previous-epoch-event-steps-cursor-back
  (testing "[previous-epoch] from :b lands the cursor on :a"
    (let [snapshot @rf.db/app-db]
      (try
        (prime-app-db! [:a :b :c] :b)
        ;; The exported identifier is a string; the inlined router's
        ;; handler-lookup keys are keywords, so coerce when bypassing
        ;; dispatch! (which would normally do this for us).
        (rf/dispatch-sync [(keyword public/previous-epoch)])
        ;; NOTE: this does not exercise the forwarder→internal :dispatch edge.
        ;; dispatch-sync runs the forwarder synchronously but its :dispatch fx
        ;; queues the internal event for the next tick, so we fire
        ;; ::nav.events/previous directly to drive the cursor. The forwarder
        ;; edge itself is covered by
        ;; reset-app-db-event-resets-userland-app-db-without-moving-cursor,
        ;; which intercepts the :fx :dispatch handler.
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/previous])
        (is (= :a (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest previous-epoch-event-resets-userland-app-db-to-after-when-following-events
  (testing "[previous-epoch] resets userland app-db to the previous epoch's :app-db-after when app-db follows events"
    (let [rf-snapshot       @rf.db/app-db
          userland-snapshot @userland.re-frame.db/app-db
          previous-state    {:counter 1  :marker :previous}
          current-state     {:counter 99 :marker :diverged}
          previous-trace    {:op-type :event
                             :tags    {:app-db-after previous-state
                                       :event        [::previous-event]}}]
      (try
        (rf/purge-event-queue)
        (reset! rf.db/app-db
                {:epochs   {:match-ids         [:previous :current]
                            :selected-epoch-id :current
                            :matches-by-id     {:previous {:match-info [previous-trace]}
                                                :current  {:match-info []}}}
                 :settings {:app-db-follows-events? true}})
        (reset! userland.re-frame.db/app-db current-state)
        (rf/dispatch-sync [(keyword public/previous-epoch)])
        ;; NOTE: this does not exercise the forwarder→internal :dispatch edge.
        ;; dispatch-sync runs the forwarder but neither its :dispatch fx nor
        ;; the cascaded ::reset-current-epoch-app-db it would produce drain
        ;; under dispatch-sync, so we fire both directly to drive the reset.
        ;; The forwarder edge is covered by
        ;; reset-app-db-event-resets-userland-app-db-without-moving-cursor.
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/previous])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/reset-current-epoch-app-db :previous])
        (is (= previous-state @userland.re-frame.db/app-db)
            "userland app-db must follow the newly focused epoch's :app-db-after")
        (finally
          (rf/purge-event-queue)
          (reset! rf.db/app-db rf-snapshot)
          (reset! userland.re-frame.db/app-db userland-snapshot))))))

(deftest previous-epoch-fx-honours-no-op-at-oldest
  ;; Why test the pure helper instead of dispatch-syncing public/previous-epoch:
  ;; the internal ::nav.events/previous clobbers :selected-epoch-id to nil at
  ;; the oldest match and from live-tail, and a behavioural test that flushes
  ;; the inner re-fires that clobber regardless of what the forwarder did.
  (testing "at the oldest match — empty fx map (no-op)"
    (is (= {} (#'public/previous-epoch-fx {:match-ids [:a :b :c] :selected-epoch-id :a}))))
  (testing "live tail with multiple matches — load second-newest"
    (is (= {:dispatch [:day8.re-frame-10x.navigation.epochs.events/load :b]}
           (#'public/previous-epoch-fx {:match-ids [:a :b :c] :selected-epoch-id nil}))))
  (testing "live tail with a single match — no-op (no second-newest exists)"
    (is (= {} (#'public/previous-epoch-fx {:match-ids [:a] :selected-epoch-id nil}))))
  (testing "empty match-ids — no-op"
    (is (= {} (#'public/previous-epoch-fx {:match-ids [] :selected-epoch-id nil}))))
  (testing "middle of the list — defer to internal ::previous"
    (is (= {:dispatch [:day8.re-frame-10x.navigation.epochs.events/previous]}
           (#'public/previous-epoch-fx {:match-ids [:a :b :c] :selected-epoch-id :b})))))

(deftest next-epoch-event-steps-cursor-forward
  (testing "[next-epoch] from :b lands the cursor on :c"
    (let [snapshot @rf.db/app-db]
      (try
        (prime-app-db! [:a :b :c] :b)
        (rf/dispatch-sync [(keyword public/next-epoch)])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/next])
        (is (= :c (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest next-epoch-event-from-no-selection-jumps-to-tail
  (testing "[next-epoch] with no :selected-epoch-id lands on the newest match"
    (let [snapshot @rf.db/app-db]
      (try
        (prime-app-db! [:a :b :c :d :e] nil)
        (rf/dispatch-sync [(keyword public/next-epoch)])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/next])
        (is (= :e (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest load-epoch-event-loads-target
  (testing "[load-epoch <id>] from :a lands the cursor on :c"
    (let [snapshot @rf.db/app-db]
      (try
        (prime-app-db! [:a :b :c] :a)
        (rf/dispatch-sync [(keyword public/load-epoch) :c])
        ;; NOTE: this does not exercise the forwarder→internal :dispatch edge
        ;; (same gap as previous-epoch-event-steps-cursor-back); the forwarder
        ;; edge is covered by
        ;; reset-app-db-event-resets-userland-app-db-without-moving-cursor.
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/load :c])
        (is (= :c (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest reset-app-db-event-resets-userland-app-db-without-moving-cursor
  (testing "[reset-app-db-event <id>] resets app-db to a target epoch without moving 10x's cursor"
    (let [rf-snapshot       @rf.db/app-db
          userland-snapshot @userland.re-frame.db/app-db
          restore!          (rf/make-restore-fn)
          captured-dispatch (atom nil)
          target-state      {:counter 1 :marker :target}
          current-state     {:counter 99 :marker :current}
          target-trace      {:op-type :event
                             :tags    {:app-db-after target-state
                                       :event        [::target-event]}}]
      (try
        (rf/purge-event-queue)
        (reset! rf.db/app-db
                {:epochs   {:match-ids         [:target :current]
                            :selected-epoch-id :current
                            :matches-by-id     {:target  {:match-info [target-trace]}
                                                :current {:match-info []}}}
                 :settings {:app-db-follows-events? true}})
        (reset! userland.re-frame.db/app-db current-state)
        (swap! rf.registrar/kind->id->handler assoc-in [:fx :dispatch] #(reset! captured-dispatch %))
        (rf/dispatch-sync [(keyword public/reset-app-db-event) :target])
        (is (= [:day8.re-frame-10x.navigation.epochs.events/reset-current-epoch-app-db :target]
               @captured-dispatch)
            "public reset-app-db-event must dispatch the internal app-db reset primitive")
        (rf/dispatch-sync @captured-dispatch)
        (is (= target-state @userland.re-frame.db/app-db)
            "userland app-db must reset to the target epoch's :app-db-after")
        (is (= :current (get-in @rf.db/app-db [:epochs :selected-epoch-id]))
            "reset-app-db-event must not move 10x's selected epoch cursor")
        (finally
          (rf/purge-event-queue)
          (restore!)
          (reset! rf.db/app-db rf-snapshot)
          (reset! userland.re-frame.db/app-db userland-snapshot))))))

(deftest most-recent-epoch-event-jumps-to-tail
  (testing "[most-recent-epoch] from :a jumps to the tail :c"
    (let [snapshot @rf.db/app-db]
      (try
        (prime-app-db! [:a :b :c] :a)
        (rf/dispatch-sync [(keyword public/most-recent-epoch)])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/most-recent])
        (is (= :c (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest reset-epochs-clears-epochs-and-traces
  (testing "[reset-epochs] clears :epochs and :traces/:all"
    (let [snapshot @rf.db/app-db]
      (try
        (reset! rf.db/app-db
                {:epochs {:match-ids         [:a]
                          :selected-epoch-id :a
                          :matches-by-id     {:a {:match-info []}}}
                 :traces {:all [{:op-type :event :id 1}]}})
        (rf/dispatch-sync [(keyword public/reset-epochs)])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/reset])
        (is (nil? (:epochs @rf.db/app-db))
            ":epochs must be removed after reset")
        (is (nil? (get-in @rf.db/app-db [:traces :all]))
            ":traces/:all must be cleared after reset (dissoc-in)")
        (finally (reset! rf.db/app-db snapshot))))))

(deftest replay-epoch-empty-match-info-does-not-throw
  ;; Guard against a NPE-style regression in replay-epochs when the
  ;; selected epoch's :match-info is empty (no event-run trace yet —
  ;; happens at cold-start while the parser is still mid-stream). The
  ;; threading (-> nil metam/matched-event) and subsequent get-in calls
  ;; must all tolerate the missing trace; otherwise a probe-style poll
  ;; that lands on a half-built epoch crashes the whole router.
  (testing "[replay-epoch] with an empty :match-info must not throw"
    (let [rf-snapshot       @rf.db/app-db
          userland-snapshot @userland.re-frame.db/app-db]
      (try
        (reset! rf.db/app-db
                {:epochs   {:match-ids         [:only]
                            :selected-epoch-id :only
                            :matches-by-id     {:only {:match-info []}}}
                 :settings {:app-db-follows-events? false}})
        ;; If dispatch-sync throws, the assertion below is never reached
        ;; and the test fails. If it returns cleanly, we record the
        ;; positive observation.
        (rf/dispatch-sync [(keyword public/replay-epoch)])
        (is true "replay-epoch with empty :match-info returned without throwing")
        (finally
          (reset! rf.db/app-db rf-snapshot)
          (reset! userland.re-frame.db/app-db userland-snapshot))))))

(deftest replay-epoch-resets-userland-app-db-to-before
  (testing "[replay-epoch] resets userland app-db to the focused epoch's :app-db-before, ignoring whatever state was current — confirms the idempotent time-travel semantic the docstring promises"
    (let [rf-snapshot       @rf.db/app-db
          userland-snapshot @userland.re-frame.db/app-db
          before-state      {:counter 0  :marker :before}
          current-state     {:counter 99 :marker :diverged}
          replay-evt        [::test-replay-noop]
          stub-event-trace  {:op-type :event
                             :tags    {:app-db-before before-state
                                       :app-db-after  {:counter 1 :marker :after}
                                       :event         replay-evt}}]
      (try
        (reset! rf.db/app-db
                {:epochs   {:match-ids         [42]
                            :selected-epoch-id 42
                            :matches-by-id     {42 {:match-info [stub-event-trace]}}}
                 :settings {:app-db-follows-events? false}})
        (reset! userland.re-frame.db/app-db current-state)
        (rf/dispatch-sync [(keyword public/replay-epoch)])
        (is (= before-state @userland.re-frame.db/app-db)
            "userland app-db must be reset to :app-db-before, not retain the diverged current-state")
        (finally
          (reset! rf.db/app-db rf-snapshot)
          (reset! userland.re-frame.db/app-db userland-snapshot))))))
