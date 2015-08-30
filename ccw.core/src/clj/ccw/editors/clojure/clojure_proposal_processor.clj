(ns ccw.editors.clojure.clojure-proposal-processor
  (:require [paredit.parser :as p]
            [ccw.core.trace :as trace]
            [ccw.editors.clojure.editor-common :as common :refer (find-var-metadata)]
            [schema.core :as s]
            [ccw.api.schema.content-assist :refer :all])
  (:import [org.eclipse.jface.viewers StyledString
                                      StyledString$Styler]
           [org.eclipse.jface.text ITextViewer]
           [org.eclipse.jface.text.contentassist 
            IContentAssistProcessor
            ContentAssistant
            CompletionProposal
            ICompletionProposal
            ICompletionProposalExtension4
            ICompletionProposalExtension6
            IContextInformation
            IContextInformationExtension
            IContextInformationValidator]
           [org.eclipse.jdt.core JavaCore
                                 IMethod
                                 IType]
           [ccw.editors.clojure IClojureEditor]))

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

(s/defn ^:always-validate as-eclipse-context-information
 [{:keys [information-display image display-string context-information-position
          context-information-position-start context-information-position-stop]} :- ContextInformationMap
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

(s/defn ^:always-validate as-eclipse-completion-proposal
  [{:keys [cursor-position image display-string
           additional-proposal-info-delay display-string-style
           auto-insertable?]
    :or {auto-insertable? true}
    {:keys [text offset length]} :replacement} :- CompletionProposalMap]
  (let [text (or text "")
        display-string (or display-string "")
        cp (CompletionProposal.
             text
             offset
             length
             cursor-position
             image
             display-string
             nil ;; context-information is computed on demand
             nil)] ;; additional-proposal is computed on demand
    (reify 
      ICompletionProposal
      (apply [this document] (.apply cp document))
      (getSelection [this document] (.getSelection cp document))
      (getAdditionalProposalInfo [this] @additional-proposal-info-delay)
      (getDisplayString [this] (.getDisplayString cp))
      (getImage [this] (.getImage cp))
      (getContextInformation [this] nil #_@context-information-delay)
      
      ICompletionProposalExtension4
      (isAutoInsertable [this] (boolean auto-insertable?))
      
      ICompletionProposalExtension6
      (getStyledDisplayString [this]
        (let [s (StyledString. display-string)]
          (when (seq display-string-style)
            (doseq [i (reductions (partial + 1) display-string-style)]
              (if (< i (count display-string))
                (.setStyle s i 1 StyledString/COUNTER_STYLER)
                (printf (str "ERROR: Completion proposal trying to apply color style"
                             "at invalid offset %d for display-string '%s'")
                        i display-string))))
          s)))))

;; TODO remove in favor of ... ?
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

(def activation-characters
  "Characters which will trigger auto-completion"
  (concat (map char 
               (concat 
                 (range (int \a) (inc (int \z)))
                 (range (int \A) (inc (int \Z)))))
          [\. \- \? \!]))

(defonce context-information-providers (atom #{}))

(defonce completion-proposal-providers (atom #{}))

(defn- compute-context-information
  [^IClojureEditor editor, text-viewer, offset]
  (reduce
    (fn [r {:keys [label provider]}]
      (if-let [ci (provider editor text-viewer offset)]
        (conj r (as-eclipse-context-information ci label offset))
        r))
    []
    @context-information-providers))

(defn- compute-completion-proposals
  [^IClojureEditor editor, ^ITextViewer text-viewer offset]
  (reduce
    (fn [r {:keys [label provider]}]
      (concat r (map as-eclipse-completion-proposal (provider editor text-viewer offset))))
    nil
    @completion-proposal-providers))

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
      (into-array ICompletionProposal
        (when (should-compute-proposals? editor offset)
          (compute-completion-proposals
            editor
            text-viewer
            offset))))
    
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
