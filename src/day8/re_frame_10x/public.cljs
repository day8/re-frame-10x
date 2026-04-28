(ns day8.re-frame-10x.public
  "Stable public surface for downstream tooling — re-frame-pair,
   custom devtools, performance dashboards.

   Resolves the rf1-jum companion-bead's five open questions:

   - Q1 (namespace name): `day8.re-frame-10x.public`. The bead's
     placeholder; chosen for consistency with the doc shorthand
     used in re-frame-pair's `companion-re-frame-10x.md` proposal.

   - Q2 (mutation API shape): event-keyword API. The mutation
     surface is exposed as keyword constants (see `load-epoch`,
     `most-recent-epoch`, `previous-epoch`, `next-epoch`,
     `reset!-event`, `replay-event` below) plus a `dispatch!`
     fn that routes through 10x's *inlined*
     re-frame router. Rationale: 10x events are registered against
     the inlined `day8.re-frame-10x.inlined-deps.re-frame.v1v3v0`
     re-frame core, NOT the user's re-frame; a consumer's plain
     `(re-frame.core/dispatch ...)` would never hit them. `dispatch!`
     is the bridge. Keyword constants are the durable contract — the
     internal event names (`:day8.re-frame-10x.navigation.epochs.events/load`
     etc.) are not part of the public API and may change.

   - Q3 (fields to expose): each public-epoch record carries
     `:sub-state-raw` and `:timings` alongside the existing
     `:match-info` per the doc recommendation. Naming intentionally
     differs from 10x's internal `:sub-state` / `:timing` so the
     public shape can evolve independently of the internal one.

   - Q4 (lifecycle hooks): deferred. Will surface here as a thin
     forwarder once re-frame's A4 (`register-epoch-cb`, tracked as
     rf-ybv) lands. No `on-epoch-start` / `on-epoch-complete` in
     this iteration — the bead recommends not duplicating an API
     that should belong to re-frame core.

   - Q5 (settings exposure): yes, `app-db-follows-events?` is
     part of the public API. Navigation events behave differently
     when it's false (epoch loads don't reset the user's app-db),
     and downstream tools that drive 10x programmatically need to
     branch on it.

   COMPATIBILITY

   - Strictly additive. Internal pipelines and the 10x UI continue
     to use internal namespaces; nothing here changes existing
     behaviour.
   - Public ns leaks NO inlined-rf version slug (`v1v3v0` etc.).
     Once consumers migrate to this surface, they can delete any
     hard-coded version-path fallback (the rf1-jum bead specifically
     calls out re-frame-pair's `inlined-rf-known-version-paths`
     vec as the target deletion).
   - `(version)` and `(capabilities)` exist for consumer
     branch-on-availability.

   USAGE FROM A SHADOW-CLJS CONSUMER

   At runtime, every defn here is reachable via the JS-munged path
   `day8.re_frame_10x.public.<munged-name>` on `goog.global`. A
   consumer that doesn't compile against re-frame-10x can probe
   the surface lazily — see `day8.re_frame_10x.public.loaded_QMARK_`
   as the durable feature-detection hook (analogous to
   re-frame-debux's `runtime-api?`).

   IMPLEMENTATION NOTE on the goog.global path: `public` is a JS
   reserved word, so the Closure :advanced compiler emits the
   per-var `goog.exportSymbol` calls under
   `day8.re_frame_10x.public$.<name>` — note the trailing `$` on the
   ns segment. Consumers that walk the documented un-suffixed path
   (`public.<name>`) would otherwise see undefined in :advanced
   builds while :none builds would work, masking the regression in
   dev. The bottom-of-namespace mirror block aliases the suffixed
   path back to the un-suffixed one on `goog.global` at namespace
   load time, so the documented contract above is the path consumers
   actually read in both :none and :advanced builds. The
   :public-advanced shadow-cljs build + the public-advanced-probe
   in test/ gate this in CI."
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.db   :as rf.db]
   [day8.re-frame-10x.navigation.epochs.events                   :as nav.events]
   [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

;; ---------------------------------------------------------------------------
;; Versioning + feature detection
;; ---------------------------------------------------------------------------

(def ^:export api-version
  "Integer that bumps with each backwards-incompatible change to the
   shape of the read API or the meaning of an event keyword. Consumers
   can branch on it via `(capabilities)` or read it directly via
   `goog.global.day8.re_frame_10x.public.api_version`."
  1)

(defn ^:export ^boolean loaded?
  "True when this namespace is loaded — i.e. when the public surface
   is available in the runtime. Stable feature-detection hook for
   downstream tooling. The presence of the var IS the contract;
   the body is `true` and consumers shouldn't read meaning into the
   return value beyond 'this fn ran'.

   Probed externally as `goog.global.day8.re_frame_10x.public.loaded_QMARK_`
   so consumers don't have to compile against this namespace."
  []
  true)

(defn ^:export version
  "Returns `{:api <int>}` describing the public-surface version the
   currently-loaded 10x build implements. Bumps with backwards-
   incompatible changes to read-API shape or event-kw semantics.
   Consumers branch on this when they want to support multiple 10x
   versions side-by-side."
  []
  {:api api-version})

(defn ^:export capabilities
  "Set of feature keywords this build supports. Reserved for
   future growth — today returns the baseline set. Consumers
   should treat unknown keywords as 'not supported'.

   Read API flags use a `:resource/action` shape — `:epochs/read`,
   `:traces/read`, `:settings/app-db-follows-events`. The
   `:events/...` family flags the mutation API: `:events/navigate`,
   `:events/reset`, `:events/replay` mark the corresponding event
   keyword constants, and `:events/dispatch!` marks the bridge fn
   that routes event vectors into 10x's inlined re-frame router.

   `:epochs/navigate` and `:events/navigate` are synonyms — the
   former predates the `:events/...` family and is retained for
   compatibility. New consumers should prefer the `:events/...`
   namespace when branching on the mutation surface, since it
   keeps reset / replay / dispatch! / navigate flags in one
   semantic family."
  []
  #{:public/v1
    :epochs/read
    :epochs/navigate
    :traces/read
    :settings/app-db-follows-events
    :events/navigate
    :events/reset
    :events/replay
    :events/dispatch!})

