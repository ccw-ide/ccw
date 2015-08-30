(ns ccw.editors.clojure.code-context-information
  (:require [ccw.api.parse-tree :as parse-tree]
            [ccw.editors.clojure.editor-common :as common :refer (find-var-metadata)]
            [ccw.api.content-assist :as api]))

(defn compute-context-information
  ([editor, call-symbol]
    (let [call-metadata (find-var-metadata (.findDeclaringNamespace editor) 
                          (.getCorrespondingREPL editor)
                          call-symbol)]
      (when-let [message (common/context-message call-symbol call-metadata)]
        {:information-display message})))
  ([editor, text-viewer, new-offset]
    (when-let [loc (parse-tree/call-context-loc (-> text-viewer .getParseState :parse-tree) new-offset)]
      (let [call-symbol (parse-tree/call-symbol loc)]
        (compute-context-information editor call-symbol)))))

(defn start []
  (api/register-context-information-provider!
    {:label "Clojure Code" :provider #'compute-context-information}))
