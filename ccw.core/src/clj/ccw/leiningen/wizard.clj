(ns ccw.leiningen.wizard
  (:require [ccw.leiningen.util :as u]
            [ccw.eclipse :as e]
            [ccw.leiningen.nature :as n]
            [ccw.leiningen.handlers :as handlers]
            [clojure.java.io :as io]
            [ccw.core.trace :as t])
  (:import  [org.eclipse.core.resources IProject IResource]
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

(defn perform-finish [lein-project-name ^IProject project template-name template-args]
  (let [project-file (-> project .getLocation .toFile)]
    (t/format :leiningen
      "lein-project-name: %s\nproject-file: %s" lein-project-name project-file)
    (u/lein-new (.getAbsolutePath project-file) template-name lein-project-name template-args)
    (.refreshLocal project (IResource/DEPTH_INFINITE) nil)
    (handlers/add-natures
      project
      [(JavaCore/NATURE_ID) n/NATURE-ID]
      (str "Adding leiningen support to project " project))))
