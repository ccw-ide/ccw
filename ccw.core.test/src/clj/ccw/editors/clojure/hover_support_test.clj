(ns ccw.editors.clojure.hover-support-test
  (:use clojure.java.data
        clojure.pprint)
  (:require [ccw.editors.clojure.hover-support :refer :all]
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
                                                      remove-and-cons]]
  (deftest hover-support-tests

    (defn build-test-descriptor-fn
      ([element]
       (build-test-descriptor-fn element true))
      ([element enabled]
       {:element element
        :instance nil
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

    (testing "Descriptor list ops"
      (def mock-descriptors (list {:id "Test1" :enabled true} {:id "Test2" :enabled false}))
      (is (some #(= "Test3" (:id %1)) (remove-and-cons #(= (:id %1) "Test2") {:id "Test3"} mock-descriptors)))
      (is (not-any? #(= "Test2" (:id %1)) (remove-and-cons #(= (:id %1) "Test2") {:id "Test3"} mock-descriptors))))

    (testing "Proxy creation"
      (def hover-proxy (create-hover-proxy "" 262144))
      (is (identical? hover-proxy (create-hover-proxy "" 262144))))))
