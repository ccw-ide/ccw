;*******************************************************************************
;* Copyright (c) 2009 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Laurent PETIT - initial API and implementation
;*******************************************************************************/
(ns ccw.clojure-project-nature
  (:import
    [ccw CCWPlugin ClojureCore]
    [java.io IOException File]
    [ccw.builder ClojureBuilder] 
    [org.eclipse.core.runtime CoreException Platform Path Status IPath IProgressMonitor FileLocator]
    [org.eclipse.core.resources WorkspaceJob IResource ResourcesPlugin]
    [org.eclipse.jdt.core JavaCore])
  (:require [clojure.java.io :as io]))

;; FIXME : see in Clojure 1.3 if it is possible to extend copy's behaviour ...
(defn copy [^IPath input ^IPath output & opts]
  (apply io/copy (.toFile input) (.toFile output) opts))

;; adding a method to clojure.java.io
;(defmethod clojure.java.io/do-copy [IPath IPath] 
;  [^IPath input ^IPath output opts]
;  (clojure.java.io/do-copy (.toFile input) (.toFile output) opts))

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
  (not (nil? (.findPackageFragmentRoot java-project (-> java-project .getProject (.getFolder "classes") .getLocation)))))     

(defn has-path-on-classpath? 
  [java-project searched-path]
  (let [p (if (instance? Path searched-path) searched-path (Path. searched-path))]
    (not (nil? (.findElement java-project p)))))
    
(defn has-clojure-contrib-on-classpath?
  [java-project]
  (has-path-on-classpath? java-project "clojure/contrib"))

(defn has-clojure-on-classpath?
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

(defn- throw-error
  [mess]
  (throw (CoreException.
           (Status. (Status/ERROR)
                    (CCWPlugin/PLUGIN_ID)
                    mess))))
(defn- make-ws-path
  "Make a workspace-relative path from the given path if possible"
  [path]
  (if path
    (let
	    [uri (-> path .toFile .toURI)
				root (.getRoot (ResourcesPlugin/getWorkspace))
				files (.findFilesForLocationURI root uri)]
     (if (pos? (count files))
				(.getFullPath (aget files 0))
				path))))

(defn- add-lib-on-classpath!
  [java-project lib-path libSrc-path copy?]
  (println "about to add lib-path=" lib-path ", libSrc-path=" libSrc-path)
  (io!
    (if (nil? lib-path)
      (throw (CoreException. (Status/CANCEL_STATUS)))
      (let [entries-old (vec (.getRawClasspath java-project))]
      	(when-not (has-classpath-entry? java-project lib-path)
      	  (let [make-dest-path    #(-> java-project .getProject .getLocation (.append (.lastSegment %)))
      	        in-project-lib    (make-dest-path lib-path)
      	        in-project-libSrc (when libSrc-path
      	        										(if copy?
		      	        									(-> in-project-lib 
		      	        										(.removeLastSegments 1) 
		      	        										(.append (-> in-project-lib 
		      	        																.removeFileExtension 
		      	        																.lastSegment
		      	        																(str "-src")))
		      	        										(.addFileExtension (.getFileExtension libSrc-path)))
		      	        									(make-dest-path libSrc-path)))]
      	    (when copy?
      	    	(copy lib-path in-project-lib)
      	    	(when in-project-libSrc (copy libSrc-path in-project-libSrc)))
      	    (let 
             [entries-new 
               (into-array 
                 (conj entries-old 
                   (JavaCore/newLibraryEntry (make-ws-path in-project-lib) (make-ws-path in-project-libSrc) nil)))]
             (println "java-project:" java-project)
             (println "entries-new:" (seq entries-new))
      	      (doto java-project
      	        (.setRawClasspath entries-new nil)
      	        (.save nil false)
                (-> .getProject (.refreshLocal (IResource/DEPTH_INFINITE) nil))))))))))

(defn- file-to-path
  [file] (Path/fromOSString (.getAbsolutePath file)))

(defn- add-clojure-lib-on-classpath!
  [java-project]
  (add-lib-on-classpath!
    java-project
    (file-to-path (get-jar-inside-plugin "ccw.clojure", "clojure"))
    (file-to-path (get-jar-inside-plugin "ccw.clojure", "src"))
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
	      nil ; (-> java-project .getPath (.append "src"))
	      false))))

(defn- setup-clojure-project-classpath!
  [proj]
  (io!
	  (let [java-project (.getJavaProject (ClojureCore/getClojureProject proj))]
	    (doseq [[pred add-fn] {has-clojure-on-classpath? add-clojure-lib-on-classpath!
	                           has-classes-folder? add-classes-directory!}]
	      (when (not (pred java-project)) (add-fn java-project))))))

(defn- insert-clojure-builder!
  [proj spec desc]
  (io!
	  (let [clojure-command (.newCommand desc)]
		  (.setBuilderName clojure-command (ClojureBuilder/BUILDER_ID))
		  (.setBuildSpec desc (into-array (cons clojure-command spec)))
		  (.setDescription proj desc (IResource/FORCE), nil))))
		             

(defn make []
  (let [state (ref {:project nil :errors []})]
    (reify org.eclipse.core.resources.IProjectNature
    (configure
      [this]
      (let [proj (:project @state)]
        (when-let [desc (get-project-description proj)]
          (let [spec (.getBuildSpec desc)]
            (when (not (builder-present? spec (ClojureBuilder/BUILDER_ID)))
              (insert-clojure-builder! proj spec desc)
              (setup-clojure-project-classpath! proj))))))
    (deconfigure
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
    (getProject
      [this] (:project @state))
    (setProject
      [this proj] (dosync (alter state assoc :project proj))))))

(defn factory [ _ ] (make))