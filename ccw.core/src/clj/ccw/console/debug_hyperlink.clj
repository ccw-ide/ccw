(ns ccw.console.debug-hyperlink
  "Detect and create hyperlinks in Consoles when a pattern describing a JVM
   listening to some debug port is found.
   The first time the pattern is encountered for the currently attached console,
   will automatically start a remote process for connecting to this port"
  (:require [ccw.string :as s]
            [ccw.launch :as launch]
            [ccw.eclipse :as e]
            [ccw.swt :as swt])
  (:use [clojure.test])
  (:import [org.eclipse.ui.console PatternMatchEvent TextConsole]))

(def ^:private debug-port-pattern #"\d+")

(defn remote-connect 
  "Launch a remote java debugging connection on port for project."
  [project port]
  (launch/launch :debug
    {:type-id              :remote-java
     :private              true
     :name                 (str (and project (e/project-name project)) " VM")
     :launch-in-background false
     :java/project-name    (and project (e/project-name project))
     :java/vm-connector    (launch/vm-connector :socket-attach-vm-connector)
     :java/connect-map     {"port"     port
                            "hostname" "localhost"}
     :source-path-computer-id "ccw.sourcePathComputer"}))

(defn match-found 
  "state contains the console instance, and a set of seen (if many) debug port patterns.
   This allows to e.g. only process opening remote connections once, even if
   match-found is called several times on the same text."
  [^PatternMatchEvent event ^TextConsole {:keys [console debug-ports] :as state}]
  (let [offset (.getOffset event)
        length (.getLength event)
        document (.getDocument console)
        s (.get document offset length)
        [debug-port] (re-seq debug-port-pattern s)
        remote-connect #(remote-connect
                          (some-> console .getProcess .getLaunch e/project)
                          debug-port)
        hyperlink (reify org.eclipse.ui.console.IHyperlink
                    (linkActivated [this] (remote-connect))
                    (linkExited [this])
                    (linkEntered [this]))]
    (.addHyperlink console hyperlink offset length)
    (when-not (debug-ports debug-port)
      (swt/doasync (remote-connect)))
    (update-in state [:debug-ports] conj debug-port)))

(defn make []
  (let [state (atom nil)]
    (reify org.eclipse.ui.console.IPatternMatchListenerDelegate
      (connect [this console]  (reset! state {:console console
                                              :debug-ports #{}}))
      (disconnect [this]       (reset! state nil))
      (matchFound [this event] 
        (swap! state (partial match-found event))
        nil))))

(defn factory "plugin.xml hook" [ _ ] (make))
