(ns day8.re-frame.preload
  (:require [day8.re-frame.trace :as trace]))


;; Use this namespace with the :preloads compiler option to perform the necessary setup for enabling tracing:
;; {:compiler {:preloads [day8.re-frame.preload] ...}}
(trace/init-tracing!)
