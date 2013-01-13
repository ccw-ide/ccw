(ns ccw.repl.cmdhistory
  (:use [clojure.core.incubator :only [-?>]])
  (:require [ccw.util.edn :refer [read-vector]])
  (:import
    ccw.CCWPlugin
    ccw.preferences.PreferenceConstants
    org.eclipse.core.runtime.jobs.Job
    org.eclipse.core.resources.IResource
    org.eclipse.core.runtime.IProgressMonitor
    org.osgi.service.prefs.BackingStoreException
    org.eclipse.core.runtime.preferences.IEclipsePreferences
    org.eclipse.jface.preference.IPreferenceStore))


(def ^{:private true
       :doc "Map between project names and vectors of REPL expressions that have yet to
   be persisted to the respective projects' preferences.  This is cleared
   periodically."}
      queued-commands (ref {}))

(def ^{:private true}
      pref-node-id "ccw.repl.cmdhistory")

(def ^{:private true}
      history-key "cmdhistory")

(defn ^IPreferenceStore preference-store
  []
  (.getCombinedPreferenceStore (CCWPlugin/getDefault)))

(defn max-history
  "Maximum number of retained history entries."
  []
  (.getInt (preference-store) PreferenceConstants/REPL_HISTORY_MAX_SIZE))

(defn- persist-schedule-ms
  "Queued commands are persisted to project preferences every _ milliseconds."
  []
  (.getInt (preference-store) PreferenceConstants/REPL_HISTORY_PERSIST_SCHEDULE))

(defn- ^IEclipsePreferences get-pref-node
  [^String project-name]
  (-?> project-name
    ccw.launching.LaunchUtils/getProject
    org.eclipse.core.resources.ProjectScope.
    (.getNode pref-node-id)
    (doto .sync))) 

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
      [(into (or (-?> pref-node
                   (.get history-key "[]")
                   read-vector)
               [])
         (@queued-commands project-name))
       (partial queue-expression project-name)]
      [[] identity])))

(defn- save-cmds
  [^IProgressMonitor pm queued-commands]
  (loop [[[^String project-name exprs] & rqs :as queued-commands] (seq queued-commands)
         retrying? false]
    (when project-name
      (let [[history] (get-history project-name)
            node (get-pref-node project-name)
            retry? (atom false)]
        (.subTask pm project-name)
        (try (doto node
               (.put history-key (->> (reverse exprs)
                                   (concat history)
                                   (take-last (max-history))
                                   ; prevent consecutive duplicate expressions
                                   (partition-by identity)
                                   (mapcat (partial take 1))
                                   vec
                                   pr-str))
               .flush)
          (catch BackingStoreException e
            (if retrying?
              (CCWPlugin/logError e)
              (reset! retry? true))))
        (if (not @retry?)
          (recur rqs false)
          (do
            ; maybe someone touched our project prefs; refresh
            (CCWPlugin/log (format "Refreshing settings dir for %s, re-attempting saving REPL command history" project-name))
            (-?> project-name
              ccw.launching.LaunchUtils/getProject
              (.getFolder ".settings")
              (.refreshLocal IResource/DEPTH_INFINITE nil))
            (recur queued-commands true)))))))

(def ^{:private true} save-cmds-job
  (doto (proxy [Job] ["ccw REPL command history persistence"]
          (run [^IProgressMonitor pm]
            (try
              (.beginTask pm "Persisting REPL histories" IProgressMonitor/UNKNOWN)
              (save-cmds pm (dosync (let [queued (ensure queued-commands)]
                                      (ref-set queued-commands {})
                                      queued)))
              (.done pm)
              org.eclipse.core.runtime.Status/OK_STATUS
              (finally
                (.schedule ^Job this (persist-schedule-ms))))))
    (.setSystem true)))

(defn- schedule-job
  []
  (when (== Job/NONE (.getState ^Job save-cmds-job))
    (.schedule ^Job save-cmds-job)))