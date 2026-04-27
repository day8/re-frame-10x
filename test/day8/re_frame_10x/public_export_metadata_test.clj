(ns day8.re-frame-10x.public-export-metadata-test
  "Regression test for the ^:export metadata contract on the public surface.

   Static structural check: scans day8.re-frame-10x.public's source and
   asserts every top-level `def`/`defn` form (other than `defn-`) carries
   `^:export` in the metadata segment between its head and its name.

   Why this matters: the public ns is reached by downstream tooling
   (re-frame-pair) via `goog.global.day8.re_frame_10x.public.<name>`.
   Without `^:export`, the Closure :advanced compiler mangles or inlines
   the symbol, the goog.global lookup returns undefined in production,
   and consumers silently fall back to brittle inlined-rf-version-paths
   walking. :debug builds mask the bug because they don't mangle.

   Why static rather than runtime: a true contract check would compile
   public.cljs in :advanced mode and probe the resulting JS. That needs
   shadow-cljs / lein-cljsbuild infrastructure and is owned by the
   operator's CI. This test catches the much commoner regression — a
   new public defn added without ^:export — at JVM-test speed.

   Why text-scanned rather than read: clojure.core/read tripped over
   ns-aliased autoresolved keywords like `::nav.events/load` in the
   public.cljs body without an `*alias-map*` binding. A regex scan
   over `(def...)` / `(defn...)` heads avoids the alias-resolution
   complexity and is sufficient for the metadata-presence check this
   test exists for."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def ^:private public-source
  "src/day8/re_frame_10x/public.cljs")

(def ^:private head-pattern
  "Match a top-level (defn ...) or (def ...) head line, rejecting
   private forms (defn-, def-).

   Capture group 1: head (`defn` or `def`) — `defn` listed first so
   regex alternation matches the longer keyword first.
   Capture group 2: the metadata-and-whitespace segment between the
   head and the name. Includes type hints (`^boolean`), keyword
   metadata (`^:const`, `^:export`), and whitespace.
   Capture group 3: the var name.

   Anchored at line start so it doesn't match `defn`/`def` text that
   appears inside docstrings or nested forms."
  #"(?m)^\((defn|def)(?!-)(\s+(?:\^[\w?:]+\s+)*)([\w!?*+<>=&%-]+)")

(defn- public-defs
  "Find every top-level public def/defn in `source` (excluding `defn-`/`def-`).
   Returns a vec of `{:head :meta :name :line}` maps."
  [source]
  (let [matcher (re-matcher head-pattern source)]
    (loop [acc []]
      (if (.find matcher)
        (let [head     (.group matcher 1)
              metadata (.group matcher 2)
              var-name (.group matcher 3)
              line     (count (re-seq #"\n" (subs source 0 (.start matcher))))]
          (recur (conj acc {:head head
                            :meta metadata
                            :name var-name
                            :line (inc line)})))
        acc))))

(defn- exported?
  "True iff `^:export` appears in the metadata segment between the
   form head and the var name."
  [{:keys [meta]}]
  (boolean (re-find #"\^:export\b" meta)))

(deftest every-public-def-is-exported
  (testing
   (str "Every top-level def/defn in " public-source " carries ^:export — "
        "without it the Closure :advanced compiler mangles names and "
        "breaks the documented goog.global.day8.re_frame_10x.public.* "
        "lookup contract.")
    (let [source     (slurp (io/file public-source))
          forms      (public-defs source)
          unexported (remove exported? forms)]
      (is (seq forms)
          (str public-source " parsed but found no public def/defn forms "
               "— the test is reading the wrong file or the source was "
               "restructured."))
      (is (empty? unexported)
          (str "These public-surface vars are missing ^:export and will "
               "be mangled by :advanced compilation: "
               (pr-str (mapv (juxt :name :line) unexported)))))))
