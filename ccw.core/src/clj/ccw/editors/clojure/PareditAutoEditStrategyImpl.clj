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
(def unstrict-commands
  {"\t" :paredit-indent
   "\n" :paredit-newline
   "\r\n" :paredit-newline})

(def strict-commands
  {"'" :paredit-wrap-quote
   "(" :paredit-open-round
   "[" :paredit-open-square
   "{" :paredit-open-curly
   ")" :paredit-close-round
   "]" :paredit-close-square
   "}" :paredit-close-curly
   "\"" :paredit-doublequote
   "\t" :paredit-indent
   "\n" :paredit-newline
   "\r\n" :paredit-newline
   "\u007F" :paredit-forward-delete
   "\b" :paredit-backward-delete})
 
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

(defn par-command [#^IClojureEditor editor command]
  (let [mode-commands (let [mode (:mode @(.getState editor))]
                        (cond
                          (= mode :struct) strict-commands
                          (= mode :paredit) strict-commands
                          :else unstrict-commands))
        par-command (mode-commands (:text command))
        enabled (if-let [pref (*configuration-based-commands* par-command)]
                  (support/boolean-ccw-pref pref)
                  true)]
    (when enabled par-command)))

(defn- paredit-args [editor command document-text]
  (let [command (if (and (= "" (:text command)) (= 1 (:length command)))
                  (if (= (:offset command) (:caret-offset document-text))
                    (assoc command :text "\u007F")
                    (assoc command :text "\b" :offset (inc (:offset command))))
                  command)]
    (when-let [par-command (par-command editor command)]
      [par-command
       {:text (:text document-text)
        :offset (:offset command)
        :length (:length command)}])))

(defn paredit-options [command]
  (when-let [options-prefs (command-preferences command)]
    (mapcat (fn [[k pref]] [k (support/boolean-ccw-pref pref)]) 
            options-prefs)))
        
(defn customizeDocumentCommand 
  [^PareditAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (let [^IClojureEditor editor (-> this .state deref :editor)]
    (when (and (.doit command) 
               (not (:esc @(.getState editor))) 
               (.isStructuralEditionPossible editor))
      (let [signed-selection (bean (.getSignedSelection editor))
            document-text {:text (.get document) 
                           :caret-offset (+ (:offset signed-selection) (:length signed-selection)) 
                           :selection-length (:length signed-selection)}
            par-command {:text (.text command) :offset (.offset command) :length (.length command)}
            [par-command par-text] (paredit-args editor par-command document-text)
            result (and 
                     par-command
                     (apply paredit
                            par-command
                            (.getParseState editor)
                            par-text
                            (paredit-options par-command)))]
        (support/apply-modif! editor command result)))))
