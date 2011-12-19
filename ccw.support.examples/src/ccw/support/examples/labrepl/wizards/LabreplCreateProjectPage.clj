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
(ns ccw.support.examples.labrepl.wizards.LabreplCreateProjectPage
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Button Composite]
    [org.eclipse.swt.layout GridLayout GridData]
    [org.eclipse.swt.events SelectionAdapter] )
  (:gen-class
   :extends org.eclipse.ui.dialogs.WizardNewProjectCreationPage
   :exposes-methods {createControl super_createControl}
   :init init
   :state state))

(defn- -init
  [page-name]
  [[page-name] (ref {})])

(defn -createControl
  [this parent]
  (.super_createControl this parent)
  (let
    [composite (Composite. (.getControl this) SWT/NONE)
     layout (GridLayout.)]

    (doto composite
      (.setLayout layout)
      (.setLayoutData (GridData. (bit-or GridData/VERTICAL_ALIGN_FILL GridData/FILL_HORIZONTAL))))

    ; Create the check buttons for running "lein deps" and for starting up
    ; the REPL and for displaying the Labrepl start page.
    ; The latter button only is enabled if the "lein deps" button is checked.
    (let [run-lein-deps-button (Button. composite SWT/CHECK)
          run-repl-button (Button. composite SWT/CHECK)]
      (doto run-lein-deps-button
        (.setText "Run \"lein deps\" to download dependencies (Internet connection required,\ndownloads third-party software from Clojars repository)")
        (.setSelection true)
        (.addSelectionListener
          (proxy [SelectionAdapter] []
            (widgetSelected [_]
              (.setEnabled run-repl-button (.getSelection run-lein-deps-button))))))

      (doto run-repl-button
        (.setText "Run REPL and open tutorial web page")
        (.setSelection true))

      (dosync (alter (.state this) assoc :run-repl-button run-repl-button :run-lein-deps-button run-lein-deps-button))))

  (.validatePage this))
