(println "ccw.leiningen.classpath-container before ns decl")

(ns ccw.leiningen.classpath-container
  (:use [clojure.core.incubator :only [-?> -?>>]])
  (:require [leiningen.core.project :as p]
            [leiningen.core.classpath :as cp]
            [cemerick.pomegranate.aether :as aether]
            [clojure.string :as str]
            [ccw.eclipse :as e]
            [ccw.jdt :as jdt]
            [clojure.java.io :as io]
            [ccw.leiningen.util :as u])
  (:import [org.eclipse.core.runtime CoreException
                                     IPath
                                     Path]
           [org.eclipse.jdt.core ClasspathContainerInitializer
                                 IClasspathContainer
                                 IClasspathEntry
                                 IJavaProject
                                 IClasspathEntry
                                 JavaCore]
           [org.eclipse.swt.widgets Composite]
           [org.eclipse.core.resources IResource
                                       IProject
                                       IMarker
                                       ResourcesPlugin]
           [org.sonatype.aether.resolution DependencyResolutionException]
           [ccw.util Logger
                     Logger$Severity]
           [java.io File
                    FilenameFilter]))

(println "ccw.leiningen.classpath-container load starts")

(def CONTAINER-PATH (Path. "ccw.LEININGEN_CONTAINER"))

(def CONTAINER-DESCRIPTION "Leiningen dependencies")

(def LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE
  "ccw.leiningen.problemmarkers.classpathcontainer")

(def logger (Logger. (ccw.CCWPlugin/PLUGIN_ID)))

