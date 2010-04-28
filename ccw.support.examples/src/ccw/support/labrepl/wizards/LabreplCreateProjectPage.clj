;*******************************************************************************
;* Copyright (c) 2010 Stephan Muehlstrasser.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Stephan Muehlstrasser - initial API and implementation
;*******************************************************************************/
(ns ccw.support.labrepl.wizards.LabreplCreateProjectPage
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Button Composite]
    [org.eclipse.swt.layout GridLayout GridData])
  (:gen-class
   :extends org.eclipse.ui.dialogs.WizardNewProjectCreationPage
   :exposes-methods {createControl super-createControl}
   :init myinit
   :post-init mypostinit
   :state state))

(defn- -myinit
  [page-name]
  [[page-name] (ref {})])

(defn- -mypostinit
  [_ page-name])

(defn -createControl
  [this parent]
  (.super-createControl this parent)
  (let
    [composite (Composite. (.getControl this) SWT/NONE)
     layout (GridLayout.)]
    (doto composite
      (.setLayout layout)
      (.setLayoutData (GridData. (bit-or GridData/VERTICAL_ALIGN_FILL GridData/FILL_HORIZONTAL))))
    (let [run-lein-deps-button (Button. composite SWT/CHECK)]
      (doto run-lein-deps-button
        (.setText "Run \"lein deps\" to download dependencies")
        (.setSelection  true)))
    (let [run-repl-button (Button. composite SWT/CHECK)]
      (doto run-repl-button
        (.setText "Run REPL and open web page")
        (.setSelection  true)))
    (.validatePage this)))
