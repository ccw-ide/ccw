(ns ccw.ClojureProjectNature
  (:import
    [ccw CCWPlugin ClojureCore]
    [java.io IOException File]
    [ccw.builder ClojureBuilder]
    [org.eclipse.core.runtime CoreException Platform Path Status IPath IProgressMonitor FileLocator]
    [org.eclipse.core.resources WorkspaceJob IResource]
    [org.eclipse.jdt.core JavaCore])
  (:use [clojure.contrib [duck-streams :as cc.stream]])
  (:gen-class
   :implements [org.eclipse.core.resources.IProjectNature]
   :init init
   #_:constructors #_{[java.io.Writer Integer] [], 
                  [java.io.Writer] []}
   #_:methods #_[[configure [] Void]
             [deconfigure [] Integer]
             [getMaxColumn [] Integer]
             [setMaxColumn [Integer] Void]
             [getWriter [] java.io.Writer]]
   :state state))

;; adding a definition to clojure.contrib.duck-streams
(defmethod cc.stream/copy [IPath IPath] [#^IPath input #^IPath output]
  (println "input:" input ", output:" output)
  (cc.stream/copy (.toFile input) (.toFile output)))
   
(defn- -init
  [] [[] (ref {:project nil :errors []})])

(defn- get-project-description
  "returns the project description or null if the project
   is null, closed, or an error occured while getting description
  "
  [proj]
  (cond
    (nil? proj) 
      (do
	      (CCWPlugin/logError "Could not add or remove clojure nature: project is null")
	      nil)
    ; closed clojure projects cannot be modified
    (not (.isOpen proj))
      (do
        (CCWPlugin/logWarning "Nature modification asked on a closed project!")
        nil)
    :else
      (try (.getDescription proj)
        (catch CoreException e
          (CCWPlugin/logError "Could not get project description", e)
          nil))))

(defn- builder-present?
  [builders builder-name]
  (some #(= builder-name (.getBuilderName %)) builders))
    
(defn- get-jar-inside-plugin
  [plugin-name jar-name]
  (try
    (let [bundle (Platform/getBundle plugin-name)
          clojure-bundle-path (FileLocator/getBundleFile bundle)]
      (if (.isFile clojure-bundle-path)
        (do
          (CCWPlugin/logError (str plugin-name " plugin should be deployed as a directory. This is a regression."))
          nil)
        (let [clojure-lib-entry (File. clojure-bundle-path
        											     (str jar-name ".jar"))]
          (if (.exists clojure-lib-entry)
            clojure-lib-entry
            (do
              (CCWPlugin/logError (str "Unable to locate " + jar-name " jar in " plugin-name " plugin. This is a regression."))
              nil)))))
    (catch IOException e
      (do
        (CCWPlugin/logError (str "Unable to find " plugin-name " plugin. This is probably a regression."))
        nil))))
          
(defn- has-classes-folder?
  [java-project]
  (not (nil? (.findPackageFragmentRoot java-project (-> java-project .getProject (.getFolder "classes") .getFullPath)))))     

(defn- has-path-on-classpath? 
  [java-project searched-path]
  (let [p (if (instance? Path searched-path) searched-path (Path. searched-path))]
    (not (nil? (.findElement java-project p)))))
    
(defn- has-clojure-contrib-on-classpath?
  [java-project]
  (has-path-on-classpath? java-project "clojure/contrib"))

(defn- has-clojure-on-classpath?
  [java-project]
  (has-path-on-classpath? java-project "clojure/lang"))  

(defn- has-classpath-entry?
  [java-project lib-path]
  (let [entries-old (.getRawClasspath java-project)]
    (some #(= lib-path (.getPath %)) entries-old)))

(defn- project-name
  [java-project] (-> java-project .getProject .getName))

(defn- make-workspace-job
  [job-name runInWorkspace-fn]
  (proxy [WorkspaceJob] [job-name]
    (runInWorkspace [monitor]
      (runInWorkspace-fn monitor))))

#_(defn- add-error!
  [instance mess]
  (io!
    (when (not (nil? mess))
      (dosync (alter (.state instance)
                update-in [:errors] conj mess)))))
    
(defn- throw-error
  [#_instance mess]
  #_(add-error! instance mess)
  (throw (CoreException.
           (Status. (Status/ERROR)
                    (CCWPlugin/PLUGIN_ID)
                    #_(apply str (interpose "\n" (:errors @(.state instance))))
                    mess))))
    
(defn- add-lib-on-classpath!
  [java-project lib-path libSrc-path copy?]
  (io!
    (if (nil? lib-path)
      (throw (CoreException. (Status/CANCEL_STATUS)))
      (let [entries-old (vec (.getRawClasspath java-project))]
      	(when-not (has-classpath-entry? java-project lib-path)
      	  (let [make-dest-path    #(-> java-project .getProject .getLocation (.append (.lastSegment %)))
      	        in-project-lib    (make-dest-path lib-path)
      	        in-project-libSrc (make-dest-path libSrc-path)]
      	        ;copy-lib-job      (make-workspace-job (str "adding clojure libraries to project " (project-name java-project))
      	        ;                    (fn [_] (cc.stream/copy lib-path in-project-lib)))
      	        ;copy-libSrc-job   (make-workspace-job (str "adding clojure-contrib libraries to project " (project-name java-project))
      	        ;                    (fn [_] (cc.stream/copy libSrc-path in-project-libSrc)))
      	        ;jobs              [copy-lib-job copy-libSrc-job]]
      	    ;(doseq [job jobs]
      	    ;  (println "scheduling job" job)
      	    ;  (.schedule job)
      	    ;  (try
      	    ;    ;(println "joining job" job)
      	    ;    ;(.join job)
      	    ;    ;(println "job" job )
      	    ;    (catch InterruptedException e
      	    ;      (throw-error "Error while trying to copy clojure.jar and/or clojure-contrib.jar in the project."))))
      	    (when copy?
      	    	(cc.stream/copy lib-path in-project-lib)
      	    	(cc.stream/copy libSrc-path in-project-libSrc)
      	    	(-> java-project .getProject (.refreshLocal (IResource/DEPTH_ZERO) nil)))
      	    (let [entries-new (into-array (conj entries-old (JavaCore/newLibraryEntry in-project-lib in-project-libSrc nil)))]
      	      (doto java-project
      	        (.setRawClasspath entries-new nil)
      	        (.save nil true)))))))))

(defn- file-to-path
  [file] (Path/fromOSString (.getAbsolutePath file)))


(defn- add-clojure-lib-on-classpath!
  [java-project]
  (add-lib-on-classpath!
    java-project
    (file-to-path (get-jar-inside-plugin "ccw.clojure", "clojure"))
    (file-to-path (get-jar-inside-plugin "ccw.clojure", "src"))
    true))      	    
      
(defn- add-clojure-contrib-lib-on-classpath!
  [java-project]
  (add-lib-on-classpath!
    java-project
    (file-to-path (get-jar-inside-plugin "ccw.clojurecontrib", "clojure-contrib"))
    (file-to-path (get-jar-inside-plugin "ccw.clojurecontrib", "src"))
    true))      	    

(defn- add-classes-directory!
  [java-project]
  (io!
	  (let [classes-folder (-> java-project .getProject (.getFolder "classes"))]
	    (if (not (.exists classes-folder))
	      (.create classes-folder true true nil)
	      ; TODO preparer un "rapport" au cas où certaines choses ne se soient pas correctement passees
	    )
	    (add-lib-on-classpath!
	      java-project
	      (.getFullPath classes-folder)
	      (-> java-project .getPath (.append "src"))
	      false))))

(defn- setup-clojure-project-classpath!
  [proj]
  (io!
	  (let [java-project (.getJavaProject (ClojureCore/getClojureProject proj))]
	    (doseq [[pred add-fn] {has-clojure-on-classpath? add-clojure-lib-on-classpath!
	                           has-clojure-contrib-on-classpath? add-clojure-contrib-lib-on-classpath!
	                           has-classes-folder? add-classes-directory!}]
	      (when (not (pred java-project)) (add-fn java-project))))))

(defn- insert-clojure-builder!
  [proj spec desc]
  (io!
	  (let [clojure-command (.newCommand desc)]
		  (.setBuilderName clojure-command (ClojureBuilder/BUILDER_ID))
		  (.setBuildSpec desc (into-array (cons clojure-command spec)))
		  (.setDescription proj desc (IResource/FORCE), nil))))
		             
(defn- -configure
  [this]
  (let [proj (:project @(.state this))]
	  (when-let [desc (get-project-description proj)]
	    (let [spec (.getBuildSpec desc)]
		    (when (not (builder-present? spec (ClojureBuilder/BUILDER_ID)))
		      (insert-clojure-builder! proj spec desc)
		      (setup-clojure-project-classpath! proj))))))
  
(defn- -deconfigure
  [this]
  (when-let [desc (get-project-description (.getProject this))]
    (let [spec (.getBuildSpec desc)]
      (when (builder-present? spec (ClojureBuilder/BUILDER_ID))
        (let [newSpec (remove #(= (ClojureBuilder/BUILDER_ID) (.getBuilderName %)) spec)]
          (.setBuildSpec desc (into-array newSpec))
          (try
            (.setDescription (.getProject this) desc nil)
            (catch CoreException e
              (CCWPlugin/logError "Could not set project description" e)))))))) 
  
(defn -getProject
  [this] (:project @(.state this)))

(defn -setProject
  [this proj] (dosync (alter (.state this) assoc :project proj)))      
