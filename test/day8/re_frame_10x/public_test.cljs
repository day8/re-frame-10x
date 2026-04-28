(ns day8.re-frame-10x.public-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [clojure.walk :as walk]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.db :as rf.db]
   [day8.re-frame-10x.public :as public]))

(def ^:private stub-match
  {:match-info [{:id 7 :event [:user/save 42]}]
   :sub-state  {:before {} :after {}}
   :timing     {:start 100 :end 105}})

(def ^:private stub-db
  {:epochs   {:matches           [stub-match]
              :match-ids         [7]
              :matches-by-id     {7 stub-match}
              :selected-epoch-id 7}
   :traces   {:all [{:op-type :event :id 1}]}
   :settings {:app-db-follows-events? true}})

(deftest feature-detection-contract
  (is (some? (public/loaded?)))
  (is (= {:api 1} (public/version)))
  (is (= 1 public/api-version))
  (let [caps (public/capabilities)]
    (is (set? caps))
    (is (contains? caps :public/v1))
    (is (contains? caps :epochs/read))
    (is (contains? caps :epochs/navigate))
    (is (contains? caps :traces/read))
    (is (contains? caps :settings/app-db-follows-events))
    ;; The mutation-API flags exposed under the :events/... namespace
    ;; let consumers branch on whether reset / replay / dispatch! /
    ;; navigation event keywords are wired up. Without them, a future
    ;; read-only inspect mode (mutation API stripped for security)
    ;; would be undetectable, and consumers that want to test for the
    ;; bridge fn would have to fall back to (boolean public/dispatch!),
    ;; which fights the var-presence-is-the-contract pattern used by
    ;; loaded?.
    (is (contains? caps :events/navigate))
    (is (contains? caps :events/reset))
    (is (contains? caps :events/replay))
    (is (contains? caps :events/dispatch!))))

(deftest public-surface-presence
  (is (fn? public/loaded?))
  (is (fn? public/version))
  (is (fn? public/capabilities))
  (is (fn? public/epochs))
  (is (fn? public/epoch-count))
  (is (fn? public/latest-epoch-id))
  (is (fn? public/selected-epoch-id))
  (is (fn? public/epoch-by-id))
  (is (fn? public/all-traces))
  (is (fn? public/app-db-follows-events?))
  (is (fn? public/dispatch!)))

(deftest mutation-event-id-constants
  ;; Event identifiers are fully-qualified strings rather than CLJS
  ;; keywords so a pure-JS caller probing via `goog.global` gets back
  ;; a JS-constructable value. dispatch! coerces strings back to
  ;; keywords for the inlined router's handler-lookup.
  (is (string? public/load-epoch))
  (is (string? public/most-recent-epoch))
  (is (string? public/reset-event))
  (is (string? public/replay-event))
  (is (= 4 (count #{public/load-epoch
                    public/most-recent-epoch
                    public/reset-event
                    public/replay-event}))
      "the four mutation event identifiers must be distinct"))

