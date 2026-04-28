# Consumer Integration

A walkthrough for downstream tooling — `re-frame-pair`, custom devtools,
performance dashboards — that wants to read epoch and trace data out of
re-frame-10x without compiling against it.

The contract this document describes lives in
[`day8.re-frame-10x.public`](../src/day8/re_frame_10x/public.cljs). That
namespace is the stable surface; everything else under `day8.re-frame-10x.*`
is internal and may change between releases.

## Why probe lazily

Tools like `re-frame-pair` ship as `.cljs` scripts that load alongside the
user's app, but they do not list re-frame-10x as a dependency — 10x is a
dev-time tool that may or may not be preloaded on any given page. The
consumer therefore cannot `:require` it.

Instead, a consumer probes for 10x at runtime by reading
[`goog.global`](https://google.github.io/closure-library/api/goog.html#global)
properties. Once the probe succeeds, the consumer holds a JS object whose
methods are exactly the `^:export`-tagged vars from the public namespace.

## Feature detection

`day8.re-frame-10x.public/loaded?` is the durable presence hook. The var
*existing* on `goog.global` is the contract — the body returns `true` for
ergonomic calling, but consumers should not read meaning into the return
value beyond "this fn ran without throwing".

```clojure
(defn ten-x-public
  "JS object for day8.re-frame-10x.public if loaded, else nil."
  []
  (when-let [g (some-> js/goog .-global)]
    (let [pub (some-> g (aget "day8") (aget "re_frame_10x") (aget "public"))]
      (when (and pub (aget pub "loaded_QMARK_"))
        pub))))
```

`loaded?` is the right hook because it is the cheapest var on the surface
and the one most likely to remain present across versions. Probing for any
specific read or mutation API risks false negatives if that fn is renamed
or split.

## Name munging cheat-sheet

The Closure compiler munges Clojure-y identifiers when emitting JS. The
rules that matter to a probing consumer:

| Clojure name              | JS name on `goog.global`      |
| ------------------------- | ----------------------------- |
| `loaded?`                 | `loaded_QMARK_`               |
| `dispatch!`               | `dispatch_BANG_`              |
| `app-db-follows-events?`  | `app_db_follows_events_QMARK_` |
| `epoch-by-id`             | `epoch_by_id`                 |
| `latest-epoch-id`         | `latest_epoch_id`             |

Hyphens become underscores; trailing `?` becomes `_QMARK_`; trailing `!`
becomes `_BANG_`. The namespace `day8.re-frame-10x.public` mounts at
`goog.global.day8.re_frame_10x.public`.

### One subtlety: the `public$` alias

`public` is a JS reserved word, so the Closure `:advanced` compiler emits
its `goog.exportSymbol` calls under `day8.re_frame_10x.public$.<name>` —
note the trailing `$`. The public namespace itself runs a top-of-file
mirror that aliases `public$` back onto `public` on the same parent at
namespace-load time, so the documented un-suffixed path
(`goog.global.day8.re_frame_10x.public.<name>`) works in both `:none` and
`:advanced` builds.

A consumer that probes the suffixed path directly will work in `:advanced`
builds but break in `:none`. Always walk the un-suffixed path.

## Version branching

The public namespace exports an `api-version` integer (currently `2`) and a
`(version)` fn that returns `{:api <int>}`. The integer bumps on public
contract revisions, including new stable event identifiers and changes to
read-API shape or event-identifier semantics. Consumers that want to support
multiple 10x versions side-by-side branch on this.

```clojure
(defn ten-x-api-version
  "Integer api-version of the loaded 10x build, or nil if 10x missing."
  []
  (when-let [pub (ten-x-public)]
    (some-> ((aget pub "version")) (aget "api"))))
```

Treat any value `>= 1` as "supports the v1 contract"; treat `nil` as "fall
back to legacy probing". API version `2` stays backwards-compatible at the
v1 surface and adds the low-level `reset-app-db-event` primitive.

## Capabilities

`(capabilities)` returns a set of feature keywords describing what this
build supports. Today the set is:

```clojure
#{:public/v1
  :public/v2
  :epochs/read                       ;; (epochs), (epoch-count), (latest-epoch-id),
                                     ;; (selected-epoch-id), (epoch-by-id)
  :epochs/navigate                   ;; load-epoch / most-recent-epoch / previous-epoch /
                                     ;; next-epoch event identifiers (synonym of :events/navigate)
  :epochs/reset-app-db               ;; reset app-db to one epoch without moving the 10x cursor
  :traces/read                       ;; (all-traces)
  :settings/app-db-follows-events    ;; (app-db-follows-events?)
  :events/navigate                   ;; explicit flag for the four navigation event identifiers
  :events/reset                      ;; reset-epochs identifier
  :events/replay                     ;; replay-epoch identifier
  :events/reset-app-db               ;; reset-app-db-event identifier
  :events/dispatch!}                 ;; dispatch! bridge fn
```

Consumers should treat unknown keywords as "not supported" rather than as
errors — the set will grow. New consumers should prefer the `:events/...`
namespace when branching on the mutation surface; `:epochs/navigate`
predates `:events/navigate` and is retained as a synonym for compatibility.

## Read API at a glance

All functions take no arguments unless noted; all return seq-able CLJS
values when called from CLJS-land, and JS arrays / objects when called via
`goog.global`.

| Fn                       | Returns                                          |
| ------------------------ | ------------------------------------------------ |
| `(epochs)`               | Vec of every retained epoch, oldest-first.       |
| `(epoch-count)`          | Integer; cheap (reads `:match-ids` length).      |
| `(latest-epoch-id)`      | Id of newest match, or nil.                      |
| `(selected-epoch-id)`    | Id of UI-focused epoch, or nil before selection. |
| `(epoch-by-id id)`       | Single public-epoch record, or nil.              |
| `(all-traces)`           | Full retained trace stream as a vec.             |
| `(app-db-follows-events?)` | Boolean for the eponymous setting.             |

A public-epoch record carries `{:id :match-info :sub-state-raw :timings}`.
Public keys (`:sub-state-raw`, `:timings`) intentionally differ from
internal ones (`:sub-state`, `:timing`) so the public shape can evolve
independently.

On the live tail, `selected-epoch-id` normally equals `latest-epoch-id`.
Consumers that need to know whether 10x is following the newest retained
epoch should compare those two values rather than treating `nil` as the
only live-tail signal.

## Mutation API

Mutations route through the inlined re-frame router. A consumer's plain
`(re-frame.core/dispatch ...)` would not reach 10x's handlers because 10x
events register against the *inlined*
`day8.re-frame-10x.inlined-deps.re-frame.v1v3v0` core, not the user's
re-frame.

The pattern is:

```clojure
(let [pub      (ten-x-public)
      dispatch (aget pub "dispatch_BANG_")
      load-id  (aget pub "load_epoch")]
  (dispatch #js [load-id target-id]))
```

`dispatch!` accepts either a CLJS vector or a plain JS array (it coerces
internally) and keywordises a string head before forwarding to the inlined
router, so pure-JS callers via the JS console can read the exported
identifier var directly...

```js
day8.re_frame_10x.public.dispatch_BANG_(
  [day8.re_frame_10x.public.load_epoch, 42]);
```

...or build the same fully-qualified string literal from scratch:

```js
day8.re_frame_10x.public.dispatch_BANG_(
  ['day8.re-frame-10x.public/load-epoch', 42]);
```

The exported event identifier constants are the durable contract — each
holds a fully-qualified string that `dispatch!` keywordises internally:

| Constant           | Value                                              | Effect when dispatched                                           |
| ------------------ | -------------------------------------------------- | ---------------------------------------------------------------- |
| `load-epoch`       | `"day8.re-frame-10x.public/load-epoch"`            | Focus 10x on the given match id; takes one arg.                  |
| `most-recent-epoch`| `"day8.re-frame-10x.public/most-recent-epoch"`     | Focus on the live tail (newest match).                           |
| `previous-epoch`   | `"day8.re-frame-10x.public/previous-epoch"`        | Step the cursor one match backwards. No-op at oldest.            |
| `next-epoch`       | `"day8.re-frame-10x.public/next-epoch"`            | Step the cursor one match forwards. Jumps to live tail if unset. |
| `reset-epochs`     | `"day8.re-frame-10x.public/reset-epochs"`          | Clear the epoch buffer; reset trace id counter.                  |
| `replay-epoch`     | `"day8.re-frame-10x.public/replay-epoch"`          | Re-fire the focused epoch's event from its `:app-db-before`.     |
| `reset-app-db-event` | `"day8.re-frame-10x.public/reset-app-db"`        | Reset userland app-db to one epoch's `:app-db-after`; takes one arg and does not move the 10x cursor. |

When `(app-db-follows-events?)` is true (the default), the four navigation
events also reset the user's app-db to the focused epoch's `:app-db-after`
snapshot. When it is false, navigation events update only 10x's UI cursor.
Tools that drive 10x programmatically should branch on this flag if their
callers care about userland mutation.

`reset-app-db-event` uses the same `app-db-follows-events?` guard, but it is
not a navigation event: it resets userland app-db for the supplied epoch id
without changing 10x's selected epoch.

## Worked example: feature-detect + legacy fallback

Putting the pieces together, a downstream tool that wants to read the
newest epoch id (with a fallback for older 10x JARs that pre-date the
public surface) looks like:

```clojure
(def ^:private inlined-rf-known-version-paths
  ;; Best-known 10x-inlined re-frame version slugs. Used when the
  ;; public namespace isn't loaded — see ten-x-public for the
  ;; preferred path. Once every supported 10x carries public, this
  ;; vec can be deleted entirely.
  ["v1v3v0"])

(defn- aget-path [obj path]
  (reduce (fn [acc k] (when acc (try (aget acc k) (catch :default _ nil))))
          obj path))

(defn- ten-x-public
  "JS object for day8.re-frame-10x.public if loaded, else nil."
  []
  (when-let [g (some-> js/goog .-global)]
    (let [pub (aget-path g ["day8" "re_frame_10x" "public"])]
      (when (and pub (aget pub "loaded_QMARK_"))
        pub))))

(defn- ten-x-app-db-via-version-walk
  "Legacy fallback: walk the inlined-rf version slugs looking for
   re_frame.db.app_db. Drop this once the consumer floor is a 10x
   release that ships public."
  []
  (when-let [g (some-> js/goog .-global)]
    (let [base (aget-path g ["day8" "re_frame_10x" "inlined_deps" "re_frame"])]
      (some (fn [ver] (aget-path base [ver "re_frame" "db" "app_db"]))
            inlined-rf-known-version-paths))))

(defn latest-epoch-id
  "Newest retained match id from 10x, or nil."
  []
  (if-let [pub (ten-x-public)]
    ((aget pub "latest_epoch_id"))
    (some-> (ten-x-app-db-via-version-walk) deref :epochs :match-ids last)))
```

The public-surface branch reads exactly one cell. The fallback branch
walks an inlined-rf version slug and dereferences a Reagent ratom — much
more brittle, since the slug bumps every time 10x rev's its inlined
re-frame.

`re-frame-pair` ships this exact two-branch pattern in
[`scripts/re_frame_pair/runtime.cljs`](https://github.com/day8/re-frame-pair/blob/master/scripts/re_frame_pair/runtime.cljs)
and is the reference consumer.

## What downstream tools should plan to drop

The premise of the public surface is that consumers stop walking inlined-rf
version slugs entirely. Once a downstream tool's supported-10x floor is a
release that ships `day8.re-frame-10x.public`, it can delete:

- the `inlined-rf-known-version-paths` vec (or any equivalent table of
  slugs);
- any version-walk fallback that derefs `re_frame.db.app_db` directly;
- any duplicated knowledge of internal navigation event keywords like
  `:day8.re-frame-10x.navigation.epochs.events/load`.

Until then the legacy path is the bridge for older 10x JARs. The
`(version)` and `(capabilities)` probes mean a consumer never has to gate
on a specific 10x version string — branch on the integer or the
keyword-set instead.
