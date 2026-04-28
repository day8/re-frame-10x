(ns day8.re-frame-10x.public-export-metadata-test
  "Regression test for the ^:export metadata contract on the public surface.

   Static structural check: reads day8.re-frame-10x.public's source and
   asserts every top-level public defining form carries export metadata
   on its var.

   Why this matters: the public ns is reached by downstream tooling
   (re-frame-pair) via `goog.global.day8.re_frame_10x.public.<name>`.
   Without `^:export`, the Closure :advanced compiler mangles or inlines
   the symbol, the goog.global lookup returns undefined in production,
   and consumers silently fall back to brittle inlined-rf-version-paths
   walking. :debug builds mask the bug because they don't mangle.

   Why static rather than runtime: a true contract check would compile
   public.cljs in :advanced mode and probe the resulting JS. That needs
   shadow-cljs / lein-cljsbuild infrastructure and is owned by the
   operator's CI. This test catches the much commoner regression - a
   new public defn added without ^:export - at JVM-test speed."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [day8.re-frame-10x.public-export-metadata :as export-metadata]))

(deftest public-defs-recognises-public-defining-forms
  (testing "public-defs reads top-level public def-form families"
    (let [source (str "(def ^:export exported-def 1)\n"
                      "(def ^{:export true} exported-map-meta 1)\n"
                      "(defonce ^:export exported-once 1)\n"
                      "(defn ^:export exported-fn [] nil)\n"
                      "(defn exported-attr-map {:export true} [] nil)\n"
                      "(defn ^:export app-db-follows-events?! [] nil)\n"
                      "(defmacro ^:export exported-macro [] nil)\n"
                      "(defmulti ^:export exported-multi identity)\n"
                      "(defprotocol ^:export ExportedProtocol)\n"
                      "(defrecord ^:export ExportedRecord [])\n"
                      "(deftype ^:export ExportedType [])\n"
                      "(defmulti unexported-multi identity)\n"
                      "(def ^:private private-via-meta nil)\n"
                      "(defn- private-fn [] nil)\n"
                      "(def- private-def nil)\n"
                      "(comment\n"
                      "(defn column-zero-inside-comment [] nil)\n"
                      ")\n")
          forms  (export-metadata/public-defs source)]
      (is (= [["def" "exported-def" 1]
              ["def" "exported-map-meta" 2]
              ["defonce" "exported-once" 3]
              ["defn" "exported-fn" 4]
              ["defn" "exported-attr-map" 5]
              ["defn" "app-db-follows-events?!" 6]
              ["defmacro" "exported-macro" 7]
              ["defmulti" "exported-multi" 8]
              ["defprotocol" "ExportedProtocol" 9]
              ["defrecord" "ExportedRecord" 10]
              ["deftype" "ExportedType" 11]
              ["defmulti" "unexported-multi" 12]]
             (mapv (juxt :head :name :line) forms))
          "^:private defs are filtered out — they're not on the goog.global path")
      (is (= [["unexported-multi" 12]]
             (mapv (juxt :name :line)
                   (remove export-metadata/exported? forms)))))))

(deftest every-public-def-is-exported
  (testing
   (str "Every top-level public defining form in "
        export-metadata/public-source
        " carries ^:export - without it the Closure :advanced compiler "
        "mangles names and breaks the documented "
        "goog.global.day8.re_frame_10x.public.* lookup contract.")
    (let [source     (slurp (io/file export-metadata/public-source))
          forms      (export-metadata/public-defs source)
          unexported (remove export-metadata/exported? forms)]
      (is (seq forms)
          (str export-metadata/public-source
               " parsed but found no public defining forms - the test "
               "is reading the wrong file or the source was restructured."))
      (is (empty? unexported)
          (str "These public-surface vars are missing ^:export and will "
               "be mangled by :advanced compilation: "
               (pr-str (mapv (juxt :name :line) unexported)))))))
