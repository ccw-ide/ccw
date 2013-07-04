(ns ccw.leiningen.classpath-container-ui
  (:require [ccw.util.jdt :as jdt]
            [ccw.leiningen.classpath-container :as cpc])
  (:import [org.eclipse.jdt.ui.wizards IClasspathContainerPage]
           [org.eclipse.jface.wizard WizardPage]
           [org.eclipse.swt SWT]
           [org.eclipse.swt.widgets Composite]
           [ccw.leiningen Messages]))

(println "ccw.leiningen.classpath-container-ui load starts")

(defn page-factory 
  "Creates a IClasspathContainerPage instance for Leiningen Managed Dependencies"
  [_]
  (doto 
    (proxy [WizardPage, IClasspathContainerPage]
           [(Messages/PageName), (Messages/PageTitle), nil]
      (createControl [parent]
        (let [composite (Composite. parent SWT/NULL)]
          (proxy-super setControl composite)))
      
      (finish [] true)
      
      (getSelection []
        (jdt/container-entry {:path cpc/CONTAINER-PATH
                            :is-exported true}))
      
      (setSelection [_]))
    (.setDescription (Messages/PageDesc))
    (.setPageComplete true)))

(println "classpath-container-ui namespace loaded")