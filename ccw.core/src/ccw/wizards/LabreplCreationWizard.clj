(ns ccw.wizards.LabreplCreationWizard
  (:import
    [org.eclipse.ui.actions WorkspaceModifyDelegatingOperation]
    [org.eclipse.ui.dialogs IOverwriteQuery]
    [ccw.wizards LabreplCreateProjectPage
                 LabreplCreationOperation]
    )
  (:gen-class
   :implements [org.eclipse.ui.INewWizard]
   :extends org.eclipse.jface.wizard.Wizard
   :exposes-methods {addPages super-addPages}
   :init myinit
   :post-init mypostinit
   :state state))

(defn- -myinit
  []
  (println "myinit")
  [[] (ref {})])

(defn- -mypostinit
  [this]
  (println (str "mypostinit " (class this)))
  (.setDialogSettings this (.getDialogSettings (ccw.CCWPlugin/getDefault)))
	(.setWindowTitle this "Clojure Labrepl Example project")
  (.setNeedsProgressMonitor this true)
  (println "mypostinit"))

(defn -addPages
  [this]
  (println (str "addPages " (class this)))
	(.super-addPages this)
 (let [main-page (LabreplCreateProjectPage. "New Labrepl Project")]
   (dosync (alter (.state this) assoc :main-page main-page))
   (.addPage this main-page))
  (println (str "addPages nach super" (class this)))
	nil)
 
(defn -init
	[this workbench currentSelection]
	nil)

(defn -performFinish
	[this]
  (println "performFinish")
  (let 
    [runnable (LabreplCreationOperation. [(:main-page (.state this))])
     op (WorkspaceModifyDelegatingOperation. runnable)]
    (.run (.getContainer this) false true op))
	true)

(defn -setInitializationData
	[this cfig propertyName data]
  (println "setInitializationData")
	nil)