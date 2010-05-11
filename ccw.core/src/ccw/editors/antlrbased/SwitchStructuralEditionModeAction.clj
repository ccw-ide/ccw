(ns ccw.editors.antlrbased.SwitchStructuralEditionModeAction
  (:use [paredit [core :only [paredit]]])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand]
    [ccw.editors.antlrbased AntlrBasedClojureEditor
                            ClojureEditorMessages])
  (:gen-class
   :extends org.eclipse.jface.action.Action
   :constructors {[ccw.editors.antlrbased.AntlrBasedClojureEditor] [String]}
   :init init
   :post-init post-init
   :state state))
   
#_(set! *warn-on-reflection* true)

(defn- -init
  [#^AntlrBasedClojureEditor editor]
  [[ClojureEditorMessages/SwitchStructuralEditionModeAction_label] (ref {:editor editor})])   

(defn- -post-init
  [#^ccw.editors.antlrbased.SwitchStructuralEditionModeAction this editor]
  (.setEnabled this true))

(defn -run
  [#^ccw.editors.antlrbased.SwitchStructuralEditionModeAction this]
  (let [editor #^AntlrBasedClojureEditor (:editor @(.state this))]
    (.toggleStructuralEditionMode editor)))

