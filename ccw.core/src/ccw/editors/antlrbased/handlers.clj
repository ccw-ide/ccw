;*******************************************************************************
;* Copyright (c) 2010 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Laurent PETIT - initial API and implementation
;*******************************************************************************/

(ns ccw.editors.antlrbased.handlers
  (:require [paredit.core :as pc]
            [ccw.editors.antlrbased.ClojureHyperlinkDetector :as hlu]) ; hlu as HyperLinkUtil
  (:use [clojure.contrib.core :only [-?>]])  
  (:import
    [org.eclipse.ui.handlers HandlerUtil]
    [ccw.util PlatformUtil]
    [ccw.editors.antlrbased IClojureEditor
                            SourceRange]))
   
(defn- editor [event] (PlatformUtil/getAdapter (HandlerUtil/getActivePart event) IClojureEditor))

(defn ignoring-selection-changes [editor f]
  (try       (-> editor .getSelectionHistory .ignoreSelectionChanges)
             (f)
    (finally (-> editor .getSelectionHistory .listenToSelectionChanges))))

;; TODO remove duplications with appli-paredit-command
(defn- apply-paredit-selection-command [editor command-key]
  (let [{:keys #{length offset}} (bean (.getUnSignedSelection editor))
        text  (.get (.getDocument editor))
        {new-length :length new-offset :offset} (pc/paredit command-key (.getParsed editor) {:text text :offset offset :length length})]
    (-> editor .getSelectionHistory (.remember (SourceRange. offset length)))
    (ignoring-selection-changes editor 
      #(.selectAndReveal editor new-offset new-length))))

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
(defn expand-left [_ event] (apply-paredit-selection-command (editor event) :paredit-expand-left))
(defn expand-right [_ event] (apply-paredit-selection-command (editor event) :paredit-expand-right))
(defn expand-up [_ event] (apply-paredit-selection-command (editor event) :paredit-expand-up))
(defn indent-selection [_ event] (apply-paredit-selection-command (editor event) :paredit-indent-line))


;; TODO won't work if the ClojureSourceViewer is reused many times via configure/unconfigure (since after a re-configure,
;; a fresh SelectionHistory instance will be created)
;; Inspired directly by JDT
(defn select-last [_ event] 
  (let [editor (editor event)]
    (when-let [old (-> editor .getSelectionHistory .getLast)]
      (ignoring-selection-changes editor 
        #(.selectAndReveal editor (.getOffset old) (.getLength old))))))

(defn open-declaration [_ event]
  (let [editor (editor event)
        caret-offset (-> editor .getUnSignedSelection .getOffset)]
    (if-let [[{open :open}] (hlu/detect-hyperlinks caret-offset editor)]
      (open))))