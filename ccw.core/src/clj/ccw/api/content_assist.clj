(ns ccw.api.content-assist
  (:require
    [ccw.api.schema.content-assist :refer :all]
    [ccw.editors.clojure.clojure-proposal-processor :as cpp]
    [clojure.string :as str]
    [clojure.test :as test]
    [schema.core :as s]))

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

(s/defn ^:always-validate register-completion-proposal-provider!
  [provider :- CompletionProposalProvider]
  (swap! cpp/completion-proposal-providers conj provider))

;(c/register-content-assist-processor!
;  {:id               :completions.code
;   :label            "Clojure Code Content Assist"
;   :display-strategy :replace ; only strategy available yet. Default content assist will be replaced by this one if :trigger-fn returns true
;   :trigger-fn       #'find-suggestions
;   :proposals-fn     (constantly true)
;   })

(s/defn ^:always-validate register-context-information-provider!
  [provider :- ContextInformationProvider]
  (swap! cpp/context-information-providers conj provider))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; General purpose Utility fn for content assist

;; TODO find the prefix via the editor's parse tree !
(defn prefix-info 
  [ns offset ^String prefix]
  (let [[n1 n2] (str/split prefix #"/")
        [n1 n2] (cond
                  (and (nil? n2)
                       (not (.endsWith prefix "/"))) [n2 n1]
                  :else [n1 n2])]
    {:curent-namespace ns
     :offset offset
     :prefix prefix
     :namespace n1 
     :prefix-name n2}))

;; TODO find the prefix via the editor's parse tree !
(defn invalid-symbol-char? [^Character c]
  (let [invalid-chars
        #{ \(, \), \[, \], \{, \}, \', \@,
          \~, \^, \`, \#, \" }]
    (or (Character/isWhitespace c)
        (invalid-chars c))))

;; TODO find the prefix via the editor's parse tree !
(test/deftest test-invalid-symbol-char?
  (test/are [char] (invalid-symbol-char? char)
            \(, \newline, \@, \space)
  (test/are [char] (not (invalid-symbol-char? char))
            \a, \-))

;; TODO find the prefix via the editor's parse tree !
(defn compute-prefix-offset
  [^String string offset]
  (if-let [start (some
                   #(when (invalid-symbol-char? (.charAt string %)) %)
                   (range (dec offset) -1 -1))]
    (inc start)
    0))

;; TODO find the prefix via the editor's parse tree !
(test/deftest test-compute-prefix-offset
  (test/are [result string offset] 
            (= result (compute-prefix-offset string offset))
    0 ""       0
    0 "a"      0
    0 "a"      1
    0 "abc"    2
    0 " abc"   0
    1 " abc"   1
    1 " abc"   2
    1 " abc"   4
    1 " .abc"  4
    1 "\n.abc" 4))
