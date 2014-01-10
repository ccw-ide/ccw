(ns ccw.leiningen.handlers
  (:require [ccw.leiningen.classpath-container :as cpc]
            [ccw.leiningen.nature              :as n]
            [ccw.leiningen.launch              :as launch]
            [ccw.leiningen.generic-launch      :as glaunch]
            [clojure.string                    :as str]
            [ccw.eclipse                       :as e]
            [clojure.java.io                   :as io])
  (:import [org.eclipse.core.runtime       CoreException
                                           IPath
                                           Path
                                           IProgressMonitor]
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
           [ccw.leiningen                  Messages]
           [ccw.util                       Logger
                                           Logger$Severity]
           [java.io                        File
                                           FilenameFilter]))

(println "ccw.leiningen.handlers load starts")

(defn event->project
  "Extract (if possible) a project from an execution event"
  [event]
  (let [sel (e/current-selection event)
        part (e/active-part event)]
    (cond
      (e/project part) (e/project part)
      (and (instance? IStructuredSelection sel)
           (-> ^IStructuredSelection sel .getFirstElement))
        ;; TODO consider giving the user a hint for why the expected command did not work
        (some-> ^IStructuredSelection sel .getFirstElement e/resource .getProject e/project)
      :else (when-let [editor (e/active-editor event)]
              (e/project editor)))))

(defn event->java-project
  "Extract (if possible) a java project from an execution event"
  [event]
  (when-let [p (event->project event)]
    (JavaCore/create p)))

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
  (when-let [java-project (event->java-project event)]
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
  ;(println "update-dependencies")
  (if-let [project (event->project event)]
    (glaunch/generic-launch (when (e/project-open? project) project))
    (println "unable to launch leiningen - no project found")))

(defn launch-headless-repl
  "Same pre-requisites as generic-launch concerning the detection of the project"
  [handler event]
  (println "launch-headless-repl")
  (if-let [project-name (some-> event event->project e/project-open? e/project-name)]
    (launch/lein
      project-name "repl :headless"
      :launch-name (str project-name " lein repl"))
    (e/info-dialog "Headless REPL Launch" "No project found in the current context")))

(defn leiningen-enabled-project-factory 
  "Creates a PropertyTester. It will try to derive the IProject from the
   receiver, and if an IProject is found, detect if the project is open,
   and the Leiningen Nature is enabled."
  [_]
  (proxy [PropertyTester]
         []
    (test [receiver, property, args, expectedValue]
      (boolean
        (when-let [project (e/project receiver)]
          (and
            (.isOpen project)
            (.isNatureEnabled project "ccw.leiningen.nature")))))))

(defn leiningen-project-file-present-factory
  "Creates a PropertyTester whose purpose will be to test the presence of the
   project.clj file."
  [_]
  (proxy [PropertyTester]
         []
    (test [receiver, property, args, expectedValue]
      true
      #_(let [res  (boolean
                   (when-let [project (e/project receiver)]
                     (and
                       (.isOpen project)
                       (.exists (.getFile project "project.clj")))))]
        res)))) 

;; TODO too many imbrications here, decomplecting the code
;;      from the job, etc. will help ...
(defn add-natures
  [project natures legend]
  ;(println "add-natures:" project ", natures:" (seq natures) ", legend:'" legend "'")
  (e/run-in-background
    (e/runnable-with-progress-in-workspace
      (fn [^IProgressMonitor monitor]
        ;(println "add-natures: background job started for natures:" (seq natures))
        (.beginTask monitor 
          (str legend)
          1)
        (e/project-desc! 
          project
          (apply e/add-desc-natures! (e/project-desc project) natures))
        (.worked monitor 1)
        (.done monitor)
        ;(println "add-natures: background job stopped for natures:" (seq natures))
        )
      (e/workspace-root))))

(defn add-leiningen-nature
  "Pre-requisites:
   - The event's selection has size one, and is an open project
   - The project does not already have the leiningen nature"
  ([handler event]
    (add-leiningen-nature (e/project event)))
  ([^IProject project]
    (add-natures 
      project
      [(JavaCore/NATURE_ID) n/NATURE-ID]
      (str "Adding leiningen support to project " (.getName project)))))

(defn- upgrade-project-build-path [java-project overwrite?]
  (e/run-in-background
    (fn [monitor]
      (n/reset-project-build-path java-project overwrite? monitor))))

(defn reset-project-build-path [handler event]
  (upgrade-project-build-path (event->java-project event) true))

(defn update-project-build-path [handler event]
  (upgrade-project-build-path (event->java-project event) false))

(println "ccw.leiningen.handlers namespace loaded")
