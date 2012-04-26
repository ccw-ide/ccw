(ns ccw.leiningen.builder
  (:require [ccw.leiningen.classpath-container :as cpc]
            
            
            [leiningen.core.project :as p]
            [leiningen.core.classpath :as cp]
            [clojure.string :as str]
            [ccw.util.eclipse :as e]
            [clojure.java.io :as io])
  (:import 
           [org.eclipse.core.runtime CoreException
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
                                       ResourcesPlugin
                                       IncrementalProjectBuilder]
           [org.sonatype.aether.resolution DependencyResolutionException]
           [org.eclipse.jface.text ITextSelection]
           [org.eclipse.jface.viewers IStructuredSelection]
           [org.eclipse.core.expressions PropertyTester]
           [ccw.leiningen Activator
                          Messages
                          ]
           [ccw.util Logger
                     Logger$Severity]
           [java.io File
                    FilenameFilter]))

(println "ccw.leiningen.builder load starts")

(def id "ccw.leiningen.builder")

(defn factory [_]
  (proxy [IncrementalProjectBuilder]
           []
      (build
        [kind args monitor]
        (when-let [project (proxy-super getProject)]
          (when (#{IncrementalProjectBuilder/AUTO_BUILD IncrementalProjectBuilder/INCREMENTAL_BUILD} kind)
            (let [delta (proxy-super getDelta)]
              (println (-> delta .getResource .getLocation))
              (println (-> delta (.findMember (e/path "project.clj")) .getResource .getLocation))))))))

(println "ccw.leiningen.builder namespace loaded")