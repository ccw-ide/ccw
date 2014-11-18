(ns ccw.leiningen.launch
  (:require [ccw.launch      :as launch]
            [ccw.eclipse     :as e]))

(def leiningen-standalone-path-pref
  "ccw.core's preference key for storing the absolute filesystem path of a leiningen standalone jar
   to use instead of the one provided by Counterclockwise."
  "ccw.leiningen.standalone-path.pref")

(defn lein-launch-configuration 
  "project can be nil"
  ([project command] 
    (lein-launch-configuration project command nil))
  ([project command {:keys [leiningen-standalone-path launch-name] :as options}]
    (let [leiningen-standalone-path (or leiningen-standalone-path
                                        (e/preference leiningen-standalone-path-pref nil))
          leiningen-standalone-path (if-not (seq leiningen-standalone-path)
                                      (e/get-file-inside-plugin "ccw.core" "leiningen-standalone.jar")
                                      leiningen-standalone-path)]
      {
       :private                true
       :launch-in-background   false
       :append-environment-variables true
       :name                   launch-name
       
       :java/project-name      (and project (e/project-name project))
       :java/classpath         [{:entry-type :lib
                                 :path leiningen-standalone-path}
                                {:entry-type :lib
                                 :path (e/get-file-inside-plugin "ccw.core" "lein")}
                                {:entry-type :jre-container
                                 :name launch/default-jre-container-name}]
       :java/default-classpath false
       :java/vm-arguments      (str 
                                 " -client" 
                                 " -Xbootclasspath/a:" "\"" (.toOSString (e/path leiningen-standalone-path)) "\"" 
                                 " -XX:+TieredCompilation"
                                 " -XX:TieredStopAtLevel=1"
                                 " -Dfile.encoding=UTF-8"
                                 " -Dmaven.wagon.http.ssl.easy=false")
       :java/main-type-name    "clojure.main"
       :java/program-arguments (str "-m ccw.leiningen.main " command)
      })))

(defn lein 
  "project can be nil"
  [project command & {:keys [leiningen-standalone-path launch-name] :as rest}]
  (println (lein-launch-configuration project command rest))
  (launch/run (lein-launch-configuration project command rest)))
  
(comment 
  (lein "project-name" "repl :headless"))

