;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial implementation reviewed by Laurent Petit)
;*******************************************************************************/
(ns ^{:author "Andrea Richiardi" }
  ccw.editors.clojure.hovers.macro-hover
  "Supports macro expansion hovers for Clojure Editor"
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

(set! *warn-on-reflection* true)

(defn- macro-expand!
  "Returns the result of the expasion in a format that
  ccw.core/doc-utils can understand and render."
  [expander part offset]
  (when-let [offset-loc (ecommon/offset-loc part offset)]
    (let [parse-symbol (ecommon/parse-symbol offset-loc)
          ns (.findDeclaringNamespace part)
          metadata (ecommon/find-var-metadata ns
                                              (.getCorrespondingREPL part)
                                              parse-symbol)
          is-macro? (:macro metadata)
          symbol-ns (:ns metadata) ]
      (trace :support/hover (str "current-ns -> " ns "\n"
                                 "parse-symbol -> " parse-symbol "\n"
                                 "metadata -> " metadata "\n"
                                 "is-macro? -> " is-macro? "\n"
                                 "symbol-ns -> " symbol-ns))
      (when is-macro?
        (let [form (ecommon/offset-parent-text offset-loc)
              expansion (ecommon/expand-macro-form (.getCorrespondingREPL part)
                                                   expander
                                                   ns
                                                   form)]
          (trace :support/hover (str "form -> " form))
          (trace :support/hover (str "expansion -> " expansion))
          {:name (str parse-symbol)
           :ns (str symbol-ns)
           :macro (str is-macro?)
           :macro-source (str form)
           :macro-expanded (str expansion)})))))

(defn- macro-expand-html!
  "Returns the result of expanding the macro at the given offset."
  [expander part offset]
  (hsupport/hover-html (doc/var-doc-info-html (macro-expand! expander part offset))))

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

(defn reify-macro-hover
  "Reifies an instance of ITextHover, with expander identifying the the
  desired expander (\"macroexpand\" or \"macroexpand-all\" are supported
  at the moment)."
  [expander-string-or-keyword]
  (trace :support/hover (str "[MACRO-HOVER] expander will be: " expander-string-or-keyword))
  (let [hover-control (atom nil)
        hover-enriched-control (atom nil)
        expand! (partial macro-expand-html! (name expander-string-or-keyword))]
    (reify
      IClojureHover
      (getHoverInfo2 [this text-viewer hover-region]
        (trace :support/hover (str "[MACRO-HOVER] " (interop/simple-name this) ".getHoverInfo2 called:\n"
                                   "text-viewer -> " (.toString text-viewer) "\n"
                                   "region -> " (.toString hover-region) "\n"))
        (let [[i msg] (if-let [info (expand! text-viewer (.getOffset hover-region))]
                        [info nil]
                        [nil Messages/You_need_a_running_repl_macro])]
          (do (esupport/set-status-line-error-msg-async text-viewer msg) i)))

      (getHoverControlCreator [this]
        (trace :support/hover (str "[MACRO-HOVER] " (interop/simple-name this) ".getHoverControlCreator called"))
        (swap! hover-control (partial ensure-control-created (swap! hover-enriched-control ensure-enriched-control-created))))

      (getHoverInfo [this text-viewer hover-region]
        (trace :support/hover (str "[MACRO-HOVER] " (interop/simple-name this) ".getHoverInfo called:\n"
                                   "text-viewer -> " (.toString text-viewer) "\n"
                                   "region -> " (.toString hover-region) "\n"))
        ;; AR - Deprecated (hover-info text-viewer (.getOffset hover-region))
        nil)

      (getHoverRegion [this text-viewer offset]
        (trace :support/hover (str "[MACRO-HOVER] " (interop/simple-name this) ".getHoverRegion called:\n"
                                   "text-viewer -> "(.toString text-viewer) "\n"
                                   "offset -> " offset "\n"))
        (let [[offset length] (ecommon/offset-region text-viewer offset)]
          (Region. offset length)))

      (getInformationPresenterControlCreator [this]
        (trace :support/hover (str "[MACRO-HOVER] " (interop/simple-name this) ".getInformationPresenterControlCreator called"))
        (swap! hover-enriched-control ensure-enriched-control-created)))))

(defn create-macro-hover
  "IExecutableExtension entry point."
  [& params]
  (reify-macro-hover (get (first params) "expander" "macroexpand")))
