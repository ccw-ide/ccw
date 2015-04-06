;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial delayed atom implementation
;*******************************************************************************/

(ns ^{:doc "Generic utility functions in the Clojure world."}
  ccw.util)

(defn- ^{:author "Andrea Richiardi"}
  delayed-atom-fill
  "Returns a fn that will swap in a delay containing (apply f
  swap-args), where swap-args are the (wrapped) params given to swap!."
  [f]
  (fn [old-atom swap-args] (if old-atom
                            old-atom
                            (delay (apply f swap-args)))))

(defn- ^{:author "Andrea Richiardi"}
  delayed-atom-clear
  "Returns a function that clears the atom instance to a delay that
  returns nil and, if previously realized, executes (f (deref delay)
  args).
  Therefore, f is a function which accepts the result of the delay
  in first position, together with the swap! args. Always returns nil."
  [f]
  (fn [old-atom wrapped-args] (if (and old-atom (realized? old-atom))
                               (delay (apply f (force old-atom) wrapped-args) nil)
                               (delay nil))))

(defn ^{:author "Andrea Richiardi"}
  delayed-atom-swap!
  "Swaps in the input atom a delay containing (apply f args). It returns
  the swapped delay."
  [delayed-atom f & args]
  (swap! delayed-atom (delayed-atom-fill f) args))

(defn ^{:author "Andrea Richiardi"}
  delayed-atom-reset!
  "Resets the input atom instance to a delay that returns nil and, if
  previously realized, executes (f (deref delay) args).

  Therefore, f is a function which accepts the result of the delay in
  first position, together with other args. It is intended for
  side-effects only and the result is discarded.

  Always returns the nilled delay (forcing will produce nil)."
  [delayed-atom f & args]
  (swap! delayed-atom (delayed-atom-clear f) args))
