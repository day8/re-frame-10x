(ns day8.re-frame-10x.preload-loads-public-test
  "Regression test for the preload → public-ns require chain.

   Static structural check: discovers every preload source under
   src/day8/re_frame_10x/preload (plus the parent preload.cljs) and
   asserts that `day8.re-frame-10x.public` appears in its `(:require ...)`
   clause. Also asserts the public.cljs require target itself resolves
   on disk, so deletion of the file is caught here rather than as an
   opaque shadow-cljs compile failure.

   Discovery is dynamic (file-seq) rather than a hardcoded list so that
   any future preload variant — preload/react_19.cljs, preload/react_native.cljs,
   etc. — is automatically gated by this test the moment it lands.

   Why static rather than runtime: requiring a preload at test time
   triggers DOM-touching side effects (`patch!`, `render`, `dispatch-sync`)
   which need a browser/JS runtime. The bug guarded against here is purely
   a require-graph reachability bug — a static read of the source is the
   right granularity, runs in the JVM, and catches any future drop of the
   public require from a preload."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(defn- read-ns-form
  "Read the first form of a Clojure(Script) source file — by convention,
   the (ns ...) form."
  [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (read r)))

(defn- require-clause
  "Return the body of the `:require` clause from an `ns` form, or nil."
  [ns-form]
  (some (fn [x]
          (when (and (sequential? x) (= :require (first x)))
            (rest x)))
        ns-form))

(defn- requires-public?
  "True iff `require-body` mentions `day8.re-frame-10x.public` as a
   required ns (with or without options like `:as`)."
  [require-body]
  (boolean
   (some (fn [spec]
           (let [sym (cond
                       (symbol? spec) spec
                       (sequential? spec) (first spec))]
             (= 'day8.re-frame-10x.public sym)))
         require-body)))

(def ^:private preload-root
  (io/file "src/day8/re_frame_10x/preload.cljs"))

(def ^:private preload-variants-dir
  (io/file "src/day8/re_frame_10x/preload"))

(def ^:private public-source
  (io/file "src/day8/re_frame_10x/public.cljs"))

(defn- discover-preload-sources
  "Every `.cljs` preload entry-point: the parent preload.cljs plus any
   `.cljs` file under preload-variants-dir. Sorted for stable test order."
  []
  (->> (cons preload-root
             (when (.isDirectory preload-variants-dir)
               (file-seq preload-variants-dir)))
       (filter (fn [^java.io.File f]
                 (and (.isFile f)
                      (.endsWith (.getName f) ".cljs"))))
       (map (fn [^java.io.File f] (.getPath f)))
       sort))

(deftest public-source-resolves
  (testing
   (str "Require target " (.getPath public-source) " exists on disk")
    (is (.exists public-source)
        (str (.getPath public-source) " is missing — preload entry-points "
             "require day8.re-frame-10x.public, so deletion of the file "
             "while the require survives breaks the public-surface "
             "contract. shadow-cljs would fail at compile time; this "
             "JVM-test catches the regression before a build runs."))))

(deftest preloads-require-public-ns
  (testing "Every preload entry-point requires day8.re-frame-10x.public"
    (let [paths (discover-preload-sources)]
      (is (seq paths)
          (str "no preload sources discovered under "
               (.getPath preload-variants-dir) " or at "
               (.getPath preload-root)
               " — running from the wrong working directory, or the "
               "preload sources have moved."))
      (doseq [path paths]
        (testing path
          (let [ns-form  (read-ns-form path)
                required (require-clause ns-form)]
            (is (some? required)
                (str path " has no :require clause"))
            (is (requires-public? required)
                (str path
                     " does not require day8.re-frame-10x.public — "
                     "consumers using only the preload will not see "
                     "the public surface, and downstream tooling that "
                     "probes goog.global.day8.re_frame_10x.public.* "
                     "will fall back to brittle internal-path walking."))))))))
