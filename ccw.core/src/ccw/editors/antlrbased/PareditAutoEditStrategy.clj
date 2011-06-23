(ns ccw.editors.antlrbased.PareditAutoEditStrategy
  (:use [paredit [core :only [paredit]]])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand]
    [ccw.editors.antlrbased IClojureEditor])
  (:gen-class
   :implements [org.eclipse.jface.text.IAutoEditStrategy]
   :constructors {[ccw.editors.antlrbased.IClojureEditor org.eclipse.jface.preference.IPreferenceStore] []}
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
   "\r\n" [:paredit-newline true]
   })

(def *strict-commands*
  #{:paredit-close-round, :paredit-close-square, :paredit-close-curly, 
    :paredit-forward-delete, :paredit-backward-delete,
    :paredit-open-round, :paredit-open-square, :paredit-open-curly,
    :paredit-doublequote})

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

(defn- ccw-prefs
  []
  (.getCombinedPreferenceStore (ccw.CCWPlugin/getDefault)))

(defn do-command?
  "Will do command if it is :strict and the editor allows it, or if it is not :strict"
  [#^IClojureEditor editor par-command]
  ;(println (str "do-command? : '" par-command "'"))
  (cond
    (*strict-commands* par-command)
      (.isStructuralEditingEnabled editor)
    (*configuration-based-commands* par-command) ; works because I know no value can be nil in *configuration-based-commands*
      (.getBoolean (ccw-prefs) (*configuration-based-commands* par-command))
    :else 
      true))

(defn -customizeDocumentCommand 
  [#^ccw.editors.antlrbased.PareditAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (let [^IClojureEditor editor (-> this .state deref :editor)]
    (when (and (.doit command) 
               (not (.isInEscapeSequence editor)) 
               (.isStructuralEditionPossible editor))
      ;(println "yo!")
      (let [signed-selection (bean (.getSignedSelection editor))
            ;_ (println (str "signed-selection:" signed-selection))
            document-text {:text (.get document) 
                           :caret-offset (+ (:offset signed-selection) (:length signed-selection)) 
                           :selection-length (:length signed-selection)}
            ;_ (println "document-text:" document-text)
            par-command {:text (.text command) :offset (.offset command) :length (.length command)}
            ;_ (println (str "par-command:" par-command))
            [par-command par-text] (paredit-args par-command document-text)
            ;_ (println "here is the par-command:" par-command)
            ;_ (println "do-command?" (do-command? editor par-command))
            result (and 
                     par-command 
                     (do-command? editor par-command)
                     (paredit par-command (.getParseTree editor) par-text))
            ;_ (println "result:" result)
            ]
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
            ;(println (str "result:" result))
          (.selectAndReveal editor (:offset result) (:length result))))
        #_(.setStructuralEditingPossible editor (true? (and result (not= :ko (-> result :parser-state)))))))))