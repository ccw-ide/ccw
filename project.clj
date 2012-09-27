(defproject org.lpetit/paredit.clj "0.18.1.STABLE001"
  :description "paredit in clojure, tailored for clojure"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure           "1.3.0"]
                 [org.clojure/core.incubator "0.1.1"]
                 [org.lpetit/net.cgrand.parsley "0.0.12.STABLE001"]
                 [org.lpetit/net.cgrand.regex   "0.0.4.STABLE001"]]
  :warn-on-reflection true)
  #_:manifest #_{"Project-awesome-level" "super-great"
             ;; function values will be called with the project as an argument.
             "Class-Path" ~#(clojure.string/join
                             \space
                             (leiningen.core.classpath/get-classpath %))
             ;; symbol values will be resolved to find a function to call.
             "Grunge-level" my.plugin/calculate-grunginess}
;:manifest {
;"Bundle-ManifestVersion" "2"
;"Bundle-Name" Regex
;Bundle-SymbolicName: net.cgrand.regex;singleton:=true
;Bundle-Version: 0.0.4.STABLE001
;Bundle-ClassPath: .,
; classes/
;Bundle-ActivationPolicy: lazy
;Bundle-Vendor: Christophe Grand
;Bundle-RequiredExecutionEnvironment: J2SE-1.5
;Export-Package: net.cgrand
;Import-Package: clojure;version="1.3.0",
; clojure.asm;version="1.3.0",
; clojure.asm.commons;version="1.3.0",
; clojure.core;version="1.3.0",
; clojure.core.protocols;version="1.3.0",
; clojure.core.proxy$clojure.lang;version="1.3.0",
; clojure.inspector.proxy$java.lang;version="1.3.0",
; clojure.inspector.proxy$javax.swing.table;version="1.3.0",
; clojure.java;version="1.3.0",
; clojure.java.browse_ui.proxy$java.lang;version="1.3.0",
; clojure.java.io;version="1.3.0",
; clojure.lang;version="1.3.0",
; clojure.pprint.proxy$java.io;version="1.3.0",
; clojure.repl.proxy$java.io;version="1.3.0",
; clojure.test;version="1.3.0"

