(ns ccw.util.edn-test
  (:require [clojure.test :refer [deftest testing is]]
            [ccw.util.edn :refer :all]))

(deftest test-read-vector
  (testing "read-vector"
    (testing "returns the vector in the input string"
      (is (= [123 456] (read-vector "[123 456]"))))
    (testing "returns an empty vector if the input is invalid EDN"
      (is (= [] (read-vector "[123"))))
    (testing "returns an empty vector if the input is not a vector"
      (is (= [] (read-vector "123"))))))
