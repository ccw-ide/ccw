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
                            ClojureEditorMessages
                            IHyperlinkConstants
                            ClojureHyperlink]
    [ccw.debug              ClojureClient]
    [ccw                    ClojureCore])
  (:gen-class
    :extends ccw.editors.antlrbased.AbstractHyperlinkDetector
    :implements [org.eclipse.jface.text.hyperlink.IHyperlinkDetector]
    ;:constructors {[]}
    ;:init init
    :state state))

(def *ID* (IHyperlinkConstants/ClojureHyperlinkDetector_ID)) 
(def *TARGET_ID* (IHyperlinkConstants/ClojureHyperlinkDetector_TARGET_ID))  

(defn editor [this] (.getClassAdapter this AntlrBasedClojureEditor))

(defn find-decl [sym editor]
  (let [split (.split sym "/")
        n (when (= 2 (count split)) (aget split 0))
        s (aget split (if (= 2 (count split)) 1 0))  
        declaring-ns (.getDeclaringNamespace editor)
        command (String/format "(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\" \"%s\")"
                  (into-array Object [s declaring-ns n]))
        clojure-client (ClojureClient/newClientForActiveRepl)]
    (if-not clojure-client
      (do
        (.setStatusLineErrorMessage editor ClojureEditorMessages/You_need_a_running_repl)
        nil)
      (let [result2 (.remoteLoadRead clojure-client command)
            result (result2 "response")]
        (if (or (nil? result) (.isEmpty result) (= -1 (result2 "response-type")))
          (do
            (.setStatusLineErrorMessage editor ClojureEditorMessages/Cannot_find_declaration)
            nil)
          {"file" (nth result 0) "line" (Integer/valueOf (nth result 1)) "ns" (nth result 3)})))))

(defn detect-hyperlinks
  [offset editor]
  (let [rloc (-> editor .getParsed lu/parsed-root-loc)
        l (lu/loc-for-offset rloc offset)]
    (when-let [{:strs #{ns file line}} (and (= :symbol (-> l z/node :tag)) ; TODO transform :strs -> :keys
                                         (find-decl (lu/loc-text l) editor))]
      [{:offset (lu/start-offset l) :length (-> l z/node :count)
        :open #(ccw.ClojureCore/openInEditor ns file line)}])))

(defn -detectHyperlinks
  [this textViewer region canShowMultipleHyperlinks?]
    (when-let [hyperlinks (detect-hyperlinks (.getOffset region) (editor this))] 
      (into-array IHyperlink (map (fn [{:keys #{offset length open}}] 
                                    (ClojureHyperlink. 
                                      (Region. offset length) 
                                      open))
                               hyperlinks))))
