(ns ccw.wizards.LabreplCreationWizard

  ; (:use [paredit [core :only [paredit]]])
  ; (:use [clojure.contrib.core :only [-?>]])  
  ; (:import
  ;  [org.eclipse.jface.text IAutoEditStrategy
  ;                         IDocument
  ;                          DocumentCommand]
  ; [ccw.editors.antlrbased AntlrBasedClojureEditor])
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
 (let [main-page (ccw.wizards.LabreplCreateProjectPage. "New Labrepl Project")]
   (dosync (alter (.state this) assoc :main-page main-page)
   (.addPage this main-page)))
  (println (str "addPages nach super" (class this)))
	nil)
 
(defn -init
	[this workbench currentSelection]
	nil)

(defn performFinish
	[this]
	true)

(defn setInitializationData
	[this cfig propertyName data]
	nil)