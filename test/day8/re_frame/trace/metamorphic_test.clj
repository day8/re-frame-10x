(ns day8.re-frame.trace.metamorphic-test
  (:require [clojure.test :refer :all])
  (:require [day8.re-frame.trace.metamorphic :as m]))

(deftest parse-events-test
  (= (m/parse-events)
     '(({:id        327,
        :operation [:idle :add-event],
        :op-type   :re-frame.router/fsm-trigger,
        :tags      {:current-state :idle, :new-state :scheduled}}
        {:id 329, :operation :estimate/new, :op-type :event, :tags {:event [:estimate/new]}}
        {:id        330,
         :operation [:running :finish-run],
         :op-type   :re-frame.router/fsm-trigger,
         :tags      {:current-state :running, :new-state :idle}}))
     ))
