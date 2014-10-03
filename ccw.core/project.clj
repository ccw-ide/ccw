(defproject ccw.core "0.0.1-SNAPSHOT"
  :description "Counterclockwise"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [leiningen "2.3.4"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/core.incubator "0.1.0"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [net.cgrand/parsley "0.9.1"]
                 [ccw/ccw.server "0.1.1"]
                 ]
  :source-paths ["src" "src/clj"]
  :plugins [[ccw/lein-ccw-deps "0.1.0"]])
