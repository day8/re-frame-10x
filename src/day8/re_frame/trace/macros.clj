(ns day8.re-frame.trace.macros
  (:require [clojure.java.io :as io]))

(defmacro slurp-macro
  "Reads a file as a string. Slurp is wrapped in a macro so it can interact with local files before clojurescript compilation."
  [path]
  (slurp (io/resource path)))
