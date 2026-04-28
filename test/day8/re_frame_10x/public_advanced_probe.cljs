(ns day8.re-frame-10x.public-advanced-probe
  "Runtime-emit verification for the ^:export contract on the public
   surface. Compiled as a :node-script with :advanced optimizations and
   executed via `node out/public-advanced.js`. Exits 0 when every
   ^:export var declared in public.cljs resolves to a non-undefined
   value at goog.global.day8.re_frame_10x.public.<munged-name>; exits 1
   on the first failure mode encountered.

   The companion JVM-side test public_export_metadata_test.clj catches
   the source-side regression — a new public defn added without
   ^:export. This probe catches the EMIT-side regression: a correctly
   tagged var whose symbol nevertheless ends up missing from
   goog.global after :advanced compilation. Reasons that could happen
   include an :externs collision, a Closure compiler upgrade with
   different export semantics, an unintended :pseudo-names interaction,
   or a build-config change that strips the exportSymbol calls.

   The expected-export list comes from the public-export-names macro,
   which regex-scans the live public.cljs source — so adding a new
   ^:export var grows the probe automatically.

   Lives under test/ rather than src/ so it stays out of the
   library jar — only ships into the throwaway out/ build."
  (:require-macros
   [day8.re-frame-10x.public-advanced-probe-macros :refer [public-export-names]])
  (:require
   [day8.re-frame-10x.public]))

(def ^:private expected-exports
  (public-export-names))

(defn- fail!
  "Print to stderr and exit non-zero. Stderr keeps the failure message
   visible in CI logs even when stdout is captured/buffered."
  [msg]
  (.error js/console msg)
  (.exit js/process 1))

(defn -main [& _]
  (when (empty? expected-exports)
    (fail! (str "FAIL: public-export-names macro returned no expected exports."
                " The macro reads src/day8/re_frame_10x/public.cljs;"
                " either the file moved or its top-level def-form layout"
                " no longer matches the scanner. Check"
                " day8.re-frame-10x.public-advanced-probe-macros.")))
  (let [ns-obj (some-> js/goog.global
                       (aget "day8")
                       (aget "re_frame_10x")
                       (aget "public"))]
    (when (nil? ns-obj)
      (fail! (str "FAIL: goog.global.day8.re_frame_10x.public is absent"
                  " after :advanced compile. Either ^:export emit broke or"
                  " the build target stripped the goog.exportSymbol calls.")))
    (let [missing (filterv #(undefined? (aget ns-obj %)) expected-exports)]
      (when (seq missing)
        (fail! (str "FAIL: " (count missing) " of " (count expected-exports)
                    " expected exports missing from"
                    " goog.global.day8.re_frame_10x.public after :advanced"
                    " compile: " (pr-str missing)))))
    (println (str "OK: all " (count expected-exports)
                  " ^:export vars resolve on"
                  " goog.global.day8.re_frame_10x.public after :advanced compile"))))
