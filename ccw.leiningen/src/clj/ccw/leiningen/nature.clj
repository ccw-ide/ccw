(ns ccw.leiningen.nature
  (:require [ccw.leiningen.classpath-container :as cpc]
            [leiningen.core.project            :as p]
            [leiningen.core.classpath          :as cp]
            [clojure.string                    :as str]
            [ccw.util.eclipse                  :as e]
            [clojure.java.io                   :as io]
            [ccw.leiningen.util                :as u]
            [ccw.util.bundle-utils             :as b])
  (:import 
    [org.eclipse.core.resources     IProjectNature]
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
    [org.sonatype.aether.resolution DependencyResolutionException]
    [org.eclipse.jface.text         ITextSelection]
    [org.eclipse.jface.viewers      IStructuredSelection]
    [org.eclipse.core.expressions   PropertyTester]
    [ccw.leiningen                  Activator
                                    Messages
                                    LeiningenBuilder]
    [ccw.util                       Logger
                                    Logger$Severity]
    [java.io                        File
                                    FilenameFilter]))

(println "ccw.leiningen.nature load starts")

(def NATURE-ID "ccw.leiningen.nature")

(def logger (Logger. (Activator/PLUGIN_ID)))

;; TODO copie de ccw.core, exporter dans ccw.util

(defn description [project] (.getDescription project))

(defn alter-builders!
  [desc f & args]
  (let [spec (.getBuildSpec desc)
        new-spec (apply f spec args)]
    (doto desc
      (.setBuildSpec (into-array new-spec)))))

(defn builder [desc builder-id]
  (doto (.newCommand desc)
    (.setBuilderName builder-id)))

