(ns ccw.editors.clojure.code-content-assist
  (:require [ccw.api.parse-tree :as parse-tree]
            [ccw.editors.clojure.editor-common :as common :refer (find-var-metadata)]
            [ccw.editors.clojure.clojure-proposal-processor :as cpp])
  #_(:require [ccw.api.content-assist :as c]))

;(defn namespace-processor-predicate
;  "Return true if the current insertion-offset / clojure-editor context
;   is relevant for namespace completion"
;  [clojure-editor prefix-offset prefix]
;  true
;  #_(when-let [loc (some-> clojure-editor .getParseTree p/root-loc (l/loc-for-offset prefix-offset))]
;     (let [loc (if (= :quote (l/loc-type loc)) (l/loc-parent loc) loc)]
;       (and (= :list (l/loc-type loc)) (#{"require" "use" "in-ns"} (-> loc l/loc-children (get 1) l/loc-text))))))
;
;suggestions:
;{:keys
; :completion
; :match
; :filter
; :type
; :ns
; :metadata {:arglists name added static doc line file}
; }
;
;(defn find-suggestions
;  "List all namespaces. This can be a lazy seq, which will or will not be
;   entirely consumed depending on processor configuration"
;  [clojure-editor prefix-offset prefix]
;  [{:replacement "clojure.core"}
;   {:replacement "clojure.pprint"}])
;
;(c/register-content-assist-processor!
;  {:id               :completions.code
;   :label            "Clojure Code Content Assist"
;   :display-strategy :replace ; only strategy available yet. Default content assist will be replaced by this one if :trigger-fn returns true
;   :trigger-fn       #'find-suggestions
;   :proposals-fn     (constantly true)
;   })
;

(defn compute-context-information
  [editor, text-viewer, new-offset]
  (when-let [loc (parse-tree/call-context-loc (-> text-viewer .getParseState :parse-tree) new-offset)]
    (let [call-symbol (parse-tree/call-symbol loc)
          call-metadata (find-var-metadata (.findDeclaringNamespace editor) 
                          (.getCorrespondingREPL editor)
                          call-symbol)]
      (when-let [message (common/context-message call-symbol call-metadata)]
        {:information-display message}))))

(defn start []
  (cpp/register-context-information-provider! {:label "Clojure Code" :provider #'compute-context-information}))
