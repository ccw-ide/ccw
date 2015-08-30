(ns ccw.editors.clojure.clojure-proposal-processor
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.test :as test]
            [clojure.tools.nrepl :as repl]
            [paredit.core   :as pc]
            [paredit.parser :as p]
            [ccw.core.doc-utils :as doc]
            [ccw.api.parse-tree :as parse-tree]
            [ccw.debug.serverrepl :as serverrepl]
            [ccw.core.trace :as trace]
            [ccw.editors.clojure.editor-common :as common :refer (find-var-metadata)]
            [schema.core :as s]
            [ccw.schema.core :as cs])
  (:use [clojure.core.incubator :only [-?>]])
  (:import [org.eclipse.jface.viewers StyledString
                                      StyledString$Styler]
           [org.eclipse.jface.text ITextViewer]
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
           [ccw.editors.clojure IClojureEditor]))

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


(def completion-limit 
  "Maximum number of returned results" 50)

;; TODO remove in favor of as-eclipse-context-information
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

(def ContextInformation
  {(s/optional-key :information-display) String
   (s/optional-key :display-string)       String
   (s/optional-key :image)               org.eclipse.swt.graphics.Image
   (s/optional-key :context-information-position) s/Int
   (s/optional-key :context-information-position-start) s/Int
   (s/optional-key :context-information-position-stop) s/Int})

(s/defn ^:always-validate as-eclipse-context-information
 [{:keys [information-display image display-string context-information-position
          context-information-position-start context-information-position-stop]} :- ContextInformation
  default-display-string :- String
  offset :- s/Int]
 (with-meta
     (reify 
       IContextInformation
       (getContextDisplayString [this] (or display-string default-display-string ""))
       (getImage [this] image)
       (getInformationDisplayString [this] (or information-display ""))
       
       IContextInformationExtension
       (getContextInformationPosition [this] (or context-information-position context-information-position-start offset)))
     {:start (or context-information-position-start offset)
      :stop  (or context-information-position-stop context-information-position-start offset)}))

