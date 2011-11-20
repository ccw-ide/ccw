(ns ccw.editors.clojure.clojure-hyperlink-detector
  (:require [clojure.zip :as z]
            [paredit.loc-utils :as lu]
            [clojure.tools.nrepl :as repl]
            [ccw.editors.clojure.editor-support :as editor]
            [ccw.editors.clojure.hyperlink :as hyperlink])
  (:use clojure.contrib.core)
  (:import 
    [org.eclipse.jface.text BadLocationException
                            IRegion
                            Region
                            ITextViewer] 
    [org.eclipse.jface.text.hyperlink IHyperlink
                                      IHyperlinkDetector]
    [ccw.editors.clojure IClojureEditor
                         ClojureEditorMessages
                         IHyperlinkConstants
                         AbstractHyperlinkDetector]
    [ccw.debug           ClojureClient]
    [ccw                 ClojureCore]))

(def *ID* (IHyperlinkConstants/ClojureHyperlinkDetector_ID)) 
(def *TARGET_ID* (IHyperlinkConstants/ClojureHyperlinkDetector_TARGET_ID))  

(defn editor [this] (.getClassAdapter this IClojureEditor))

(defn find-decl [sym editor]
  (let [split (.split sym "/")
        n (when (= 2 (count split)) (aget split 0))
        s (aget split (if (= 2 (count split)) 1 0))  
        declaring-ns (.findDeclaringNamespace editor)
        command (String/format "(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\" \"%s\")"
                  (into-array Object [s declaring-ns n]))
        {:keys [send]} (-?> editor .getCorrespondingREPL .getToolingConnection .conn)]
    (if-not send
      (do
        (.setStatusLineErrorMessage editor ClojureEditorMessages/You_need_a_running_repl)
        nil)
      (let [[ [file line _ ns] ] (repl/response-values (send command))]
        (if (and file line ns)
          {"file" file
           "line" (Integer/valueOf line)
           "ns" ns}
          (do
            (.setStatusLineErrorMessage editor ClojureEditorMessages/Cannot_find_declaration)
            nil))))))

(defn detect-hyperlinks
  [offset editor]
  (let [rloc (-> editor .getParseState (editor/getParseTree) lu/parsed-root-loc)
        l (lu/loc-for-offset rloc offset)]
    (when-let [{:strs #{ns file line}} (and (= :symbol (-> l z/node :tag)) ; TODO transform :strs -> :keys
                                         (find-decl (lu/loc-text l) editor))]
      [{:offset (lu/start-offset l) :length (-> l z/node :count)
        :open #(ccw.ClojureCore/openInEditor ns file line)}])))

(defn factory [ _ ]
  (proxy [AbstractHyperlinkDetector]
         []
    (detectHyperlinks [textViewer region canShowMultipleHyperlinks?]
      (when-let [hyperlinks (detect-hyperlinks (.getOffset region) (editor this))] 
        (into-array IHyperlink (map (fn [{:keys #{offset length open}}] 
                                      (hyperlink/make 
                                        (Region. offset length) 
                                        open))
                                    hyperlinks))))))