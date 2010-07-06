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
;  each command : trigger-str  [:paredit-command-name only-one-char-command?]
(def *commands* 
  {"(" [:paredit-open-round true]
   "[" [:paredit-open-square true]
   "{" [:paredit-open-curly true]
   ")" [:paredit-close-round true]
   "]" [:paredit-close-square true]
   "}" [:paredit-close-curly true]
   "\"" [:paredit-doublequote true]
   "\t" [:paredit-indent-line true]
   "\n" [:paredit-newline true]
   })

(def *strict-commands*
  #{:paredit-close-round, :paredit-close-square, :paredit-close-curly, 
    :paredit-forward-delete, :paredit-backward-delete})

(def 
  #^{:doc "{:command configuration-key ...}"} 
  *configuration-based-commands*
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

(defn do-command?
  "Will do command if it is :strict and the editor allows it, or if it is not :strict"
  [#^AntlrBasedClojureEditor editor par-command]
  (println (str "do-command? : '" par-command "'"))
  (cond
    (*strict-commands* par-command)
      (.useStrictStructuralEditing editor)
    (*configuration-based-commands* par-command) ; works because I know no value can be nil in *configuration-based-commands* 
      (.getBooleanPreference editor (*configuration-based-commands* par-command))
    :else 
      true))

(defn -customizeDocumentCommand 
  [#^ccw.editors.antlrbased.PareditAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  #_(println "Called!")
  (println "doit?" (.doit command))
  (when (and (.doit command) (not (-> this .state deref #^ccw.editors.antlrbased.AntlrBasedClojureEditor (:editor) .isInEscapeSequence)))
    (let [signed-selection (bean (-> this .state deref #^ccw.editors.antlrbased.AntlrBasedClojureEditor (:editor) .getSignedSelection))
           #__ #_(println (str "signed-selection:" signed-selection))
          document-text {:text (.get document) 
                         :caret-offset (+ (:offset signed-selection) (:length signed-selection)) 
                         :selection-length (:length signed-selection)}
          par-command {:text (.text command) :offset (.offset command) :length (.length command)}
          _ (println (str "par-command:" par-command))
          [par-command par-text] (paredit-args par-command document-text)
          _ (println "here is the par-command:" par-command)
          result (and 
                   par-command 
                   (do-command? (-> this .state deref :editor) par-command)
                   (paredit par-command par-text))]
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
        (set! (.caretOffset command) (:offset result))
        (when-not (zero? (:length result)) 
          #_(println (str "result:" result))
          (.selectAndReveal (-> this .state deref :editor) (:offset result) (:length result))))
      (.setStructuralEditingPossible (-> this .state deref :editor) (true? (and result (not= :ko (-> result :parser-state))))))))