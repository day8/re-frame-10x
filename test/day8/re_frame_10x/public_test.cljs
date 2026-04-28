(ns day8.re-frame-10x.public-test
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [clojure.walk :as walk]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.db   :as rf.db]
   [day8.re-frame-10x.public                                     :as public]))

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

(def ^:private forbidden-version-slug-substrings
  ["v1v3v0" "inlined-deps" "inlined_deps"])

(defn- version-slug-leaks [value]
  (let [leaked (atom [])]
    (walk/postwalk
     (fn [x]
       (when (or (string? x) (keyword? x) (symbol? x))
         (let [s (if (string? x) x (str x))]
           (doseq [bad forbidden-version-slug-substrings]
             (when (str/includes? s bad)
               (swap! leaked conj {:value x :substring bad})))))
       x)
     value)
    @leaked))

(deftest feature-detection-contract
  (is (some? (public/loaded?)))
  (is (= {:api 2} (public/version)))
  (is (= 2 public/api-version))
  (let [caps (public/capabilities)]
    (is (set? caps))
    (is (contains? caps :public/v1))
    (is (contains? caps :public/v2))
    (is (contains? caps :epochs/read))
    (is (contains? caps :epochs/navigate))
    (is (contains? caps :epochs/reset-app-db))
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
    (is (contains? caps :events/reset-app-db))
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
  (is (string? public/previous-epoch))
  (is (string? public/next-epoch))
  (is (string? public/reset-epochs))
  (is (string? public/replay-epoch))
  (is (string? public/reset-app-db-event))
  ;; Include the navigation identifiers in the distinctness check so a
  ;; future identifier-collision regression on the navigation pair is
  ;; caught alongside the others.
  (is (= 7 (count #{public/load-epoch
                    public/most-recent-epoch
                    public/previous-epoch
                    public/next-epoch
                    public/reset-epochs
                    public/reset-app-db-event
                    public/replay-epoch}))
      "the seven mutation event identifiers must be distinct"))

(deftest capabilities-forward-compat-contract
  ;; The capabilities docstring (public.cljs:120-147) promises consumers
  ;; should treat unknown keywords as "not supported". Lock the contract:
  ;; (1) the returned set must be a subset of what the docstring lists
  ;;     so a future change can't ship an undocumented capability
  ;;     without updating the docstring;
  ;; (2) (contains? caps unknown-kw) must be false for any keyword the
  ;;     docstring doesn't enumerate, so consumer code that branches on
  ;;     unknown features gets a clean "not supported" read.
  (let [caps         (public/capabilities)
        documented   #{:public/v1
                       :public/v2
                       :epochs/read
                       :epochs/navigate
                       :epochs/reset-app-db
                       :traces/read
                       :settings/app-db-follows-events
                       :events/navigate
                       :events/reset
                       :events/replay
                       :events/reset-app-db
                       :events/dispatch!}
        undocumented (set/difference caps documented)]
    (is (empty? undocumented)
        (str "capabilities returns undocumented keywords: " (pr-str undocumented)))
    (is (false? (contains? caps :events/unknown-future-action))
        "unknown-capability lookup must be contains?-false")
    (is (false? (contains? caps :something/totally-made-up))
        "the docstring promises consumers can treat unknown keywords as 'not supported'")))