;; ---------------------------------------------------------------------------
;; Read API — epochs
;; ---------------------------------------------------------------------------

(defn- match->public-epoch
  "Convert one internal match record into the public-epoch shape.
   Public keys (`:sub-state-raw`, `:timings`) intentionally differ
   from internal (`:sub-state`, `:timing`) so the public shape can
   evolve independently. The match's id is hoisted to a top-level
   `:id` for ergonomic indexing."
  [match]
  (when match
    {:id            (some-> match :match-info first :id)
     :match-info    (:match-info match)
     :sub-state-raw (:sub-state match)
     :timings       (:timing match)}))

(defn ^:export epochs
  "Vec of every retained epoch in 10x's ring buffer, in the order
   10x stored them (oldest first; `last` is newest). Each element is
   a public-epoch record (see `match->public-epoch`).

   Returns `[]` when 10x's app-db hasn't initialised yet — this lets
   consumers no-op gracefully on cold starts instead of having to
   probe `loaded?` ahead of every call."
  []
  (mapv match->public-epoch
        (some-> rf.db/app-db deref :epochs :matches)))

(defn ^:export epoch-count
  "Number of retained epochs. Cheap — reads the `:match-ids` vec
   length without rebuilding the full coerced epoch maps. Suitable
   for poll-cadence callers (e.g. re-frame-pair's
   `watch-epochs.sh`)."
  []
  (count (some-> rf.db/app-db deref :epochs :match-ids)))

(defn ^:export latest-epoch-id
  "Id of the newest (most-recent) match in the buffer, or nil if
   empty. Cheap — reads `:match-ids`' last element. 10x stores
   epochs oldest-first, so the newest dispatch is at the tail."
  []
  (some-> rf.db/app-db deref :epochs :match-ids last))