(defn- add-builder!
  [desc builder-id]
  (alter-builders! desc #(cons (builder desc builder-id) %)))

(defn remove-builder!
  [desc builder-id]
  (alter-builders! desc (partial remove #(= builder-id (.getBuilderName %)))))

(defn set-description! 
  ([proj desc] (set-description! proj desc nil))
  ([proj desc progress-monitor]
    (doto proj
      (.setDescription
        desc,
        IResource/KEEP_HISTORY
        progress-monitor))))

(defn has-builder? [desc builder-id]
  (let [spec (.getBuildSpec desc)]
    (some #(= builder-id (.getBuilderName %)) spec)))

(defn has-nature? 
  "Returns the fact that project has nature-id declared. Not the fact that the
   nature is currently activated (which may not be the case if there's a consistency
   problem).
   Pre-requisite: project exists and is open"
  [project nature-id]
  (.hasNature project nature-id))

(defn set-natures! [desc natures]
  (doto desc
    (.setNatureIds (into-array String natures))))

(defn add-natures! [desc & nature-ids]
  (let [natures         (.getNatureIds desc)
        missing-natures (remove (set natures) nature-ids)
        new-natures     (concat natures missing-natures)]
    (set-natures! desc new-natures)))

(defn remove-nature! [desc nature-id]
  (let [natures (.getNatureIds desc)]
    (when-not (some #{nature-id} natures)
      (let [new-natures (remove #{nature-id} natures)
            new-natures (into [] natures)]
        (set-natures! desc new-natures)))))

(defn jvm-entry
  "Returns the Classpath Entry for the found JVM, or nil if none found"
  [java-project]
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
  (concat (:source-path lein-proj)))

(defn lein-raw-resource-folders [lein-proj]
  (concat (:test-path lein-proj)
          (:resources-path lein-proj)))

(defn lein-raw-compile-folder [lein-proj] (:compile-path lein-proj))

(defn path-to-folder [path] 
  (.getContainerForLocation (e/workspace-root) (e/path path)))

(defn lein-source-folders [lein-proj]
  (map path-to-folder (lein-raw-source-folders lein-proj)))

(defn lein-resource-folders [lein-proj]
  (map path-to-folder (lein-raw-resource-folders lein-proj)))

(defn lein-compile-folder [lein-proj]
  (-> lein-proj lein-raw-compile-folder path-to-folder))

(defn ccw-bundle-version []
  (when-let [bundle (b/bundle "ccw.core")]
    (.getVersion bundle)))

(defn lein-entries
  "Computes the build path as per leiningen project.clj.
   - java source folders are added from defaults or :source, :java-source, :test, :resources Leiningen entries
   - a classes folder is added which represents the target clojure AOT compilation folder, as per leiningen default \"classes\" folder or via :compile-path entry
   - TODO if a JVM version is specified in Lein, pick a matching version in the Eclipse defined. Pick the default one if does not match.
     - or if no JVM version specified in Lein, pick the default one
   - Install a Leiningen Classpath Container" 
  [java-proj]
  (let [lein-proj        (u/lein-project (e/project java-proj))
        
        jvm-entry        (jvm-entry-or-default java-proj)
        
        source-entries   (map #(u/source-entry {:path %}) 
                              (lein-source-folders lein-proj))

        ;; TODO remove this hack once CCW 0.7.0 has been delivered with the Builder correction for missing source folders
        optional-entry-start-version (b/version :major 0 :minor 7 :micro 0 :qualifier "201203300000")
        ccw-version (or (ccw-bundle-version) (b/version :major 9999 :minor 9999 :micro 9999))
        optional-entries? (b/> ccw-version optional-entry-start-version)

        _ (println "will handle optional entries: " optional-entries?)
        ; TODO remove the folder check, and add the optional extra attribute
        resource-entries (if optional-entries?
                           (->> lein-proj
                             lein-resource-folders
                             (map #(u/source-entry {:path %, 
                                                    :extra-attributes {u/optional "true"}})))
                           (->> lein-proj
                             lein-resource-folders
                             (filter #(.exists %))
                             (map #(u/source-entry {:path %}))))

        compile-entry    (u/library-entry {:path (lein-compile-folder lein-proj)
                                           :extra-attributes {u/optional "true"}})

        lein-container   (cpc/get-lein-container java-proj)
        _                (cpc/set-classpath-container
                           java-proj
                           cpc/CONTAINER-PATH
                           lein-container)
        container-entry  (JavaCore/newContainerEntry (.getPath lein-container))]
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
  [java-proj overwrite? & [progress-monitor]]
  (println "get-lein-container: " (cpc/get-lein-container java-proj))
  (let [lein-entries (lein-entries java-proj)
        existing-entries (if overwrite? () (seq (.getRawClasspath java-proj)))
        new-entries (remove (set existing-entries) lein-entries)
        entries (concat existing-entries new-entries)]
    (.setRawClasspath 
      java-proj
      (into-array IClasspathEntry entries)
      (or progress-monitor (e/null-progress-monitor)))
    (println "Added (enfin j'espere)")
    (println "project now has container?" (cpc/has-lein-container? java-proj))
    (cpc/update-project-dependencies java-proj)))

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
                  desc (description proj)
                  java-proj (JavaCore/create proj)]
              (when-not (has-builder? desc LeiningenBuilder/ID)
                (set-description! proj (add-builder! desc LeiningenBuilder/ID)))
              (println "done here, setting the description")
              (reset-project-build-path java-proj true progress-monitor)))))
      (deconfigure
        [this]
        (e/run-in-background
          (fn [progress-monitor]
            (let [proj      (.getProject this)
                  desc      (description proj)
                  java-proj (JavaCore/create proj)]
              (when (has-builder? desc LeiningenBuilder/ID)
                (set-description! proj (remove-builder! desc LeiningenBuilder/ID)))
              (when-let [cont (cpc/has-lein-container? java-proj)]
                (let [raw-classpath (.getRawClasspath java-proj)
                      raw-classpath (remove #{cont} raw-classpath)]
                  (.setRawClasspath java-proj (into-array IClasspathEntry raw-classpath) progress-monitor))))))))))

(println "ccw.leiningen.nature namespace loaded")