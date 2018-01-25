(ns day8.re-frame.trace.utils.macros
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defmacro slurp-macro
  "Reads a file as a string. Slurp is wrapped in a macro so it can interact with local files before clojurescript compilation.
  #'s are replaced with %23 for URL encoding."
  ;; For reasons unknown (to me), browsers don't like URL encoding the entire string.
  ;; I suspect XML encoding the attributes is the correct way to do this, but
  ;; #'s are the problem here, so we so we do surgery on the one problematic symbol.
  [path]
  (str/replace
    (slurp (io/resource path))
    "#"
    "%23"))

(defmacro with-cljs-devtools-prefs [prefs & body]
  `(let [previous-config# (devtools.prefs/get-prefs)
         prefs# ~prefs]
     (try
       (devtools.prefs/set-prefs! prefs#)
       ~@body
       (finally
         (assert (= (devtools.prefs/get-prefs) prefs#) "someone modified devtools.prefs behind our back!")
         (devtools.prefs/set-prefs! previous-config#)))))
