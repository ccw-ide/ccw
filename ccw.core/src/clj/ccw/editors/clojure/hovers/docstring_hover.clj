(ns ccw.editors.clojure.hovers.docstring-hover
  "Supports documentation hovers for Clojure Editor"
  (:import [org.eclipse.jface.text Region
                                   ITextHover
                                   ITextHoverExtension2]
           ccw.editors.clojure.IClojureEditor
           ccw.CCWPlugin)
  (:require [ccw.core.trace :refer [trace]]
            [ccw.interop :refer [simple-name]]
            [paredit.loc-utils :as lu]
            [ccw.editors.clojure.editor-common :as common]
            [ccw.core.doc-utils :as doc-utils]
            [ccw.editors.clojure.editor-support :as editor]))

(defn hover-info
  "Return the documentation hover text to be displayed at offset offset for 
   editor. The text can be composed of a subset of html (e.g. <pre>, <i>, etc.)"
  [^IClojureEditor editor offset]
  (let [loc (common/offset-loc (-> editor .getParseState (editor/getParseTree)) offset)
        parse-symbol (common/parse-symbol? loc)]
    (trace :support/hover (str "[DOCSTRING-HOVER] hover-info:\n"
                               "offset -> " offset "\n"
                               "parse-symbol -> " parse-symbol "\n"))
    (when parse-symbol
      (let [m (common/find-var-metadata 
                (.findDeclaringNamespace editor) 
                (.getCorrespondingREPL editor)
                parse-symbol)]
        (doc-utils/var-doc-info-html m)))))

(defn hover-region 
  "For editor, given the character offset, return a vector of [offset length]
   representing a region of the editor (containing offset).
   The idea is that for every offset in that region, the same documentation 
   hover as the one computed for offset will be used.
   This is a function for optimizing the number of times the hover-info
   function is called."
  [^IClojureEditor editor offset]
  (let [loc (common/offset-loc (-> editor .getParseState (editor/getParseTree)) offset)]
    [(lu/start-offset loc) (lu/loc-count loc)]))

(defn- make-TextHover
  "Factory function for creating an ITextHover instance for the editor."
  []
  (reify
    ITextHoverExtension2
    (getHoverInfo2 [this text-viewer hover-region]
      (trace :support/hover (str "[DOCSTRING-HOVER] " (simple-name this) ".getHoverInfo2 called:\n"
                                 "text-viewer -> " (.toString text-viewer) "\n"
                                 "region -> " (.toString hover-region) "\n"))
      (hover-info text-viewer (.getOffset hover-region)))

    ITextHover
    (getHoverInfo [this text-viewer hover-region]
      (trace :support/hover (str "[DOCSTRING-HOVER] " (simple-name this) ".getHoverInfo called:\n"
                                 "text-viewer -> " (.toString text-viewer) "\n"
                                 "region -> " (.toString hover-region) "\n"))
      ;; AR - Deprecated (hover-info text-viewer (.getOffset hover-region))
      nil)

    (getHoverRegion [this text-viewer offset]
      (trace :support/hover (str "[DOCSTRING-HOVER] " (simple-name this) ".getHoverRegion called:\n"
                                 "text-viewer -> "(.toString text-viewer) "\n"
                                 "offset -> " offset "\n"))
      (let [[offset length] (hover-region (CCWPlugin/getClojureEditor) offset)]
        (Region. offset length)))))

(defn create-docstring-hover [& params]
  (make-TextHover))
