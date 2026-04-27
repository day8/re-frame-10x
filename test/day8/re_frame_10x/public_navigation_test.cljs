(ns day8.re-frame-10x.public-navigation-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [re-frame.db                                                  :as userland.re-frame.db]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.db   :as rf.db]
   [day8.re-frame-10x.public                                     :as public]))

(deftest previous-epoch-keyword-is-stable
  (testing "previous-epoch resolves to the public namespaced keyword"
    (is (= :day8.re-frame-10x.public/previous-epoch public/previous-epoch))))

(deftest next-epoch-keyword-is-stable
  (testing "next-epoch resolves to the public namespaced keyword"
    (is (= :day8.re-frame-10x.public/next-epoch public/next-epoch))))

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
        (rf/dispatch-sync [public/previous-epoch])
        ;; The public forwarder's :dispatch fx enqueues the internal event
        ;; asynchronously; dispatch-sync of the forwarder alone won't run it,
        ;; so flush by firing the internal event synchronously too.
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/previous])
        (is (= :a (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

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
        (rf/dispatch-sync [public/next-epoch])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/next])
        (is (= :c (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest next-epoch-event-from-no-selection-jumps-to-tail
  (testing "[next-epoch] with no :selected-epoch-id lands on the newest match"
    (let [snapshot @rf.db/app-db]
      (try
        (prime-app-db! [:a :b :c :d :e] nil)
        (rf/dispatch-sync [public/next-epoch])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/next])
        (is (= :e (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))

(deftest replay-event-resets-userland-app-db-to-before
  (testing "[replay-event] resets userland app-db to the focused epoch's :app-db-before, ignoring whatever state was current — confirms the idempotent time-travel semantic the docstring promises"
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
        (rf/dispatch-sync [public/replay-event])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/replay])
        (is (= before-state @userland.re-frame.db/app-db)
            "userland app-db must be reset to :app-db-before, not retain the diverged current-state")
        (is (= replay-evt (get-in @rf.db/app-db [:epochs :replay]))
            "the event vector must be staged for ::quiescent to re-fire after the reset")
        (finally
          (reset! rf.db/app-db rf-snapshot)
          (reset! userland.re-frame.db/app-db userland-snapshot))))))
