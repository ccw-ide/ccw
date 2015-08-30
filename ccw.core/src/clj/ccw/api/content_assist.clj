(ns ccw.api.content-assist
  (:require
    [ccw.api.schema.content-assist :refer :all]
    [ccw.editors.clojure.clojure-proposal-processor :as cpp]
    [schema.core :as s]))

(s/defn ^:always-validate register-completion-proposal-provider!
  [provider :- CompletionProposalProvider]
  (swap! cpp/completion-proposal-providers conj provider))

(s/defn ^:always-validate register-context-information-provider!
  [provider :- ContextInformationProvider]
  (swap! cpp/context-information-providers conj provider))

;(defn namespace-processor-predicate
;  "Return true if the current insertion-offset / clojure-editor context
;   is relevant for namespace completion"
;  [clojure-editor insertion-offset]
;  ;; for instance, check that we're in a form which is a list and whose first
;  ;; child is a 'in-ns 'use or 'require symbol
;  
;  true)
;
;(defn list-namespaces
;  "List all namespaces. This can be a lazy seq, which will or will not be
;   entirely consumed depending on processor configuration"
;  [clojure-editor prefix]
;  [{:replacement "foo.bar"}
;   {:replacement "foo.core.bleh"}])
;
;(c/register-content-assist-processor!
;  {:id               :completions.namespace
;   :label            "Available namespaces"
;   :display-strategy :replace ; default strategy is replace means if trigger-fn returns true, let's replace all other installed predicates. If several processors
;                              ; are active, each one will have its own list, interchangeable via repeated invocation of Ctrl+Space
;                              ; other display-strategies: :append, :prepend, :separate, :mixed-in
;   :trigger-fn       #'namespace-processor-predicate
;   :proposals-fn     #'list-namespaces
;   :context-information-fn .....
;   ; optional :prefix-fn        c/clojure-code-prefix-fn
;   })
;
