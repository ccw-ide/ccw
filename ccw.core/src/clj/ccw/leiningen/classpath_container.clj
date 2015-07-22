(ns ccw.leiningen.classpath-container
  (:require [leiningen.core.project :as p]
            [leiningen.core.classpath :as cp]
            [leiningen.core.user :as lcu]
            [cemerick.pomegranate.aether :as aether]
            [clojure.string :as str]
            [ccw.eclipse :as e]
            [ccw.jdt :as jdt]
            [clojure.java.io :as io]
            [ccw.leiningen.util :as u]
            [ccw.core.trace :as t])
  (:import [org.eclipse.core.runtime CoreException
                                     IPath
                                     IProgressMonitor
                                     Path
                                     Status
                                     jobs.IJobChangeListener]
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

(t/trace :leiningen "ccw.leiningen.classpath-container load starts")

(def CONTAINER-PATH (Path. "ccw.LEININGEN_CONTAINER"))

(def CONTAINER-DESCRIPTION "Leiningen dependencies")

(def LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE
  "ccw.leiningen.problemmarkers.classpathcontainer")

(defonce ^{:private true
           :author "Andrea Richiardi"
           :doc "Atom containing the state of this namespace.
                 :update-dependencies will contain a map project->{:updating? , :can-update?}
                                      indicating that a project is currently updating or can update."}
  state (atom {:update-dependencies nil}))

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
  [{:keys [path native-path source-attachment-path]}]
  (let [params {:path path :source-attachment-path source-attachment-path}
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

;; copied from lein-ubersource
(defn- find-transitive-deps
  [deps repositories]
  (->> (aether/resolve-dependencies
        :coordinates deps
        :repositories repositories)
      keys
      set))

;; copied from lein-ubersource
(defn- resolve-artifact
  [dep repositories]
  (->> (find-transitive-deps [dep] repositories)
      (filter #(= % dep))
      first))

;; copied from lein-ubersource
(defn- try-resolve-sources-artifact!
  [dep repositories]
  (try
    (resolve-artifact (concat dep [:classifier "sources"]) repositories)
    (catch DependencyResolutionException ex
      nil)))

(defn- artifacts-entry
  [dep repositories]
  (let [main (resolve-artifact dep repositories)
        source (try-resolve-sources-artifact!
                 (take 2 dep) ; take only id and version, not optional exclusion sections
                 repositories)]
    (if source [(-> main meta :file)
                (-> source meta :file)]
               nil)))

(defn get-source-map
  [{:keys [repositories dependencies] :as project} & args]
  (let [;; we must explicitly call leiningen.core.user/resolve-crendentials, or repositories
        ;; with credentials like  {:username :env/SOME_ENV} are left as keywords
        ;; and later down the road, pomegranate calls Aether's Authentication ctor which expects
        ;; Strings, not keys ... resulting in a runtime exception such as:
        ;; "No matching ctor found for class org.sonatype.aether.repository.Authentication"
        ;; See Issue #666 - https://code.google.com/p/counterclockwise/issues/detail?id=666
        repositories (map (fn [[name settings]] [name (lcu/resolve-credentials settings)]) repositories)
        dep (find-transitive-deps dependencies repositories)]
    (into {} (keep #(artifacts-entry % repositories) dep))))

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

(defn ser-dep [path native-path source-attachment-path]
  (let [s {:path (.getAbsolutePath (io/as-file path))
           :native-path (.getAbsolutePath (io/as-file native-path))}]
    (if source-attachment-path
      (assoc s :source-attachment-path (.getAbsolutePath (io/as-file source-attachment-path)))
      s)))

(defn get-project-dependencies
  "Return the dependencies sorted alphabetically via their file name.
   Throws Aether exceptions if a problem occurred. Supports thread
  interruption. The function break!, a function that presumably throws
  an exception), is called were appropriate to break execution."
  [break! project-name lein-project]
  (let [dependencies (resolve-dependencies project-name :dependencies lein-project)
        default-native-platform-path (u/lein-native-platform-path lein-project)
        srcmap (get-source-map lein-project)]
    (t/format :leiningen "default-native-platform-path: %s" default-native-platform-path)
    (for [dep (->> dependencies
                   (filter #(re-find #"\.(jar|zip)$" (.getName ^File %)))
                   (sort-by #(.getName ^File %)))]
      (do (break!)
          (ser-dep dep
                   default-native-platform-path
                   (get srcmap dep)
                   #_(or #_(u/lein-native-dependency-path lein-project dep) ;; TODO make this work :-(
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
    (update-in [:native-path] #(File. ^String %))
    (update-in [:source-attachment-path] #(when % (File. ^String %)))))

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
  (t/trace :leiningen message e))

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
  (let [o-class-name (some-> o .getClass .getName)
        c-classname (-> c .getName)]
    (= o-class-name c-classname)))

(defn- resource-message
  "Return [resource message] where resource is the Eclipse IResource to be used
   as the target of the problem marker, and message is personalized given exc
   and java-project values"
  [exc java-project]
  (if-not exc
    [(e/resource java-project) "unknown problem (missing exception)"]
    (let [exc (if (check-class java.lang.reflect.InvocationTargetException exc) (or (.getCause exc) exc) exc)
          exc (if (check-class clojure.lang.ExceptionInfo exc) (or (.getCause exc) exc) exc)]
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
  [project-coercible monitor]
  (if-let [target-folder (.getFolder (e/project project-coercible) "target")]
    (.refreshLocal target-folder (IResource/DEPTH_INFINITE) monitor)))

(defn- start-updating
  "Return the state that signals that the update is going to
  start/already started and there is no need for other (threads) to
  perform it."
  [old-state project-id workspace-job]
  (if (get-in old-state [:update-dependencies project-id :updating?])
    (assoc-in old-state [:update-dependencies project-id :can-update?] false)
    (-> (assoc-in old-state [:update-dependencies project-id :can-update?] true)
        (assoc-in [:update-dependencies project-id :updating?] true)
        (assoc-in [:update-dependencies project-id :update-job] workspace-job))))

(defn- finish-updating
  "Return the state that signals that the update is finished"
  [old-state project-id]
  (-> (assoc-in old-state [:update-dependencies project-id :updating?] false)
      (assoc-in [:update-dependencies project-id :update-job] nil)))

(defn- can-update?
  "Check in state if the project id in input can update"
  [current-state project-id]
  (get-in current-state [:update-dependencies project-id :can-update?]))

(defn- project-job
  "Check in state if the project id in input can update"
  [current-state project-id]
  (get-in current-state [:update-dependencies project-id :update-job]))

(defn update-project-dependencies
  "Get the dependencies.
   If deps fetched ok: sets lein container, save the dependencies list on disk.
   If an exception is thrown while fetching deps: report problem markers,
   do not touch the current lein container.
   Executes in a background workspace job."
  [java-project] ;; TODO checks
  (let [project-id (e/identifier java-project)
        project-name (e/project-name java-project)
        error-fn (fn [[workspace-job exception]]
                   (let [[jresource message] (resource-message exception java-project)
                         error-message (format "Leiningen Managed Dependencies issue: %s" message)]
                     ;; TODO enhance this in the future ... (more accurate problem markers)
                     (report-container-error jresource error-message exception)
                     error-message))
        finally-fn (fn [workspace-job] (doto ;; we refresh the target folder outside the workspace-root lock
                             (e/workspace-job
                              (format "Refreshing project %s" project-name)
                              (fn [^IProgressMonitor monitor]
                                (refresh-target-folder java-project monitor)
                                Status/OK_STATUS))
                           (.setUser true)
                           (.schedule)))
        job (e/cancelable-workspace-job
             (format "Update project dependencies for project %s" project-name)
             (fn [^IProgressMonitor monitor]
               (letfn [(cancel! [] (e/throw-op-canceled! (format "The update of %s was canceled" project-name) monitor))]
                 (let [_ (cancel!)
                       lein-project (u/lein-project java-project :enhance-fn #(do (t/trace :leiningen %) (dissoc % :hooks)))
                       deps (get-project-dependencies cancel! (.getName (e/project java-project)) lein-project)]
                   (t/trace :leiningen "Project dependencies calculated, setting up the workspace...")
                   ;; Here, get-project-dependencies has succeeded or thrown an error
                   ;; it can take a long time, so we do not put it inside the workspace job which blocks on the workspace root
                   (cancel!)
                   (doto
                       (e/workspace-job
                        (format "Upgrade project build path for project %s" (e/project-name java-project))
                        (fn [^IProgressMonitor monitor]
                          (set-lein-container java-project deps)
                          (delete-container-markers java-project)
                          (save-project-dependencies java-project deps)
                          Status/OK_STATUS))
                     (.setUser true)
                     ;; this rule is OK because we know the job needs it and will not take too long
                     (.setRule (e/workspace-root))
                     (.schedule))))
               (if-not (.isCanceled monitor)
                 Status/OK_STATUS
                 Status/CANCEL_STATUS))
             :error-handler! error-fn
             :finally-handler! finally-fn)
        new-state (swap! state start-updating project-id job)]
    (when (can-update? new-state project-id)
      (doto (project-job new-state project-id)
        (.addJobChangeListener (reify
                                 IJobChangeListener
                                 (running [this ijobchangeevent])

                                 (done [this ijobchangeevent]
                                   (t/trace :leiningen (str "Job completed with status -> " (-> (.getResult ijobchangeevent))
                                                            ", name was " (-> (.getJob ijobchangeevent) (.getName))))
                                   (swap! state finish-updating project-id))

                                 (scheduled [this ijobchangeevent])
                                 (aboutToRun [this ijobchangeevent])
                                 (awake [this ijobchangeevent])
                                 (sleeping [this ijobchangeevent])))
        (.setUser true)
        (.schedule)))))

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
        (let [job (e/workspace-job
                    (str "Initializing classpath container" container-path)
                    (fn [monitor]
                      (update-project-dependencies java-project)))] 
          (doto job
            (.setUser false)
            (.schedule))
          nil)))
    
    (canUpdateClasspathContainer [container-path, java-project]
      false)
    
    (getDescription [container-path, java-project]
      CONTAINER-DESCRIPTION)))

(t/trace :leiningen "classpath-container namespace loaded")
