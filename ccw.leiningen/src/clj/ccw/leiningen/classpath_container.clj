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

#_(defn- lower-case-exts-set [ext-string]
  (into #{} (map #(.toLowerCase %) (.split ext-string ","))))

#_(defn- dir-filter [exts]
  (reify
  FilenameFilter
  (accept [this dir name]
    ;; lets avoid including filenames that end with -src since we will use this
    ;; as the convention for attaching source
    (let [name-segs (.split name "[.]")]
      (cond
        (not= (count name-segs) 2) false
        (-> (aget name-segs 0) (.endsWith "-src")) false
        (.contains exts (.toLowerCase (aget name-segs 1))) true
        :else false)))))

#_(defn- library-entry2 [lib]
  (let [ext (-> lib .getName (.split "[.]") (aget 1))
        src-arc (File. (-> lib .getAbsolutePath (.replace (str "." ext) (str "-src." ext))))
        src-path (when (.exists src-arc) (Path. (.getAbsolutePath src-arc)))]
    (JavaCore/newLibraryEntry
      (Path. (.getAbsolutePath lib)),
      src-path,
      (Path. "/"))))


#_(defprotocol ClasspathContainerValidation
  (isValid [this]))

;; TODO remove or adapt
#_(defn folder-content-classpath-container 
  [path, project]
  (println "leiningen-classpath-container called " path project)
  (let [exts (lower-case-exts-set (.lastSegment path))
        ; extract the directory string from the PATH and create the directory relative 
        ; to the project
        path (-> path (.removeLastSegments 1) (.removeFirstSegments 1))
        root-proj (-> project .getProject .getLocation .makeAbsolute .toFile)
        project-dir? (and 
                       (= (.segmentCount path) 1)
                       (= (.segment path 0) ROOT-DIR))
        dir (if project-dir? root-proj (File. root-proj (.toString path)))
        path (if project-dir? (.removeFirstSegments path 1) path)
        dir-filter (dir-filter exts)
        desc (str "/" path " Libraries")]
    (reify 
      IClasspathContainer
      (getClasspathEntries [this]
        (println "(.getClasspathEntries)" desc)
        (let [libs (.listFiles dir dir-filter)
              entry-list (map library-entry2 libs)]
          (into-array IClasspathEntry entry-list)))
      (getDescription [this]
        (println ".getDescription " desc)
        desc)
      (getKind [this] 
        (println ".getKind " desc)
        (IClasspathContainer/K_APPLICATION))
      (getPath [this]
        (println ".getPath " desc)
        path)
      
      ClasspathContainerValidation
      (isValid [this]
        (boolean
          (and (.exists dir)
               (.isDirectory dir)))))))

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
        dependencies (future (cp/resolve-dependencies lein-project))
        desc "Leiningen dependencies"]
    (reify 
      IClasspathContainer
      (getClasspathEntries [this]
        (println "(.getClasspathEntries)" desc)
        (let [entry-list (map library-entry (filter #(-> % .getName (.endsWith ".jar")) @dependencies))]
          (into-array IClasspathEntry entry-list)))
      (getDescription [this]
        (println ".getDescription " desc)
        desc)
      (getKind [this] 
        (println ".getKind " desc)
        (IClasspathContainer/K_APPLICATION))
      (getPath [this]
        (println ".getPath " desc)
        path)
      
      #_ClasspathContainerValidation
      #_(isValid [this]
        (boolean
          (and (.exists dir)
               (.isDirectory dir)))))))


(defn- create-and-register
  [container-path project]
  (if-let [container (leiningen-classpath-container container-path project)]
    (JavaCore/setClasspathContainer
      container-path
      (into-array IJavaProject [project])
      (into-array IClasspathContainer [container])
      nil)
    (do
      (println "Invalid container:" container-path)
      (.log logger 
        (Logger$Severity/WARNING)
        (str (Messages/InvalidContainer) container-path)))))

(defn initializer-factory 
  "Creates a ClasspathContainerInitializer instance for Leiningen projects"
  [ _ ]
  (println "initializer-factory called")
  (proxy [ClasspathContainerInitializer]
         []
    (initialize [container-path, project]
      (println (str "(LeiningenClasspathContainerInitializer.initialize "
                    container-path ", " (.getName project) ")"))
      (create-and-register container-path project))
    
    (canUpdateClasspathContainer [container-path, project]
      (println (str "(LeiningenClasspathContainerInitializer.canUpdateClasspathContainer "
                    container-path ", " (.getName project) ")"))
      false)
    
    (requestClasspathContainerUpdate [container-path, project, container-suggestion]
      (println (str "(LeiningenClasspathContainerInitializer.requestClasspathContainerUpdate "
                    container-path ", " (.getName project) ", " container-suggestion ")"))
      (create-and-register container-path project))
    
    (getDescription [container-path, project]
      (println (str "(LeiningenClasspathContainerInitializer.getDescription "
                    container-path ", " (.getName project) ")"))
      (proxy-super getDescription container-path, project))
    
    (getFailureContainer [container-path, project]
      (println (str "(LeiningenClasspathContainerInitializer.getFailureContainer "
                    container-path ", " (.getName project) ")"))
      nil)))

(println "classpath-container namespace loaded")