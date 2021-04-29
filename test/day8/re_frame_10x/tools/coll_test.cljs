(ns day8.re-frame-10x.tools.coll-test
  (:require [clojure.test :refer :all]
            [day8.re-frame-10x.tools.coll :as tools.coll]))

(deftest last-in-vec-test
  (is (nil? (tools.coll/last-in-vec [])))
  (is (= 1 (tools.coll/last-in-vec [1])))
  (is (= 2 (tools.coll/last-in-vec [1 2])))
  (is (= 3 (tools.coll/last-in-vec [1 2 3]))))
