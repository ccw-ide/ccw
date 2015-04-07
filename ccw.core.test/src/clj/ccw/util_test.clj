;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial tests for delayed atom
;*******************************************************************************/

(ns ccw.util_test
  (:use clojure.test
        ccw.util))

(defmacro with-private-fns
  "Refers private fns from ns and runs tests in context."
  [[ns fns] & tests]
  `(let ~(reduce #(conj %1 %2 `(ns-resolve '~ns '~%2)) [] fns)
     ~@tests))

(defmacro with-atom
  "Quick and dirty anaphoric macro to build an atom which can be
  referred in the body with the symbol a."
  [init & body]
  `(let [~'a (atom ~init)]
     ~@body))

(with-private-fns [ccw.util [delayed-atom-fill
                             delayed-atom-clear]]

  (deftest ccw-core-tests
    "Tests the ccw.core namespace."

    (testing "Delayed atom - common"
      (is (not (some? (with-atom nil (deref a)))) "Delayed atom is nil if no op."))

    (testing "Delayed atom swaps - f with params"
      (defn create-vec
        [& nums]
        (apply vector nums))

      (defn keep-vec-index
        [vec index]
        (vec (nth vec index)))

      (is (not (realized? (with-atom nil (delayed-atom-swap! a create-vec 4 5 6)))) "Swapping: delay has to be realized explicitely")
      (is (= [4 5 6] (with-atom nil (force (delayed-atom-swap! a create-vec 4 5 6)))) "Swapping: delay's value is [4 5 6]")
      (is (some? (with-atom nil (do (delayed-atom-swap! a create-vec 4 5 6) (deref a)))) "Swapping: deref'd delayed atom is not nil.")

      (is (some? (with-atom nil (do (delayed-atom-reset! a keep-vec-index 2) (deref a)))) "Resetting: deref'd delayed atom is not nil (it is still a delay.")
      (is (= nil (with-atom nil (force (delayed-atom-reset! a keep-vec-index 2)))) "Resetting: delay's value is always nil."))

    (testing "Delayed atom swaps - f with no params"
      (defn build
        []
        {:db-instance "Some instance"})

      (defn unbuild
        [map]
        (let [instance (:db-instance map)] (println "Cleared " instance)))

      (is (not (realized? (with-atom nil (delayed-atom-swap! a build)))) "Swapping: delay has to be realized explicitely")
      (is (= {:db-instance "Some instance"} (with-atom nil (force (delayed-atom-swap! a build)))) "Swapping: delay's value is [4 5 6]")
      (is (some? (with-atom nil (do (delayed-atom-swap! a build) (deref a)))) "Swapping: deref'd delayed atom is not nil.")

      (is (some? (with-atom nil (do (delayed-atom-reset! a unbuild) (deref a)))) "Resetting: deref'd delayed atom is not nil (it is still a delay.")
      (is (= nil (with-atom nil (force (delayed-atom-reset! a unbuild)))) "Resetting: delay's value is always nil."))

))
;; (run-tests)
