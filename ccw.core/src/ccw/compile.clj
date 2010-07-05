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
       'ccw.editors.antlrbased.PareditAutoEditStrategy
       'ccw.editors.antlrbased.ClojureFormat
       'ccw.editors.antlrbased.StacktraceHyperlink
       'ccw.editors.antlrbased.ExpandSelectionUpAction
       'ccw.editors.antlrbased.ExpandSelectionLeftAction
       'ccw.editors.antlrbased.ExpandSelectionRightAction
       'ccw.editors.antlrbased.RaiseSelectionAction
       'ccw.editors.antlrbased.IndentSelectionAction
       'ccw.editors.antlrbased.SplitSexprAction
       'ccw.editors.antlrbased.JoinSexprAction
       'ccw.editors.antlrbased.SwitchStructuralEditionModeAction])))