(deftest match-to-public-epoch-shape
  (with-redefs [rf.db/app-db (atom stub-db)]
    (let [[ep] (public/epochs)]
      (is (= #{:id :match-info :sub-state-raw :timings} (set (keys ep))))
      (is (= 7 (:id ep)))
      (is (= [{:id 7 :event [:user/save 42]}] (:match-info ep)))
      (is (= {:before {} :after {}} (:sub-state-raw ep)))
      (is (= {:start 100 :end 105} (:timings ep)))
      (is (= ep (public/epoch-by-id 7))))
    (is (nil? (public/epoch-by-id 999)))
    (is (= 1 (public/epoch-count)))
    (is (= 7 (public/latest-epoch-id)))
    (is (= 7 (public/selected-epoch-id)))
    (is (true? (public/app-db-follows-events?)))))

(deftest latest-epoch-id-returns-newest
  ;; 10x stores epochs oldest-first; latest-epoch-id must return the tail
  ;; (newest dispatch), not the head (oldest). A first/last swap in the
  ;; implementation would silently flip the semantic — guard against that
  ;; by priming :match-ids with multiple distinct ids.
  (with-redefs [rf.db/app-db (atom {:epochs {:match-ids [1 2 3 7 11]}})]
    (is (= 11 (public/latest-epoch-id))
        "latest-epoch-id must return the last (newest) match-id, not the first")
    (is (not= 1 (public/latest-epoch-id))
        "latest-epoch-id must not return the head (oldest) of :match-ids")
    (is (some? (public/latest-epoch-id))
        "latest-epoch-id must not return nil when :match-ids is non-empty")))

(deftest read-api-cold-start
  (with-redefs [rf.db/app-db (atom {})]
    (is (= [] (public/epochs)))
    (is (= 0 (public/epoch-count)))
    (is (nil? (public/latest-epoch-id)))
    (is (nil? (public/selected-epoch-id)))
    (is (nil? (public/epoch-by-id 1)))
    (is (= [] (public/all-traces)))
    (is (false? (public/app-db-follows-events?)))))

(deftest dispatch-bang-coerces-js-array
  ;; Pure-JS callers reach dispatch! via goog.global as
  ;; day8.re_frame_10x.public.dispatch_BANG_(["evt", arg]) and pass a
  ;; JS Array. The inlined re-frame router's first-in-vector predicate
  ;; uses (vector? v), which is false for JS arrays, so re-frame logs
  ;; an error and drops the dispatch. dispatch! must coerce.
  (let [captured (atom nil)]
    (with-redefs [rf/dispatch (fn [event-v] (reset! captured event-v))]
      (public/dispatch! #js ["my-event" 42])
      (is (vector? @captured)
          "dispatch! must convert a JS Array to a CLJS vector before forwarding")
      (is (= [:my-event 42] @captured)
          "the coerced vector must preserve element order and values, with the head string keywordised for handler-lookup")))
  ;; CLJS-vector callers with a keyword head must pass through —
  ;; no re-allocation when input is already a fully-typed vector.
  (let [captured (atom nil)
        v        [::foo 1 2]]
    (with-redefs [rf/dispatch (fn [event-v] (reset! captured event-v))]
      (public/dispatch! v)
      (is (identical? v @captured)
          "a CLJS vector with a keyword head must be passed through unchanged"))))

(deftest dispatch-bang-coerces-string-head-to-keyword
  ;; Event identifiers are exported as fully-qualified strings, so a
  ;; CLJS caller using `(public/dispatch! [public/load-epoch 42])` and
  ;; a pure-JS caller using
  ;; `dispatch_BANG_(["day8.re-frame-10x.public/load-epoch", 42])` both
  ;; arrive here with a string head. The inlined router's handler-lookup
  ;; keys are keywords, so dispatch! must keywordise the head before
  ;; forwarding. Without this, the dispatch silently no-ops.
  (let [captured (atom nil)]
    (with-redefs [rf/dispatch (fn [event-v] (reset! captured event-v))]
      (public/dispatch! [public/load-epoch 42])
      (is (= [:day8.re-frame-10x.public/load-epoch 42] @captured)
          "the head string must be keywordised so it matches the registered handler key"))))

(deftest no-version-slug-leak
  (with-redefs [rf.db/app-db (atom stub-db)]
    (let [returns   [(public/loaded?)
                     (public/version)
                     (public/capabilities)
                     (public/epochs)
                     (public/epoch-count)
                     (public/latest-epoch-id)
                     (public/selected-epoch-id)
                     (public/epoch-by-id 7)
                     (public/epoch-by-id 999)
                     (public/all-traces)
                     (public/app-db-follows-events?)
                     public/api-version
                     public/load-epoch
                     public/most-recent-epoch
                     public/previous-epoch
                     public/next-epoch
                     public/reset-event
                     public/replay-event]
          forbidden ["v1v3v0" "inlined-deps" "inlined_deps"]
          leaked    (atom [])]
      (doseq [r returns]
        (walk/postwalk
         (fn [x]
           (when (or (string? x) (keyword? x) (symbol? x))
             (let [s (if (string? x) x (str x))]
               (doseq [bad forbidden]
                 (when (str/includes? s bad)
                   (swap! leaked conj {:value x :substring bad})))))
           x)
         r))
      (is (empty? @leaked)
          (str "Public API leaks inlined-rf version-slug content: "
               (pr-str @leaked))))))
