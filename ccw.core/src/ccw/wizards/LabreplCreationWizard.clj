(ns ccw.wizards.LabreplCreationWizard
  (:import
    [org.eclipse.ui.actions WorkspaceModifyDelegatingOperation]
    [org.eclipse.ui.dialogs IOverwriteQuery]
    [org.eclipse.jface.dialogs IDialogConstants]
    [org.eclipse.osgi.util NLS]
    [org.eclipse.jface.dialogs MessageDialog]
    [ccw.wizards LabreplCreateProjectPage
                 LabreplCreationOperation])
  (:gen-class
   :implements [org.eclipse.ui.INewWizard]
   :extends org.eclipse.jface.wizard.Wizard
   :exposes-methods {addPages super-addPages}
   :init myinit
   :post-init mypostinit
   :state state))

(defn- -myinit
  []
  [[] (ref {})])

(defn- -mypostinit
  [this]
  (.setDialogSettings this (.getDialogSettings (ccw.CCWPlugin/getDefault)))
	(.setWindowTitle this "Clojure Labrepl Example project")
  (.setNeedsProgressMonitor this true))

(defn -addPages
  [this]
	(.super-addPages this)
  (let [main-page (LabreplCreateProjectPage. "New Labrepl Project")]
    (dosync (alter (.state this) assoc :main-page main-page))
    (.addPage this main-page)))
 
(defn -init
	[this workbench currentSelection]
	nil)

(defn -performFinish
	[this]
  (let 
    [shell (.getShell this)
     display (.getDisplay shell)
     open-dialog
      (fn [file]
        (let
          [result (atom IDialogConstants/CANCEL_ID)
            title "Overwrite?"
            msg (str "Do you want to overwrite " file "?")
            options (into-array String [IDialogConstants/YES_LABEL
                                       IDialogConstants/NO_LABEL
                                       IDialogConstants/YES_TO_ALL_LABEL
                                       IDialogConstants/CANCEL_LABEL])
            dialog (MessageDialog. shell title nil msg MessageDialog/QUESTION options 0)
            run-dialog
              (fn [] (reset! result (.open dialog)))]
          (.syncExec display run-dialog)
          (deref result)))
     import-overwrite-query
       (proxy [IOverwriteQuery] []
         (queryOverwrite [file]
           (let 
             [return-codes [IOverwriteQuery/YES IOverwriteQuery/NO 
                            IOverwriteQuery/ALL IOverwriteQuery/CANCEL]
               return-value (open-dialog file)]
             (if (< return-value 0)
               IOverwriteQuery/CANCEL
               (return-codes return-value)))))
     runnable (LabreplCreationOperation. [(:main-page @(.state this))] import-overwrite-query)
     op (WorkspaceModifyDelegatingOperation. runnable)]
  (.run (.getContainer this) false true op)
	true))

(defn -setInitializationData
	[this cfig propertyName data]
	nil)