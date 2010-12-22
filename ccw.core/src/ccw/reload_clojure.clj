; *******************************************************************************
; * Copyright (c) 2010 Tuomas KARKKAINEN.
; * All rights reserved. This program and the accompanying materials
; * are made available under the terms of the Eclipse Public License v1.0
; * which accompanies this distribution, and is available at
; * http://www.eclipse.org/legal/epl-v10.html
; *
; * Contributors: 
; *    Tuomas KARKKAINEN - initial API and implementation
; *    Laurent PETIT     - moved namespace
; *******************************************************************************/

(ns ccw.reload-clojure
  (:gen-class
    :init init
    :state state
    :extends org.eclipse.jface.action.Action))

(defn -init [])

(defn reload-rest []
  (require :reload '[ccw
                      [ClojureProjectNature]])
  (require :reload '[ccw.editors.antlrbased
                      [ClojureFormat]
                      [StacktraceHyperlink]
                      [PareditAutoEditStrategy]
                      [SwitchStructuralEditionModeAction]
                      [EditorSupport]
                      [ClojureHyperlinkDetector]
                      [ClojureHyperlink]]))

(defn -run [this]
  (require :reload 'ccw.reload-clojure)
  ; now that reload-rest took a chance to be updated via the reload, call it
  (reload-rest))


