(ns day8.re-frame-10x.public-navigation-test
  (:require
   [clojure.test :refer [deftest is testing]]
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
        (prime-app-db! [:a :b :c] nil)
        (rf/dispatch-sync [public/next-epoch])
        (rf/dispatch-sync [:day8.re-frame-10x.navigation.epochs.events/next])
        (is (= :c (get-in @rf.db/app-db [:epochs :selected-epoch-id])))
        (finally (reset! rf.db/app-db snapshot))))))
