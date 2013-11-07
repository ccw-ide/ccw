(ns ccw.leiningen.launch
  (:require [ccw.util.launch :as launch]
            [ccw.util.eclipse :as e]))

(defn lein-launch-configuration 
  "project can be nil"
  ([project command] 
    (lein-launch-configuration project command nil))
  ([project command {:keys [leiningen-standalone-path] :as options}]
    {
     :private                true
     :launch-in-background   false
     
     :java/project-name      (and project (e/project-name project))
     :java/classpath         [{:entry-type :archive
                               :path (or 
                                       leiningen-standalone-path
                                       (e/get-file-inside-plugin "ccw.core" "leiningen-standalone.jar")) }
                              {:entry-type :jre-container
                               :name launch/default-jre-container-name}]
     :java/default-classpath false
     :java/vm-arguments      (str " -Dfile.encoding=UTF-8"
                                  " -Dmaven.wagon.http.ssl.easy=false"
                                  ;; TODO see if we can safely remove this argument 
                                  ;; " -Dleiningen.original.pwd=\"/Users/laurentpetit/tmp\""
                                  )
     :java/main-type-name    "clojure.main"
     :java/program-arguments (str "-m leiningen.core.main " command)
     }))

(defn lein 
  "project can be nil"
  [project command & {:keys [leiningen-standalone-path] :as rest}]
  (println (lein-launch-configuration project command rest))
  (launch/run (lein-launch-configuration project command rest)))
  
(comment 
  (lein "project-name" "repl :headless"))
 