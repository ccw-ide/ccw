;*******************************************************************************
;* Copyright (c) 2010 Stephan Muehlstrasser.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Stephan Muehlstrasser - initial API and implementation
;*******************************************************************************/
(ns ccw.support.labrepl.wizards.LabreplCreationOperation
  (:import
     [java.lang.reflect InvocationTargetException]
     [java.net URL]
     [java.util.zip ZipFile]
     [org.eclipse.core.runtime NullProgressMonitor
                            SubProgressMonitor
                            CoreException
                            IStatus
                            Platform
                            Status
                            FileLocator]
     [org.eclipse.ui PlatformUI]
     [org.eclipse.ui.dialogs IOverwriteQuery]
     [org.eclipse.jdt.core JavaCore]
     [org.eclipse.debug.core ILaunchManager]
     [java.io IOException]
     [org.eclipse.core.resources IResource ResourcesPlugin]
     [org.eclipse.jface.viewers StructuredSelection]
     [ccw ClojureCore]
     [ccw.support.labrepl Activator]
     [ccw.launching ClojureLaunchShortcut]
     [ccw.editors.antlrbased EvaluateTextAction]
     [org.eclipse.ui.wizards.datatransfer ImportOperation
                                          ZipFileStructureProvider]
     [org.eclipse.ui.browser IWorkbenchBrowserSupport])
  
  (:use
    [leiningen.core :only [read-project defproject]]
    [leiningen.deps :only [deps]])
  (:gen-class
   :implements [org.eclipse.jface.operation.IRunnableWithProgress]
   :constructors {[ccw.support.labrepl.wizards.LabreplCreateProjectPage org.eclipse.ui.dialogs.IOverwriteQuery] []}
   :init myinit
   :state state))

(defn- -myinit
  [pages overwrite-query]
  [[] (ref {:page pages :overwrite-query overwrite-query})])

(defn config-new-project
  [root name monitor]
  (try
    (let
      [project (.getProject root name)]
      (if (not (.exists project)) (.create project nil))
      (if (not (.isOpen project)) (.open project nil))
      (let
        [desc (.getDescription project)]
        (.setLocation desc nil)
        (.setDescription project desc (SubProgressMonitor. monitor 1))
        project))
    (catch CoreException exception (throw (InvocationTargetException. exception)))))

(defn get-zipfile-from-plugin-dir
  [plugin-relative-path]
  (try
    (let
      [bundle (.getBundle (Activator/getDefault))
        starter-url (URL. (.getEntry bundle "/") plugin-relative-path)]
      (ZipFile. (.getFile (FileLocator/toFileURL starter-url))))
    (catch IOException exception
      (let
        [message (str plugin-relative-path ": " (.getMessage exception))
          status (Status. IStatus/ERROR Activator/PLUGIN_ID IStatus/ERROR  message exception)]
        (throw (CoreException. status))))))

(defn import-files-from-zip
  [src-zip-file dest-path monitor overwrite-query]
  (let
    [structure-provider (ZipFileStructureProvider. src-zip-file)
      op (ImportOperation. dest-path (.getRoot structure-provider) structure-provider overwrite-query)]
    (.run op monitor)))

(defn make-project-folder
  [project folder monitor]
  (let [project-folder (.getFolder project folder)]
    (.create project-folder true true monitor)))

(defn do-imports
  [project monitor overwrite-query]
  (try
    (let
      [dest-path (.getFullPath project)
        additional-folders ["lib" "classes" "bin"]
        zip-file (get-zipfile-from-plugin-dir "examples/labrepl.zip")]
      (doall (map #(make-project-folder project % monitor) additional-folders))
      (import-files-from-zip zip-file dest-path (SubProgressMonitor. monitor 1) overwrite-query))
    (catch CoreException exception (throw (InvocationTargetException. exception)))))

 (defn open-browser
   "Open a browser for the running labrepl web page"
   []
   (let
     [browser-support (.getBrowserSupport (PlatformUI/getWorkbench))
      browser (.createBrowser 
                browser-support 
                (reduce bit-or 
                  [IWorkbenchBrowserSupport/LOCATION_BAR
                    IWorkbenchBrowserSupport/NAVIGATION_BAR
                    IWorkbenchBrowserSupport/AS_EDITOR]) 
                nil "Labrepl" "Labrepl Instructions")]
     (.openURL browser (URL. "http://localhost:8080"))))
 
(defn fix-libraries
  "Enter all the JAR files in the lib directory to the Java build path of the project"
  [project]
  (let
    [java-project (.getJavaProject (ClojureCore/getClojureProject project))
     lib-folder (.getFolder project "lib")
     _ (.refreshLocal lib-folder (IResource/DEPTH_ONE) nil)
     lib-members (.members lib-folder)
     old-lib-entries (vec (.getRawClasspath java-project))
     new-lib-entries
       (into-array 
         (concat old-lib-entries (map #(JavaCore/newLibraryEntry (.getFullPath %) nil nil) lib-members)))]
    (doto java-project
      (.setRawClasspath new-lib-entries nil)
      (.save nil true))))

(defn create-project
  [root page monitor overwrite-query]
  
  (.beginTask monitor "Configuring project..." 1)
  
  (let
    [project-name (.getProjectName page)
      project (config-new-project root project-name monitor)
      page-state @(.state page)
      run-lein-deps (.getSelection (:run-lein-deps-button page-state))]
    
    (do-imports project (SubProgressMonitor. monitor 1) overwrite-query)
    
    (if run-lein-deps
      (let
        [leiningen-pfile (.toOSString (.getLocation (.getFile project "project.clj")))
          labrepl-leiningen-project (read-project (str leiningen-pfile))
          run-repl (.getSelection (:run-repl-button page-state))]
        (deps labrepl-leiningen-project)
        (fix-libraries project)
        (if run-repl
          (let
            [startup-file-selection (StructuredSelection. (.getFile (.getFolder project "src") "labrepl.clj"))
              run-labrepl-action
                (proxy [EvaluateTextAction] ["Start labrepl"]
                  (run [] (.evaluateText "(labrepl/-main)")))]
            (.launch (ClojureLaunchShortcut.) startup-file-selection ILaunchManager/RUN_MODE)
            ; TODO the following does not work
            #_(.run run-labrepl-action)
            (open-browser)))))))

(defn -run
  [this monitor]
  (let
    [monitor (if monitor monitor (NullProgressMonitor.))
      state @(.state this)
      page (:page state)
      overwrite-query (:overwrite-query state)]
    (try
      (.beginTask monitor "Labrepl Creation" 1)
      (let [root (.getRoot (ResourcesPlugin/getWorkspace))]
        (create-project root page monitor overwrite-query))
      (finally (.done monitor)))))
