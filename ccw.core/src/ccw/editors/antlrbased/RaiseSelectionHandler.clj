(ns ccw.editors.antlrbased.RaiseSelectionHandler
  (:require [paredit.core :as pc])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.ui.handlers HandlerUtil]
    [ccw.util PlatformUtil]
    [ccw.editors.antlrbased IClojureEditor])
  (:gen-class
   :extends org.eclipse.core.commands.AbstractHandler))
   
(defn- editor [event] (PlatformUtil/getAdapter (HandlerUtil/getActivePart event) IClojureEditor))

(defn -execute
  [this event]
  (let [editor (editor event)
        {:keys #{length offset}} (bean (.getUnSignedSelection editor))
        text  (.get (.getDocument editor))
        result (pc/paredit :paredit-raise-sexp (.getParsed editor) {:text text :offset offset :length length})]
    (when-let [modif (-?> result :modifs first)]
      (let [{:keys #{length offset text}} modif
            document (-> editor .getDocument)]
        (.replace document offset length text)
        (.selectAndReveal editor offset (.length text))))))

