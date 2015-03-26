(ns ccw.editors.clojure.hover-support-test
  (:use clojure.java.data
        clojure.pprint)
  (:require [ccw.editors.clojure.hover-support :refer :all]
            [ccw.editors.clojure.hovers.docstring-hover :refer [create-docstring-hover]]
            [ccw.editors.clojure.hovers.debug-hover :refer [create-debug-hover]]
            [ccw.extensions :refer :all]
            [clojure.test :refer :all])
  (:import org.eclipse.swt.SWT
           ccw.editors.clojure.hovers.HoverDescriptor))

(defmacro with-private-fns [[ns fns] & tests]
  "Refers private fns from ns and runs tests in context."
  `(let ~(reduce #(conj %1 %2 `(ns-resolve '~ns '~%2)) [] fns)
     ~@tests))

(with-private-fns [ccw.editors.clojure.hover-support [hover-element->descriptor
                                                      descriptor-valid?
                                                      assoc-create-hover-closure
                                                      merge-descriptors
                                                      read-and-sanitize-descriptor-string
                                                      select-default-descriptor
                                                      select-hover-by-state-mask
                                                      select-hover-or-default
                                                      hover-result-pair
                                                      create-hover-instance!]]
  (deftest hover-support-tests

    (defn build-test-descriptor-fn
      ([element]
       (build-test-descriptor-fn element true))
      ([element enabled]
       {:element element
        :enabled enabled
        :description "Mock Description"
        :label "Mock"
        :id "ccw.editors.clojure.hovers.MockHover"
        :activate "true"
        :modifier-string nil
        :state-mask SWT/DEFAULT}))

    ;; Test of validity fiters
    (def test-invalid-element (mock-element :valid false))
    (def test-valid-element (mock-element :valid true))
    (def test-invalid-descriptor (build-test-descriptor-fn test-invalid-element))
    (def test-valid-descriptor (build-test-descriptor-fn test-valid-element))

    (testing "Descriptor validity"
      (defn load-valid-hovers! []
        (lazy-seq [test-valid-descriptor test-valid-descriptor test-valid-descriptor]))

      (defn load-invalid-hovers! []
        (lazy-seq [test-invalid-descriptor test-valid-descriptor]))

      (is (every? descriptor-valid? (load-valid-hovers!)))
      (is (not (every? descriptor-valid? (load-invalid-hovers!))))
      (is (function? (:create-hover (assoc-create-hover-closure test-valid-descriptor)))))

    (testing "Descriptor Java conversion"
      (def descriptor {:enabled true  :description "Mock Description" :label "Mock"
                       :id "ccw.editors.clojure.hovers.MockHover" :modifier-string nil
                       :state-mask SWT/DEFAULT})
      (def java-descriptor (HoverDescriptor. "ccw.editors.clojure.hovers.MockHover"
                                             "Mock" true "Mock Description" SWT/DEFAULT nil))

      (is (= java-descriptor (to-java HoverDescriptor descriptor)))
      (is (= descriptor (from-java java-descriptor))))

    (testing "Descriptors from preferences"
      (def test-pref-descriptors (list {:id "ccw.editors.clojure.hovers.MockHover"
                                        :enabled false
                                        :modifier-string "Alt"}
                                       {:id "not.editors.clojure.hovers.MockHover"
                                        :enabled true
                                        :modifier-string "Shift + Alt"}))

      (def merged-descriptors (merge-descriptors (list test-valid-descriptor) test-pref-descriptors))
      (is (= "ccw.editors.clojure.hovers.MockHover" (:id (first merged-descriptors))))
      (is (= false (:enabled (first merged-descriptors))))
      (is (= "Alt" (:modifier-string (first merged-descriptors))))

      (def test-string-invalid-modifier "({:modifier-string \"Shiftrasars\", :enabled true, :id \"Wrong\"}
      {:modifier-string \"\", :enabled true, :id \"Empty\"}
      { :enabled true, :id \"Nil\"})")
      (def invalid-descriptors (read-and-sanitize-descriptor-string test-string-invalid-modifier))
      (def wrong-modifier-descriptor (first (filter #(= "Wrong" (%1 :id)) invalid-descriptors)))
      (def empty-modifier-descriptor (first (filter #(= "Empty" (%1 :id)) invalid-descriptors)))
      (def nil-modifier-descriptor (first (filter #(= "Nil" (%1 :id)) invalid-descriptors)))

      (is (nil? (:modifier-string wrong-modifier-descriptor)))
      (is (= SWT/DEFAULT (:state-mask wrong-modifier-descriptor)))
      (is (= "" (:modifier-string empty-modifier-descriptor)))
      (is (= SWT/NONE (:state-mask empty-modifier-descriptor)))
      (is (nil? (:modifier-string nil-modifier-descriptor)))
      (is (= SWT/DEFAULT (:state-mask nil-modifier-descriptor)))

      (def test-string-valid-modifier "({:modifier-string \"Alt + Shift\", :enabled true, :id \"Id1\"}
      {:modifier-string \"Ctrl\", :enabled true, :id \"Id2\"})")
      (def test-descriptors (read-and-sanitize-descriptor-string test-string-valid-modifier))
      (def test-first-descriptor (first (filter #(= "Id1" (%1 :id)) test-descriptors)))
      (def test-second-descriptor (first (filter #(= "Id2" (%1 :id)) test-descriptors)))

      (is (= "Alt + Shift" (:modifier-string test-first-descriptor)))
      (is (= 196608 (:state-mask test-first-descriptor)))
      (is (= "Ctrl" (:modifier-string test-second-descriptor)))
      (is (= 262144 (:state-mask test-second-descriptor))))

    (testing "Descriptor selection"
      (is (= test-first-descriptor (select-hover-by-state-mask "" 196608 test-descriptors)))
      (is (not= test-second-descriptor (select-hover-by-state-mask "" 196608 test-descriptors)))
      (is (not= test-first-descriptor (select-hover-by-state-mask "" 262144 test-descriptors)))
      (is (= test-second-descriptor (select-hover-by-state-mask "" 262144 test-descriptors)))

      ;; Test if the default is picked with nil or ""
      (is (= "Id1" (:id (select-default-descriptor (read-and-sanitize-descriptor-string "({:modifier-string \"\", :enabled true, :id \"Id1\"})")))))
      (is (= "Id1" (:id (select-default-descriptor (read-and-sanitize-descriptor-string "({:enabled true, :id \"Id1\"})")))))

      (def test-string-no-modifier "({:modifier-string \"\", :enabled true, :id \"Id1\"}
      {:modifier-string \"Alt + Shift\", :enabled true, :id \"Id2\"}
      {:modifier-string \"Ctrl\", :enabled true, :id \"Id3\"})")
      (def no-modifier-descriptors (read-and-sanitize-descriptor-string test-string-no-modifier))
      (def no-modifier-descriptor (first (filter #(= "Id1" (%1 :id)) no-modifier-descriptors)))
      (def ctrl-modifier-descriptor (first (filter #(= "Id3" (%1 :id)) no-modifier-descriptors)))

      (is (= no-modifier-descriptor (select-default-descriptor no-modifier-descriptors)))
      (is (= no-modifier-descriptor (select-hover-or-default select-hover-by-state-mask "" 42 no-modifier-descriptors)))
      (is (= ctrl-modifier-descriptor (select-hover-or-default select-hover-by-state-mask "" 262144 no-modifier-descriptors)))
      (is (not= ctrl-modifier-descriptor (select-hover-or-default select-hover-by-state-mask "" 196608 no-modifier-descriptors))))

    (testing "Hover instances creation"
      (def mock-contributed-hovers (list {:id "mock-debug" :modifier-string "" :state-mask SWT/NONE :enabled true :create-hover #(create-debug-hover) :label "Mock debug" :activate "true"},
                                    {:id "mock-docstring" :modifier-string "Ctrl" :state-mask 262144 :enabled true :create-hover #(create-docstring-hover) :label "Mock docstring" :activate "true"}))
      (def docstring-hover (create-hover-instance! mock-contributed-hovers "" 262144))
      (def debug-hover (create-hover-instance! mock-contributed-hovers "" SWT/NONE))
      (is (identical? docstring-hover (create-hover-instance! mock-contributed-hovers "" 262144)))
      (is (identical? debug-hover (create-hover-instance! mock-contributed-hovers "" SWT/NONE)))

      (def mock-debug-hover (create-debug-hover))
      (def mock-docstring-hover (create-docstring-hover))
      (def mock-hover-instances {0 mock-docstring-hover, 262144 mock-debug-hover })
      (is (identical? mock-docstring-hover (first (hover-result-pair nil
                                                                     #(.toString %1)
                                                                     #(.contains %1 "docstring")
                                                                     mock-hover-instances))) "Should return the found hover instance")
      (is (every? nil? (hover-result-pair nil
                                          #(.toString %1)
                                          #(.contains %1 "something")
                                          mock-hover-instances)) "Should return nil because not found")
      (is (identical? mock-debug-hover (first (hover-result-pair mock-debug-hover
                                                                 #(.toString %1)
                                                                 #(.contains %1 "debug")
                                                                 mock-hover-instances))) "Should correctly return mock-debug-hover instance")
      (is (identical? mock-debug-hover (first (hover-result-pair mock-debug-hover
                                                                 #(.toString %1)
                                                                 #(.contains %1 "something")
                                                                 mock-hover-instances))) "Should ignore the predicate and return mock-debug-hover instance"))
    ))
;(run-tests)
