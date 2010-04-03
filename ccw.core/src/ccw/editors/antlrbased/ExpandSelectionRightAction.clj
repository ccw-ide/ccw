(ns ccw.editors.antlrbased.ExpandSelectionRightAction 
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

(def *ID* "ExpandSelectionRightAction")

#_(set! *warn-on-reflection* true)

(defn- -init
  [#^AntlrBasedClojureEditor editor] [[ClojureEditorMessages/ExpandSelectionRightAction_label] (ref {:editor editor})])  

(defn- -post-init
  [#^ccw.editors.antlrbased.ExpandSelectionRightAction this editor]
  (.setEnabled this true))

(defn -run
  [#^ccw.editors.antlrbased.ExpandSelectionRightAction this]
  (let [editor #^AntlrBasedClojureEditor (:editor @(.state this))
        {:keys #{length offset}} (bean (.getUnSignedSelection editor))
        text  (.get (.getDocument #^AntlrBasedClojureEditor editor))
        {:keys #{length offset}} (paredit :paredit-expand-right {:text text :offset offset :length length})]
    (.selectAndReveal editor offset length)))