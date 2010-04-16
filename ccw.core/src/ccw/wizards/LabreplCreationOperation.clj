(ns ccw.wizards.LabreplCreationOperation
  (:import
     [java.lang.reflect InvocationTargetException]
     [org.eclipse.core.runtime NullProgressMonitor
                            SubProgressMonitor
                            CoreException
                            IStatus
                            Platform
                            Status
                            FileLocator]
     [org.eclipse.ui.dialogs IOverwriteQuery]
     [java.io IOException]
     [org.eclipse.core.resources ResourcesPlugin]
     [ccw CCWPlugin]
     [java.net URL]
     [java.util.zip ZipFile]
     [org.eclipse.ui.wizards.datatransfer ImportOperation 
                                          ZipFileStructureProvider])
  (:gen-class
   :implements [org.eclipse.jface.operation.IRunnableWithProgress]
   :constructors {[clojure.lang.IPersistentVector org.eclipse.ui.dialogs.IOverwriteQuery] []}
   :init myinit
   :state state))

(defn- -myinit
  [pages overwrite-query]
  [[] (ref {:pages pages :overwrite-query overwrite-query})])

(defn config-new-project
  [root name nature-ids monitor]
  (try
    (let
      [project (.getProject root name)]
      (if (not (.exists project)) (.create project nil))
      (if (not (.isOpen project)) (.open project nil))
      (let
        [desc (.getDescription project)]
        (doto desc
          (.setLocation nil)
          (.setNatureIds (into-array String nature-ids)))
        (.setDescription project desc (SubProgressMonitor. monitor 1))
        project))
    (catch CoreException exception (throw (InvocationTargetException. exception)))))

(defn get-zipfile-from-plugin-dir
  [plugin-relative-path]
  (try
    (let
      [bundle (.getBundle (CCWPlugin/getDefault))
        starter-url (URL. (.getEntry bundle "/") plugin-relative-path)]
      (ZipFile. (.getFile (FileLocator/toFileURL starter-url))))
    (catch IOException exception
      (let
        [message (str plugin-relative-path ": " (.getMessage exception))
          status (Status. IStatus/ERROR CCWPlugin/PLUGIN_ID IStatus/ERROR  message exception)]
        (throw (CoreException. status))))))

(defn import-files-from-zip
  [src-zip-file dest-path monitor overwrite-query]
  (let
    [structure-provider (ZipFileStructureProvider. src-zip-file)
      op (ImportOperation. dest-path (.getRoot structure-provider) structure-provider overwrite-query)]
    (.run op monitor)))

(defn do-imports
  [project monitor overwrite-query]
  (try
    (let
      [dest-path (.getFullPath project)
        zip-file (get-zipfile-from-plugin-dir "examples/labrepl.zip")]
      (import-files-from-zip zip-file dest-path (SubProgressMonitor. monitor 1) overwrite-query))
    (catch CoreException exception (throw (InvocationTargetException. exception)))))

(defn create-project
  [root page monitor overwrite-query]
  (.beginTask monitor "Configuring project..." 1)
  (let [nature-ids ["org.eclipse.pde.PluginNature" 
                    "org.eclipse.jdt.core.javanature" 
                    "ccw.nature"]
        project-name (.getProjectName page)
        project (config-new-project root project-name nature-ids monitor)]
     (do-imports project (SubProgressMonitor. monitor 1) overwrite-query)))

(defn -run
  [this monitor]
  (let 
    [monitor (if monitor monitor (NullProgressMonitor.))
      state @(.state this)
      pages (:pages state)
      overwrite-query (:overwrite-query state)
      page-count (count pages)]
    (try
      (.beginTask monitor "Labrepl Creation" page-count)
      (let [root (.getRoot (ResourcesPlugin/getWorkspace))]
        (doall (map #(create-project root (pages %) monitor overwrite-query) (range page-count))))
      (finally (.done monitor)))))
