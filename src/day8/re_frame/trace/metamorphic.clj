(ns day8.re-frame.trace.metamorphic
  (:require [metamorphic.api :as m]
            [metamorphic.runtime :as rt]
            [metamorphic.viz :as v]))

;; Next, we define predicate functions that take exactly 4 arguments.
;; These predicates are obviously incredibly boring, but they help
;; save your brain power for the real concepts.

;; Each predicate will receive each event as it arrives, a history (which we'll discuss later),
;; the entire pattern sequence, and the particular pattern that this predicate
;; is being used in. This is helpful for parameterizing a predicate.

(defn a? [event history pattern-sequence pattern]
  (= event "a"))

(defn b? [event history pattern-sequence pattern]
  (= event "b"))

(defn c? [event history pattern-sequence pattern]
  (= event "c"))

;; Now let's create a pattern sequence. We're looking for "a", "b", then "c".
;; This pattern says: find "a", then immediately look for "b". After you find "b",
;; look for "c", but if there's something that doesn't match in the middle, that's
;; okay. The relaxation of looking for "c" is called a contiguity constraint, denoted
;; by "followed-by" instead of "next".

(defn run-test []
  (let [runtime (-> (m/new-pattern-sequence "a b c")
                    (m/begin "a" a?)
                    (m/next "b" b?)
                    (m/followed-by "c" c?)
                    (rt/initialize-runtime))
        events  ["a" "b" "q" "c" "z" "a" "b" "d" "x" "c"]]
    (:matches (reduce rt/evaluate-event runtime events))))


;;;

(defn new-epoch-started? [event history pattern-sequence pattern]
  (and (= :re-frame.router/fsm-trigger (:op-type event))
       (= (:operation event)
          [:idle :add-event])))

(defn event-run? [event history pattern-sequence pattern]
  (= :event (:op-type event)))

(defn redispatched-event? [event history pattern-sequence pattern]
  (and (= :re-frame.router/fsm-trigger (:op-type event))
       (= (:operation event)
          [:running :add-event])))

(defn router-scheduled? [event history pattern-sequence pattern]
  (and (= :re-frame.router/fsm-trigger (:op-type event))
       (= (:operation event)
          [:running :finish-run])
       (= :running (get-in event [:tags :current-state]))
       (= :scheduled (get-in event [:tags :new-state]))))

(defn router-finished? [event history pattern-sequence pattern]
  (and (= :re-frame.router/fsm-trigger (:op-type event))
       (= (:operation event)
          [:running :finish-run])
       (= :running (get-in event [:tags :current-state]))
       (= :idle (get-in event [:tags :new-state]))))


(defn trace-events [] (->> (slurp "test-resources/events2.edn")
                       (clojure.edn/read-string {:readers {'utc    identity
                                                           'object (fn [x] "<object>")}})
                       (sort-by :id))
  )


(defn summarise-event [ev]
  (dissoc ev :start :duration :end :child-of))

(defn summarise-match [match]
  (map summarise-event match))

(defn parse-events []
  (let [runtime (-> (m/new-pattern-sequence "simple traces")
                    (m/begin "new-epoch-started" new-epoch-started?)
                    #_(m/followed-by "redispatched-event" redispatched-event? {:optional? true})
                #_   (m/followed-by "router-scheduled" router-scheduled? {:optional? true})
                    (m/followed-by "event-run" event-run?)
                    (m/followed-by "router-finished" router-finished?)
                    (rt/initialize-runtime))
        events  (trace-events)
        rt      (reduce rt/evaluate-event runtime events)]
    #_(println "Count"
             (count (:matches rt))
             (map count (:matches rt)))
    (map summarise-match (:matches rt))))
