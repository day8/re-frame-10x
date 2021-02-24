(ns day8.reagent.impl.batching
  (:require
    [reagent.impl.batching :as batching]
    [re-frame.trace :as trace :include-macros true]))

(defonce original-next-tick reagent.impl.batching/next-tick)

(defn next-tick
  [f]
  ;; Schedule a trace to be emitted after a render if there is nothing else scheduled after that render.
  ;; This signals the end of the epoch.

  (original-next-tick
    (fn []
      (trace/with-trace
        {:op-type :raf}
        (f)
        (trace/with-trace {:op-type :raf-end})
        (when (false? (.-scheduled? reagent.impl.batching/render-queue))
          (trace/with-trace {:op-type :reagent/quiescent}))))))

(defn patch-next-tick
  []
  (set! reagent.impl.batching/next-tick next-tick))