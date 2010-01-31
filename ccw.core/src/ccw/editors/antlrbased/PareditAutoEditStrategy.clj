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

(defn paredit-enabled?
  [#^org.eclipse.jface.preference.IPreferenceStore prefs-store]
  (.getBoolean prefs-store ccw.preferences.PreferenceConstants/ACTIVATE_PAREDIT))

(defn -customizeDocumentCommand 
  [#^IAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (when (paredit-enabled? (-> this .state deref :prefs-store))
        (and (.doit command)
             (= 0 (.length command))
             (contains? *one-char-command* (.text command)))
    (let [result (paredit (get *one-char-command* (.text command))
                          {:text (.get document) 
                           :offset (.offset command) 
                           :length 0})]
      (when (not= :ko (-> result :parser-state))
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