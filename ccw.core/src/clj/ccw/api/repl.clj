(ns ccw.api.repl
  "Functions related to repl [views] interactions.
   API for User Plugins."
  (:refer-clojure :exclude [send namespace])
  (:import [ccw.repl REPLView]))

(defn send
  "Send expression expr (a String) to repl (repl object obtained e.g. via an 
   :ccw.editor.saved event).
   add-to-history:   truthy if the expression should be added to the history of
                     sent repl commands (defaults to false)
   print-to-log:     truthy if the expression should be logged in the log area
                     of the repl (defaults to true)
   repeat-last-eval: truthy if the preferences for repeating the last
                     user sent expression should be honored (defaults to false)"
  ([repl expr] (send repl expr false true false))
  ([repl expr add-to-history print-to-log repeat-last-eval]
    (when (and repl expr)
      (.evalExpression repl expr
        (boolean add-to-history)
        (boolean print-to-log)
        (boolean repeat-last-eval)))
    (println "done sending")))

(defn namespace
  "Get the current namespace for the repl connection"
  [repl] (.getCurrentNamespace repl))

(defn namespace!
  "Set the namespace (a String) for the repl"
  [repl namespace] (.setCurrentNamespace repl namespace))