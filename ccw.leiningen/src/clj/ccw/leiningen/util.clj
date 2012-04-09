(ns ccw.leiningen.util
  (:require [ccw.util.eclipse :as e]
            [leiningen.core.project :as p])
  (:import [org.eclipse.jdt.core JavaCore
                                 IClasspathAttribute
                                 IAccessRule]
           [org.eclipse.core.runtime IPath]))

(println "ccw.leiningen.util load starts")

(defn file-exists? "Return the file if it exists, or nil" [f]
  (when (.exists f) f))

(defn lein-project
  "Given a project (anything that coerces to ccw.util.eclipse/IProjectCoercion),
   analyze its project.clj file and return the project map"
  [project]
  (let [project (e/project project)
        project-clj (-> project 
                      (.getFile "project.clj")
                      .getLocation
                      .toOSString)]
    (p/read project-clj)))

;;; JDT utilities

(def optional (IClasspathAttribute/OPTIONAL))
(def javadoc-location (IClasspathAttribute/JAVADOC_LOCATION_ATTRIBUTE_NAME))

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