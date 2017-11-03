(ns day8.re-frame.trace.utils.macros
  (:require [clojure.java.io :as io]))

(defmacro slurp-macro
  "Reads a file as a string. Slurp is wrapped in a macro so it can interact with local files before clojurescript compilation."
  [path]
  (slurp (io/resource path)))

(defmacro with-cljs-devtools-prefs [prefs & body]
  `(let [previous-config# (devtools.prefs/get-prefs)
         prefs# ~prefs]
     (try
       (devtools.prefs/set-prefs! prefs#)
       ~@body
       (finally
         (assert (= (devtools.prefs/get-prefs) prefs#) "someone modified devtools.prefs behind our back!")
         (devtools.prefs/set-prefs! previous-config#)))))
