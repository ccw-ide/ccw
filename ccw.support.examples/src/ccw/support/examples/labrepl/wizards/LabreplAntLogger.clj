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
(ns ccw.support.examples.labrepl.wizards.LabreplAntLogger
  (:gen-class
   :extends org.apache.tools.ant.NoBannerLogger
   :constructors {[org.eclipse.core.runtime.IProgressMonitor] []}
   :exposes-methods {messageLogged super_messageLogged}
   :init init
   :state monitor))

(defn- -init
  [monitor]
  [[] monitor])

(defn -messageLogged
  [this build-event]
  (let [message (-> build-event .getMessage .trim)]
    (.subTask (.monitor this) message)
    ;(println "LabreplAntLogger: monitor " (.monitor this) " message " message)
    (.worked (.monitor this) 1))
  (.super_messageLogged this build-event))
