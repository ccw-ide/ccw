(ns ccw.wizards.LabreplCreationOperation
  (:import
     [org.eclipse.core.runtime NullProgressMonitor
                            SubProgressMonitor]
     [org.eclipse.ui.dialogs IOverwriteQuery]
     [org.eclipse.core.resources ResourcesPlugin])
  (:gen-class
   :implements [org.eclipse.jface.operation.IRunnableWithProgress]
   :constructors {[clojure.lang.IPersistentVector org.eclipse.ui.dialogs.IOverwriteQuery] []}
   :init myinit
   :state state))

(defn- -myinit
  [pages overwrite-query]
  (println "myinit")
  [[] (ref {:pages pages :overwrite-query overwrite-query})])

(defn create-project
  ""
  [root page monitor]
  (println create-project (str page) (str monitor)))

(defn -run
  "org.eclipse.jface.operation.IRunnableWithProgress.run"
  [this monitor]
  (let 
    [monitor (if monitor monitor (NullProgressMonitor.))
      pages (:pages @(.state this))
      page-count (count pages)]
    (try
      (.beginTask monitor "Labrepl Creation" page-count)
      (let [root (.getRoot (ResourcesPlugin/getWorkspace))]
        (doall (map #(create-project root (pages %) monitor) (range page-count))))
      (finally (.done monitor)))))