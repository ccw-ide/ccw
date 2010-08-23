(ns ccw.editors.antlrbased.ClojureHyperlinkDetector
  (:require [clojure.zip :as z]
            [paredit.loc-utils :as lu]
            [ccw.editors.antlrbased.ClojureHyperlink])
  (:import 
    [org.eclipse.jface.text BadLocationException
                            IRegion
                            Region
                            ITextViewer] 
    [org.eclipse.jface.text.hyperlink AbstractHyperlinkDetector
                                      IHyperlink
                                      IHyperlinkDetector]
    [ccw.editors.antlrbased AntlrBasedClojureEditor
                            IHyperlinkConstants
                            ClojureHyperlink])
  (:gen-class
    :extends ccw.editors.antlrbased.AbstractHyperlinkDetector
    :implements [org.eclipse.jface.text.hyperlink.IHyperlinkDetector]
    ;:constructors {[]}
    ;:init init
    :state state))

(def *ID*  IHyperlinkConstants/ClojureHyperlinkDetector_ID)
(def *TARGET_ID* IHyperlinkConstants/ClojureHyperlinkDetector_TARGET_ID)  

(defn editor [this] (.getClassAdapter this AntlrBasedClojureEditor))

(defn -detectHyperlinks
  [this textViewer region canShowMultipleHyperlinks?]
  (let [rloc (-> this editor .getParsed lu/parsed-root-loc)
        l (lu/loc-for-offset rloc (.getOffset region))]
    (when (= :symbol (-> l z/node :tag))
      (into-array 
        [(ClojureHyperlink. (Region. (lu/start-offset l) (-> l z/node :count)) 
                            nil)]))))
