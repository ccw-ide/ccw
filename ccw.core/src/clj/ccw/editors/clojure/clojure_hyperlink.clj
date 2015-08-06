(ns ccw.editors.clojure.clojure-hyperlink
  (:require [clojure.zip :as z]
            [clojure.string :as str]
            [paredit.loc-utils :as lu]
            [clojure.tools.nrepl :as repl]
            [ccw.editors.clojure.editor-support :as ed])
  (:import  [org.eclipse.jface.text BadLocationException] 
            [ccw.editors.clojure IClojureEditor
                                 ClojureEditorMessages]
            [ccw                 ClojureCore]
            [ccw.repl IConnectionClient]))

(defn find-decl [^String sym ^IClojureEditor editor]
  (let [split (.split sym "/")
        n (when (= 2 (count split)) (aget split 0))
        s (aget split (if (= 2 (count split)) 1 0))  
        declaring-ns (.findDeclaringNamespace editor)
        command (String/format "(ccw.debug.serverrepl/find-symbol \"%s\" \"%s\" \"%s\")"
                  (into-array Object [s declaring-ns n]))
        safeConnection (some-> editor .getCorrespondingREPL .getSafeToolingConnection)]
    
    (if-not safeConnection
      (do
        (.setStatusLineErrorMessage editor ClojureEditorMessages/You_need_a_running_repl)
        nil)
      (let [[ [file ^String line _ ns] ] (.withConnection safeConnection
                                           (reify IConnectionClient
                                             (withConnection [this connection]
                                               (let [client (.client connection)]
                                                 (repl/response-values (repl/message client {:op :eval :code command})))))
                                           1000)]
        (if (every? str/blank? [file line ns])
          (do
            (.setStatusLineErrorMessage editor ClojureEditorMessages/Cannot_find_declaration)
            nil)
          {"file" file
           "line" (Integer/valueOf line)
           "ns" ns})))))

(defn detect-hyperlinks
  [[offset length] ^IClojureEditor editor]
  (let [rloc (-> editor .getParseState ed/getParseTree lu/parsed-root-loc)
        l (lu/loc-for-offset rloc offset)]
    (when-let [{:strs #{ns file line}} (and (= :symbol (-> l z/node :tag)) ; TODO transform :strs -> :keys
                                         (find-decl (lu/loc-text l) editor))]
      [{:region [(lu/start-offset l) (-> l z/node :count)]
        :open #(ccw.ClojureCore/openInEditor ns file line)}])))
