(ns ccw.editors.clojure.editor-common
  (:require [clojure.string :as s]
            [clojure.test :as test]
            [clojure.set :as set]
            [clojure.tools.nrepl :as repl]
            [paredit.parser :as p]
            [paredit.loc-utils :as lu]
            [clojure.zip :as z]
            [ccw.core.doc-utils :as doc]
            [ccw.core.trace :as t]
            [ccw.editors.clojure.editor-support :as editor])
  (:import [org.eclipse.jface.viewers StyledString
                                      StyledString$Styler]
           [org.eclipse.jface.text.contentassist IContentAssistProcessor
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
           [ccw.editors.clojure IClojureEditor]
           [clojure.tools.nrepl Connection]))

(defn offset-loc
  "Return the zip loc for offset in part"
  [^IClojureEditor part offset]
  (let [rloc (-> part .getParseState (editor/getParseTree) lu/parsed-root-loc)]
    (lu/loc-for-offset rloc offset)))

(defn offset-region
  "For editor, given the character offset, return a vector of [offset
  length] representing a region of the editor (containing offset).  The
  idea is that for every offset in that region, the same documentation
  hover as the one computed for offset will be used.  This is a function
  for optimizing the number of times the hover-info function is called."
  [^IClojureEditor part offset]
  (let [loc (offset-loc part offset)]
    [(lu/start-offset loc) (lu/loc-count loc)]))

(defn send-message**
  "Send the message over the nrepl connection. This version is \"bare\", ie it
   calls into the REPL without timeout protection. If you want to protect the 
   IDE to freeze if e.g. the REPL never times out, call send-message instead."
  [^Connection safe-connection message]
  (try
    (-> safe-connection 
      .getUnsafeConnection
      .client
      (repl/message message))
    (catch Exception e
      (ccw.CCWPlugin/logError (str "exception while sending message " message " to connection " (.getUnsafeConnection safe-connection)) e)
      (when (instance? java.net.SocketException e)
        (.connectionLost safe-connection))
      nil)))

(defn send-message*
  "Same as send-message**, but guarded by a client timeout
   so that Eclipse cannot hang forever.
   timeout in milliseconds"
  [safe-connection message & {:keys [timeout] :or {timeout 1000}}]
  (let [timeout-val (Object.)
        secure-call (future (send-message** safe-connection message))
        result (deref secure-call timeout timeout-val)]
    (if (not= result timeout-val)
      result
      (ccw.CCWPlugin/logError (str 
        "timeout while calling send-message** for message "
        (pr-str message))))))

(defn send-code
  "Send code represented as String, guarded by a client timeout,
   see 'send-message* options. Return a responses vector"
  [safe-connection code & {:keys [session] :as rest}]
  (let [msg {"op" "eval", "code" code}
        msg (if-not session msg (assoc msg "session" session))]
    (when-let [r (apply send-message* safe-connection msg rest)]
     (repl/response-values r))))

(def timed-out-safe-connections 
  "Keeps the timed out safe-connections in a map of [safe-connection nb-timeouts]"
  (atom {}))

(defn send-message
  "Sends message and keep tracks of safe-connections that have timeouts.
   When a safe-connection has had 2 timeouts, stop trying to use
   it and return nil."
  [safe-connection message & rest]
  (let [nb-timeouts (@timed-out-safe-connections safe-connection 0)]
    (when (< nb-timeouts 3)
      (try
        (apply send-message* safe-connection message rest)
        (catch java.util.concurrent.TimeoutException e
          (t/trace :editor "timeout sending message: %s" message)
          (swap! timed-out-safe-connections update-in [safe-connection] (fnil inc 0)))))))

(defn parse-symbol
  "If loc's node is a symbol, return the symbol String. Otherwise, return nil."
  [loc]
  (when (= :symbol (:tag (z/node loc)))
    (symbol (lu/loc-text loc))))

(defmulti find-var-metadata
  (fn [current-namespace repl var]
    (when repl
      (some #{"info"} (.getAvailableOperations repl)))))

(defmethod find-var-metadata :default
  [current-namespace repl var]
  (when repl
    (let [safe-connection (.getSafeToolingConnection repl)
          code (format (str "(ccw.debug.serverrepl/var-info "
                              "(clojure.core/ns-resolve "
                                "(clojure.core/the-ns '%s) '%s))")
                       current-namespace
                       var)
          response (first (send-code safe-connection code
                            ; we do not use session via send-code yet because
                            ; we cannot distinguish between clojure or clojurescript
                            ; back-end and adapt appropriately
                            ;:session (.getSessionId repl)
                            ))]
      response)))

(defmethod find-var-metadata "info"
  [current-namespace repl var]
  (when repl
    (let [safe-connection (.getSafeToolingConnection repl)
          response (-> (first
                         (send-message safe-connection
                           {"op" "info"
                            "symbol" var
                            "ns" current-namespace
                            "session" (.getSessionId repl)}))
                     (set/rename-keys {:arglists-str :arglists
                                       :resource :file}))]
      response)))

(defn context-message
  "Creates the context message"
  [callee-name callee-metadata]
  (when (some #{:arglists :doc} (keys callee-metadata))
    (format "%s: %s\n%s" 
      callee-name
      (or (:arglists callee-metadata) "")
      (doc/slim-doc (:doc callee-metadata)))))
