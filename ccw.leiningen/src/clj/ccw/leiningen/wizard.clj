(ns ccw.leiningen.wizard
  (:require [ccw.leiningen.util :as u]
            [ccw.util.eclipse :as e]
            [ccw.leiningen.nature :as n]
            [ccw.leiningen.handlers :as handlers]
            [clojure.java.io :as io])
  (:import  [org.eclipse.core.resources IResource]
            [org.eclipse.jdt.core JavaCore]))

(defn perform-finish [project]
  (let [project-name (.getName project)
        project-file (-> project .getLocation .toFile)]
    (println "project-name:" project-name
             \newline
             "project-file:" project-file)
    ;(handlers/add-leiningen-nature (e/project project-name))
    ;(.refreshLocal (e/project project-name) (IResource/DEPTH_INFINITE) nil)
    ;(Thread/sleep 2000)
    (u/lein-new (.getAbsolutePath project-file) project-name)
    (.refreshLocal project (IResource/DEPTH_INFINITE) nil)
    (handlers/add-natures
      project
      [(JavaCore/NATURE_ID) n/NATURE-ID]
      (str "Adding leiningen support to project " project-name))
    #_(handlers/add-natures
      project
      ["ccw.nature"]
      "Adding Clojure Support")
    
    ;(.refreshLocal (e/project project-name) (IResource/DEPTH_INFINITE) nil)
    ))