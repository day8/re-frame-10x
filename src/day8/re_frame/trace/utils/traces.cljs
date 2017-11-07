(ns day8.re-frame.trace.utils.traces
  (:require [reagent.core :as r]))

;; Put here to avoid cyclic dependencies
(defonce traces (r/atom []))
