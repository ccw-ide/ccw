(ns ccw.leiningen.nature
  (:require [ccw.leiningen.classpath-container :as cpc]
            [clojure.string                    :as str]
            [ccw.eclipse                       :as e]
            [ccw.jdt                           :as jdt]
            [clojure.java.io                   :as io]
            [ccw.leiningen.util                :as u]
            [ccw.bundle                        :as b])
  (:import 
    [org.eclipse.core.resources     IProjectNature
                                    IWorkspaceRoot]
    [org.eclipse.core.runtime       CoreException
                                    IPath
                                    Path]
    [org.eclipse.jdt.core           ClasspathContainerInitializer
                                    IClasspathContainer
                                    IJavaProject
                                    IClasspathEntry
                                    JavaCore]
    [org.eclipse.jdt.launching      JavaRuntime]
    [org.eclipse.core.resources     IResource
                                    IProject
                                    IMarker
                                    ResourcesPlugin]
    [org.osgi.framework             Bundle]
    [org.sonatype.aether.resolution DependencyResolutionException]
    [org.eclipse.jface.text         ITextSelection]
    [org.eclipse.jface.viewers      IStructuredSelection]
    [org.eclipse.core.expressions   PropertyTester]
    [ccw.leiningen                  Messages
                                    LeiningenBuilder]
    [ccw.util                       Logger
                                    Logger$Severity]
    [java.io                        File
                                    FilenameFilter]))

(println "ccw.leiningen.nature load starts")

(def NATURE-ID "ccw.leiningen.nature")

(def logger (Logger. (ccw.CCWPlugin/PLUGIN_ID)))

;; TODO copie de ccw.core, exporter dans ccw.util

(defn jvm-entry
  "Returns the Classpath Entry for the found JVM, or nil if none found"
  [^IJavaProject java-project]
  (when-let [jre-entry (JavaRuntime/computeJREEntry java-project)]
    (.getClasspathEntry jre-entry)))

(defn default-jvm-entry 
  "Returns the JVM entry defined by default for the Eclipse installation"
  [] 
  (JavaRuntime/getDefaultJREContainerEntry))

;; TODO should check whether the JVM entry is for the right declared version
(defn jvm-entry-or-default
  "Checks whether the project has a JVM entry, and if not, add a default one
   to the passed entries, and return the new list of entries"
  [java-project]
  (or (jvm-entry java-project) (default-jvm-entry)))

(defn lein-raw-source-folders [lein-proj]
  (concat (:source-paths lein-proj)
          (:java-source-paths lein-proj)))

(defn lein-raw-optional-source-folders [lein-proj]
  (println "lein-raw-optional-source-folders:"
           (concat (:test-paths lein-proj)
                   (:resource-paths lein-proj)))
  (concat (:test-paths lein-proj)
          (:resource-paths lein-proj)))

(defn lein-raw-compile-folder [lein-proj] (:compile-path lein-proj))

(defn path-to-folder [path] 
  (.getContainerForLocation (e/workspace-root) (e/path path)))

(defn lein-source-folders [lein-proj]
  (map path-to-folder (lein-raw-source-folders lein-proj)))

(defn lein-optional-source-folders [lein-proj]
  (map path-to-folder (lein-raw-optional-source-folders lein-proj)))

(defn lein-compile-folder [lein-proj]
  (-> lein-proj lein-raw-compile-folder path-to-folder))

(defn ccw-bundle-version []
  (when-let [^Bundle bundle (b/bundle "ccw.core")]
    (.getVersion bundle)))

