(ns ccw.leiningen.util
  (:require [ccw.util.eclipse :as e]
            [leiningen.core.eval :as eval]
            [classlojure.core :as c]
            [clojure.java.io :as io])
  (:import [org.eclipse.jdt.core JavaCore
                                 IClasspathAttribute
                                 IAccessRule]
           [org.eclipse.jdt.launching JavaRuntime]
           [org.eclipse.core.runtime IPath
                                     Platform
                                     FileLocator]
           [java.io File IOException]))

(println "ccw.leiningen.util load starts")

;; from ccw.core

(defn bundle-file 
  [bundle-name]
  (-> bundle-name Platform/getBundle FileLocator/getBundleFile))

(defn bundle-dir 
  [bundle-name]
  (let [bundle-dir (bundle-file bundle-name)]
    (if (.isFile bundle-dir)
      (throw (RuntimeException. (str bundle-name " bundle should be deployed as a directory.")))
      bundle-dir)))

(defn- plugin-entry
  [plugin-name entry-name]
  (let [bundle-dir (bundle-dir plugin-name)
        entry (File. bundle-dir entry-name)]
    (if (.exists entry)
      entry
      (throw (RuntimeException. (str "Unable to locate " + entry-name " in " plugin-name " plugin."))))))

(defn lein-env []
  (let [leiningen-core (bundle-dir "ccw.leiningen-core")
        jar (fn [f] (and (.isFile f) (.endsWith (.getName f) ".jar")))
        classes-dir (fn [d] (let [manifest (io/file d "META-INF" "MANIFEST.MF")]
                               (and (.exists manifest)
                                    (.isFile manifest))))
        libs (->> leiningen-core file-seq (filter #(or (jar %) (classes-dir %))))]
    (apply c/classlojure libs)))

(defonce projects-envs (ref {}))

(defn project-env! [project & recreate?]
  (let [pname (-> project e/project .getName)]
    (dosync
      (commute projects-envs
               #(if (or recreate? (not (% pname)))
                  (assoc % pname (delay (lein-env)))
                  %)))
    @(@projects-envs pname)))

(defn file-exists? "Return the file if it exists, or nil" [f]
  (when (.exists f) f))

(defn eval-in-project [project form & args]
  (apply c/eval-in (project-env! project) form args))

(defn project-clj 
  [project]
  (-> project
    e/project
    (.getFile "project.clj")
    .getLocation
    .toOSString))

(defn lein-project
  "Given a project (anything that coerces to ccw.util.eclipse/IProjectCoercion),
   analyze its project.clj file and return the project map"
  [project]
  (eval-in-project project '(require 'leiningen.core.project))
  (eval-in-project project 'leiningen.core.project/read (project-clj project)))

(defn lein-native-platform-path [lein-project]
  (eval/native-arch-path lein-project))

(defn lein-new [project]
  (binding [leiningen.core.user/profiles (constantly {:user {:plugins [[lein-newnew "0.2.6"]]}})]))

;;; JDT utilities

(def optional         (IClasspathAttribute/OPTIONAL))
(def javadoc-location (IClasspathAttribute/JAVADOC_LOCATION_ATTRIBUTE_NAME))
(def native-library   (JavaRuntime/CLASSPATH_ATTR_LIBRARY_PATH_ENTRY))

(defn classpath-attribute [name value]
  (JavaCore/newClasspathAttribute name value))

(defn source-entry 
  "Takes a ma representing a source entry, with following keys:
     :path -> IPathCoercionable
     :inclusion-patterns -> list of IPathCoercionables. Optional
     :exclusion-patterns -> list of IPathCoercionables. Optional
     :compile-path    -> IPathCoercionable. Optional
     :extra-attributes   -> map of name/values of IClasspathAttributes (see classpath-attribute*). Optional

   For complete semantic description, see javadoc for org.eclipse.jdt.core.JavaCore/newSourceEntry"
  [{:keys [path,
           inclusion-patterns,
           exclusion-patterns,
           compile-path,
           extra-attributes]}]
  (JavaCore/newSourceEntry
    (e/path path),
    (into-array IPath (map e/path inclusion-patterns)),
    (into-array IPath (map e/path exclusion-patterns)),
    (e/path compile-path)
    (into-array IClasspathAttribute (for [[k v] extra-attributes] 
                                      (classpath-attribute k v)))))

(defn library-entry 
  "Takes a ma representing a source entry, with following keys:
     :path -> IPathCoercionable
     :source-attachment-path -> IPathCoercionable, for finding source files, Optional
     :source-attachment-root-path -> IPathCoercionable, the location of the root of the source files within the source archive or folder, Optional
     :access-rules -> list of IAccessRule 
     :extra-attributes   -> map of name/values of IClasspathAttributes (see classpath-attribute*). Optional
     :is-exported -> boolean-like, indicates whether this entry is contributed to dependent projects in addition to the output location , Optional (default false)
   For complete semantic description, see javadoc for org.eclipse.jdt.core.JavaCore/newLibraryEntry"
  [{:keys [path, 
           source-attachment-path,
           source-attachment-root-path, 
           access-rules, 
           extra-attributes,
           is-exported]}]
  (JavaCore/newLibraryEntry
      (e/path path),
      (e/path source-attachment-path),
      (e/path source-attachment-root-path),
      (into-array IAccessRule access-rules),
      (into-array IClasspathAttribute (for [[k v] extra-attributes] 
                                      (classpath-attribute k v))),
      (boolean is-exported)))

(defn optional-source-entry [desc]
  (source-entry (update-in desc [:extra-attributes] assoc optional "true")))

(println "util namespace loaded")