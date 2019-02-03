(ns day8.re-frame-10x.utils.utils-test
  (:require [clojure.test :refer :all]
            [day8.re-frame-10x.utils.utils :as utils]))

(deftest last-in-vec-test
  (is (nil? (utils/last-in-vec [])))
  (is (= 1 (utils/last-in-vec [1])))
  (is (= 2 (utils/last-in-vec [1 2])))
  (is (= 3 (utils/last-in-vec [1 2 3]))))
