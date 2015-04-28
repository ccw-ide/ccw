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

(ns ccw.extensions-test
  (:require [ccw.test-common :refer :all]
            [ccw.extensions :refer :all]
            [clojure.test :refer :all]))

(with-private-vars [ccw.extensions [mock-element
                                    mock-with-kids
                                    mock-zero-kids-element]] 
  (deftest extensions-tests

    (let [test-invalid-element (mock-element :valid false)
          test-valid-element (mock-element :valid true)
          p (mock-element :tag "parent" :kids (list (mock-zero-kids-element "tizio" nil)
                                                    (mock-zero-kids-element "caio" nil)
                                                    (mock-zero-kids-element "sempronio" nil)
                                                    (mock-zero-kids-element "tizio" nil)))
          one-kid-parent (element->map (mock-element :tag "ab"
                                                     :attrs {:a "a" :b "b"}
                                                     :kids (list (mock-zero-kids-element "kab" {:ka "ka" :kb "kb"}))))
          content-of-one-kid-parent (:content one-kid-parent)
          two-kid-parent (element->map (mock-with-kids "ab" {:c "c" :d "d"} 2 {:kc "kc" :kd "kd"} {:ke "ke" :kf "kf"}))
          content-of-two-kid-parent (:content two-kid-parent)
          ] 
      (is (element-valid? test-valid-element)) 
      (is (not (element-valid? test-invalid-element))) 
      (is (= "pippo" (element-name (mock-element :tag "pippo")))) 
      (is (= "pluto" (element-name (first (element-children (mock-element :kids (list (mock-zero-kids-element "pluto" nil)))))))) 
      (is (= "a" (element-attribute (mock-element :attrs {:a "a"}) "a"))) 
      (is (= 4 (count (element-children (mock-with-kids "p" nil 4))))) 

      (is (= {:a "a"} (attributes->map (mock-element :attrs {:a "a"})))) 
      (is (empty? (attributes->map (mock-element :attrs nil)))) 
      (is (empty? (attributes->map (mock-element :attrs {})))) 
      (is (not (nil? (attributes->map (mock-element :attrs nil))))) 
      (is (not (nil? (attributes->map (mock-element :attrs {}))))) 

      (is (= 2 (count (element-children p "tizio")))) 
      (is (every? #(= "tizio" (element-name %1)) (element-children p "tizio"))) 
      (is (not-any? #(= "caio" (element-name %1)) (element-children p "tizio"))) 
      (is (not-any? #(= "sempronio" (element-name %1)) (element-children p "tizio"))) 
      (is (empty? (element-children p "asd"))) 
      
      (is (= "ab" (:tag one-kid-parent))) 
      (is (= 1 (count content-of-one-kid-parent))) 
      (is (= {:a "a" :b "b"} (:attrs one-kid-parent))) 
      (is (= {:ka "ka" :kb "kb"} (:attrs (first content-of-one-kid-parent)))) 
      (is (= "kab" (:tag (first content-of-one-kid-parent)))) 

      (is (= 2 (count content-of-two-kid-parent))) 
      (is (= {:kc "kc" :kd "kd"} (:attrs (first content-of-two-kid-parent)))) 
      (is (= {:ke "ke" :kf "kf"} (:attrs (second content-of-two-kid-parent)))))))
