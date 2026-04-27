(ns day8.re-frame-10x.public-export-metadata-test
  "Regression test for the ^:export metadata contract on the public surface.

   Static structural check: scans day8.re-frame-10x.public's source and
   asserts every top-level public defining form this scanner recognizes
   carries `^:export` in the metadata segment between its head and its name.

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
   public.cljs body without an `*alias-map*` binding. A regex scan over
   top-level public defining heads avoids the alias-resolution complexity
   and is sufficient for the metadata-presence check this test exists for.
   The scan is intentionally anchored at column zero: indented nested forms
   are ignored, but a column-zero form inside `(comment ...)` would still be
   treated as top-level by this text scanner."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def ^:private public-source
  "src/day8/re_frame_10x/public.cljs")

(def ^:private head-pattern
  "Match a top-level public defining head line, rejecting private forms
   such as defn- and def-.

   Capture group 1: defining head. Longer `def...` forms are listed
   before `def` so regex alternation cannot shadow them.
   Capture group 2: the metadata-and-whitespace segment between the
   head and the name. Includes type hints (`^boolean`), keyword
   metadata (`^:const`, `^:export`), and whitespace.
   Capture group 3: the var name.

   Anchored at line start so it doesn't match `defn`/`def` text that
   appears inside docstrings or nested forms."
  #"(?m)^\((defprotocol|defrecord|defmacro|defmulti|defonce|deftype|defn|def)(?!-)(\s+(?:\^[\w?:]+\s+)*)([\w!?*+<>=&%-]+)")

(defn- public-defs
  "Find every top-level public defining form in `source`.
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

(deftest public-defs-recognises-public-defining-forms
  (testing "public-defs scans the public def-form family at column zero"
    (let [source (str "(def ^:export exported-def 1)\n"
                      "(defonce ^:export exported-once 1)\n"
                      "(defn ^:export exported-fn [] nil)\n"
                      "(defmacro ^:export exported-macro [] nil)\n"
                      "(defmulti ^:export exported-multi identity)\n"
                      "(defprotocol ^:export ExportedProtocol)\n"
                      "(defrecord ^:export ExportedRecord [])\n"
                      "(deftype ^:export ExportedType [])\n"
                      "(defmulti unexported-multi identity)\n"
                      "(defn- private-fn [] nil)\n"
                      "(def- private-def nil)\n"
                      "  (defn nested-at-column-two [] nil)\n")
          forms  (public-defs source)]
      (is (= [["def" "exported-def" 1]
              ["defonce" "exported-once" 2]
              ["defn" "exported-fn" 3]
              ["defmacro" "exported-macro" 4]
              ["defmulti" "exported-multi" 5]
              ["defprotocol" "ExportedProtocol" 6]
              ["defrecord" "ExportedRecord" 7]
              ["deftype" "ExportedType" 8]
              ["defmulti" "unexported-multi" 9]]
             (mapv (juxt :head :name :line) forms)))
      (is (= [["unexported-multi" 9]]
             (mapv (juxt :name :line) (remove exported? forms)))))))

(deftest every-public-def-is-exported
  (testing
   (str "Every top-level public defining form in " public-source " carries ^:export — "
        "without it the Closure :advanced compiler mangles names and "
        "breaks the documented goog.global.day8.re_frame_10x.public.* "
        "lookup contract.")
    (let [source     (slurp (io/file public-source))
          forms      (public-defs source)
          unexported (remove exported? forms)]
      (is (seq forms)
          (str public-source " parsed but found no public defining forms "
               "— the test is reading the wrong file or the source was "
               "restructured."))
      (is (empty? unexported)
          (str "These public-surface vars are missing ^:export and will "
               "be mangled by :advanced compilation: "
               (pr-str (mapv (juxt :name :line) unexported)))))))
