(ns ccw.util.edn
  (:require [clojure.edn :as edn]
            [clojure.test :refer [testing is]]))

(defn read-vector
  "Reads a vector from the EDN string s. Returns an empty vector if s does not
  contain a valid EDN vector."
  [s]
  (let [x (try
            (edn/read-string s)
            (catch Throwable _
              nil))]
    (if (vector? x)
      x
      [])))

(testing "read-vector"
  (testing "returns the vector in the input string"
    (is (= [123 456] (read-vector "[123 456]"))))
  (testing "returns an empty vector if input is invalid EDN"
    (is (= [] (read-vector "[123"))))
  (testing "returns an empty vector if input is not a vector"
    (is (= [] (read-vector "123")))))
