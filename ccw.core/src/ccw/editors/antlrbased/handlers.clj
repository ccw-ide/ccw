(ns ccw.editors.antlrbased.handlers
  (:require [paredit.core :as pc])
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.ui.handlers HandlerUtil]
    [ccw.util PlatformUtil]
    [ccw.editors.antlrbased IClojureEditor]))
   
(defn- editor [event] (PlatformUtil/getAdapter (HandlerUtil/getActivePart event) IClojureEditor))

(defn- apply-paredit-command [editor command-key]
  (let [{:keys #{length offset}} (bean (.getUnSignedSelection editor))
        text  (.get (.getDocument editor))
        result (pc/paredit command-key (.getParsed editor) {:text text :offset offset :length length})]
    (when-let [modif (-?> result :modifs first)]
      (let [{:keys #{length offset text}} modif
            document (-> editor .getDocument)]
        (.replace document offset length text)
        (.selectAndReveal editor offset (.length text))))))

(defn raise [_ event] (apply-paredit-command (editor event) :paredit-raise-sexp))
(defn split [_ event] (apply-paredit-command (editor event) :paredit-split-sexp))
(defn join  [_ event] (apply-paredit-command (editor event) :paredit-join-sexps))