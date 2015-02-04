(ns ccw.editors.clojure.clojure-proposal-processor
  (:require [clojure.string :as s]
            [clojure.test :as test]
            [clojure.tools.nrepl :as repl]
            [paredit.parser :as p]
            [paredit.loc-utils :as lu]
            [clojure.zip :as z]
            [ccw.core.doc-utils :as doc]
            [ccw.debug.serverrepl :as serverrepl]
            [ccw.core.trace :as trace]
            [ccw.editors.clojure.editor-common :as common])
  (:import [org.eclipse.jface.viewers StyledString
                                      StyledString$Styler]
           [org.eclipse.jface.text ITextViewer]
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
           [ccw.editors.clojure IClojureAwarePart
                                IClojurePart
                                IReplAwarePart]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; parse tree manipulation functions

(defn call? 
  "is element e a function/macro-call?"
  [e] (= "(" (first (p/code-children e))))
(defn callee 
  "the called symbol node (without metadata"
  [e] (second (p/code-children e)))

(defn parent-call
  "return the loc for the parent call, or nil"
  [loc]
  (let [parents (take-while (comp not nil?) (iterate z/up loc))]
    (first (filter (comp call? z/node) parents))))

(defn call-context-loc
  "For a IClojureAwarePart, at offset 12, return the loc containing the encapsulating
   call, or nil"
  [^IClojureAwarePart part offset]
  (when (pos? offset)
    (let [loc (lu/loc-containing-offset
                (-> part .getParseState :parse-tree lu/parsed-root-loc)
                offset)
          maybe-call-loc (some-> loc parent-call)]
      (when (some-> maybe-call-loc z/node call?)
        maybe-call-loc))))

(defn call-symbol
  "for viewer, at offset 12, return the symbol name if the offset
   is inside a function/macro call, or nil"
  [loc]
  (when loc (-> loc z/node callee p/remove-meta p/sym-name)))

;; TODO find the prefix via the editor's parse tree !
(defn prefix-info 
  [ns offset ^String prefix]
  (let [[n1 n2] (s/split prefix #"/")
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


(def completion-limit 
  "Maximum number of returned results" 50)

; TODO introduce polymorphism?
(defn completion-proposal
  [replacement-string
   replacement-offset
   replacement-length
   cursor-position
   image
   display-string
   filter
   context-information
   additional-proposal-info]
  (let [cp (CompletionProposal.
             (or replacement-string "")
             replacement-offset
             replacement-length
             cursor-position
             image
             (or display-string "")
             context-information
             (or additional-proposal-info ""))]
    (reify 
      ICompletionProposal
      (apply [this document] (.apply cp document))
      (getSelection [this document] (.getSelection cp document))
      (getAdditionalProposalInfo [this] (.getAdditionalProposalInfo cp))
      (getDisplayString [this] (.getDisplayString cp))
      (getImage [this] (.getImage cp))
      (getContextInformation [this] (.getContextInformation cp))
      
      ICompletionProposalExtension6
      (getStyledDisplayString [this]
        (let [s (StyledString. (or display-string ""))]
          (when (seq filter)
            (doseq [i (reductions (partial + 1) filter)]
              (if (< i (count display-string))
                (.setStyle s i 1 StyledString/COUNTER_STYLER)
                (printf (str "ERROR: Completion proposal trying to apply color style"
                             "at invalid offset %d for display-string '%s'")
                        i display-string))))
          s)))))

(defn context-information
  [context-display-string
   image
   information-display-string
   position-start
   position-stop]
   (with-meta
     (reify 
       IContextInformation
       (getContextDisplayString [this] (or context-display-string ""))
       (getImage [this] image)
       (getInformationDisplayString [this] (or information-display-string ""))
       
       IContextInformationExtension
       (getContextInformationPosition [this] position-start))
     {:start position-start
      :stop  position-stop}))

(defn complete-command
  "Create the complete command to be sent remotely to get back a list of
   completion proposals."
  [namespace prefix find-only-public]
  (format (str "(ccw.complete/ccw-completions \"%s\" (clojure.core/the-ns '%s) %d)") 
          prefix
          namespace
          completion-limit))

(def timed-out-safe-connections 
  "Keeps the timed out safe-connections in a map of [safe-connection nb-timeouts]"
  (atom {}))

(defn send-message
  "Sends message and keep tracks of safe-connections that have timeouts.
   When a safe-connection has had 2 timeouts, stop trying to use
   it and return nil."
  [safe-connection command timeout]
  (let [nb-timeouts (@timed-out-safe-connections safe-connection 0)]
    (when (< nb-timeouts 3)
      (try
        (common/send-message safe-connection command :timeout timeout)
        (catch java.util.concurrent.TimeoutException e
          (swap! timed-out-safe-connections update-in [safe-connection] (fnil inc 0)))))))

(defn find-var-metadata
  [current-namespace ^IClojurePart editor var]
  (when-let [repl (.getCorrespondingREPL editor)]
    (let [safe-connection (.getSafeToolingConnection repl)
          command (format (str "(ccw.debug.serverrepl/var-info "
                                 "(clojure.core/ns-resolve "
                                   "(clojure.core/the-ns '%s) '%s))")
                          current-namespace
                          var)
          response (send-message safe-connection command 1000)]
      ;(println "response:" response)
      (first response))))

;; TODO encadrer les appels externes avec un timeout
;; si le timeout est depasse, ejecter la repl correspondante
;; TODO faire aussi pour la recherche de documentation, etc... lister tout
;; pour le code completion, un timeout > 1s est deja enorme
;; A terme: decoupler la recuperation des infos et leur exploitation
;; - un dictionnaire de suggestions mis à jour en batch à des points clés (interactions avec repl)
;; - interrogation du dictionnaire en usage courant (tant pis si pas a jour)
(defn find-suggestions
  "For the given prefix, inside the current editor and in the current namespace,
   query the remote REPL for code completions list"
  [current-namespace prefix ^IReplAwarePart editor find-only-public]
  (cond
    (nil? namespace) []
    (s/blank? prefix) []
    :else (when-let [repl (.getCorrespondingREPL editor)]
            (let [safe-connection (.getSafeToolingConnection repl)
                  command (complete-command current-namespace prefix false)
                  response (send-message safe-connection command 1000)]
              (first response)))))

(defn context-info-data
  "Create a context-information from the data"
  [callee-name cursor-offset callee-metadata]
  (when-let [message (common/context-message callee-name callee-metadata)]
    (context-information
      message
      nil
      message
      cursor-offset
      cursor-offset)))

(def ca (atom nil))

(defn find-hippie-suggestions
  [^String prefix offset parse-state]
  (let [buffer-wo-prefix (p/edit-buffer (parse-state :buffer) 
                                        (- offset (count prefix))
                                        (count prefix) "")
        parse-tree (p/buffer-parse-tree buffer-wo-prefix :dummy-build-id)
        tokens ((-> parse-tree :abstract-node) 
                 #_p/hippie-view
                 (if (.startsWith prefix ":")
                   p/hippie-keyword-view
                   p/hippie-symbol-view))]
    (sort-by
      :completion
      (if (<= (count tokens) 50) ; TODO remove magic number
        (serverrepl/textmate-comparator prefix)
        (serverrepl/distance-comparator
          prefix
          serverrepl/index-of-distance
          compare
          compare))
      (for [token tokens
;            :when (not (or 
;                         (= token (str ":" prefix))
;                         (= token prefix)
;                         (.contains token "/")))
            :let [filter (ccw.debug.serverrepl/textmate-filter token prefix)]
            :when filter]
        {:completion token
         :match token
         :filter filter
         :metadata nil #_{:doc (.substring
                                 (-> editor .getDocument .get)
                         (max 0 (- offset 50))
                         (min (+ offset 50) (-> editor .getDocument .get .length))
                         )}}))))

;; TODO move to in ccw.util.core in ccw.util bundle
(defn adapt-args 
  "Delegate calls to f by first applying args-fns to its arguments
   (applies identity if less args-fns than actual arguments)."
  ([f arg1-fn]
    (fn 
      ([arg1] (f (arg1-fn arg1)))
      ([arg1 arg2] (f (arg1-fn arg1) arg2))
      ([arg1 arg2 arg3] (f (arg1-fn arg1) arg2 arg3))
      ([arg1 arg2 arg3 & more] (apply f (arg1-fn arg1) arg2 arg3 more))))
  ([f arg1-fn arg2-fn]
    (fn 
      ([arg1] (f (arg1-fn arg1)))
      ([arg1 arg2] (f (arg1-fn arg1) (arg2-fn arg2)))
      ([arg1 arg2 arg3] (f (arg1-fn arg1) (arg2-fn arg2) arg3))
      ([arg1 arg2 arg3 & more] (apply f (arg1-fn arg1) (arg2-fn arg2) arg3 more))))
  ([f arg1-fn arg2-fn & args-fn]
    (fn [& args]
      (apply f (map #(%1 %2) (cons arg1-fn (cons arg2-fn (concat args-fn (repeat identity)))) args)))))

(defn compute-completion-proposals
  "Return the list of java completion objects to the Completion framework."
  [^IClojureAwarePart source-viewer content-assistant offset]
  (reset! ca content-assistant)
  (let [prefix-offset (compute-prefix-offset
                        (-> source-viewer .getDocument .get)
                        offset)
        current-namespace (.findDeclaringNamespace source-viewer)
        prefix (.substring
                 (-> source-viewer .getDocument .get)
                 prefix-offset
                 offset)]
    (when (pos? (count prefix))
      (let [hippie-suggestions (find-hippie-suggestions
                                 prefix
                                 offset
                                 (.getParseState source-viewer))
            repl-suggestions (find-suggestions
                               current-namespace
                               prefix
                               source-viewer
                               false)
            suggestions (apply sorted-set-by 
                               (adapt-args (serverrepl/textmate-comparator prefix) :completion :completion)
                               #_#(let [comparator (serverrepl/textmate-comparator prefix)]
                                    (comparator (:completion %1) (:completion %2)))
                               (concat repl-suggestions hippie-suggestions))]
        (for [{:keys [completion match filter]
               {:keys [arglists ns name added static 
                       type doc line file] 
                :as metadata} :metadata
               } suggestions]
          (completion-proposal
            completion
            prefix-offset
            (- offset prefix-offset)
            (count completion)
            nil
            (doc/join " - " completion ns)
            filter
            (context-info-data completion (+ prefix-offset (count completion)) metadata)
            (doc/var-doc-info-html metadata)))))))

(def activation-characters
  "Characters which will trigger auto-completion"
  (concat (map char 
               (concat 
                 (range (int \a) (inc (int \z)))
                 (range (int \A) (inc (int \Z)))))
          [\. \- \? \!]))

(defn compute-context-information
  [^IClojureAwarePart part new-offset]
  (into-array
    IContextInformation
    (when-let [loc (call-context-loc part new-offset)]
      (let [call-symbol (call-symbol loc)
            context-info (context-info-data
                           call-symbol
                           new-offset
                           (find-var-metadata (.findDeclaringNamespace part)
                                              part
                                              call-symbol))]
        (when context-info [context-info])))))

(defn should-compute-proposals? 
  "There is an edge case in Eclipse, inherited by the ClojurePartitionScanner,
   which wrongly assumes that the cursor index at the end of the document
   belongs to the default partition type, instead of the token defined for the
   EndOfLineRule to which it really belongs.
   Example: if the document ends with a single line comment and the cursor is
   at the end of the document, then the partition type returned is DEFAULT
   instead of COMMENT.
   The problem is that Eclipse framework then triggers wrongly code completion
   feature.
   This predicate is there to restore the truth :-)"
  [^IClojureAwarePart part offset]
  (let [parse-tree     ((-> part .getParseState :parse-tree :abstract-node)
                         p/parse-tree-view)
        last-token-tag (-> parse-tree :content peek :tag)]
    (not= last-token-tag :comment)))

(defn make-process [^ContentAssistant content-assistant]
  (reify IContentAssistProcessor
    (computeCompletionProposals
      [this text-viewer offset]
      (.setStatusMessage content-assistant "")
      ;; TODO manage error message
      (into-array 
        ICompletionProposal
        (when (should-compute-proposals? text-viewer offset)
          (compute-completion-proposals text-viewer content-assistant offset))))

    (computeContextInformation
      [this text-viewer new-offset]
      (compute-context-information text-viewer new-offset))

    (getCompletionProposalAutoActivationCharacters
      [this] 
      (into-array 
        Character/TYPE
        activation-characters))
    
    (getContextInformationAutoActivationCharacters
      [this]
      (into-array 
        Character/TYPE
        activation-characters
        #_(concat activation-characters
                [\newline \space \tab \( \) \{ \} \[ \] \,])))
    
    (getErrorMessage [this] )
    
    (getContextInformationValidator
      [this]
      (let [context-info (atom nil)]
        (reify IContextInformationValidator
          (install [this new-context-info new-viewer new-offset]
            (reset! context-info new-context-info))
          (isContextInformationValid 
            [this offset]
            ;(println "isValid called for offset:" offset)
            (let [{:keys [start stop]} (meta @context-info)]
              (<= start offset stop))))))))
