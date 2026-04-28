(ns ^:experimental day8.re-frame-10x.public
  "Experimental public surface for downstream tooling — re-frame-pair,
   custom devtools, performance dashboards.

   STABILITY

   Marked `^:experimental` on the ns and on every public defn per
   companion-re-frame-10x.md §A2 (line 227): the marker stays until a
   *released* re-frame-pair JAR consumes this surface, at which point
   the markers can be removed and the opener flipped to 'Stable'. The
   local re-frame-pair migration wires this up under `:local/root`,
   but no released JAR ships against it yet, so the spec's
   experimental gate is not met.

   VERSIONING + FEATURE DETECTION

   `loaded?` is the durable presence hook — its body returns `true`,
   so consumers branch on the var existing, not on its return value.
   `(version)` returns `{:api <int>}`, where the integer bumps on
   every public-surface contract revision (new stable event
   identifiers, read-API shape or semantic changes).
   `(capabilities)` returns the feature-keyword set this build
   supports; consumers branching across builds should treat unknown
   keywords as 'not supported'.

   READ API — EPOCHS

   Each public-epoch record carries `:id`, `:match-info`,
   `:sub-state-raw`, and `:timings`. `:sub-state-raw` / `:timings`
   intentionally differ from 10x's internal `:sub-state` / `:timing`
   so the public shape can evolve independently of the internal
   one. `epochs` is a vec ordered oldest-first (newest dispatch at
   the tail), and returns `[]` before 10x's app-db initialises —
   consumers can no-op on cold start without probing `loaded?` ahead
   of every call.

   READ API — TRACES

   `all-traces` exposes the full underlying re-frame.trace stream
   in order. Consumers that want to slice differently than 10x's
   epoch buffer (by op-type, time range, custom group key) read
   this directly.

   READ API — SETTINGS

   `app-db-follows-events?` reports whether 10x is currently
   configured to reset the user's app-db to the focused epoch's
   `:app-db-after` snapshot when navigation events fire (the
   default). Downstream tools driving 10x programmatically branch
   on it because navigation events only mutate userland when it is
   true.

   MUTATION API

   The mutation surface is a set of fully-qualified-string event
   identifiers (`load-epoch`, `most-recent-epoch`, `previous-epoch`,
   `next-epoch`, `reset-epochs`, `replay-epoch`,
   `reset-app-db-event`) plus a `dispatch!` bridge fn. Strings (not
   keywords) so a pure-JS caller probing
   `goog.global.day8.re_frame_10x.public.load_epoch` gets a
   JS-constructable value back; CLJS callers see exactly the same
   string and pass it through `dispatch!` the same way. The string
   identifiers are the durable contract — the internal event names
   (`:day8.re-frame-10x.navigation.epochs.events/load` etc.) are
   not part of the public API and may change.

   `dispatch!` is the bridge: 10x's events register against the
   inlined `day8.re-frame-10x.inlined-deps.re-frame.v1v3v0`
   re-frame core, NOT the user's re-frame, so a consumer's plain
   `(re-frame.core/dispatch ...)` would never reach them.
   `dispatch!` coerces a string head into the keyword the inlined
   router needs and routes through that core.

   COMPATIBILITY

   - Strictly additive. Internal pipelines and the 10x UI continue
     to use internal namespaces; nothing here changes existing
     behaviour.
   - The public surface leaks NO inlined-rf version slug (`v1v3v0`
     etc.). Once consumers migrate to this surface, they can
     delete any hard-coded version-path fallback (re-frame-pair's
     `inlined-rf-known-version-paths` vec is the target deletion).
   - Every public top-level form carries `^:export ^:experimental`
     and is gated by public_test.cljs's surface-presence and
     version-slug-leak checks. The `^:experimental` markers come
     off in one sweep when the spec's gate is met (see STABILITY).
   - `(version)` and `(capabilities)` exist for consumer
     branch-on-availability; both bump together with every public-
     surface contract revision.

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
   in test/ gate this in CI.

   NAMESPACE DISCIPLINE (for contributors)

   Every public top-level form here is part of the contract called
   out under COMPATIBILITY above. New entries must carry
   `^:export ^:experimental` and pick up coverage in
   public_test.cljs's surface-presence and version-slug-leak
   checks. `^:private` defs (e.g. `public->internal`) are exempt
   from the `^:export` contract — they're not on the goog.global
   path and public_export_metadata_test.clj filters them out by
   metadata.

   Side-effecting handler registrations live in
   `day8.re-frame-10x.public.events`, required below so registrations
   fire at namespace-load time; consumers must NOT reach into that
   namespace. Helpers used only here (e.g. `match->public-epoch`) are
   `defn-`. Default to behavioural testing through the public
   surface; reach for a private helper via `#'` from a test only when
   behavioural testing would obscure the contract under test (e.g.
   when an inner re-fire clobbers the very state being asserted on).
   Such reach-arounds must comment why."
  (:require
   ;; The two inlined-rf requires below are intentional, not cleanup
   ;; targets: 10x's events register against the inlined re-frame
   ;; (see MUTATION API in the ns docstring), so this ns must route
   ;; through it even though the docstring promises the version slug
   ;; never leaks into the public surface. Replacing them with the
   ;; user's re-frame.core would silently break every mutation event.
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.db   :as rf.db]
   ;; Side-effect-only require: registers the public mutation-API
   ;; handlers against the inlined router. See NAMESPACE DISCIPLINE.
   [day8.re-frame-10x.public.events]
   [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

;; ---------------------------------------------------------------------------
;; Versioning + feature detection
;; ---------------------------------------------------------------------------

(def ^:export ^:experimental api-version
  "Integer that bumps with each public-surface contract revision,
   including new stable event identifiers downstream tools may gate on.
   Consumers can branch on it via `(capabilities)` or read it directly
   via `goog.global.day8.re_frame_10x.public.api_version`."
  2)

(defn ^:export ^:experimental ^boolean loaded?
  "True when this namespace is loaded — i.e. when the public surface
   is available in the runtime. Stable feature-detection hook for
   downstream tooling. The presence of the var IS the contract;
   the body is `true` and consumers shouldn't read meaning into the
   return value beyond 'this fn ran'.

   Probed externally as `goog.global.day8.re_frame_10x.public.loaded_QMARK_`
   so consumers don't have to compile against this namespace."
  []
  true)

(defn ^:export ^:experimental version
  "Returns `{:api <int>}` describing the public-surface version the
   currently-loaded 10x build implements. Bumps with public contract
   revisions, including new stable event identifiers and read API
   shape or event-identifier semantic changes.
   Consumers branch on this when they want to support multiple 10x
   versions side-by-side."
  []
  {:api api-version})

(defn ^:export ^:experimental capabilities
  "Set of feature keywords this build supports. Reserved for
   future growth — today returns the baseline set. Consumers
   should treat unknown keywords as 'not supported'.

   Read API flags use a `:resource/action` shape — `:epochs/read`,
   `:epochs/reset-app-db`, `:traces/read`,
   `:settings/app-db-follows-events`. The
   `:events/...` family flags the mutation API: `:events/navigate`,
   `:events/reset`, `:events/replay`, `:events/reset-app-db` mark
   the corresponding event identifier constants, and
   `:events/dispatch!` marks the bridge fn that routes event
   vectors into 10x's inlined re-frame router.

   `:epochs/navigate` and `:events/navigate` are synonyms — the
   former predates the `:events/...` family and is retained for
   compatibility. New consumers should prefer the `:events/...`
   namespace when branching on the mutation surface, since it
   keeps reset / replay / dispatch! / navigate flags in one
   semantic family."
  []
  #{:public/v1
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

(def ^:export ^:experimental epochs
  "Vec of every retained epoch in 10x's ring buffer, in the order
   10x stored them (oldest first; `last` is newest). Each element is
   a public-epoch record (see `match->public-epoch`).

   Returns `[]` when 10x's app-db hasn't initialised yet — this lets
   consumers no-op gracefully on cold starts instead of having to
   probe `loaded?` ahead of every call."
  (let [uncached-matches (js-obj)
        cache            (volatile! [uncached-matches []])]
    (fn []
      (let [matches                       (some-> rf.db/app-db deref :epochs :matches)
            [cached-matches cached-epochs] @cache]
        (if (identical? matches cached-matches)
          cached-epochs
          (let [public-epochs (mapv match->public-epoch matches)]
            (vreset! cache [matches public-epochs])
            public-epochs))))))

(defn ^:export ^:experimental epoch-count
  "Number of retained epochs. Cheap — reads the `:match-ids` vec
   length without rebuilding the full coerced epoch maps. Suitable
   for poll-cadence callers (e.g. re-frame-pair's
   `watch-epochs.sh`)."
  []
  (count (some-> rf.db/app-db deref :epochs :match-ids)))

(defn ^:export ^:experimental latest-epoch-id
  "Id of the newest (most-recent) match in the buffer, or nil if
   empty. Cheap — reads `:match-ids`' last element. 10x stores
   epochs oldest-first, so the newest dispatch is at the tail."
  []
  (some-> rf.db/app-db deref :epochs :match-ids tools.coll/last-in-vec))

(defn ^:export ^:experimental selected-epoch-id
  "Id of the epoch the 10x UI is currently focused on, or nil before
   an epoch has been selected. On the live tail this is normally the
   newest retained id, so consumers should compare it with
   `latest-epoch-id` to detect whether 10x is following the tail.
   When the user navigates back through history, `selected-epoch-id`
   stays put while `latest-epoch-id` advances with new dispatches."
  []
  (some-> rf.db/app-db deref :epochs :selected-epoch-id))

(defn ^:export ^:experimental epoch-by-id
  "Public-epoch record for the given match id, or nil if unknown
   (id never existed, or aged out of the buffer)."
  [id]
  (some-> rf.db/app-db deref :epochs :matches-by-id (get id)
          match->public-epoch))

;; ---------------------------------------------------------------------------
;; Read API — traces (the underlying re-frame.trace stream)
;; ---------------------------------------------------------------------------

(defn ^:export ^:experimental all-traces
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

(defn ^:export ^:experimental ^boolean app-db-follows-events?
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
;; Mutation API — event identifiers + dispatch! bridge
;; ---------------------------------------------------------------------------

(def ^:export ^:experimental load-epoch
  "Public event identifier. Dispatch via `(dispatch! [load-epoch <id>])`
   to make 10x focus on the epoch with the given match id. When
   `app-db-follows-events?` is true (the default), the user's app-db
   resets to that epoch's `:app-db-after`.

   Value is the fully-qualified string `\"day8.re-frame-10x.public/load-epoch\"`
   — JS-constructable, so pure-JS callers via `goog.global` can
   either read this var or build the same literal."
  "day8.re-frame-10x.public/load-epoch")

(def ^:export ^:experimental most-recent-epoch
  "Public event identifier. Dispatch via `(dispatch! [most-recent-epoch])`
   to make 10x focus on the newest match (the 'live tail'). Useful
   after a programmatic load-epoch to return control to the user.

   When `app-db-follows-events?` is true, this is the canonical way
   to re-sync userland to the live tail after a programmatic
   load-epoch overwrote the user's app-db with a historical
   `:app-db-after` snapshot — it moves the cursor to the newest
   match and resets the user's app-db to that match's
   `:app-db-after`. No other public mutation event combines those
   two steps; without it, userland app-db keeps evolving from the
   historical starting point load-epoch left it in.

   Value is the fully-qualified string
   `\"day8.re-frame-10x.public/most-recent-epoch\"`."
  "day8.re-frame-10x.public/most-recent-epoch")

(def ^:export ^:experimental previous-epoch
  "Public event identifier. Dispatch via `(dispatch! [previous-epoch])`
   to step the 10x UI cursor one match backwards from the currently
   focused epoch. No-op when already at the oldest retained match.
   When no epoch is focused (the 'live tail'), steps to the
   second-newest retained match; no-op if fewer than two matches
   are retained. When `app-db-follows-events?` is true, the user's
   app-db resets to the new epoch's `:app-db-after`.

   Value is the fully-qualified string
   `\"day8.re-frame-10x.public/previous-epoch\"`."
  "day8.re-frame-10x.public/previous-epoch")

(def ^:export ^:experimental next-epoch
  "Public event identifier. Dispatch via `(dispatch! [next-epoch])` to
   step the 10x UI cursor one match forwards from the currently
   focused epoch. No-op when already at the newest retained match.
   When no epoch is focused, jumps to the live tail. When
   `app-db-follows-events?` is true, the user's app-db resets to the
   new epoch's `:app-db-after`.

   Value is the fully-qualified string
   `\"day8.re-frame-10x.public/next-epoch\"`."
  "day8.re-frame-10x.public/next-epoch")

(def ^:export ^:experimental reset-epochs
  "Public event identifier. Dispatch via `(dispatch! [reset-epochs])` to
   clear 10x's epoch buffer and reset re-frame.trace's id counter.
   Equivalent to clicking the 'reset' button in 10x's UI.

   Value is the fully-qualified string
   `\"day8.re-frame-10x.public/reset-epochs\"`."
  "day8.re-frame-10x.public/reset-epochs")

(def ^:export ^:experimental replay-epoch
  "Public event identifier. Dispatch via `(dispatch! [replay-epoch])` to
   replay the focused epoch's event against the app-db state captured
   BEFORE that event originally fired (the epoch's `:app-db-before`).
   Equivalent to time-travelling to the epoch and re-firing — the
   resulting userland app-db is the post-event state of that epoch,
   regardless of any subsequent dispatches. Idempotent: repeated
   replays of the same epoch produce the same post-event state.
   Equivalent to clicking 10x's 'replay' button.

   Value is the fully-qualified string
   `\"day8.re-frame-10x.public/replay-epoch\"`."
  "day8.re-frame-10x.public/replay-epoch")

(def ^:export ^:experimental ^:const reset-app-db-event
  "Public event identifier. Dispatch via
   `(dispatch! [reset-app-db-event <id>])` to reset the user's app-db
   to the `:app-db-after` snapshot for the epoch with the given match
   id, without moving 10x's selected epoch cursor. No-op when
   `app-db-follows-events?` is false.

   Lower-level than `load-epoch`: `load-epoch` moves the 10x cursor
   and then resets the user's app-db when following is enabled; this
   event is only the app-db-reset half.

   Value is the fully-qualified string
   `\"day8.re-frame-10x.public/reset-app-db\"`."
  "day8.re-frame-10x.public/reset-app-db")

(def ^:private public->internal
  "Translation table from public mutation event identifiers (kw form
   of the exported strings) to the internal inlined-rf event keywords
   they fan out to. `dispatch!` consults this on every call; entries
   not in the map (e.g. `::previous-epoch`, whose load-bearing cond
   logic lives in a public.events forwarder) pass through unchanged
   so the registered forwarder can still pick them up.

   This is the contract boundary: public string identifiers are the
   durable LHS, internal kws are the volatile RHS. A future internal
   rename touches only this map — the public string consts above stay
   put."
  {:day8.re-frame-10x.public/load-epoch        :day8.re-frame-10x.navigation.epochs.events/load
   :day8.re-frame-10x.public/most-recent-epoch :day8.re-frame-10x.navigation.epochs.events/most-recent
   :day8.re-frame-10x.public/next-epoch        :day8.re-frame-10x.navigation.epochs.events/next
   :day8.re-frame-10x.public/reset-epochs      :day8.re-frame-10x.navigation.epochs.events/reset
   :day8.re-frame-10x.public/replay-epoch      :day8.re-frame-10x.navigation.epochs.events/replay
   :day8.re-frame-10x.public/reset-app-db      :day8.re-frame-10x.navigation.epochs.events/reset-current-epoch-app-db})

(defn ^:export ^:experimental dispatch!
  "Mutation-API bridge: routes `event-vec` (e.g.
   `[load-epoch 42]`) through 10x's *inlined* re-frame router.
   Necessary because 10x events register against the inlined
   `day8.re-frame-10x.inlined-deps.re-frame.v1v3v0` re-frame core
   — a consumer's plain `(re-frame.core/dispatch ...)` would never
   reach them. Use the string identifiers exported from this
   namespace as the first element of `event-vec`; the strings are
   the durable contract.

   Coerces JS-array arguments to CLJS vectors, so pure-JS callers
   via `goog.global.day8.re_frame_10x.public.dispatch_BANG_(['evt', arg])`
   work — the inlined router validates events with `(vector? ...)`,
   which JS arrays fail.

   Coerces a string head to a keyword before forwarding, since
   re-frame's handler-lookup keys are keywords. Pure-JS callers
   that don't have access to `cljs.core.keyword` can therefore pass
   the exported string identifiers directly.

   Translates public mutation kws to their internal counterparts via
   `public->internal` so the public surface and the internal handlers
   need not share names — the public strings are durable, the internal
   kws can rename freely. Heads not in the map pass through unchanged
   so direct dispatches (e.g. the `::previous-epoch` forwarder, or
   ad-hoc internal kws routed through this bridge by tooling) still
   resolve."
  [event-vec]
  (let [v          (if (vector? event-vec) event-vec (vec (js->clj event-vec)))
        h          (first v)
        kw         (if (string? h) (keyword h) h)
        translated (get public->internal kw)]
    (rf/dispatch
     (cond
       (some? translated) (assoc v 0 translated)
       (string? h)        (assoc v 0 kw)
       :else              v))))

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
;; inlined-rf-version walking — defeating the purpose of this
;; public namespace.
;;
;; Guarded on `js/goog.global` existence so this is a no-op in any
;; environment that doesn't have the Closure global object set up
;; (e.g. CLJS REPLs that load namespaces through different shims).
(when-let [g (and (exists? js/goog) (.-global js/goog))]
  (let [rf10x   (some-> g (aget "day8") (aget "re_frame_10x"))
        suffixed (some-> rf10x (aget "public$"))]
    (when (and rf10x suffixed)
      (aset rf10x "public" suffixed))))
