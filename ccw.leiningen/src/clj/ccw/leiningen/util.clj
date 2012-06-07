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

;; TODO port back to ccw.core for the nature (place in ccw.util)
(defn bundle-file 
  "given bundle-name (a String), return the java.io.File corresponding to the
   currently loaded instance of this bundle"
  [bundle-name]
  (-> bundle-name Platform/getBundle FileLocator/getBundleFile))

(defn bundle-dir 
  "given bundle-name (a String), assumes there exists a currently loaded instance
   of it, and that it has been installed as a directory and not a jar.
   Return the java.io.File corresponding to its directory.
   (So, it's really just bundle-file with a check)"
  [bundle-name]
  (let [bundle-dir (bundle-file bundle-name)]
    (if (.isFile bundle-dir)
      (throw (RuntimeException. (str bundle-name " bundle should be deployed as a directory.")))
      bundle-dir)))

(defn- plugin-entry
  "given a plugin-name and an entry name inside the plugin, returns the 
   java.io.File instance for the entry if it is found in the plugin."
  [plugin-name entry-name]
  (let [bundle-dir (bundle-dir plugin-name)
        entry (File. bundle-dir entry-name)]
    (if (.exists entry)
      entry
      (throw (RuntimeException. (str "Unable to locate " + entry-name " in " plugin-name " plugin."))))))

(defn lein-env 
  "Create a new classlojure based leiningen environment by fetching all jars
   and classes directories found inside the ccw.leiningen-core plugin."
  []
  (let [leiningen-core (bundle-dir "ccw.leiningen-core")
        jar (fn [f] (and (.isFile f) (.endsWith (.getName f) ".jar")))
        classes-dir (fn [d] (let [manifest (io/file d "META-INF" "MANIFEST.MF")]
                               (and (.exists manifest)
                                    (.isFile manifest))))
        libs (->> leiningen-core file-seq (filter #(or (jar %) (classes-dir %))))]
    (apply c/classlojure libs)))

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
                (-> project e/project .getName))]
    (dosync
      (commute projects-envs
               #(if (or recreate? (not (% pname)))
                  (assoc % pname (delay (lein-env)))
                  %)))
    @(@projects-envs pname)))

