(ns ccw.leiningen.handlers
  (:require [ccw.leiningen.classpath-container :as cpc]
            [ccw.leiningen.nature              :as n]
            [clojure.string                    :as str]
            [ccw.util.eclipse                  :as e]
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
           [ccw.leiningen                  Activator
                                           Messages]
           [ccw.util                       Logger
                                           Logger$Severity]
           [java.io                        File
                                           FilenameFilter]))

(println "ccw.leiningen.handlers load starts")

(defn event->java-project
  "Extract (if possible) a java project from an execution event"
  [event]
  (let [sel (e/current-selection event)
        part (e/active-part event)]
    (cond
      (e/project part)
        (-> part e/project JavaCore/create)
      (instance? IStructuredSelection sel)
        ;; TODO consider giving the user a hint for why the expected command did not work
        (-> sel .getFirstElement e/project JavaCore/create))))

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
      (let [res  (boolean
                   (when-let [project (e/project receiver)]
                     (and
                       (.isOpen project)
                       (.exists (.getFile project "project.clj")))))]
        res)))) 

(defn add-leiningen-nature
  "Pre-requisites:
   - The event's selection has size one, and is an open project
   - The project does not already have the leiningen nature"
  [handler event]
  (let [project (e/project event)]
    (when-not (n/has-nature? project n/NATURE-ID)
    (e/run-in-background
      (fn [monitor]
        (.beginTask monitor 
          (str "Adding leiningen support to project " (.getName project))
          1)
        (n/set-description! 
          project
          (-> (n/description project)
            (n/add-natures! JavaCore/NATURE_ID)))
        (n/set-description! 
          project
          (-> (n/description project)
            (n/add-natures! n/NATURE-ID)))
        ; TODO : report done ?
        (.done monitor))))))

(defn- upgrade-project-build-path [java-project overwrite?]
  (e/run-in-background
    (fn [monitor]
      (n/reset-project-build-path java-project overwrite? monitor))))

(defn reset-project-build-path [handler event]
  (upgrade-project-build-path (event->java-project event) true))

(defn update-project-build-path [handler event]
  (upgrade-project-build-path (event->java-project event) false))

(println "ccw.leiningen.handlers namespace loaded")