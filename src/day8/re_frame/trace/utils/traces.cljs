(ns day8.re-frame.trace.utils.traces
  (:require [reagent.core :as r]
            [clojure.string :as str]))

;; Put here to avoid cyclic dependencies
(defonce traces (r/atom []))
(defonce total-traces (r/atom 0))

(defn log-trace? [trace]
  (let [render-operation? (= (:op-type trace) :render)
        component-path    (get-in trace [:tags :component-path] "")]
    (if-not render-operation?
      true
      (not (str/includes? component-path "devtools outer")))))

(defn disable-tracing! []
  (re-frame.trace/remove-trace-cb ::cb))

(defn enable-tracing! []
  (re-frame.trace/register-trace-cb ::cb (fn [new-traces]
                                           (when-let [new-traces (filter log-trace? new-traces)]
                                             (swap! total-traces + (count new-traces))
                                             (swap! traces
                                                    (fn [existing]
                                                      (let [new  (reduce conj existing new-traces)
                                                            size (count new)]
                                                        (if (< 4000 size)
                                                          (let [new2 (subvec new (- size 2000))]
                                                            (if (< @total-traces 20000) ;; Create a new vector to avoid structurally sharing all traces forever
                                                              (do (reset! total-traces 0)
                                                                  (into [] new2))))
                                                          new))))))))