(defn ^:export selected-epoch-id
  "Id of the epoch the 10x UI is currently focused on, or nil if
   the user is on the live tail. Different from `latest-epoch-id`:
   when the user navigates back through history, `selected-epoch-id`
   stays put while `latest-epoch-id` advances with new dispatches."
  []
  (some-> rf.db/app-db deref :epochs :selected-epoch-id))

(defn ^:export epoch-by-id
  "Public-epoch record for the given match id, or nil if unknown
   (id never existed, or aged out of the buffer)."
  [id]
  (some-> rf.db/app-db deref :epochs :matches-by-id (get id)
          match->public-epoch))

;; ---------------------------------------------------------------------------
;; Read API — traces (the underlying re-frame.trace stream)
;; ---------------------------------------------------------------------------

(defn ^:export all-traces
  "The full retained trace stream — every `:event :sub/run :sub/create
   :render :raf` etc. trace, in order. Vec; empty when 10x hasn't
   initialised. Consumers that want to slice differently than 10x's
   epoch-buffer (e.g. by op-type, time range, custom group key)
   read this directly."
  []
  (or (some-> rf.db/app-db deref :traces :all) []))

;; ---------------------------------------------------------------------------
;; Read API — settings
;; ---------------------------------------------------------------------------

(defn ^:export ^boolean app-db-follows-events?
  "True iff 10x is currently configured to reset the user's app-db
   to the focused epoch's `:app-db-after` snapshot when navigation
   events fire (the default). When false, navigation events update
   only 10x's UI cursor without touching userland — important for
   downstream tools that drive 10x programmatically and need to
   branch on whether `[load-epoch <id>]` will mutate userland or
   just update the cursor."
  []
  (boolean (some-> rf.db/app-db deref :settings :app-db-follows-events?)))

;; ---------------------------------------------------------------------------
;; Mutation API — event keywords + dispatch! bridge
;; ---------------------------------------------------------------------------

(def ^:export load-epoch
  "Public event keyword. Dispatch via `(dispatch! [load-epoch <id>])`
   to make 10x focus on the epoch with the given match id. When
   `app-db-follows-events?` is true (the default), the user's app-db
   resets to that epoch's `:app-db-after`."
  ::load-epoch)

(def ^:export most-recent-epoch
  "Public event keyword. Dispatch via `(dispatch! [most-recent-epoch])`
   to make 10x focus on the newest match (the 'live tail'). Useful
   after a programmatic load-epoch to return control to the user."
  ::most-recent-epoch)

(def ^:export previous-epoch
  "Public event keyword. Dispatch via `(dispatch! [previous-epoch])`
   to step the 10x UI cursor one match backwards from the currently
   focused epoch. No-op when already at the oldest retained match.
   When no epoch is focused (the 'live tail'), steps to the
   second-newest retained match; no-op if fewer than two matches
   are retained. When `app-db-follows-events?` is true, the user's
   app-db resets to the new epoch's `:app-db-after`."
  ::previous-epoch)

(def ^:export next-epoch
  "Public event keyword. Dispatch via `(dispatch! [next-epoch])` to
   step the 10x UI cursor one match forwards from the currently
   focused epoch. When no epoch is focused, jumps to the live tail.
   When `app-db-follows-events?` is true, the user's app-db resets
   to the new epoch's `:app-db-after`."
  ::next-epoch)

(def ^:export reset-event
  "Public event keyword. Dispatch via `(dispatch! [reset-event])` to
   clear 10x's epoch buffer and reset re-frame.trace's id counter.
   Equivalent to clicking the 'reset' button in 10x's UI."
  ::reset)

(def ^:export replay-event
  "Public event keyword. Dispatch via `(dispatch! [replay-event])` to
   replay the focused epoch's event against the app-db state captured
   BEFORE that event originally fired (the epoch's `:app-db-before`).
   Equivalent to time-travelling to the epoch and re-firing — the
   resulting userland app-db is the post-event state of that epoch,
   regardless of any subsequent dispatches. Idempotent: repeated
   replays of the same epoch produce the same post-event state.
   Equivalent to clicking 10x's 'replay' button."
  ::replay)

(rf/reg-event-fx
 ::load-epoch
 [rf/trim-v]
 (fn [_ [id]]
   {:dispatch [::nav.events/load id]}))

(rf/reg-event-fx
 ::most-recent-epoch
 (fn [_ _]
   {:dispatch [::nav.events/most-recent]}))

(defn- previous-epoch-fx
  "Compute the re-frame effect map for the `::previous-epoch` forwarder
   given the `:epochs` substate. Extracted so the no-op-at-oldest and
   live-tail contracts can be unit-tested without flushing the async
   fx queue. The internal `::nav.events/previous` handler clobbers
   `:selected-epoch-id` to nil at both boundaries, so the public
   forwarder gates those entry conditions itself instead of delegating."
  [{:keys [match-ids selected-epoch-id]}]
  (cond
    (empty? match-ids)                      {}
    (= selected-epoch-id (first match-ids)) {}
    (nil? selected-epoch-id)                (if-let [target (when (> (count match-ids) 1)
                                                              (tools.coll/last-in-vec (pop match-ids)))]
                                              {:dispatch [::nav.events/load target]}
                                              {})
    :else                                   {:dispatch [::nav.events/previous]}))

(rf/reg-event-fx
 ::previous-epoch
 (fn [{:keys [db]} _]
   (previous-epoch-fx (:epochs db))))

(rf/reg-event-fx
 ::next-epoch
 (fn [_ _]
   {:dispatch [::nav.events/next]}))

(rf/reg-event-fx
 ::reset
 (fn [_ _]
   {:dispatch [::nav.events/reset]}))

(rf/reg-event-db
 ::replay
 [(rf/path [:epochs])]
 (fn [epochs _]
   (nav.events/replay-epochs epochs)))

(defn ^:export dispatch!
  "Mutation-API bridge: routes `event-vec` (e.g.
   `[load-epoch 42]`) through 10x's *inlined* re-frame router.
   Necessary because 10x events register against the inlined
   `day8.re-frame-10x.inlined-deps.re-frame.v1v3v0` re-frame core
   — a consumer's plain `(re-frame.core/dispatch ...)` would never
   reach them. Use the keyword constants exported from this
   namespace as the first element of `event-vec`; the keywords are
   the durable contract.

   Coerces JS-array arguments to CLJS vectors, so pure-JS callers
   via `goog.global.day8.re_frame_10x.public.dispatch_BANG_(['evt', arg])`
   work — the inlined router validates events with `(vector? ...)`,
   which JS arrays fail."
  [event-vec]
  (rf/dispatch (if (vector? event-vec) event-vec (vec (js->clj event-vec)))))

;; ---------------------------------------------------------------------------
;; goog.global path mirror — see ns docstring "IMPLEMENTATION NOTE".
;; ---------------------------------------------------------------------------

;; Top-level side effect: after every ^:export above has emitted a
;; goog.exportSymbol("day8.re_frame_10x.public$.<name>", ...) call (the
;; Closure compiler suffixes `public` with `$` because it is a JS
;; reserved word), mirror the resulting `public$` object back onto
;; `public` on the same parent so the documented un-suffixed path
;; consumers walk (`goog.global.day8.re_frame_10x.public.<name>`)
;; resolves in :advanced builds. Without this, every consumer that
;; relies on the public surface from a JS-globals lookup gets
;; undefined post-:advanced and silently falls back to brittle
;; inlined-rf-version walking — defeating the rf1-jum premise.
;;
;; Guarded on `js/goog.global` existence so this is a no-op in any
;; environment that doesn't have the Closure global object set up
;; (e.g. CLJS REPLs that load namespaces through different shims).
(when-let [g (and (exists? js/goog) (.-global js/goog))]
  (let [rf10x   (some-> g (aget "day8") (aget "re_frame_10x"))
        suffixed (some-> rf10x (aget "public$"))]
    (when (and rf10x suffixed)
      (aset rf10x "public" suffixed))))
