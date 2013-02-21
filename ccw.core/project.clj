(defproject ccw.core "0.0.1-SNAPSHOT"
  :description "Counterclockwise"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0-RC6"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [leiningen-core "2.0.0"]
                 [org.clojure/tools.nrepl "0.2.1"]
                 [org.clojure/core.incubator "0.1.0"]
                 [com.cemerick/drawbridge "0.0.6"]]
  :source-paths []
  :plugins [[ccw/lein-ccw-deps "0.1.0"]])