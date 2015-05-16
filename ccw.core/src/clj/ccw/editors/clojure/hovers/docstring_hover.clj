;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial implementation (code reviewed by Laurent Petit)
;*******************************************************************************/

(ns ^{:author "Andrea Richiardi" }
  ccw.editors.clojure.hovers.docstring-hover
  "Supports documentation hovers for Clojure Editor"
  (:import [org.eclipse.jface.text Region
                                   ITextHover
                                   ITextHoverExtension
                                   ITextHoverExtension2
                                   DefaultInformationControl
                                   IInformationControlCreator
                                   IInformationControlCreatorExtension]
           ccw.CCWPlugin
           ccw.util.UiUtils
           ccw.editors.clojure.IClojureEditor
           [ccw.editors.clojure.hovers HoverEnrichedControlCreator
                                       HoverControlCreator
                                       IClojureHover
                                       Messages])
  (:require [ccw.editors.clojure.editor-support :as esupport]
            [ccw.editors.clojure.editor-common :as ecommon]
            [ccw.core.doc-utils :as doc]
            [ccw.core.trace :refer [trace]]
            [ccw.interop :as interop]
            [ccw.editors.clojure.hover-support :as hsupport]))

#_(set! *warn-on-reflection* true)

(defn- docstring-hover-html
  "Return the documentation hover text to be displayed at offset offset
  for editor. The text can be composed of a subset of html (e.g. <pre>,
  <i>, etc.). If no info is available or the REPL is nil, it returns
  nil."
  [^IClojureEditor part offset]
  (hsupport/hover-html (when-let [parse-symbol (ecommon/parse-symbol (ecommon/offset-loc part offset))]
                (let [ns (.findDeclaringNamespace part)
                      m (ecommon/find-var-metadata ns
                                                    (.getCorrespondingREPL part)
                                                    parse-symbol)]
                  (trace :support/hover (str "docstring-hover-info:\n" "ns -> " ns "\n" "find-var-metadata -> " m "\n"))
                  (doc/var-doc-info-html m)))))

(defn- ensure-control-created
  "Creates the IInformationControlCreator for this hover."
  [enriched-control-creator previous-value]
  (if-some [value previous-value]
    value
    (HoverControlCreator. enriched-control-creator)))

(defn- ensure-enriched-control-created
  "Creates the IInformationControlCreator for this hover."
  [previous-value]
  (if-some [value previous-value]
    value
    (HoverEnrichedControlCreator.)))

(defn- make-TextHover
  "Factory function for creating an ITextHover instance for the editor."
  []
  (let [hover-control (atom nil)
        hover-enriched-control (atom nil)]
    (reify
      IClojureHover
      (getHoverInfo2 [this text-viewer hover-region]
        (trace :support/hover (str "[DOCSTRING-HOVER] " (interop/simple-name this) ".getHoverInfo2 called:\n"
                                   "text-viewer -> " (.toString text-viewer) "\n"
                                   "region -> " (.toString hover-region) "\n"))
        (let [[i msg] (if-let [info (docstring-hover-html text-viewer (.getOffset hover-region))]
                        [info nil]
                        [nil Messages/You_need_a_running_repl_docstring])]
          (do (esupport/set-status-line-error-msg-async text-viewer msg) i)))

      (getHoverControlCreator [this]
        (trace :support/hover (str "[DOCSTRING-HOVER] " (interop/simple-name this) ".getHoverControlCreator called"))
        (swap! hover-control (partial ensure-control-created (swap! hover-enriched-control ensure-enriched-control-created))))

      (getHoverInfo [this text-viewer hover-region]
        (trace :support/hover (str "[DOCSTRING-HOVER] " (interop/simple-name this) ".getHoverInfo called:\n"
                                   "text-viewer -> " (.toString text-viewer) "\n"
                                   "region -> " (.toString hover-region) "\n"))
        ;; AR - Deprecated (hover-info text-viewer (.getOffset hover-region))
        nil)

      (getHoverRegion [this text-viewer offset]
        (trace :support/hover (str "[DOCSTRING-HOVER] " (interop/simple-name this) ".getHoverRegion called:\n"
                                   "text-viewer -> "(.toString text-viewer) "\n"
                                   "offset -> " offset "\n"))
        (let [[offset length] (ecommon/offset-region text-viewer offset)]
          (Region. offset length)))

      (getInformationPresenterControlCreator [this]
        (trace :support/hover (str "[DOCSTRING-HOVER] " (interop/simple-name this) ".getInformationPresenterControlCreator called"))
        (swap! hover-enriched-control ensure-enriched-control-created)))))

(defn create-docstring-hover [& params]
  (make-TextHover))
