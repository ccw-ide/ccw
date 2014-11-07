(ns ccw.jdt
  (:use [clojure.core.incubator :only [-?> -?>>]])
  (:require [ccw.eclipse :as e])
  (:import [org.eclipse.jdt.core JavaCore
                                 IClasspathAttribute
                                 IClasspathEntry
                                 IAccessRule]
           [org.eclipse.jdt.launching JavaRuntime]
           [org.eclipse.core.resources IResource]
           [org.eclipse.core.runtime IPath]
           [org.eclipse.jdt.ui PreferenceConstants]))

(println "ccw.jdt load starts")

(def optional         (IClasspathAttribute/OPTIONAL))
(def javadoc-location (IClasspathAttribute/JAVADOC_LOCATION_ATTRIBUTE_NAME))
(def native-library   (JavaRuntime/CLASSPATH_ATTR_LIBRARY_PATH_ENTRY))

(defn native-library-path
  "Take a path, absolute or relative, and transform it into a suitable path
   for JDT library path entry (make it relative to workspace if possible)"
  [native-path]
  (str (or (-?> native-path e/resource e/path .makeRelative) 
           (e/path native-path))))
  
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
  "Takes a map representing a source entry, with following keys:
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

(defn container-entry 
  "Takes a map representing a container entry, with following keys:
     :path -> IPathCoercionable
     :access-rules -> list of IAccessRule 
     :extra-attributes   -> map of name/values of IClasspathAttributes (see classpath-attribute*). Optional
     :is-exported -> boolean-like, indicates whether this entry is contributed to dependent projects in addition to the output location , Optional (default false)
   For complete semantic description, see javadoc for org.eclipse.jdt.core.JavaCore/newLibraryEntry"
  [{:keys [path, 
           access-rules, 
           extra-attributes,
           is-exported]}]
  (JavaCore/newContainerEntry
      (e/path path),
      (into-array IAccessRule access-rules),
      (into-array IClasspathAttribute (for [[k v] extra-attributes] 
                                      (classpath-attribute k v))),
      (boolean is-exported)))

(defn optional-source-entry [desc]
  (source-entry (update-in desc [:extra-attributes] assoc optional "true")))

(defn ^:private update-entries! [java-project entries overwrite?]
  (let [existing-entries (if overwrite? () (seq (.getRawClasspath java-project)))
        new-entries (remove (set existing-entries) entries)
        entries (concat existing-entries new-entries)]
    (.setRawClasspath 
      java-project
      (into-array IClasspathEntry entries)
      (e/null-progress-monitor))))

(defn conj-entries! [java-project entries] 
  (update-entries! java-project entries false))

(defn default-output-path
  "Return what would constitute default output path for java-project, which can
   be passed to set-default-output-path!"
  [java-project]
  (str "/" (e/project-name java-project) "/"
    (PreferenceConstants/getPreference
      PreferenceConstants/SRCBIN_BINNAME
      java-project)))

(defn set-default-output-path!
  "Takes a java project, a path relative to the workspace root, and an optional progress-monitor.
     java-project -> an IJavaProject
     path ->  IPathCoercionable, or (default-output-path java-project) if nil
     progress-monitor -> an optional progress monitor. Defaults to (ccw.eclipse/null-progress-monitor)"
  ([java-project path] (set-default-output-path! path (e/null-progress-monitor)))
  ([java-project path progress-monitor]
    (.setOutputLocation
      java-project
      (if-let [path (e/path path)]
        path
        (default-output-path java-project))
      progress-monitor)))

(println "ccw.jdt namespace loaded")
