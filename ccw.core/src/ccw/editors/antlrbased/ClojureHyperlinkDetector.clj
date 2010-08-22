(ns ccw.editors.antlrbased.ClojureHyperlinkDetector
  (:import 
    [org.eclipse.jface.text BadLocationException
                            IRegion
                            ITextViewer] 
    [org.eclipse.jface.text.hyperlink AbstractHyperlinkDetector
                                      IHyperlink
                                      IHyperlinkDetector]
    [ccw.editors.antlrbased AntlrBasedClojureEditor
                            IHyperlinkConstants])
  (:gen-class
    :extends org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
    :implements [org.eclipse.jface.text.hyperlink.IHyperlinkDetector]
    ;:constructors {[]}
    ;:init init
    :state state))

#_(set! *warn-on-reflection* true)

(def *ID*  IHyperlinkConstants/ClojureHyperlinkDetector_ID)
(def *TARGET_ID* IHyperlinkConstants/ClojureHyperlinkDetector_TARGET_ID)  

(defn -detectHyperlinks
  [this ^ITextViewer textViewer ^IRegion region canShowMultipleHyperlinks?]
  (println "asked for hyperlink detection in clojure code !")
  (println "region:" region)
  (try 
    (println "text:" (-> textViewer .getDocument (.get (.getOffset region), (.getLength region)))) 
    (catch BadLocationException e
      ; TODO Auto-generated catch block
      (-> e .printStackTrace)))
  (println ""))
