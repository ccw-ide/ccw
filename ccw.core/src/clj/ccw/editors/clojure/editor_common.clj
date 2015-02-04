(ns ccw.editors.clojure.editor-common
  (:require [clojure.string :as s]
            [clojure.test :as test]
            [clojure.tools.nrepl :as repl]
            [paredit.parser :as p]
            [paredit.loc-utils :as lu]
            [clojure.zip :as z]
            [ccw.core.doc-utils :as doc]
            [ccw.debug.serverrepl :as serverrepl]
            [ccw.core.trace :as trace]
            [ccw.editors.clojure.editor-support :as editor])
  (:use [clojure.core.incubator :only [-?>]])
  (:import [org.eclipse.jface.viewers StyledString
                                      StyledString$Styler]
           [org.eclipse.jface.text.contentassist 
            IContentAssistProcessor
            ContentAssistant
            CompletionProposal
            ICompletionProposal
            ICompletionProposalExtension6
            IContextInformation
            IContextInformationExtension
            IContextInformationValidator]
           [org.eclipse.jdt.core JavaCore
                                 IMethod
                                 IType]
           [ccw.editors.clojure IClojureAwarePart
                                IReplAwarePart]
           [clojure.tools.nrepl Connection]))

(defn offset-loc
  "Return the zip loc for offset in part"
  [^IClojureAwarePart part offset]
  (let [rloc (-> part .getParseState (editor/getParseTree) lu/parsed-root-loc)]
    (lu/loc-for-offset rloc offset)))

(defn send-message* 
  "Send the command over the nrepl connection. This version is \"bare\", ie it
   calls into the REPL without timeout protection. If you want to protect the 
   IDE to freeze if e.g. the REPL never times out, call send-message instead."
  [^Connection safe-connection command]
  (try
    (-> safe-connection 
      .getUnsafeConnection
      .client
      (repl/message {"op"   "eval"
                     "code" command})
      repl/response-values)
    (catch Exception e
      (ccw.CCWPlugin/logError (str "exception while sending command " command " to connection " (.getUnsafeConnection safe-connection)) e)
      (when (instance? java.net.SocketException e)
        (.connectionLost safe-connection))
      nil)))

(defn send-message 
  "Same as send-message*, but guarded by a client timeout
   so that Eclipse cannot hang forever.
   timeout in milliseconds"
  [safe-connection command & {:keys [timeout] :or {timeout 4000}}]
  (let [timeout-val (Object.)
        secure-call (future (send-message* safe-connection command))
        result (deref secure-call timeout timeout-val)]
    (if (not= result timeout-val)
      result
      (ccw.CCWPlugin/logError (str
        "timeout while calling command send-message* for command "
        command)))))

(defn parse-symbol?
  "If loc's node is a symbol, return the symbol String. Otherwise, return nil."
  [loc]
  (when (= :symbol (:tag (z/node loc)))
    (symbol (lu/loc-text loc))))

(defn find-var-metadata
  "Given a Repl aware part, and an already resolved current-namespace, makes a call to
   the part REPL to find metadata associated to symbol. If there's currently
   no REPL, just return nil.
   CURRENTLY works for namespace vars, and namespaces."
  [current-namespace ^IReplAwarePart part var]
  (when-let [repl (.getCorrespondingREPL part)]
    (let [safe-connection (.getSafeToolingConnection repl)
          command (format (str "(ccw.debug.serverrepl/var-info "
                               "  (or (try (clojure.core/ns-resolve "
                               "             (clojure.core/the-ns '%s) '%s)"
                               "        (catch Exception e nil))"
                               "      (try (clojure.core/the-ns '%s)"
                               "        (catch Exception e nil))))")
                          current-namespace
                          var var)
          response (send-message safe-connection command
                                 :timeout 1000)]
      (first response))))

(defn context-message
  "Creates the context message"
  [callee-name callee-metadata]
  (when (some #{:arglists :doc} (keys callee-metadata))
    (format "%s: %s\n%s" 
      callee-name
      (or (:arglists callee-metadata) "")
      (doc/slim-doc (:doc callee-metadata)))))