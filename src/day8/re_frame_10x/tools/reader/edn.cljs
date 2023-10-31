(ns day8.re-frame-10x.tools.reader.edn
  (:require
   [cljs.tools.reader.edn]))

(def default-readers
  {'uuid (fn default-uuid-reader [form]
           {:pre [(string? form)]}
           (uuid form))
   're-frame-10x/sorted-map (fn [form]
                              (into (sorted-map) form))})

(defn read-string-maybe [s]
  (try (cljs.tools.reader.edn/read-string {:readers default-readers} s)
       (catch :default _
         nil)))
