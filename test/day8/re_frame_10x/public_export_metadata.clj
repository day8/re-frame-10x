(ns day8.re-frame-10x.public-export-metadata
  (:require
   [clojure.tools.reader :as reader]
   [clojure.tools.reader.reader-types :as reader-types]))

(def public-source
  "src/day8/re_frame_10x/public.cljs")

(def ^:private alias-map
  '{nav.events day8.re-frame-10x.navigation.epochs.events})

(def ^:private public-defining-heads
  '#{def defmacro defmulti defn defonce defprotocol defrecord deftype})

(def ^:private post-name-metadata-heads
  '#{defmacro defmulti defn})

(defn- read-forms
  [source]
  (let [rdr (reader-types/indexing-push-back-reader source)
        eof (Object.)]
    (binding [reader/*alias-map* alias-map]
      (loop [forms []]
        (let [form (reader/read {:eof eof
                                 :features #{:cljs}
                                 :read-cond :allow}
                                rdr)]
          (if (identical? eof form)
            forms
            (recur (conj forms form))))))))

(defn- post-name-metadata
  [head form]
  (when (contains? post-name-metadata-heads head)
    (let [after-name (nnext form)
          after-doc  (if (string? (first after-name))
                       (next after-name)
                       after-name)]
      (when (map? (first after-doc))
        (first after-doc)))))

(defn- var-metadata
  [head form var-sym]
  (merge (meta var-sym)
         (post-name-metadata head form)))

(defn public-defs
  "Return top-level public defining forms from `source` as
   `{:head :name :line :metadata}` maps. `^:private` def-form variants
   are skipped — they're not on the goog.global path, so the
   `^:export` contract does not apply."
  [source]
  (->> (read-forms source)
       (keep (fn [form]
               (when (seq? form)
                 (let [head    (first form)
                       var-sym (second form)]
                   (when (and (contains? public-defining-heads head)
                              (symbol? var-sym))
                     (let [metadata (var-metadata head form var-sym)]
                       (when-not (:private metadata)
                         {:head     (name head)
                          :name     (name var-sym)
                          :line     (:line (meta form))
                          :metadata metadata})))))))))

(defn exported?
  [{:keys [metadata]}]
  (boolean (:export metadata)))
