(ns ccw.leiningen.util
  (:use [clojure.core.incubator :only [-?> -?>>]])
  (:require [ccw.util.eclipse :as e]
            [leiningen.core.eval :as eval]
            [classlojure.core :as c]
            [clojure.java.io :as io])
  (:import [org.eclipse.core.resources IProject]
           [org.eclipse.core.runtime Platform
                                     FileLocator]))

(println "ccw.leiningen.util load starts")

;; TODO port back to ccw.core for the nature (place in ccw.util)
(defn bundle-file 
  "given bundle-name (a String), return the java.io.File corresponding to the
   currently loaded instance of this bundle"
  [bundle-name]
  (println "bundle-file:" bundle-name)
  (-> bundle-name Platform/getBundle FileLocator/getBundleFile))

(defn bundle-dir 
  "given bundle-name (a String), assumes there exists a currently loaded instance
   of it, and that it has been installed as a directory and not a jar.
   Return the java.io.File corresponding to its directory.
   (So, it's really just bundle-file with a check)"
  ^File [bundle-name]
  (let [^File bundle-dir (bundle-file bundle-name)]
    (if (.isFile bundle-dir)
      (throw (RuntimeException. (str bundle-name " bundle should be deployed as a directory.")))
      bundle-dir)))

(defn- plugin-entry
  "given a plugin-name and an entry name inside the plugin, returns the 
   java.io.File instance for the entry if it is found in the plugin."
  [plugin-name ^String entry-name]
  (let [bundle-dir (bundle-dir plugin-name)
        entry (File. bundle-dir entry-name)]
    (if (.exists entry)
      entry
      (throw (RuntimeException. (str "Unable to locate " + entry-name " in " plugin-name " plugin."))))))

(defn lein-env 
  "Create a new classlojure based leiningen environment by fetching all jars
   and classes directories found inside the ccw.leiningen-core plugin."
  []
  (let [leiningen-core (io/file (bundle-dir "ccw.core") "lib")
        jar (fn [^File f] (and (.isFile f) (.endsWith (.getName f) ".jar")))
        classes-dir (fn [d] (let [manifest (io/file d "META-INF" "MANIFEST.MF")]
                               (and (.exists manifest)
                                    (.isFile manifest))))
        libs (->> leiningen-core file-seq (filter #(or (jar %) (classes-dir %))))]
    (c/classlojure leiningen-core)))

(defonce ^{:doc 
           "Ref of map of \"project-name\" -> delay of classlojure environment.
            e.g. : (ref {\"project-foo\" (delay (lein-env))})" }
          projects-envs (ref {}))

(defn project-env!
  "Returns a classlojure environment for project, creating one if none exists yet,
   or if recreate? is true."
  [project & recreate?]
  (let [pname (if (= :project-less project) 
                project
                (-> project ^IProject e/project .getName))]
    (dosync
      (commute projects-envs
               #(if (or recreate? (not (% pname)))
                  (assoc % pname (delay (lein-env)))
                  %)))
    @(@projects-envs pname)))

(defn file-exists? 
  "Return the file if it exists, or nil" 
  [^File f]
  (when (.exists f) f))

(defn eval-in-project 
  "Evaluates form in the leiningen environment for project. If args are provided,
   consider form is a function and call it with args applied to it."
  [project form & args]
  (apply c/eval-in (project-env! project) form args))

(defn project-clj 
  "Given project (which must extend IProjectCoercible), returns its project.clj
   absolute path in the filesystem."
  [project]
  (-> project
    e/project
    (.getFile "project.clj")
    .getLocation
    .toOSString))

(defn lein-project
  "Given a project (anything that coerces to ccw.util.eclipse/IProjectCoercion),
   analyze its project.clj file and return the project map.
   If static-loading? is true, does not dynamically load plugins, middlewares, etc.
   (e.g. does not call leiningen.core.project/init-project).
   project can be the specific key :project-less to get the environment associated with no specific project"
  [project & {:keys [static-loading? enhance-fn] :or {enhance-fn identity}}]
  (eval-in-project project '(require 'leiningen.core.project))
  (let [project-map (if (= :project-less project)
                      (eval-in-project 
                        project
                        `(leiningen.core.project/merge-profiles 
                           (update-in leiningen.core.project/defaults
                             [:repositories] (fnil conj []) ["clojars-ccw-added" {:url "https://clojars.org/repo/"}])
                           [:user :default]))
                      (eval-in-project
                        project 
                        'leiningen.core.project/read
                        (project-clj project)))
        project-map (enhance-fn project-map)]
    (when-not static-loading?
      (eval-in-project 
        project
        (if (= :project-less project)
          `(do
             (leiningen.core.project/load-certificates '~project-map)
             (leiningen.core.project/load-plugins '~project-map))
          `(leiningen.core.project/init-project '~project-map))))
    project-map))

(defn lein-native-platform-path [lein-project]
  (eval/native-arch-path lein-project))

(defn lein-new [location template name & args]
  (let [project-map 
         (lein-project 
           :project-less
           :enhance-fn 
           (fn [p]
             (eval-in-project 
               :project-less
               `(-> '~p
                  (leiningen.core.project/add-profiles
                    '{:user 
                      ~'{:plugins [[lein-newnew "0.3.3"]]}})
                  (leiningen.core.project/merge-profiles 
                    [:user])))))]
    (eval-in-project 
      :project-less
      `(do
         (require 'leiningen.new)
         (binding [leiningen.new.templates/*dir* ~location]
           (apply (leiningen.new/resolve-template '~template) 
                  '~name 
                  '~args))))))

(println "util namespace loaded")