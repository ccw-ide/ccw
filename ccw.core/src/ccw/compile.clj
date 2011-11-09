;*******************************************************************************
;* Copyright (c) 2010 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Laurent PETIT - initial API and implementation
;*******************************************************************************/
(ns ccw.compile)

(defn all []
  (dorun   
    (map
      compile
      ['ccw.reload-clojure
       'ccw.ClojureProjectNature
       'ccw.debug.clientrepl
       'ccw.debug.serverrepl
       'ccw.editors.clojure.PareditAutoEditStrategyImpl
       'ccw.editors.clojure.ClojureFormat
       'ccw.editors.clojure.StacktraceHyperlink
       'ccw.editors.clojure.EditorSupport
       'ccw.editors.clojure.ClojureHyperlinkDetector
       'ccw.editors.clojure.ClojureHyperlink])))

