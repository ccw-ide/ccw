(ns ccw.repl.cmdhistory
  (:use (clojure.contrib def core))
  (:import
    org.eclipse.core.runtime.jobs.Job
    org.eclipse.core.runtime.IProgressMonitor))

(defvar- queued-commands (ref {})
  "Map between project names and vectors of REPL expressions that have yet to
   be persisted to the respective projects' preferences.  This is cleared
   periodically.")

(defvar- pref-node-id "ccw.repl.cmdhistory")
(defvar- history-key "cmdhistory")
(defvar max-history 1000
  "Maximum number of retained history entries.")
(defvar- persist-schedule-ms 30000
  "Queued commands are persisted to project preferences every _ milliseconds.")

(defn- get-pref-node
  [project-name]
  (-?> project-name
    ccw.launching.LaunchUtils/getProject
    org.eclipse.core.resources.ProjectScope.
    (.getNode pref-node-id)))

(defn- queue-expression
  [project-name expr]
  (dosync
    (alter queued-commands update-in [project-name] conj expr))
  expr)

(declare schedule-job)

(defn get-history
  "Given a project name, returns a vector containing:

   - a vector of that project's expression history (strings)
   - a fn that takes a single string argument that schedules the argument
     to be appended to the persisted project's history, and returns the argument.

   If the project name is nil or a corresponding project cannot be found,
   then an empty history vector is returned, along with a no-op scheduling
   fn (which will nevertheless still return its argument)."
  [project-name]
  (schedule-job) ; ensure that history persistence job is scheduled upon first access
  (let [pref-node (get-pref-node project-name)]
    (if pref-node
      [(-?> pref-node
         (.get history-key "[]")
         read-string)
       (partial queue-expression project-name)]
      [[] identity])))

(defn- save-history
  [project-name history]
  (doto (get-pref-node project-name)
    (.put history-key (pr-str history))
    .flush))

(defvar- save-cmds-job
  (doto (proxy [Job] ["ccw REPL command history persistence"]
          (run [^IProgressMonitor pm]
            (.beginTask pm "Persisting REPL histories" IProgressMonitor/UNKNOWN)
            (doseq [[project-name exprs] (dosync (let [queued @queued-commands]
                                                  (ref-set queued-commands {})
                                                  queued))
                    :let [[history] (get-history project-name)]]
              (.subTask pm project-name)
              (save-history project-name (->> (reverse exprs)
                                           (concat history)
                                           (take-last max-history)
                                           ; prevent consecutive duplicate expressions
                                           (partition-by identity)
                                           (mapcat (partial take 1))
                                           vec)))
            (.done pm)
            (.schedule this persist-schedule-ms)
            org.eclipse.core.runtime.Status/OK_STATUS))
    (.setSystem true)))

(defn- schedule-job
  []
  (when (== Job/NONE (.getState save-cmds-job))
    (.schedule save-cmds-job)))