(ns ccw.extensions-test
  (:require [ccw.extensions :refer :all]
            [clojure.test :refer :all]))

(deftest extensions-tests
  (def test-invalid-element (mock-element :valid false))
  (def test-valid-element (mock-element :valid true))

  (is (element-valid? test-valid-element))
  (is (not (element-valid? test-invalid-element)))
  (is (= "pippo" (element-name (mock-element :tag "pippo"))))
  (is (= "pluto" (element-name (first (element-children
                                        (mock-element :kids (list (mock-zero-kids-element "pluto" nil))))))))
  (is (= "a" (element-attribute (mock-element :attrs {:a "a"}) "a")))
  (is (= 4 (count (element-children (mock-with-kids "p" nil 4)))))

  (is (= {:a "a"} (attributes->map (mock-element :attrs {:a "a"}))))
  (is (empty? (attributes->map (mock-element :attrs nil))))
  (is (empty? (attributes->map (mock-element :attrs {}))))
  (is (not (nil? (attributes->map (mock-element :attrs nil)))))
  (is (not (nil? (attributes->map (mock-element :attrs {})))))

  (def p (mock-element :tag "parent" :kids (list (mock-zero-kids-element "tizio" nil)
                                                 (mock-zero-kids-element "caio" nil)
                                                 (mock-zero-kids-element "sempronio" nil)
                                                 (mock-zero-kids-element "tizio" nil))))
  (is (= 2 (count (element-children p "tizio"))))
  (is (every? #(= "tizio" (element-name %1)) (element-children p "tizio")))
  (is (not-any? #(= "caio" (element-name %1)) (element-children p "tizio")))
  (is (not-any? #(= "sempronio" (element-name %1)) (element-children p "tizio")))
  (is (empty? (element-children p "asd")))

  (def one-kid-parent (element->map (mock-element :tag "ab"
                                                  :attrs {:a "a" :b "b"}
                                                  :kids (list (mock-zero-kids-element "kab" {:ka "ka" :kb "kb"})))))
  (def content-of-one-kid-parent (:content one-kid-parent))
  (is (= "ab" (:tag one-kid-parent)))
  (is (= 1 (count content-of-one-kid-parent)))
  (is (= {:a "a" :b "b"} (:attrs one-kid-parent)))
  (is (= {:ka "ka" :kb "kb"} (:attrs (first content-of-one-kid-parent))))
  (is (= "kab" (:tag (first content-of-one-kid-parent))))

  (def two-kid-parent (element->map (mock-with-kids "ab" {:c "c" :d "d"} 2 {:kc "kc" :kd "kd"} {:ke "ke" :kf "kf"})))
  (def content-of-two-kid-parent (:content two-kid-parent))
  (is (= 2 (count content-of-two-kid-parent)))
  (is (= {:kc "kc" :kd "kd"} (:attrs (first content-of-two-kid-parent))))
  (is (= {:ke "ke" :kf "kf"} (:attrs (second content-of-two-kid-parent)))))