(defmacro with-exc-logged [& body]
  `(try ~@body
     (catch Exception e#
       (println (.getMessage e#))
       (.printStackTrace e#)
       nil)))

(defn plugin 
  "Return the plugin instance"
  []
  (ccw.CCWPlugin/getDefault))

(defn make-leiningen-classpath-container 
  "Create an instance of an IClasspathContainer given the arguments.
   classpath-entries: list of IClasspathEntry instances"
  [classpath-entries]
  (reify 
    IClasspathContainer
    (getClasspathEntries [this] 
       ; We explicitly create a new array everytime
       (into-array IClasspathEntry classpath-entries))
    (getDescription [this] CONTAINER-DESCRIPTION)
    (getKind [this] (IClasspathContainer/K_APPLICATION))
    (getPath [this] CONTAINER-PATH)))

(defn- library-entry
  "Wrapper for the ccw.leiningen.util/library-entry function"
  [{:keys [path native-path]}]
  (let [params {:path path}
        params (if native-path
                     (update-in params [:extra-attributes] 
                                assoc jdt/native-library (jdt/native-library-path native-path))
                     params)]
    (jdt/library-entry params)))

(defn leiningen-classpath-container
  "Given a project, grab its dependencies, and create an Eclipse Classpath Container
   for them. Lets any thrown exception pass through (e.g. Aether exception)"
  [project-dependencies]
  (let [entry-list (map library-entry project-dependencies)]
    (make-leiningen-classpath-container entry-list)))

(defn resolve-dependencies
  "ADAPTED FROM LEININGEN-CORE resolve-dependencies.
  Simply delegate regular dependencies to pomegranate. This will
  ensure they are downloaded into ~/.m2/repositories and that native
  deps have been extracted to :native-path.  If :add-classpath? is
  logically true, will add the resolved dependencies to Leiningen's
  classpath.

   Returns a set of the dependencies' files."
  [project-name dependencies-key {:keys [repositories native-path] :as project} & rest]
  (let [deps-paths 
          ; We use eval-in-project or else we may not benefit from the right SSL
          ; certificates declared for the project
          (u/eval-in-project
            project-name
            `(do 
               (require 'leiningen.core.classpath)
               (let [~'dependencies (apply leiningen.core.classpath/resolve-dependencies
                                           '~dependencies-key
                                           '~project
                                           '~rest)]
                 ; we serialize paths to Strings so that clojure datastructures can be passed back
                 (map #(.getAbsolutePath %) ~'dependencies))))]
    (map #(File. ^String %) deps-paths)))

(defn ser-dep [path native-path]
  {:path (.getAbsolutePath (io/as-file path))
   :native-path (.getAbsolutePath (io/as-file native-path))})

(defn get-project-dependencies
  "Return the dependencies sorted alphabetically via their file name.
   Throws Aether exceptions if a problem occured"
  [project-name lein-project]
  (let [dependencies (resolve-dependencies project-name :dependencies lein-project)
        default-native-platform-path (u/lein-native-platform-path lein-project)]
    ;(println "default-native-platform-path:" default-native-platform-path)
    (->> dependencies
      (filter #(re-find #"\.(jar|zip)$" (.getName ^File %)))
      (sort-by #(.getName ^File %))
      (map #(ser-dep %
                     default-native-platform-path
                     #_(or #_(u/lein-native-dependency-path lein-project %) ;; TODO make this work :-(
                         default-native-platform-path))))))

(defn- delete-container-markers [?project]
  (.deleteMarkers (e/resource ?project) 
    LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE,
    true,
    IResource/DEPTH_ONE ; DEPTH_ONE so that we also remove markers from project.clj
    ))

; TODO generalize on the plugin, and then extract into ccw.eclipse
(defn- state-file 
  "Return the state file, if it exists, or nil"
  [project state-name]
  (io/file (.toFile (e/plugin-state-location (plugin)))
           (str (.getName (e/project project)) state-name)))


(defn save-project-state
  "Save on disk (in the Plugin state directory), with the specified state-name,
   the serialized clojure datastructure data.
   Writes log and returns nil if save failed, or return the file"
  [project state-name data]
  (with-exc-logged
    (let [f (state-file project state-name)]
      (spit f (pr-str data))
      f)))

(defn save-project-dependencies 
  "Save on disk (in the Plugin state directory), the project deps for the 
   corresponding project.
   project deps is a list of jar files (or coercible to jar files)
   Writes log and returns nil if save failed, or return the file"
  [project project-deps]
  (save-project-state project ".container" project-deps))

(defn load-project-state
  "Retrieve from disk (from the Plugin state directory), the state for state-name, for the project.
   Read the file content, and call read-string on it    
   Writes log and returns nil if loading failed."
  [project state-name]
  (with-exc-logged
    (when-let [state-file (-> project (state-file state-name) u/file-exists?)]
      (let [state (read-string (slurp state-file))]
        (when (or (empty? state) (map? (first state))) 
          state)))))

(defn deser-dep [dep-map]
  (-> dep-map 
    (update-in [:path] #(File. ^String %))
    (update-in [:native-path] #(File. ^String %))))

(defn load-project-dependencies
  "Retrieve from disk (from the Plugin state directory), the deps for the project.
   Return a list of Files
   Writes log and returns nil if loading failed."
  [project]
  (map deser-dep (load-project-state project ".container")))

(defn- add-container-marker 
  "Delete previous container markers, add new one"
  [?project message]
  (e/run-in-workspace
    (e/workspace-runnable 
      (fn [_] 
        (delete-container-markers ?project)
        (let [marker (.createMarker (e/resource ?project) LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE)]
          (.setAttributes marker
            (let [m (java.util.HashMap.)]
              (doto m
                (.put IMarker/MESSAGE message)
                (.put IMarker/LINE_NUMBER (Integer/valueOf "1"))
                (.put IMarker/PRIORITY (Integer. IMarker/PRIORITY_HIGH))
                (.put IMarker/SEVERITY (Integer. IMarker/SEVERITY_ERROR))))))))
    nil
    true
    nil))

(defn- report-container-error [?project message ^Throwable e]
  (add-container-marker (e/resource ?project) message)
  (println message)
  (.printStackTrace e))

(defn- unresolved-artifacts [artifact-results]
  (remove #(.isResolved %) artifact-results))

(defn- requested-artifact [artifact-result]
  (-> artifact-result .getRequest .getArtifact))

(defn- artifact-string 
  ([artifact] 
    (artifact-string (.getGroupId artifact) 
                     (.getArtifactId artifact)
                     (.getBaseVersion artifact)))
  ([group-id artifact-id base-version]
    (str 
      "["
      (when group-id (str group-id "/"))
      artifact-id
      " "
      "\"" base-version "\""
      "]")))

(defn flat-dependencies [dependency-node]
  (concat 
    (when-let [d (.getDependency dependency-node)] [d])
    (mapcat flat-dependencies (.getChildren dependency-node))))

(defn- unresolved-dependencies-string [exc]
  (let [unresolved (-> exc .getResult .getArtifactResults unresolved-artifacts)
        artifacts (map (comp artifact-string requested-artifact) unresolved)]
    (if-not (seq artifacts) 
      (-> exc .getMessage (str/replace "#<" "") (str/replace ">" ""))
      (str/join ", " artifacts))))

(defn- dependency-resolution-message [exc java-project]
  [(-> java-project e/resource (.getFile "project.clj"))
   (str "problem resolving following dependencies: "
        (unresolved-dependencies-string exc))])

(defn- check-class 
  "Return true if the fully qualified class name of (.getClass o) is same
   fully qualified name of class c.
   Use it when you can't use instance?, e.g. when classes are same but from 
   different classloaders."
  [c o]
  (let [o-class-name (-?> o .getClass .getName)
        c-classname (-> c .getName)]
    (= o-class-name c-classname)))

(defn- resource-message
  "Return [resource message] where resource is the Eclipse IResource to be used
   as the target of the problem marker, and message is personalized given exc
   and java-project values"
  [exc java-project]
  (if-not exc
    [(e/resource java-project) "unknown problem (missing exception)"]
    (let [exc (if (check-class java.lang.reflect.InvocationTargetException exc) (.getCause exc) exc)
          exc (if (check-class clojure.lang.ExceptionInfo exc) (.getCause exc) exc)]
      (cond
        (check-class DependencyResolutionException exc)
          (dependency-resolution-message exc java-project)
        (check-class DependencyResolutionException (.getCause exc))
          (dependency-resolution-message (.getCause exc) java-project)
        (nil? (.getMessage exc))
          [(e/resource java-project) "unknown problem (missing exception message)"]
        (.contains (.getMessage exc) "project.clj")
          (if (.contains (.getMessage exc) "FileNotFoundException")
            [(e/resource java-project) "project.clj file is missing"]
            [(-> java-project e/resource (.getFile "project.clj")) (str "problem with project.clj file: " (.getMessage exc))])
        (.contains (.getMessage exc) "DependencyResolutionException")
          [(e/resource java-project) (str "problem when grabbing dependencies from repositories:" (.getMessage exc))]
        :else
          [(e/resource java-project) (str "unknown problem: " (.getMessage exc))]))))

(defn set-classpath-container
  "Sets classpath-container for java-project under the key container-path"
  [java-project container-path classpath-container]
  (JavaCore/setClasspathContainer
    container-path
    (into-array IJavaProject [java-project])
    (into-array IClasspathContainer [classpath-container])
    nil))

(defn set-lein-container
  "Sets container as the leiningen container for the java-project."
  [java-project deps]
  (let [container (leiningen-classpath-container deps)]
    (set-classpath-container java-project CONTAINER-PATH container)))

(defn refresh-target-folder 
  "Refreshes the target folder of a project, if it exists.
   project may be anything coercible to an IProject.
   This function does not call refresh on a background job, nor does it run
   inside a workspace runnable."
  [project-coercible]
  (if-let [target-folder (.getFolder (e/project project-coercible) "target")]
    (.refreshLocal target-folder (IResource/DEPTH_INFINITE) nil)))

(defn update-project-dependencies
  "Get the dependencies.
   If deps fetched ok: sets lein container, save the dependencies list on disk.
   If an exception is throw while fetching deps: report problem markers, 
   do not touch the current lein container."
  [java-project] ;; TODO checks
  (try
    (let [lein-project (u/lein-project java-project :enhance-fn #(do (println %) (dissoc % :hooks)))
          deps (get-project-dependencies (.getName (e/project java-project)) lein-project)]
      (set-lein-container java-project deps)
      (delete-container-markers java-project)
      (save-project-dependencies java-project deps))
    (refresh-target-folder java-project)
    (catch Exception e
      ;; TODO enhance this in the future ... (more accurate problem markers)
      (let [[jresource message] (resource-message e java-project)
            project-name (-> java-project e/resource .getName)]
        (report-container-error 
          jresource
          ;(str "Project '" project-name "', Leiningen classpath container problem: " message)
          (str "Leiningen Managed Dependencies issue: " message)
          e)))))

(defn has-container? [java-project container-path]
  (let [entries (.getRawClasspath java-project)]
    (some #(and
             (= IClasspathEntry/CPE_CONTAINER  (.getEntryKind %))
             (= container-path (.getPath %)))
          entries)))

(defn has-lein-container? [java-project] 
  (has-container? java-project CONTAINER-PATH))

(defn get-lein-container [java-project]
  (JavaCore/getClasspathContainer CONTAINER-PATH java-project))

(defn initializer-factory 
  "Creates a ClasspathContainerInitializer instance for Leiningen projects"
  [_]
  (proxy [ClasspathContainerInitializer]
         []
    (initialize [container-path, java-project]
      (if-let [deps (seq (load-project-dependencies java-project))]
        (set-lein-container java-project deps)
        (update-project-dependencies java-project)))
    
    (canUpdateClasspathContainer [container-path, java-project]
      false)
    
    (getDescription [container-path, java-project]
      CONTAINER-DESCRIPTION)))

(println "classpath-container namespace loaded")