(defn file-exists? 
  "Return the file if it exists, or nil" 
  [f]
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
                           leiningen.core.project/defaults
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

;TODO: suggest a patch for getting rid of this monkey patch:
;lpetit: Raynes: I have some feedback for you wrt lein newnew
;[11:46pm] Raynes: I'm scared.
;[11:46pm] Raynes: technomancy: He has feedback. Prepare the cannons.
;[11:47pm] lpetit: Raynes: don't panic 
;[11:47pm] lpetit: Raynes: I had to monkey patch ->files to be able to make lein new new work in CCW
;[11:47pm] lpetit: Raynes: 2 problems.
;[11:49pm] lpetit: 1/ lein new new assumes implicitly that "user.dir" directory is the project's parent directory. In Eclipse (or other tools wanting to embed the plugin I guess), this generally is not the case
;[11:49pm] Raynes: I don't think it does.
;[11:49pm] Raynes: Oh.
;[11:49pm] Raynes: Never mind, you said user.dir, not user.home.
;[11:49pm] fbru02 left the chat room. (Ping timeout: 260 seconds)
;[11:50pm] lpetit: 2/ In Eclipse, the user can choose a name for his project, which can be different from the project's directory name
;[11:50pm] lpetit: Raynes: yeah, (System/getProperty "user.dir") is the equivalent of a "working current directory"
;[11:50pm] Raynes: I don't quite understand why user.dir wouldn't be the parent directory. It is meant to be called from lein and it is, in every case, the right directory.
;[11:51pm] Raynes: How is Eclipse calling it?
;[11:51pm] Raynes: Are you calling the plugin code from Eclipse itself, or are you calling out to lein?
;[11:51pm] lpetit: 3/ (yeah there's a 3/ finally) In Eclipse, the project's directory is created before lein new new has a chance to work, which make it fail by saying "directory already existing"
;[11:52pm] lpetit: Raynes: talking about embedding leiningen-core in the IDE JVM, and calling tasks from it
;[11:52pm] Raynes: Seems like this could be solved with an option like --to-dir to set the directory that the project will be added to.
;[11:52pm] Raynes: We could make that ignore the fact that a directory already exists.
;[11:52pm] Raynes: Would that help?
;[11:52pm] lpetit: Raynes: I played with classlojure and I maintain a map of project-name -> classlojure env. per Eclipse project
;[11:52pm] technomancy: yeah, I foresee the whole current directory thing being a common problem with leiningen-core outside leiningen
;[11:52pm] technomancy: it's just too easy to write code that assumes the current directory is the project root
;[11:53pm] lpetit: Raynes: I don't know if users would complain or not if you remove the "safeguard" ?
;[11:53pm] technomancy: I try to avoid it, but it sneaks in
;[11:53pm] Raynes: lpetit: I'm not removing the safeguard, just giving a way for Eclipse to get around it.
;[11:53pm] lpetit: Raynes: perfect, so point 3/ is solved
;[11:54pm] Raynes: Number 1 is solved too.
;[11:54pm] Raynes: Since this lets you set the directory yourself and not assume user.dir.
;[11:54pm] technomancy: lpetit: does would System/setProperty work for user.dir?
;[11:55pm] lpetit: technomancy: that's what I'm doing currently, but it's not thread safe
;[11:55pm] technomancy: oh, of course
;[11:55pm] lpetit: Ah, system classloader ...
;[11:56pm] lpetit: Raynes: indeed, for 1/, if there's also a programmatic way to change/bind the var --to-dir sets, we're good with that
;[11:56pm] Raynes: Why would that be necessary?
;[11:57pm] lpetit: Raynes: I don't start a separate jvm
;[11:58pm] Raynes: Yeah, but you call the task. You can pass the option, right?
;[11:58pm] lpetit: Raynes: oh, via a String-based interface, yeah, I can do that if that's the only option 
;[11:58pm] Raynes: Well, it's a task, not a library.
;[11:59pm] lpetit: Raynes: shouldn't a task be based on a library ? (just kidding)
;[11:59pm] Raynes: Heh
;[11:59pm] Raynes: It could have a better library-like interface.
;[11:59pm] Raynes: This should solve number 2 as well.
;[11:59pm] lpetit: Raynes: I'm fine with what you suggested
;[11:59pm] Raynes: The project name and directory name can be different with --to-dir.
;[11:59pm] lpetit: Raynes: indeed, if the --to-dir is the project's directory, that would be just fine
;[12:00am] Raynes: lpetit, technomancy: I don't know when I'll have time to implement this though, so feel free to patch it if you want.
;[12:00am] Raynes: Shouldn't be hard.
;[12:01am] technomancy: hm; yeah but I don't know if this should block the preview4 work
;[12:01am] lpetit: Raynes, technomancy: definitely something I can do. I also may have some time problem, but since I definitely want to get rid of my big monkey patch, I'll do this sooner or later if it hasn't been done then
;[12:01am] lpetit: technomancy: definitely not
;[12:01am] technomancy: yeah, now that it's not on the bootclasspath it's easy to bump independently
;[12:01am] lpetit: don't block it, I'm not blocked myself
;[12:01am] technomancy: ok, good to hear
;[12:02am] antares_: Raynes: you know what's funny? Gosu is not open source
;[12:02am] Raynes: Heh

(defn monkey-patch-lein-new [project]
  (eval-in-project 
    project
    '(do
       (require 'leiningen.new.templates)
       (in-ns 'leiningen.new.templates)
       (defn- template-path [_ path data]
         (io/file (render-text path data)))
       (defn
         ->files
         "Generate a file with content. path can be a java.io.File or string.\n   It will be turned into a File regardless. Any parent directories will\n   be created automatically. Data should include a key for :name so that\n   the project is created in the correct directory"
         [{:keys [name], :as data} & paths]
         (.mkdir (io/file (System/getProperty "user.dir")))
         (doseq
           [path paths]
           (if
             (string? path)
             (.mkdirs (template-path name path data))
             (let
               [[path content] path
                path (template-path name path data)
                path (io/file (System/getProperty "user.dir") path)
                ]
               (.mkdirs (.getParentFile path))
               (io/copy content (io/file path)))))))))

(defn lein-new [location & args]
  (let [project-map (lein-project :project-less
                      :enhance-fn 
                      (fn [p]
                        (eval-in-project :project-less
                          `(-> '~p
                             (leiningen.core.project/add-profiles
                               '{:user 
                                 ~'{:plugins [[lein-newnew "0.3.1"]]}})
                             (leiningen.core.project/merge-profiles 
                               [:user])))))]
    (monkey-patch-lein-new :project-less)
    (let [user-dir (System/getProperty "user.dir")]
      (try
        (System/setProperty "user.dir" location)
        (eval-in-project 
          :project-less
          `(do
             (require 'leiningen.new)
             (apply leiningen.new/new nil '~args)))
        (finally (System/setProperty "user.dir" user-dir))))))


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