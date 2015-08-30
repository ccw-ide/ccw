(ns ccw.editors.clojure.code-completion-proposal
  (:require
    [clojure.string :as str]
    [ccw.api.parse-tree :as parse-tree]
    [ccw.editors.clojure.editor-common :as common :refer (find-var-metadata)]
    [paredit.parser :as p]
    [paredit.core   :as pc]
    [ccw.debug.serverrepl :as serverrepl]
    [ccw.core.doc-utils :as doc]
    [ccw.editors.clojure.clojure-proposal-processor :as cpp]
    [ccw.api.content-assist :as api]
    [ccw.api.util.content-assist :as api-util]
    [ccw.editors.clojure.code-context-information :refer (compute-context-information)]))

(def completion-limit 
  "Maximum number of returned results" 50)

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

;; TODO homogeneiser parse-state, etc.
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

(defn compute-completion-proposals
  "Return the list of java completion objects to the Completion framework."
  [editor, text-viewer, offset]
  (let [prefix-offset     (api-util/compute-prefix-offset 
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
            {:replacement {:text completion, :offset prefix-offset, :length (- offset prefix-offset)}
             :cursor-position (count completion)
             :display-string (cond-> completion
                               (and ns (not (.startsWith completion (str ns "/"))))
                                 (str " (" ns ")")
                               type
                                 (str " - " type))
             :display-string-style filter
             :context-information-delay (delay (compute-context-information editor, completion))
             :additional-proposal-info-delay (delay (doc/var-doc-info-html @md-ref))}))))))

(defn start []
  (api/register-completion-proposal-provider!
    {:label "Clojure Code", :provider #'compute-completion-proposals}))
