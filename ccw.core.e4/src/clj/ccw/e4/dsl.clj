(ns ccw.e4.dsl
  "Eclipse 4 DSL namespace.
   You can consider using (:require [ccw.e4.dsl :refer-all])
   since the macros / functions exposed have been carefully chosen."
  (:require [ccw.e4.model :as m]
            [ccw.eclipse  :as e])
  (:import [ccw.util GenericHandler]))

(def ^:dynamic *load-key* "repl")

(defn handler-factory [context nsname name] 
  (GenericHandler. (str nsname "/" name)))

(defmacro defcommand [command command-name & {:as opts}]
  `(def ~command
     (let [spec# (-> (merge
                       {:id ~(str *ns* "/" command)
                        :name ~command-name}
                       ~opts)
                   (update-in [:transient-data]
                              assoc
                              "ccw/load-key" *load-key*)
                   (update-in [:tags]
                              (fnil conj #{})
                              "ccw"))]
       ;(println "spec#:" spec#)
       (m/merge-command! @m/app spec#))))

;;; TODO register for deletion in  m/*elements* (a set)
(defmacro defhandler [command var-symbol]
  (let [cmd-id (str *ns* "/" command)
        id (str cmd-id "-handler")]
    `(def ~(symbol id)
      (let [spec# (-> {:contribution-URI
                       ~(str "bundleclass://ccw.core/clojure/ccw.e4.dsl/handler-factory/"
                             (or (namespace var-symbol) (ns-name *ns*)) "/" (name var-symbol))
                       :command ~cmd-id
                       :id ~id}
                    (update-in [:transient-data]
                              assoc
                              "ccw/load-key" *load-key*)
                   (update-in [:tags]
                              (fnil conj #{})
                              "ccw"))]
        ;(println "spec#:" spec#)
       (m/merge-handler! @m/app spec#)))))

;; TODO support options !!!
(defmacro defkeybinding 
  "Example:
   (defkeybinding greeter \"Ctrl+Alt+M\")
   (defkeybinding greeter \"Ctrl+Alt+M\"
     :scheme :emacs

   :scheme
   Predefined values 

 ... TODO document all this carefully ...

:sequence
\"M1+M2+P\"
\"Ctrl+Alt+M\"

:scheme-id
:default org.eclipse.ui.defaultAcceleratorConfiguration
:emacs org.eclipse.ui.emacsAcceleratorConfiguration

:context-id
:clojure-editor ccw.ui.clojureEditorScope
:text-editor org.eclipse.ui.textEditorScope
:clojure-repl ccw.ui.contextRepl
:dialog&window org.eclipse.ui.context.dialogAndWindow
:dialog org.eclipse.ui.context.dialog
:window org.eclipse.ui.context.window
:console org.eclipse.ui.console.ConsoleView

:command-id

:platform ; Based on SWT/getPlatform ()
:win32
:gtk
:motif
:carbon
:photon

:locale
:en
:en_CA

"
  [command key-sequence & {:as opts}]
  `(let [spec# (-> (merge {:command ~command
                           :scheme  :default
                           :key-sequence ~key-sequence
                           :context "org.eclipse.ui.contexts.window"
                           :transient-data {"ccw/load-key" *load-key*}}
                          ~opts)
                 (update-in [:transient-data]
                              assoc
                              "ccw/load-key" *load-key*)
                   (update-in [:tags]
                              (fnil conj #{})
                              "ccw"))]
     ;(println "spec#:" spec#)
     (m/merge-key-binding! @m/app spec#)))
