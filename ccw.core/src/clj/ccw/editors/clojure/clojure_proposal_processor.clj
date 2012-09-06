(ns ccw.editors.clojure.clojure-proposal-processor
  (:require [clojure.string :as s]
            [clojure.test :as test]
            [clojure.tools.nrepl :as repl]
            [paredit.parser :as p]
            [paredit.loc-utils :as lu]
            [clojure.zip :as z]
            [ccw.util.doc-utils :as doc]
            [ccw.debug.serverrepl :as serverrepl])
  (:use [clojure.core.incubator :only [-?>]])
  (:import [org.eclipse.jface.viewers StyledString
                                      StyledString$Styler]
           [org.eclipse.jface.text.contentassist 
            IContentAssistProcessor
            ContentAssistant
            CompletionProposal
            ICompletionProposal
            ICompletionProposalExtension6
            IContentAssistProcessor
            IContextInformation
            IContextInformationExtension
            IContextInformationValidator]
           [org.eclipse.jdt.core JavaCore
                                 IMethod
                                 IType]))

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
  "for viewer, at offset 12, return the loc containing the encapsulatin
   call, or nil"
  [viewer offset]
  (when (pos? offset)
    (let [loc (lu/loc-containing-offset 
                (-> viewer .getParseState :parse-tree lu/parsed-root-loc)
                offset)
          maybe-call-loc (-> loc parent-call)]
      (when (call? (z/node maybe-call-loc))
        maybe-call-loc))))

(defn call-context-loc
  "for viewer, at offset 12, return the loc containing the encapsulatin
   call, or nil"
  [viewer offset]
  (when (pos? offset)
    (let [loc (lu/loc-containing-offset 
                (-> viewer .getParseState :parse-tree lu/parsed-root-loc)
                offset)
          maybe-call-loc (-?> loc parent-call)]
      (when (-?> maybe-call-loc z/node call?)
        maybe-call-loc))))

(defn call-symbol
  "for viewer, at offset 12, return the symbol name if the offset
   is inside a function/macro call, or nil"
  [loc]
  (when loc (-> loc z/node callee p/form p/sym-name)))

;; TODO find the prefix via the editor's parse tree !
(defn prefix-info 
  [ns offset prefix]
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
(defn invalid-symbol-char? [c]
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
  [string offset]
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
              (.setStyle s i 1 StyledString/COUNTER_STYLER)))
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
  (format (str "(complete.core/ccw-completions \"%s\" (clojure.core/the-ns '%s) %d)") 
          prefix
          namespace
          completion-limit))

(defn send-message 
  "Send the command over the nrepl connection."
  [connection command]
  (-> (.client connection)
    (repl/message {"op"   "eval"
                   "code" command})
    repl/response-values))

(defn find-var-metadata
  [current-namespace editor var]
  (when-let [repl (.getCorrespondingREPL editor)]
    (let [connection (.getToolingConnection repl)
          command (format (str "(ccw.debug.serverrepl/var-info "
                                 "(clojure.core/ns-resolve "
                                   "(clojure.core/the-ns '%s) '%s))")
                          current-namespace
                          var)
          response (send-message connection command)]
      ;(println "response:" response)
      (first response))))

(defn find-suggestions
  "For the given prefix, inside the current editor and in the current namespace,
   query the remote REPL for code completions list"
  [current-namespace prefix editor find-only-public]
  (cond
    (nil? namespace) []
    (s/blank? prefix) []
    :else (when-let [repl (.getCorrespondingREPL editor)]
            (let [connection (.getToolingConnection repl)
                  command (complete-command current-namespace prefix false)
                  response (send-message connection command)]
              (first response)))))

(defn safe-split-lines [s]
  (when s (s/split-lines s)))

(defn slim-doc [s]
  (let [lines (safe-split-lines s)
        nb-display-lines 2
        lines (if (> (count lines) nb-display-lines) 
                (concat (take (dec nb-display-lines) lines)
                        [(str (nth lines nb-display-lines) " ...")]) 
                lines)]
    (s/join \newline (map s/trim lines))))

(defn context-message
  "Creates the context message"
  [callee-name callee-metadata]
  (when (some #{:arglists :doc} (keys callee-metadata))
                       (format "%s: %s\n%s" 
                               callee-name
                               (or (:arglists callee-metadata) "")
                               (slim-doc (:doc callee-metadata)))))

(defn context-info-data
  "Create a context-information from the data"
  [callee-name cursor-offset callee-metadata]
  (when-let [message (context-message callee-name callee-metadata)]
    (context-information
      message
      nil
      message
      cursor-offset
      cursor-offset)))

(def ca (atom nil))

(defn find-hippie-suggestions
  [prefix offset parse-state]
  (let [buffer-wo-prefix (p/edit-buffer (parse-state :buffer) 
                                        (- offset (count prefix))
                                        (count prefix) "")
        parse-tree (p/buffer-parse-tree buffer-wo-prefix :dummy-build-id)
        tokens ((-> parse-tree :abstract-node) 
                 #_paredit.parser/hippie-view
                 (if (.startsWith prefix ":")
                   paredit.parser/hippie-keyword-view
                   paredit.parser/hippie-symbol-view))]
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
  [editor      content-assistant
   text-viewer offset]
  (reset! ca content-assistant)
  (let [prefix-offset (compute-prefix-offset 
                        (-> text-viewer .getDocument .get)
                        offset)
        current-namespace (.findDeclaringNamespace editor)
        prefix (.substring
                 (-> text-viewer .getDocument .get)
                 prefix-offset
                 offset)]
    (when (pos? (count prefix))
      (let [hippie-suggestions (find-hippie-suggestions
                                 prefix
                                 offset
                                 (.getParseState editor))
            repl-suggestions (find-suggestions 
                               current-namespace
                               prefix
                               editor
                               false)
            suggestions (apply sorted-set-by 
                               (adapt-args (serverrepl/textmate-comparator prefix) :completion :completion)
                               #_#(let [comparator (serverrepl/textmate-comparator prefix)]
                                    (comparator (:completion %1) (:completion %2)))
                               (concat repl-suggestions hippie-suggestions ))]
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

(def viewer (atom nil))
(def offset (atom nil))


(defn compute-context-information
  [editor
   text-viewer new-offset]
  (reset! viewer text-viewer)
  (reset! offset new-offset)
  (into-array
    IContextInformation
    (when-let [loc (call-context-loc text-viewer new-offset)]
      (let [call-symbol (call-symbol loc)
            context-info (context-info-data 
                           call-symbol 
                           new-offset 
                           (find-var-metadata (.findDeclaringNamespace editor) 
                                              editor
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
  [editor offset]
  (def ed editor)
  (def of offset)
  (let [parse-tree     ((-> ed .getParseState :parse-tree :abstract-node) 
                         paredit.parser/parse-tree-view)
        last-token-tag (-> parse-tree :content peek :tag)]
    (not= last-token-tag :comment)))

(defn make-process [editor content-assistant]
  (reify IContentAssistProcessor
    (computeCompletionProposals
      [this text-viewer offset]
      (.setStatusMessage content-assistant "")
      ;; TODO manage error message
      (into-array 
        ICompletionProposal
        (when (should-compute-proposals? editor offset)
          (compute-completion-proposals
            editor      content-assistant
            text-viewer offset))))
    
    (computeContextInformation
      [this text-viewer new-offset]
      (compute-context-information
        editor
        text-viewer new-offset))
    
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
