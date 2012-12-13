(ns ccw.leiningen.launch
  ;(:require [ccw.util.launch :as launch])
  (require [ccw.util.eclipse :as e]))

(alias 'launch 'ccw.util.launch)

(defn lein [project command]
  (let [launch {
                :private                true
                :java/project-name      (e/project-name project)
                :java/classpath         [{:entry-type :archive
                                          :path "/Users/laurentpetit/.lein/self-installs/leiningen-2.0.0-preview10-standalone.jar"}
                                         {:entry-type :jre-container
                                          :name launch/default-jre-container-name}]
                :java/default-classpath false
                :java/vm-arguments      (str " -Dfile.encoding=UTF-8"
                                             " -Dmaven.wagon.http.ssl.easy=false"
                                             " -Dleiningen.original.pwd=\"/Users/laurentpetit/tmp\"")
                :java/main-type-name    "clojure.main"
                :java/program-arguments (str "-m leiningen.core.main " command)
                }]
    (launch/run launch)))
  
  