(defn lein-entries
  "Computes the build path as per leiningen project.clj.
   - java source folders are added from defaults or :source, :java-source, :test, :resources Leiningen entries
   - a classes folder is added which represents the target clojure AOT compilation folder, as per leiningen default \"classes\" folder or via :compile-path entry
   - TODO if a JVM version is specified in Lein, pick a matching version in the Eclipse defined. Pick the default one if does not match.
     - or if no JVM version specified in Lein, pick the default one
   - Install a Leiningen Classpath Container" 
  [java-proj]
  (let [lein-proj        (u/lein-project (e/project java-proj) :enhance-fn #(do (println %) (dissoc % :hooks)))
        _ (println "lein-proj: " lein-proj)
        
        jvm-entry        (jvm-entry-or-default java-proj)
        
        source-entries   (map #(jdt/source-entry {:path %}) 
                              (lein-source-folders lein-proj))
        _ (println "(lein-source-folders lein-proj): " (lein-source-folders lein-proj))
        _ (println "source-entries: " source-entries)

        ;; TODO remove this hack once CCW 0.8.0 has been delivered with the Builder correction for missing source folders
        optional-entry-start-version (b/version :major 0 :minor 8 :micro 0 :qualifier "201204121312")
        ccw-version (or (ccw-bundle-version) (b/version :major 9999 :minor 9999 :micro 9999))
        optional-entries? (b/> ccw-version optional-entry-start-version)
        
        _ (println "will handle optional entries: " optional-entries?)
        _ (println "resource-folders:" (lein-optional-source-folders lein-proj))
        ; TODO remove the folder check, and add the optional extra attribute
        resource-entries (if optional-entries?
                           (->> lein-proj
                             lein-optional-source-folders
                             (map #(jdt/source-entry {:path %, 
                                                    :extra-attributes {jdt/optional "true"}})))
                           (->> lein-proj
                             lein-optional-source-folders
                             (filter #(.exists ^File %))
                             (map #(jdt/source-entry {:path %}))))
        _ (println "resource entries:" resource-entries)
        compile-entry    (jdt/library-entry {:path (lein-compile-folder lein-proj)
                                           :extra-attributes {jdt/optional "true"}})

        lein-container   (cpc/get-lein-container java-proj)
        _                (cpc/set-classpath-container
                           java-proj
                           cpc/CONTAINER-PATH
                           lein-container)
        container-entry  (jdt/container-entry 
                           {:path (.getPath lein-container)
                            :extra-attributes 
                            {jdt/native-library (jdt/native-library-path
                                                (u/lein-native-platform-path
                                                  lein-proj))}
                            :is-exported true})]
    (concat [jvm-entry]
            source-entries
            resource-entries
            [compile-entry]
            [container-entry])))

;; TODO show an error dialog if something goes wrong
;; TODO show an information dialog if everything goes well (tell what happened?)
(defn reset-project-build-path 
  "Adjusts the project's java build path to match what is present in project.clj
   file.
   If file project.clj cannot be read, nothing happens.
   If overwrite? is true, then the project's build path is replaced.
   If overwrite? is false, the the new entries found are appended to the existing build path" 
  [java-proj overwrite? & [monitor]]
  (let [monitor (or monitor (e/null-progress-monitor))
        lein-entries (lein-entries java-proj)
        existing-entries (if overwrite? () (seq (.getRawClasspath java-proj)))
        new-entries (remove (set existing-entries) lein-entries)
        entries (concat existing-entries new-entries)]
    (try 
      (.setRawClasspath 
        java-proj
        (into-array IClasspathEntry entries)
        (e/null-progress-monitor))
      (.beginTask monitor (str "Project " (-> java-proj e/project .getName) ": Updating Leiningen Dependencies") 1)
    (cpc/update-project-dependencies java-proj)
    (.worked monitor 1)
    (.done monitor)
    (catch Exception e
      (throw (org.eclipse.core.runtime.CoreException. (ccw.CCWPlugin/createErrorStatus "Could not reset project classpath", e)))))))

(defn factory [_]
  (let [state (ref {:project nil :errors []})]
    (reify IProjectNature
      (setProject
        [this proj] (dosync (alter state assoc :project proj)))
      (getProject
        [this] (:project @state))
      (configure
        [this]
        (e/run-in-background
          (fn [progress-monitor]
            (let [proj (.getProject this)
                  desc (e/project-desc proj)
                  java-proj (JavaCore/create proj)]
              (when-not (e/desc-has-builder? desc LeiningenBuilder/ID)
                (e/project-desc! proj (e/add-desc-builder! desc LeiningenBuilder/ID)))
              (reset-project-build-path java-proj true progress-monitor)))))
      (deconfigure
        [this]
        (e/run-in-background
          (fn [progress-monitor]
            (let [proj      (.getProject this)
                  desc      (e/project-desc proj)
                  java-proj (JavaCore/create proj)]
              (when (e/desc-has-builder? desc LeiningenBuilder/ID)
                (e/project-desc! proj (e/remove-desc-builder! desc LeiningenBuilder/ID)))
              (when-let [cont (cpc/has-lein-container? java-proj)]
                (let [raw-classpath (.getRawClasspath java-proj)
                      raw-classpath (remove #{cont} raw-classpath)]
                  (.setRawClasspath java-proj (into-array IClasspathEntry raw-classpath) progress-monitor))))))))))

(println "ccw.leiningen.nature namespace loaded")