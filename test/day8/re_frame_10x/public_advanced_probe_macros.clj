(ns day8.re-frame-10x.public-advanced-probe-macros
  "Compile-time macro support for the :advanced-build probe of the
   public surface. The macro reads src/day8/re_frame_10x/public.cljs at
   probe-compile time and emits a literal vec of the JS-side (munged)
   names of every ^:export var.

   Why a macro and not a hardcoded list: the probe must enumerate the
   contract that public.cljs declares right now, not a snapshot of it
   from when the probe was written. A new ^:export added to public.cljs
   automatically grows the expected-exports vec, so the probe catches
   the case where the source has ^:export but the emitted JS lacks the
   symbol on goog.global without anyone having to remember to update a
   second list.

   Source parsing is shared with public_export_metadata_test.clj so the
   metadata-presence and advanced-emit checks enumerate the same surface."
  (:require [clojure.java.io :as io]
            [day8.re-frame-10x.public-export-metadata :as export-metadata]))

(def ^:private munge-table
  "Subset of the cljs.compiler munge table covering the punctuation
   that appears in public.cljs and is realistic for future additions.
   Order matters only in that the source character must be unambiguous
   under str/replace."
  {\? "_QMARK_"
   \! "_BANG_"
   \* "_STAR_"
   \+ "_PLUS_"
   \< "_LT_"
   \> "_GT_"
   \= "_EQ_"
   \& "_AMPERSAND_"
   \% "_PERCENT_"
   \- "_"})

(defn- munge-name
  "Mirror the ClojureScript -> JS name munging used by goog.exportSymbol
   for a single var name (NOT a namespace - namespaces preserve `.` as
   object-path separators, which doesn't apply here)."
  [n]
  (apply str
         (mapcat (fn [c] (or (munge-table c) (str c))) n)))

(defn exported-names
  "Return a vec of munged JS-side names for every ^:export var in source."
  [source]
  (->> (export-metadata/public-defs source)
       (filter export-metadata/exported?)
       (mapv (comp munge-name :name))))

(defmacro public-export-names
  "Read src/day8/re_frame_10x/public.cljs at compile time and emit a
   literal vector of the JS-side (munged) names of every ^:export var.
   Path is resolved relative to the shadow-cljs process cwd, which is
   the project root in both local dev and CI."
  []
  (vec (exported-names
        (slurp (io/file export-metadata/public-source)))))
