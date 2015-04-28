;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial implementation
;*******************************************************************************/

(ns ccw.test-common
  "Common utilities for testing.")

(defmacro with-private-vars
  "Refers private fns from ns and runs tests in context."
  [[ns private-vars] & tests]
  `(let ~(reduce #(conj %1 %2 `(ns-resolve '~ns '~%2)) [] private-vars)
     ~@tests))

(defmacro with-atom
  "Quick and dirty anaphoric macro to build an atom which can be
  referred in the body with the symbol a."
  [init & body]
  `(let [~'a (atom ~init)]
     ~@body))
