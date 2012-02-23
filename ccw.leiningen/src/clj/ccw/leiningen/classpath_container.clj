(ns ccw.leiningen.classpath-container
  (:require [leiningen.core.project :as p]
            [leiningen.core.classpath :as cp])
  (:import [org.eclipse.core.runtime CoreException
                                     IPath
                                     Path]
           [org.eclipse.jdt.core ClasspathContainerInitializer
                                 IClasspathContainer
                                 IJavaProject
                                 IClasspathEntry
                                 JavaCore]
           [org.eclipse.core.resources IResource
                                       IProject
                                       IMarker
                                       IWorkspaceRunnable
                                       IWorkspace
                                       ResourcesPlugin]
           [ccw.leiningen Activator
                          Messages
                          ]
           [ccw.util Logger
                     Logger$Severity]
           [java.io File
                    FilenameFilter]))

(println "ccw.leiningen.classpath-container load starts")

(def ROOT-DIR (Path. "ccw.LEININGEN_CONTAINER"))

(def logger (Logger. (Activator/PLUGIN_ID)))

(defn- library-entry [file]
  (JavaCore/newLibraryEntry
      (Path. (.getAbsolutePath file)),
      nil, ; TODO add src jar when available
      (Path. "/")))

(defn leiningen-classpath-container 
  [path, project]
  (println "leiningen-classpath-container called " path project)
  (let [project-clj (-> project .getProject (.getFile "project.clj") .getLocation .toOSString)
        lein-project (p/read project-clj)
        dependencies (cp/resolve-dependencies lein-project)
        desc "Leiningen dependencies"]
    (println "dependencies::::::::::::::::::::::")
    (println (map #(.getAbsolutePath %) dependencies))
    (reify 
      IClasspathContainer
      (getClasspathEntries [this]
                           (println "(.getClasspathEntries)" desc)
                           (let [entry-list (map library-entry (filter #(-> % .getName (.endsWith ".jar")) dependencies))]
                             (into-array IClasspathEntry entry-list)))
      (getDescription [this]
                      (println ".getDescription " desc)
                      desc)
      (getKind [this] 
               (println ".getKind " desc)
               (IClasspathContainer/K_APPLICATION))
      (getPath [this]
               (println ".getPath " desc)
               path))))



;; TODO move this stuff in some central place
(defn- run-in-workspace
  "runnable is a function which takes an IProgressMonitor as its argument.
   rule allows to restrain the scope of locked workspace resources.
   avoid-update? enables grouping of resource modification events.
   progress-monitor optional monitor for reporting."
  [runnable rule avoid-update? progress-monitor]
  (let [avoid-update (if avoid-update? IWorkspace/AVOID_UPDATE 0)]
    (-> (ResourcesPlugin/getWorkspace)
      (.run runnable rule avoid-update progress-monitor))))

(defn- workspace-runnable [f]
  (reify IWorkspaceRunnable (run [this monitor] (f monitor))))

(def LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE "ccw.leiningen.problemmarkers.classpathcontainer")

(defprotocol Resource
  (resource [this] "Return a IResource instance"))

(extend-protocol Resource
  IResource
  (resource [this] this)
  
  IJavaProject
  (resource [this] (.getProject this))
  
  nil
  (resource [this] nil))

(defn- delete-container-markers [?project]
  (.deleteMarkers (resource ?project) 
    LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE,
    true,
    IResource/DEPTH_ONE ; DEPTH_ONE so that we also remove markers from project.clj
    ))

(defn- add-container-marker 
  "Delete previous container markers, add new one"
  [?project message]
  (run-in-workspace
    (workspace-runnable 
      (fn [_] 
        (delete-container-markers ?project)
        (let [marker (.createMarker (resource ?project) LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE)]
          (.setAttributes marker
            (let [m (java.util.HashMap.)]
              (doto m
                (.put IMarker/MESSAGE message)
                (.put IMarker/PRIORITY (Integer. IMarker/PRIORITY_HIGH))
                (.put IMarker/SEVERITY (Integer. IMarker/SEVERITY_ERROR))))))))
    nil
    true
    nil))

(defn- report-container-error [?project message e]
  (add-container-marker (resource ?project) message)
  (println message)
  (.printStackTrace e))

(defn- resource-message
  "Return [resource message] where resource is the Eclipse IResource to be used
   as the target of the problem marker, and message is personalized given exc
   and java-project values"
  [exc java-project]
  (cond
    (.contains (.getMessage exc) "project.clj")
      (if (.contains (.getMessage exc) "FileNotFoundException")
        [(resource java-project) "project.clj file is missing"]
        [(-> java-project resource (.getFile "project.clj")) "problem with project.clj file"])
    (.contains (.getMessage exc) "DependencyResolutionException")
      [(resource java-project) "problem when grabbing dependencies from repositories."]
    :else
      [(resource java-project) "unknown problem"]))

(defn- create-and-register
  [container-path java-project]
  (try
    (let [container (leiningen-classpath-container container-path java-project)]
      (JavaCore/setClasspathContainer
        container-path
        (into-array IJavaProject [java-project])
        (into-array IClasspathContainer [container])
        nil)
      (delete-container-markers java-project)
      #_(catch Exception e
          (println (str (Messages/InvalidContainer) container-path))
          (.log logger 
            (Logger$Severity/WARNING)
            (str (Messages/InvalidContainer) container-path))))
    (catch Exception e
      (let [[jresource message] (resource-message e java-project)]
        (report-container-error 
          jresource
          (str "Project '" (-> java-project resource .getName) "', Leiningen classpath container problem: " message)
          e))
      ; We expliclty return nil to enable lazy-reloading of classpath container 
      nil)))

(defn initializer-factory 
  "Creates a ClasspathContainerInitializer instance for Leiningen projects"
  [ _ ]
  (println "initializer-factory called")
  (proxy [ClasspathContainerInitializer]
         []
    (initialize [container-path, java-project]
      (println (str "(LeiningenClasspathContainerInitializer.initialize "
                    container-path ")"))
      (create-and-register container-path java-project))
    
    (canUpdateClasspathContainer [container-path, java-project]
      (println (str "(LeiningenClasspathContainerInitializer.canUpdateClasspathContainer "
                    container-path ")"))
      false)
    
    (requestClasspathContainerUpdate [container-path, java-project, container-suggestion]
      (println (str "(LeiningenClasspathContainerInitializer.requestClasspathContainerUpdate "
                    container-path ", " container-suggestion ")"))
      (create-and-register container-path java-project))
    
    (getDescription [container-path, java-project]
      (println (str "(LeiningenClasspathContainerInitializer.getDescription "
                    container-path ")"))
      (proxy-super getDescription container-path, java-project))
    
    (getFailureContainer [container-path, java-project]
      (println (str "(LeiningenClasspathContainerInitializer.getFailureContainer "
                    container-path ")"))
      nil)))

(println "classpath-container namespace loaded")