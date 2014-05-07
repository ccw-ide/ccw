(ns ccw.launch-test
  (:require [clojure.test :refer [deftest testing is]]
            [ccw.leiningen.launch :refer :all])
  (:import [org.eclipse.core.resources IProject]))

(defn new-project []
  (reify
    IProject
    (getName [this] "some-name")
    ))

(deftest test-lein-launch-configuration
  (testing "lein-launch-configuration"
           (testing "returns quoted bootclasspath with fixed path"
                    (let [config (lein-launch-configuration new-project "some-command" {:leiningen-standalone-path "some.jar"})]
                      (is (map? config))
                      (let [matches (re-seq #"-Xbootclasspath/a:([^\s]*)" (:java/vm-arguments config))]
                        (let [bootclasspath (second (first matches))]
                          (is (= \" (first bootclasspath)))
                          (is (= \" (last bootclasspath)))
                          )
                        )
                      ))
           (testing "returns quoted bootclasspath without fixed path"
                    (let [config (lein-launch-configuration new-project "some-command")]
                      (is (map? config))
                      (let [matches (re-seq #"-Xbootclasspath/a:([^\s]*)" (:java/vm-arguments config))]
                        (let [bootclasspath (second (first matches))]
                          (is (= \" (first bootclasspath)))
                          (is (= \" (last bootclasspath)))
                          )
                        ))
                    ))
  )
