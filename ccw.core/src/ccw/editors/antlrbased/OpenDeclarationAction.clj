;*******************************************************************************
;* Copyright (c) 2010 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Tuomas KARKKAINEN - initial Java Implementation
;*    Laurent PETIT - clojure reimplementation (using parsley instead of antlr)
;*******************************************************************************/
(ns ccw.editors.antlrbased.OpenDeclarationAction
  (:require [clojure.zip :as z]
            [paredit.loc-utils :as lu]
            [ccw.editors.antlrbased.ClojureHyperlink]
            [ccw.editors.antlrbased.ClojureHyperlinkDetector :as hlu]) ; hlu as HyperLinkUtil
  (:import [java.util Arrays HashMap List Map]
           [ccw.editors.antlrbased ClojureEditorMessages
                                   AntlrBasedClojureEditor
                                   IHyperlinkConstants]
           [org.eclipse.jface.action Action] 
           [ccw ClojureCore]
           [ccw.debug ClojureClient]
           [clojure.lang PersistentArrayMap])
  (:gen-class
    :extends org.eclipse.jface.action.Action
    :constructors {[ccw.editors.antlrbased.AntlrBasedClojureEditor] [String]}
    :init init
    :post-init post-init
    :state state))

(def *ID* (IHyperlinkConstants/OpenDeclarationAction_ID))

(defn- -init
  [#^AntlrBasedClojureEditor editor]
  [[ClojureEditorMessages/OpenDeclarationAction_label] (ref {:editor editor})])   

(defn- -post-init
  [#^ccw.editors.antlrbased.OpenDeclarationAction this editor]
  (.setEnabled this true))

(defn editor [this] (-> this .state deref :editor))

(defn -run
  [this]
  (let [editor (editor this)
        caret-offset (-> editor (.getUnSignedSelection (.sourceViewer editor)) .getOffset)]
    (if-let [[{open :open}] (hlu/detect-hyperlinks caret-offset editor)]
      (open))))
