;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial test implementation
;*******************************************************************************/

(ns ccw.editors.clojure.hover-support-test
  (:require [ccw.test-common :refer :all]
            [clojure.java.data :refer :all]
            [ccw.editors.clojure.hover-support :refer :all]
            [ccw.extensions :refer :all]
            [clojure.test :refer :all])
  (:import org.eclipse.swt.SWT
           ccw.editors.clojure.hovers.HoverDescriptor))

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

(def test-invalid-element (mock-element :valid false))
(def test-valid-element (mock-element :valid true))
(def test-invalid-descriptor (build-test-descriptor-fn test-invalid-element))
(def test-valid-descriptor (build-test-descriptor-fn test-valid-element))

(defn test-first-descriptor 
  [test-descriptors]
  (first (filter #(= "Id1" (%1 :id)) test-descriptors)))

(defn test-second-descriptor 
  [test-descriptors]
  (first (filter #(= "Id2" (%1 :id)) test-descriptors)))

(def test-string-invalid-modifier "({:modifier-string \"Shiftrasars\", :enabled true, :id \"Wrong\"} {:modifier-string \"\", :enabled true, :id \"Empty\"} { :enabled true, :id \"Nil\"})")
(def test-string-valid-modifier "({:modifier-string \"Alt + Shift\", :enabled true, :id \"Id1\"} {:modifier-string \"Ctrl\", :enabled true, :id \"Id2\"})")


(with-private-vars [ccw.editors.clojure.hover-support [hover-element->descriptor
                                                       descriptor-valid?
                                                       assoc-create-hover-closure
                                                       merge-descriptors
                                                       read-and-sanitize-descriptor-string
                                                       select-default-descriptor
                                                       select-hover-by-state-mask
                                                       select-descriptor
                                                       remove-and-cons]]
  (deftest hover-support-tests

    (testing "Descriptor validity"
      (let [load-valid-hovers [test-valid-descriptor test-valid-descriptor test-valid-descriptor]
            load-invalid-hovers [test-invalid-descriptor test-valid-descriptor]] 

        (is (every? descriptor-valid? load-valid-hovers)) 
        (is (not (every? descriptor-valid? load-invalid-hovers))) 
        (is (function? (:create-hover (assoc-create-hover-closure test-valid-descriptor))))))

    (testing "Descriptor Java conversion"
      (let [descriptor {:enabled true  :description "Mock Description" :label "Mock"
                        :id "ccw.editors.clojure.hovers.MockHover" :modifier-string nil
                        :state-mask SWT/DEFAULT}
            java-descriptor (HoverDescriptor. "ccw.editors.clojure.hovers.MockHover"
                                              "Mock" true "Mock Description" SWT/DEFAULT nil)]
        (is (= java-descriptor (to-java HoverDescriptor descriptor))) 
        (is (= descriptor (from-java java-descriptor)))))

    (testing "Descriptors from preferences"
      (let [test-pref-descriptors (list {:id "ccw.editors.clojure.hovers.MockHover"
                                         :enabled false
                                         :modifier-string "Alt"}
                                        {:id "not.editors.clojure.hovers.MockHover"
                                         :enabled true
                                         :modifier-string "Shift + Alt"})
            merged-descriptors (merge-descriptors (list test-valid-descriptor) test-pref-descriptors)
            invalid-descriptors (read-and-sanitize-descriptor-string test-string-invalid-modifier)
            wrong-modifier-descriptor (first (filter #(= "Wrong" (%1 :id)) invalid-descriptors))
            empty-modifier-descriptor (first (filter #(= "Empty" (%1 :id)) invalid-descriptors))
            nil-modifier-descriptor (first (filter #(= "Nil" (%1 :id)) invalid-descriptors))
            test-descriptors (read-and-sanitize-descriptor-string test-string-valid-modifier)
            first-descriptor (test-first-descriptor test-descriptors)
            second-descriptor (test-second-descriptor test-descriptors)]

        (is (= "ccw.editors.clojure.hovers.MockHover" (:id (first merged-descriptors)))) 
        (is (= false (:enabled (first merged-descriptors)))) 
        (is (= "Alt" (:modifier-string (first merged-descriptors)))) 

        (is (nil? (:modifier-string wrong-modifier-descriptor))) 
        (is (= SWT/DEFAULT (:state-mask wrong-modifier-descriptor))) 
        (is (= "" (:modifier-string empty-modifier-descriptor))) 
        (is (= SWT/NONE (:state-mask empty-modifier-descriptor))) 
        (is (nil? (:modifier-string nil-modifier-descriptor))) 
        (is (= SWT/DEFAULT (:state-mask nil-modifier-descriptor))) 

        (is (= "Alt + Shift" (:modifier-string first-descriptor))) 
        (is (= 196608 (:state-mask first-descriptor))) 
        (is (= "Ctrl" (:modifier-string second-descriptor))) 
        (is (= 262144 (:state-mask second-descriptor)))))

    (testing "Descriptor selection"
      (let [test-string-no-modifier "({:modifier-string \"\", :enabled true, :id \"Id1\"} {:modifier-string \"Alt + Shift\", :enabled true, :id \"Id2\"} {:modifier-string \"Ctrl\", :enabled true, :id \"Id3\"})"
            no-modifier-descriptors (read-and-sanitize-descriptor-string test-string-no-modifier)
            no-modifier-descriptor (first (filter #(= "Id1" (%1 :id)) no-modifier-descriptors))
            ctrl-modifier-descriptor (first (filter #(= "Id3" (%1 :id)) no-modifier-descriptors))
            test-descriptors (read-and-sanitize-descriptor-string test-string-valid-modifier)
            first-descriptor (test-first-escriptor test-descriptors)
            second-descriptor (test-second-descriptor test-descriptors)]
        
        (is (= first-descriptor (select-hover-by-state-mask "" 196608 test-descriptors))) 
        (is (not= second-descriptor (select-hover-by-state-mask "" 196608 test-descriptors))) 
        (is (not= first-descriptor (select-hover-by-state-mask "" 262144 test-descriptors))) 
        (is (= second-descriptor (select-hover-by-state-mask "" 262144 test-descriptors))) 

        ;; Test if the default is picked with nil or ""
        (is (= "Id1" (:id (select-default-descriptor (read-and-sanitize-descriptor-string "({:modifier-string \"\", :enabled true, :id \"Id1\"})"))))) 
        (is (= "Id1" (:id (select-default-descriptor (read-and-sanitize-descriptor-string "({:enabled true, :id \"Id1\"})"))))) 

        (is (= no-modifier-descriptor (select-default-descriptor no-modifier-descriptors))) 
        (is (= no-modifier-descriptor (select-descriptor (partial select-hover-by-state-mask "" 42) no-modifier-descriptors))) 
        (is (= ctrl-modifier-descriptor (select-descriptor (partial select-hover-by-state-mask "" 262144) no-modifier-descriptors))) 
        (is (not= ctrl-modifier-descriptor (select-descriptor (partial select-hover-by-state-mask "" 196608) no-modifier-descriptors)))))

    (testing "Descriptor list ops"
      (let [mock-descriptors (list {:id "Test1" :enabled true} {:id "Test2" :enabled false})]
        (is (some #(= "Test3" (:id %1)) (remove-and-cons #(= (:id %1) "Test2") {:id "Test3"} mock-descriptors))) 
        (is (not-any? #(= "Test2" (:id %1)) (remove-and-cons #(= (:id %1) "Test2") {:id "Test3"} mock-descriptors)))))))
