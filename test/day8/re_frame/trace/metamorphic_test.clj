(ns day8.re-frame.trace.metamorphic-test
  (:require [clojure.test :refer :all])
  (:require [day8.re-frame.trace.metamorphic :as m]))

(defn trace-events [file]
  (->> (slurp (str "test-resources/" file))
       (clojure.edn/read-string {:readers {'utc    identity
                                           'object (fn [x] "<object>")}})
       (sort-by :id)))

(deftest parse-app-trace1-test
  (let [rt      (m/parse-traces m/initial-parse-state (trace-events "app-trace1.edn"))
        matches (:partitions rt)
        [m1 m2 m3 m4 m5 m6] matches]
    (is (= (count matches) 6))

    (is (= (m/beginning-id m1) 1))
    (is (= (m/ending-id m1) 34))
    (is (= (:operation (m/matched-event m1)) :bootstrap))

    (is (= (m/beginning-id m2) 35))
    (is (= (m/ending-id m2) 38))
    (is (= (:operation (m/matched-event m2)) :acme.myapp.events/boot-flow))

    (is (= (m/beginning-id m3) 39))
    (is (= (m/ending-id m3) 42))
    (is (= (:operation (m/matched-event m3)) :acme.myapp.events/init-db))

    (is (= (m/beginning-id m4) 43))
    (is (= (m/ending-id m4) 47))
    (is (= (:operation (m/matched-event m4)) :acme.myapp.events/boot-flow))

    (is (= (m/beginning-id m5) 48))
    (is (= (m/ending-id m5) 49))
    (is (= (:operation (m/matched-event m5)) :acme.myapp.events/start-intercom))

    (is (= (m/beginning-id m6) 50))
    (is (= (m/ending-id m6) 181))
    (is (= (:operation (m/matched-event m6)) :acme.myapp.events/success-bootstrap))))

#_(deftest parse-todomvc-trace1-test
  (let [rt      (m/parse-traces (trace-events "todomvc-trace1.edn"))
        matches (:matches rt)
        [m1 m2 m3 m4 m5 m6] matches]
    (is (= (count matches) 3))

    (is (= (m/beginning-id m1) 1))
    (is (= (m/ending-id m1) 34))
    (is (= (:operation (m/matched-event m1)) :bootstrap))

    (is (= (m/beginning-id m2) 35))
    (is (= (m/ending-id m2) 38))
    (is (= (:operation (m/matched-event m2)) :acme.myapp.events/boot-flow))

    (is (= (m/beginning-id m3) 39))
    (is (= (m/ending-id m3) 42))
    (is (= (:operation (m/matched-event m3)) :acme.myapp.events/init-db))

    (is (= (m/beginning-id m4) 43))
    (is (= (m/ending-id m4) 47))
    (is (= (:operation (m/matched-event m4)) :acme.myapp.events/boot-flow))

    (is (= (m/beginning-id m5) 48))
    (is (= (m/ending-id m5) 49))
    (is (= (:operation (m/matched-event m5)) :acme.myapp.events/start-intercom))

    (is (= (m/beginning-id m6) 50))
    (is (= (m/ending-id m6) 181))
    (is (= (:operation (m/matched-event m6)) :acme.myapp.events/success-bootstrap))))
