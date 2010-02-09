(ns ccw.editors.antlrbased.PareditAutoEditStrategy
  (:use [paredit [core :only [paredit]]])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand])
  (:gen-class
   :implements [org.eclipse.jface.text.IAutoEditStrategy]
   :constructors {[org.eclipse.jface.preference.IPreferenceStore] []}
   :init init
   :state state))
   
(defn- -init
  [preference-store] [[] (ref {:prefs-store preference-store})])   

; TODO move this into paredit itself ...
(def *one-char-command* 
  {"(" :paredit-open-round 
   "[" :paredit-open-square
   "{" :paredit-open-curly
   ")" :paredit-close-round
   "]" :paredit-close-square
   "}" :paredit-close-curly
   "\"" :paredit-doublequote})

(defn- call-paredit [command document-text]
  (cond
    (and (zero? (:length command))
         (contains? *one-char-command* (:text command)))
      (paredit (get *one-char-command* (:text command))
               {:text document-text 
                :offset (:offset command) 
                :length 0})
    (and (zero? (-> command :text .length))
         (= 1 (:length command)))
      (paredit :paredit-forward-delete ; TODO how to distinguish from forward delete and backward delete ?
               {:text document-text
                :offset (:offset command)
                :length 1})))


(defn paredit-enabled?
  [#^org.eclipse.jface.preference.IPreferenceStore prefs-store]
  (.getBoolean prefs-store ccw.preferences.PreferenceConstants/ACTIVATE_PAREDIT))

(defn -customizeDocumentCommand 
  [#^IAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (when (and (paredit-enabled? (-> this .state deref :prefs-store))
             (.doit command))
    (let [document-text (.get document)
          par-command {:text (.text command) :offset (.offset command) :length (.length command)}
          result (call-paredit par-command document-text)]
      (when (and result (not= :ko (-> result :parser-state)))
        (if-let [modif (-?> result :modifs first)]
          (do
            (set! (.offset command) (-> result :modifs first :offset))
            (set! (.length command) (-> result :modifs first :length))
            (set! (.text command) (-> result :modifs first :text)))
          (do
            (set! (.offset command) (:offset result))
            (set! (.length command) 0)
            (set! (.text command) "")
            (set! (.doit command) false)))
        (set! (.shiftsCaret command) false)
        (set! (.caretOffset command) (:offset result))))))