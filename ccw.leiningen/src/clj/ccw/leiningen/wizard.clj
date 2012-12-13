(ns ccw.leiningen.wizard
  (:require [ccw.leiningen.util :as u]
            [ccw.util.eclipse :as e]
            [ccw.leiningen.nature :as n]
            [ccw.leiningen.handlers :as handlers]
            [clojure.java.io :as io])
  (:import  [org.eclipse.core.resources IResource]
            [org.eclipse.jdt.core JavaCore]))

(defn check-project-name 
  "A valid project name must be without space, and
   a valid symbol."
  [project-name]
  (when
    (or
      (re-find #"\s+" project-name)
      (not (symbol? 
             (try 
               (binding [*read-eval* false]
                 (read-string project-name)) 
               (catch Exception _)))))
    "Project names must be valid Clojure symbols."))

(defn perform-finish [lein-project-name project template-name]
  (let [project-file (-> project .getLocation .toFile)]
    (println "lein-project-name:" lein-project-name
             \newline
             "project-file:" project-file)
    (u/lein-new (.getAbsolutePath project-file) template-name lein-project-name)
    (.refreshLocal project (IResource/DEPTH_INFINITE) nil)
    (handlers/add-natures
      project
      [(JavaCore/NATURE_ID) n/NATURE-ID]
      (str "Adding leiningen support to project " project))))