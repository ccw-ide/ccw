(ns ccw.editors.clojure.PareditAutoEditStrategyImpl
  (:use [paredit [core :only [paredit]]])
  (:use [clojure.core.incubator :only [-?>]])
  (:require [ccw.editors.clojure.paredit-auto-edit-support :as support])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand]
    [ccw.editors.clojure IClojureEditor PareditAutoEditStrategy]))
   
#_(set! *warn-on-reflection* true)

; TODO move this into paredit itself ...
;  each command : trigger-str  [:paredit-command-name only-one-char-command?]
(def ^:dynamic *commands* 
  {"'" [:paredit-wrap-quote true]
   "(" [:paredit-open-round true]
   "[" [:paredit-open-square true]
   "{" [:paredit-open-curly true]
   ")" [:paredit-close-round true]
   "]" [:paredit-close-square true]
   "}" [:paredit-close-curly true]
   "\"" [:paredit-doublequote true]
   "\t" [:paredit-indent true]
   "\n" [:paredit-newline true]
   "\r\n" [:paredit-newline true]
   })

(def ^:dynamic *strict-commands*
  #{:paredit-open-round, :paredit-close-round,
    :paredit-open-square, :paredit-close-square,
    :paredit-open-curly, :paredit-close-curly, 
    :paredit-forward-delete, :paredit-backward-delete,
    :paredit-doublequote})
 
(def
  command-preferences
  "map of command to map of keyword -> associated global preference key"
  {:paredit-newline {:force-two-spaces-indent ccw.preferences.PreferenceConstants/FORCE_TWO_SPACES_INDENT}
   :paredit-indent-line {:force-two-spaces-indent ccw.preferences.PreferenceConstants/FORCE_TWO_SPACES_INDENT}
   :paredit-indent {:force-two-spaces-indent ccw.preferences.PreferenceConstants/FORCE_TWO_SPACES_INDENT}})

(def 
  ^:dynamic *configuration-based-commands*
  "{:command configuration-key ...}"
  {:paredit-indent-line ccw.preferences.PreferenceConstants/USE_TAB_FOR_REINDENTING_LINE})

(defn par-command? [command] (contains? *commands* (:text command)))
(defn par-command [command] (-> *commands* (get (:text command)) first))
(defn one-char-par-command? [command] (-> *commands* (get (:text command)) second))

(defn- paredit-args [command document-text]
  (cond
    (and (zero? (-> command #^String (:text) .length))
         (= 1 (:length command)))
      (let [paredit-command (if (= (:offset command) (:caret-offset document-text)) 
                              :paredit-forward-delete :paredit-backward-delete)]
        [paredit-command
                 {:text (:text document-text)
                  :offset (if (= paredit-command :paredit-backward-delete) (inc (:offset command)) (:offset command))
                  :length 1}])
    (and (par-command? command)
         (one-char-par-command? command)) ; TODO enhance to also handle the replace of a bunch of text
      [(par-command command)
               {:text (:text document-text) 
                :offset (:offset command) 
                :length (:length command)}]))

(defn paredit-options [command]
  (when-let [options-prefs (command-preferences command)]
    (mapcat (fn [[k pref]] [k (support/boolean-ccw-pref pref)]) 
            options-prefs)))

(defn do-command?
  "Will do command if it is :strict and the editor allows it, or if it is not :strict"
  [#^IClojureEditor editor par-command]
  (cond
    (*strict-commands* par-command)
      (.isStructuralEditingEnabled editor)
    (*configuration-based-commands* par-command) ; works because I know no value can be nil in *configuration-based-commands*
      (support/boolean-ccw-pref (*configuration-based-commands* par-command))
    :else 
      true))
        
(defn customizeDocumentCommand 
  [^PareditAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (let [^IClojureEditor editor (-> this .state deref :editor)]
    (when (and (.doit command) 
               (not (.isInEscapeSequence editor)) 
               (.isStructuralEditionPossible editor))
      (let [signed-selection (bean (.getSignedSelection editor))
            document-text {:text (.get document) 
                           :caret-offset (+ (:offset signed-selection) (:length signed-selection)) 
                           :selection-length (:length signed-selection)}
            par-command {:text (.text command) :offset (.offset command) :length (.length command)}
            [par-command par-text] (paredit-args par-command document-text)
            result (and 
                     par-command 
                     (do-command? editor par-command)
                     (apply paredit
                            par-command
                            (.getParseState editor)
                            par-text
                            (paredit-options par-command)))]
        (support/apply-modif! editor command result)))))