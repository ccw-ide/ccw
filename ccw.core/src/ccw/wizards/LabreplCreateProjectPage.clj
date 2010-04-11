(ns ccw.wizards.LabreplCreateProjectPage
  (:gen-class
   :extends org.eclipse.ui.dialogs.WizardNewProjectCreationPage
   :init myinit
   :post-init mypostinit
   :state state))

(defn- -myinit
  [page-name]
  (println (str "LabreplCreateProjectPage myinit " page-name))
  [[page-name] (ref {})])

(defn- -mypostinit
  [_ page-name]
  (println (str "LabreplCreateProjectPage mypostinit " page-name)))
