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

;; TODO: remove the boilerplate of the Expand* classes by leveraging the Command API of Eclipse
(ns ccw.editors.antlrbased.ExpandSelectionUpAction 
  (:use [paredit [core :only [paredit]]])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand]
    [ccw.editors.antlrbased AntlrBasedClojureEditor
                            SelectionHistory
                            ClojureEditorMessages
                            SourceRange])
  (:gen-class
   :extends org.eclipse.jface.action.Action
   :constructors {[ccw.editors.antlrbased.AntlrBasedClojureEditor ccw.editors.antlrbased.SelectionHistory] [String]}
   :init init
   :post-init post-init
   :state state))

(def *ID* "ExpandSelectionUpAction")

#_(set! *warn-on-reflection* true)

(defn- -init
  [#^AntlrBasedClojureEditor editor #^SelectionHistory selection-history] 
  [[ClojureEditorMessages/ExpandSelectionUpAction_label] (ref {:editor editor :selection-history selection-history})])  

(defn- -post-init
  [#^ccw.editors.antlrbased.ExpandSelectionUpAction this editor #^SelectionHistory selection-history]
  (.setEnabled this true))

(defn -run
  [#^ccw.editors.antlrbased.ExpandSelectionUpAction this]
  (let [editor #^AntlrBasedClojureEditor (:editor @(.state this))
        {:keys #{length offset}} (bean (.getUnSignedSelection editor))
        text  (.get (.getDocument #^AntlrBasedClojureEditor editor))
        {new-length :length new-offset :offset} (paredit :paredit-expand-up {:text text :offset offset :length length})]
    (-> this .state deref :selection-history (.remember (SourceRange. offset length)))
    (try
      (-> this .state deref :selection-history .ignoreSelectionChanges)
      (.selectAndReveal editor new-offset new-length)
      (finally
        (-> this .state deref :selection-history .listenToSelectionChanges)))))