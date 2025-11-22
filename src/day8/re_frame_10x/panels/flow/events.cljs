(ns day8.re-frame-10x.panels.flow.events
  (:require
   [clojure.set                                                  :as set]
   [clojure.string                                               :as string]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.fx.local-storage                           :as local-storage]))

(rf/reg-event-db
 ::set-filter
 [(rf/path [:flow :filter-str]) rf/trim-v]
 (fn [_ [filter-str]]
   filter-str))
