(ns ccw.editors.antlrbased.PareditAutoEditStrategy
  (:use [paredit [core :only [paredit]]])  
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand])
  (:gen-class
   :implements [org.eclipse.jface.text.IAutoEditStrategy]
   :init init
   :state state))
   
(defn- -init
  [] [[] (ref {})])   

(defn -customizeDocumentCommand 
  [#^IAutoEditStrategy this, #^IDocument document, #^DocumentCommand command]
  (when (.doit command)
    (if (and (= 0 (.length command))
             (= "(" (.text command)))
      (let [result (paredit :paredit-open-round {:text (.get document) :offset (.offset command) :length 0})]
        #_(println "result:" result)
        (set! (.offset command) 0)
        (set! (.length command) (.length (.get document)))
        (set! (.text command) (:text result))
        (set! (.caretOffset command) (:offset result))))))