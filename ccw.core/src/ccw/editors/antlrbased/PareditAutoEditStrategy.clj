(ns ccw.editors.antlrbased.PareditAutoEditStrategy
  (:use [paredit [core :only [paredit]]])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand]
    [ccw.editors.antlrbased AntlrBasedClojureEditor])
  (:gen-class
   :implements [org.eclipse.jface.text.IAutoEditStrategy]
   :constructors {[ccw.editors.antlrbased.AntlrBasedClojureEditor org.eclipse.jface.preference.IPreferenceStore] []}
   :init init
   :state state))
   
#_(set! *warn-on-reflection* true)

(defn- -init
  [editor preference-store] [[] (ref {:editor editor :prefs-store preference-store})])   

; TODO move this into paredit itself ...
(def *one-char-command* 
  {"(" :paredit-open-round 
   "[" :paredit-open-square
   "{" :paredit-open-curly
   ")" :paredit-close-round
   "]" :paredit-close-square
   "}" :paredit-close-curly
   "\"" :paredit-doublequote
   "\t" :paredit-indent-line
   "\n" :paredit-newline
   })

(defn- call-paredit [command document-text]
  (cond
    (and (zero? (:length command)) ; TODO enhance to also handle the replace of a bunch of text
         (contains? *one-char-command* (:text command)))
      (paredit (get *one-char-command* (:text command))
               {:text (:text document-text) 
                :offset (:offset command) 
                :length 0})
    (and (zero? (-> command #^String (:text) .length))
         (= 1 (:length command)))
      (let [paredit-command (if (= (:offset command) (:caret-offset document-text)) 
                              :paredit-forward-delete :paredit-backward-delete)]
        (paredit paredit-command
                 {:text (:text document-text)
                  :offset (if (= paredit-command :paredit-backward-delete) (inc (:offset command)) (:offset command))
                  :length 1}))))


(defn paredit-enabled?
  [#^org.eclipse.jface.preference.IPreferenceStore prefs-store]
  (.getBoolean prefs-store ccw.preferences.PreferenceConstants/ACTIVATE_PAREDIT))

(defn -customizeDocumentCommand 
  [#^ccw.editors.antlrbased.PareditAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (when (and (paredit-enabled? (-> this .state deref :prefs-store))
             (.doit command))
    (let [signed-selection (bean (-> this .state deref #^ccw.editors.antlrbased.AntlrBasedClojureEditor (:editor) .getSignedSelection))
          document-text {:text (.get document) :caret-offset (+ (:offset signed-selection) (:length signed-selection)) :selection-length (:length signed-selection)}
          par-command {:text (.text command) :offset (.offset command) :length (.length command)}
          result (call-paredit par-command document-text)]
      (when (and result (not= :ko (-> result :parser-state)))
        (if-let [modif (-?> result :modifs first)]
          (do
            (set! (.offset command) (-> result :modifs first :offset))
            (set! (.length command) (-> result :modifs first :length))
            (set! (.text command) (-> result :modifs first :text))
            (doseq [{:keys [offset length text]} (rest (-> result :modifs))]
              (.addCommand command offset length text nil)))
          (do
            (set! (.offset command) (:offset result))
            (set! (.length command) 0)
            (set! (.text command) "")
            (set! (.doit command) false)))
        (set! (.shiftsCaret command) false)
        (set! (.caretOffset command) (:offset result))))))