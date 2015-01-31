(ns ccw.editors.clojure.hovers.docstring-hover
  "Supports documentation hovers for Clojure Editor"
  (:import [org.eclipse.jface.text Region ITextHover]
           ccw.editors.clojure.IClojureEditor
           ccw.CCWPlugin)
  (:require [ccw.core.trace :refer [trace]]
            [ccw.interop :refer [simple-name]]
            [paredit.loc-utils :refer [start-offset
                                       loc-count]]
            [ccw.editors.clojure.editor-common :refer [offset-loc
                                                       find-var-metadata
                                                       parse-symbol?]]
            [ccw.core.doc-utils :refer [var-doc-info-html]]))

(defn- hover-info
  "Return the documentation hover text to be displayed at offset offset for
  editor. The text can be composed of a subset of html (e.g. <pre>, <i>, etc.)"
  [^IClojureEditor editor offset]
  (let [loc (offset-loc editor offset)
        parse-symbol (parse-symbol? loc)]
    (when parse-symbol
      (let [m (find-var-metadata
               (.findDeclaringNamespace editor)
               editor
               parse-symbol)]
        (var-doc-info-html m)))))

(defn- hover-region
  "For editor, given the character offset, return a vector of [offset length]
  representing a region of the editor (containing offset).
  The idea is that for every offset in that region, the same documentation
  hover as the one computed for offset will be used.
  This is a function for optimizing the number of times the hover-info
  function is called."
  [editor offset]
  (let [loc (offset-loc editor offset)]
    [(start-offset loc) (loc-count loc)]))

(defn- make-TextHover
  "Factory function for creating an ITextHover instance for the editor."
  []
  (reify
    ITextHover
    (getHoverInfo [this text-viewer hover-region]
      (trace :support/hover (str "[DOCSTRING-HOVER] " (simple-name this) ".getHoverInfo called with:\n"
                                 "text-viewer -> " (.toString text-viewer) "\n"
                                 "region -> " (.toString hover-region) "\n"))
      (hover-info (CCWPlugin/getClojureEditor) (.getOffset hover-region)))

    (getHoverRegion [this text-viewer offset]
      (trace :support/hover (str "[DOCSTRING-HOVER] " (simple-name this) ".getHoverRegion called with:\n"
                                 "text-viewer -> "(.toString text-viewer) "\n"
                                 "offset -> " offset "\n"))
      (let [[offset length] (hover-region (CCWPlugin/getClojureEditor) offset)]
        (Region. offset length)))))

(defn create-docstring-hover [& params]
  (make-TextHover))
