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

(defn par-command? [command] (contains? *commands* (:text command)))
(defn par-command [command] (-> *commands* (get (:text command)) first))
(defn one-char-par-command? [command] (-> *commands* (get (:text command)) second))

(defn- call-paredit [command document-text]
  (cond
    (and (zero? (-> command #^String (:text) .length))
         (= 1 (:length command)))
      (let [paredit-command (if (= (:offset command) (:caret-offset document-text)) 
                              :paredit-forward-delete :paredit-backward-delete)]
        (paredit paredit-command
                 {:text (:text document-text)
                  :offset (if (= paredit-command :paredit-backward-delete) (inc (:offset command)) (:offset command))
                  :length 1}))
    (and (par-command? command)
         (one-char-par-command? command)) ; TODO enhance to also handle the replace of a bunch of text
      (paredit (par-command command)
               {:text (:text document-text) 
                :offset (:offset command) 
                :length (:length command)})))

(defn paredit-enabled?
  [#^org.eclipse.jface.preference.IPreferenceStore prefs-store]
  (.getBoolean prefs-store ccw.preferences.PreferenceConstants/ACTIVATE_PAREDIT))

(defn -customizeDocumentCommand 
  [#^ccw.editors.antlrbased.PareditAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (when (and (paredit-enabled? (-> this .state deref :prefs-store))
             (.doit command))
    (let [signed-selection (bean (-> this .state deref #^ccw.editors.antlrbased.AntlrBasedClojureEditor (:editor) .getSignedSelection))
          _ (println (str "signed-selection:" signed-selection))
          document-text {:text (.get document) 
                         :caret-offset (+ (:offset signed-selection) (:length signed-selection)) 
                         :selection-length (:length signed-selection)}
          par-command {:text (.text command) :offset (.offset command) :length (.length command)}
          _ (println (str "par-command:" par-command))
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
        (set! (.caretOffset command) (:offset result))
        (when-not (zero? (:length result)) 
          (println (str "result:" result))
          (.selectAndReveal (-> this .state deref :editor) (:offset result) (:length result)))))))