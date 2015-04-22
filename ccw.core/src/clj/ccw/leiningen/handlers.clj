(ns ccw.leiningen.handlers
  (:require [ccw.leiningen.classpath-container :as cpc]
            [ccw.leiningen.nature              :as n]
            [ccw.leiningen.launch              :as launch]
            [ccw.leiningen.generic-launch      :as glaunch]
            [clojure.string                    :as str]
            [ccw.eclipse                       :as e]
            [clojure.java.io                   :as io]
            [ccw.core.trace                    :as t])
  (:import [org.eclipse.core.runtime       CoreException
                                           IPath
                                           Path
                                           IProgressMonitor
                                           Status]
           [org.eclipse.jdt.core           ClasspathContainerInitializer
                                           IClasspathContainer
                                           IJavaProject
                                           IClasspathEntry
                                           JavaCore]
           [org.eclipse.core.resources     IResource
                                           IProject
                                           IMarker
                                           ResourcesPlugin]
           [org.sonatype.aether.resolution DependencyResolutionException]
           [org.eclipse.jface.text         ITextSelection]
           [org.eclipse.jface.viewers      IStructuredSelection]
           [org.eclipse.core.expressions   PropertyTester]
           [ccw.editors.clojure            ClojureEditor]
           [ccw.launching                  ClojureLaunchShortcut]
           [ccw.leiningen                  Messages]
           [ccw.util                       Logger
                                           Logger$Severity]
           [java.io                        File
                                           FilenameFilter]))

(t/trace :leiningen "ccw.leiningen.handlers load starts")

(defn update-dependencies
  "Pre-requisites:
   - Either the event's selection is a selection containing resources of the 
     same project, in which case the first element will be picked for getting 
     the project to use
   - or the event's active Part can be coerced to a project and we can then try
     to check it.
   In both cases, the found project must be open, and with the Leiningen nature
   enabled."
  [handler event]
  (when-let [java-project (e/event->java-project event)]
    (cpc/update-project-dependencies java-project)))

(defn generic-launch
  "Pre-requisites:
   - Either the event's selection is a selection containing resources of the 
     same project, in which case the first element will be picked for getting 
     the project to use
   - or the event's active Part can be coerced to a project and we can then try
     to check it.
   In both cases, the found project must be open, and with the Leiningen nature
   enabled."
  [handler event]
  (if-let [project (e/event->project event)]
    (glaunch/generic-launch (when (e/project-open? project) project))
    (e/info-dialog "Leiningen Prompt" "unable to launch leiningen - no project found")))

(defn launch-headless-repl
  "Try to start a Leiningen Project. If focus is on a ClojureEditor, then first
   try to launch from the editor project, else try to launch from the selection."
  [handler event]
  (let [editor (e/active-part event)
        sel (e/current-selection event)]
    (cond
     (instance? ccw.editors.clojure.ClojureEditor editor)
       (future 
         (-> (ClojureLaunchShortcut.)
           (.launch editor
             nil
             true ;; force launching via leiningen
             )))
     :else
       (future
         (-> (ClojureLaunchShortcut.) 
           (.launch 
             sel
             nil 
             true ;; force launching via leiningen
             ))))))

(defn add-natures
  ([project natures legend]
    (doto (e/workspace-job
               legend
               (fn [^IProgressMonitor monitor]
                 (add-natures project natures legend monitor)))
      (.setUser true)
      (.setRule (e/workspace-root))
      (.schedule)))
  ([project natures legend ^IProgressMonitor monitor]
    (e/project-desc! 
      project
      (apply e/add-desc-natures! (e/project-desc project) natures))))

(defn remove-natures
  ([project natures legend]
    (doto (e/workspace-job
               legend
               (fn [^IProgressMonitor monitor]
                 (remove-natures project natures legend monitor)))
      (.setUser true)
      (.setRule (e/workspace-root))
      (.schedule)))
  ([project natures legend ^IProgressMonitor monitor]
    (e/project-desc!
      project
      (apply e/remove-desc-natures! (e/project-desc project) natures))))
  
(defn add-leiningen-nature
  "Pre-requisites:
   - The event's selection has size one, and is an open project
   - The project does not already have the leiningen nature"
  ([handler event]
    (when-let [project (e/project event)]
      (add-leiningen-nature project)))
  ([^IProject project]
    (add-natures
      project
      [(JavaCore/NATURE_ID) n/NATURE-ID]
      (str "Adding leiningen support to project " (.getName project)))))

(defn remove-leiningen-nature
  "Pre-requisites:
   - The event's selection has size one, and is an open project
   - The project does already have the leiningen nature"
  ([handler event]
    (when-let [project (e/project event)]
      (remove-leiningen-nature project)))
  ([^IProject project]
    (remove-natures
      project
      [n/NATURE-ID]
      (str "Removing leiningen support from project " (.getName project)))))

(defn add-leiningen-nature-with-monitor
  "Pre-requisites
   - The project does not already have the leiningen nature"
  [^IProject project ^IProgressMonitor monitor]
    (add-natures
      project
      [(JavaCore/NATURE_ID) n/NATURE-ID]
      ""
      monitor))

(defn upgrade-project-build-path
  ([java-project overwrite?]
    (doto
      (e/workspace-job
        (str "Upgrade project build path for project " (e/project-name java-project))
        (fn [^IProgressMonitor monitor]
          (n/reset-project-build-path java-project overwrite? monitor)))
      (.setUser true)
      (.schedule))))

(defn reset-project-build-path 
  ([handler event] (reset-project-build-path (e/event->java-project event)))
  ([java-project]
    (when java-project (upgrade-project-build-path java-project true))))

(defn update-project-build-path [handler event]
  (when-let [java-project (e/event->java-project event)]
    (upgrade-project-build-path java-project false)))

(t/trace :leiningen "ccw.leiningen.handlers namespace loaded")