(deftest match-to-public-epoch-shape
  (with-redefs [rf.db/app-db (atom stub-db)]
    (let [[ep] (public/epochs)]
      (is (every? #(contains? ep %) [:id :match-info :sub-state-raw :timings]))
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

(deftest epochs-caches-public-vec-by-matches-identity
  (let [matches       [stub-match]
        next-match    (assoc stub-match :match-info [{:id 8 :event [:user/delete 8]}])
        next-matches  (conj matches next-match)
        app-db        (atom {:epochs {:matches matches}})]
    (with-redefs [rf.db/app-db app-db]
      (let [first-read  (public/epochs)
            second-read (public/epochs)]
        (is (identical? first-read second-read)
            "repeat polls should reuse the public epoch vec while :matches identity is unchanged")
        (swap! app-db assoc-in [:epochs :matches] next-matches)
        (let [third-read (public/epochs)]
          (is (not (identical? first-read third-read))
              "replacing :matches must invalidate the cached public epoch vec")
          (is (= [7 8] (mapv :id third-read))))))))

(deftest match-to-public-epoch-edge-cases
  (let [to-public #'public/match->public-epoch]
    (is (nil? (to-public nil)))
    (is (= {:id nil
            :match-info []
            :sub-state-raw {}
            :timings {}}
           (to-public {:match-info []
                       :sub-state {}
                       :timing {}})))
    (is (= {:id nil
            :match-info nil
            :sub-state-raw {}
            :timings {}}
           (to-public {:sub-state {}
                       :timing {}})))
    (let [ep (to-public {:match-info [{:id :first} {:id :second}]
                         :sub-state {}
                         :timing {}})]
      (is (= :first (:id ep)))
      (is (= [{:id :first} {:id :second}] (:match-info ep))))))

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

(deftest read-api-partial-init
  (with-redefs [rf.db/app-db (atom {:epochs {:matches []}})]
    (is (= [] (public/epochs))))
  (with-redefs [rf.db/app-db (atom {:epochs {:match-ids [1 2 3]}})]
    (is (nil? (public/epoch-by-id 1)))
    (is (= 3 (public/epoch-count)))))

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

(deftest dispatch-bang-end-to-end-load-epoch
  ;; The dispatch-bang-coerces-* tests above only verify what was
  ;; passed to rf/dispatch — they don't observe the resulting state
  ;; mutation. A regression where public.cljs's rf alias was re-pointed
  ;; at userland re-frame.core would silently drop every mutation
  ;; (10x events register against the inlined router; userland's
  ;; dispatch never reaches them) yet still pass the existing
  ;; capture-style assertions.
  ;;
  ;; Close the loop end-to-end: drive a real load-epoch via
  ;; (public/dispatch! [public/load-epoch :c]) — the same shape a
  ;; consumer would use — and assert the *inlined* router's
  ;; :selected-epoch-id moves through public->internal translation
  ;; into ::nav.events/load. An alias regression that re-pointed
  ;; public.cljs at userland would dispatch to userland's
  ;; ::nav.events/load — which doesn't exist there — and the
  ;; :selected-epoch-id would not move, failing the assertion.
  (let [restore! (rf/make-restore-fn)]
    (try
      (reset! rf.db/app-db
              {:epochs   {:match-ids         [:a :b :c]
                          :selected-epoch-id :a
                          :matches-by-id     (zipmap [:a :b :c] (repeat nil))}
               :settings {:app-db-follows-events? false}})
      (with-redefs [rf/dispatch rf/dispatch-sync]
        (public/dispatch! [public/load-epoch :c]))
      (is (= :c (get-in @rf.db/app-db [:epochs :selected-epoch-id]))
          "dispatch! → ::nav.events/load must move :selected-epoch-id from :a to :c on the inlined router")
      (finally
        (rf/purge-event-queue)
        (restore!)))))

(deftest dispatch-bang-end-to-end-reset-app-db-event
  ;; Pin the public->internal translation for reset-app-db-event so a
  ;; map-entry rename or accidental drop is caught. The load-epoch
  ;; end-to-end test above closes the alias-repointing loop end-to-end;
  ;; here we only need translation correctness for this specific id.
  (let [captured (atom nil)]
    (with-redefs [rf/dispatch (fn [event-v] (reset! captured event-v))]
      (public/dispatch! [public/reset-app-db-event :epoch-42]))
    (is (= [:day8.re-frame-10x.navigation.epochs.events/reset-current-epoch-app-db :epoch-42]
           @captured)
        "dispatch! must translate public reset-app-db-event to the internal reset primitive kw")))

(deftest dispatch-bang-coerces-string-head-to-keyword-and-translates
  ;; Event identifiers are exported as fully-qualified strings, so a
  ;; CLJS caller using `(public/dispatch! [public/load-epoch 42])` and
  ;; a pure-JS caller using
  ;; `dispatch_BANG_(["day8.re-frame-10x.public/load-epoch", 42])` both
  ;; arrive here with a string head. The inlined router's handler-lookup
  ;; keys are keywords, so dispatch! must keywordise the head — and
  ;; then translate that public kw to the internal kw the handler is
  ;; registered under. Without keywordising, the dispatch silently
  ;; no-ops; without translating, it hits a non-existent
  ;; `:public/load-epoch` handler (no public-side forwarder is
  ;; registered for the map-routed events post-rf1-4zb).
  (let [captured (atom nil)]
    (with-redefs [rf/dispatch (fn [event-v] (reset! captured event-v))]
      (public/dispatch! [public/load-epoch 42])
      (is (= [:day8.re-frame-10x.navigation.epochs.events/load 42] @captured)
          "the head string must be keywordised AND translated to the internal handler kw"))))

(deftest no-version-slug-leak-canary
  (let [leaked (version-slug-leaks {:k :day8.re-frame-10x.inlined-deps.foo/bar
                                    :v "v1v3v0/something"
                                    :s 'inlined_deps.x/y})]
    (is (= 3 (count leaked))
        "canary: walker must flag every forbidden substring")))

(deftest no-version-slug-leak
  ;; Load-bearing leak-detection invariant for the public surface
  ;; (see public.cljs:53-57): every public read-fn return value must be
  ;; free of inlined-rf version-slug content. The forbidden vec covers
  ;; both kebab ("inlined-deps") and snake ("inlined_deps") forms because
  ;; cljs munging flips between them at runtime — a symbol prints kebab,
  ;; but the same identifier reaches JS as snake_case, and either form
  ;; reaching a consumer counts as a leak.
  ;;
  ;; Failing this test means re-frame-pair's planned deletion of its
  ;; inlined-rf-known-version-paths fallback (rf1-jum) is at risk. Do
  ;; NOT add an exclusion or trim the forbidden vec to make a new public
  ;; defn pass — fix the leak in the public defn instead. If the test
  ;; needs structural changes, consult the test author first.
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
                     public/reset-epochs
                     public/reset-app-db-event
                     public/replay-epoch]
          leaked    (into [] (mapcat version-slug-leaks) returns)]
      (is (empty? leaked)
          (str "Public API leaks inlined-rf version-slug content: "
               (pr-str leaked))))))