(defn completion-proposal
  [replacement-string
   replacement-offset
   replacement-length
   cursor-position
   image
   display-string
   filter
   context-information-delay
   additional-proposal-info-delay]
  (let [cp (CompletionProposal.
             (or replacement-string "")
             replacement-offset
             replacement-length
             cursor-position
             image
             (or display-string "")
             nil ;; context-information is computed on demand
             nil)] ;; additional-proposal is computed on demand
    (reify 
      ICompletionProposal
      (apply [this document] (.apply cp document))
      (getSelection [this document] (.getSelection cp document))
      (getAdditionalProposalInfo [this] @additional-proposal-info-delay)
      (getDisplayString [this] (.getDisplayString cp))
      (getImage [this] (.getImage cp))
      (getContextInformation [this] @context-information-delay)
      
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

(defn complete-command
  "Create the complete command to be sent remotely to get back a list of
   completion proposals."
  [namespace prefix find-only-public]
  (format (str "(ccw.complete/ccw-completions \"%s\" (clojure.core/the-ns '%s) %d)") 
          prefix
          namespace
          completion-limit))

(defmulti find-suggestions
  "For the given prefix, inside the current editor and in the current namespace,
   query the remote REPL for code completions list"
  (fn [current-namespace prefix repl find-only-public]
    (when repl
      (some #{"complete"} (.getAvailableOperations repl)))))

;; TODO encadrer les appels externes avec un timeout
;; si le timeout est depasse, ejecter la repl correspondante
;; TODO faire aussi pour la recherche de documentation, etc... lister tout
;; pour le code completion, un timeout > 1s est deja enorme
;; A terme: decoupler la recuperation des infos et leur exploitation
;; - un dictionnaire de suggestions mis à jour en batch à des points clés (interactions avec repl)
;; - interrogation du dictionnaire en usage courant (tant pis si pas a jour)
(defmethod find-suggestions :default
  [current-namespace prefix repl find-only-public]
  (cond
    (nil? current-namespace) []
    (str/blank? prefix) []
    :else (when repl
            (let [safe-connection (.getSafeToolingConnection repl)
                  code (complete-command current-namespace prefix false)

                  response (first (common/send-code safe-connection code
                                    ; we don't send code directly with the user session id
                                    ; because we cannot guarantee the code will be interpreted
                                    ; by the right back-end (clojure or clojurescript)
                                    ; :session (.getSessionId repl)
                                    ))]
              response))))

(defmethod find-suggestions "complete"
  [current-namespace prefix repl find-only-public]
  (cond
    (nil? current-namespace) []
    (str/blank? prefix) []
    :else (when repl
            (let [safe-connection (.getSafeToolingConnection repl)
                  response (first (common/send-message safe-connection
                                    {"op" "complete"
                                     "symbol" prefix
                                     "ns" current-namespace
                                     "session"  (.getSessionId repl)}))]
              (when-let [completions
                         (seq (->>
                                (:completions response)
                                (filter :candidate) ; protection against nil candidates returned by compliment
                                (map
                                  #(hash-map
                                     :completion (:candidate %)
                                     :match (:candidate %)
                                     :type (:type %)
                                     :ns (:ns %)
                                     :filter (serverrepl/textmate-filter (:candidate %) prefix)
                                     :metadata nil))))]
                completions)))))

(defn context-info-data
  "Create a context-information from the data"
  [callee?-name cursor-offset callee?-metadata]
  (when-let [message (common/context-message callee?-name callee?-metadata)]
    (context-information
      message
      nil
      message
      cursor-offset
      cursor-offset)))

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
    (for [token (concat pc/lisp-forms tokens)
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
                               )}})))

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
  [^IClojureEditor editor      content-assistant
   ^ITextViewer text-viewer offset]
  (let [prefix-offset     (compute-prefix-offset 
                            (-> text-viewer .getDocument .get)
                            offset)
        current-namespace (.findDeclaringNamespace editor)
        repl              (.getCorrespondingREPL editor)
        prefix            (.substring
                            (-> text-viewer .getDocument .get)
                            prefix-offset
                            offset)]
    (when (pos? (count prefix))
      (let [hippie-suggestions (find-hippie-suggestions
                                 prefix
                                 offset
                                 (.getParseState editor))
            repl-suggestions   (find-suggestions 
                                 current-namespace
                                 prefix
                                 repl
                                 false)
            suggestions        (apply sorted-set-by 
                                      (adapt-args (serverrepl/textmate-comparator prefix)
                                        :completion :completion)
                                      (concat repl-suggestions hippie-suggestions))]
        (for [{:keys [completion filter type ns]
               {:keys [arglists name added static 
                       doc line file] 
                :as metadata} :metadata
               } suggestions]
          (let [md-ref (delay (find-var-metadata current-namespace repl completion))]
            (completion-proposal
              completion
              prefix-offset
              (- offset prefix-offset)
              (count completion)
              nil
              (cond-> completion
                (and ns (not (.startsWith completion (str ns "/"))))
                  (str " (" ns ")")
                type
                  (str " - " type))
              filter
              (delay (context-info-data
                       completion
                       (+ prefix-offset (count completion))
                       @md-ref))
              (delay (doc/var-doc-info-html @md-ref)))))))))

(def activation-characters
  "Characters which will trigger auto-completion"
  (concat (map char 
               (concat 
                 (range (int \a) (inc (int \z)))
                 (range (int \A) (inc (int \Z)))))
          [\. \- \? \!]))

(defonce ^:private context-information-providers (atom #{}))

;(c/register-content-assist-processor!
;  {:id               :completions.code
;   :label            "Clojure Code Content Assist"
;   :display-strategy :replace ; only strategy available yet. Default content assist will be replaced by this one if :trigger-fn returns true
;   :trigger-fn       #'find-suggestions
;   :proposals-fn     (constantly true)
;   })

(s/defschema ContextInformationProviderFn
  "Signature of function that can produce ContextInformation map"
  (s/=>* ContextInformation [[IClojureEditor s/Any s/Int]]))

(s/defschema ContextInformationProvider
  {:label    String
   :provider (s/either ContextInformationProviderFn (cs/reference ContextInformationProviderFn))})

(s/defn ^:always-validate register-context-information-provider!
  [provider :- ContextInformationProvider]
  (swap! context-information-providers conj provider))

(defn compute-context-information
  [^IClojureEditor editor, text-viewer, offset]
  (reduce
    (fn [r {:keys [label provider]}]
      (if-let [ci (provider editor text-viewer offset)]
        (conj (or r []) (as-eclipse-context-information ci label offset))
        r))
    nil
    @context-information-providers))

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
  [^IClojureEditor editor offset]
  (if (not= offset (-> editor .getDocument .get count))
    true
    (let [parse-tree     ((-> editor .getParseState :parse-tree :abstract-node) 
                          p/parse-tree-view)
         last-token-tag (-> parse-tree :content peek :tag)]
     (not= last-token-tag :comment))))

(defn make-process [editor ^ContentAssistant content-assistant]
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
      [this text-viewer offset]
      (into-array IContextInformation
        (compute-context-information
         editor
         text-viewer
         offset)))
    
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
    
    (getErrorMessage [this] "This feature works better with a running REPL")
    
    (getContextInformationValidator
      [this]
      (let [context-info (atom nil)]
        (reify IContextInformationValidator
          (install [this new-context-info new-viewer new-offset]
            (reset! context-info new-context-info))
          (isContextInformationValid 
            [this offset]
            (let [{:keys [start stop]} (meta @context-info)]
              (<= start offset stop))))))))
