(ns ccw.editors.clojure.clojure-text-hover
  "Supports documentation hovers for Clojure Editor"
  (:import [org.eclipse.jface.text Region ITextHover]
           [ccw.editors.clojure IClojureEditor])
  (:require [ccw.core.trace :as trace]
            [paredit.loc-utils :as lu]
            [ccw.editors.clojure.editor-common :as common]
            [ccw.util.doc-utils :as doc-utils]))
   
(defn hover-info
  "Return the documentation hover text to be displayed at offset offset for 
   editor. The text can be composed of a subset of html (e.g. <pre>, <i>, etc.)"
  [^IClojureEditor editor offset]
  (let [loc (common/offset-loc editor offset)
        parse-symbol (common/parse-symbol? loc)]
    (when parse-symbol
      (let [m (common/find-var-metadata 
                (.findDeclaringNamespace editor) 
                editor
                parse-symbol)]
        (doc-utils/var-doc-info-html m)))))

(defn hover-region 
  "For editor, given the character offset, return a vector of [offset length]
   representing a region of the editor (containing offset).
   The idea is that for every offset in that region, the same documentation 
   hover as the one computed for offset will be used.
   This is a function for optimizing the number of times the hover-info
   function is called."
  [editor offset]
  (let [loc (common/offset-loc editor offset)]
    [(lu/start-offset loc) (lu/loc-count loc)]))

(defn make-TextHover 
  "Factory function for creating an ITextHover instance for the editor."
  [editor]
  (reify ITextHover
    (getHoverInfo [this text-viewer region] 
      (hover-info editor (.getOffset region)))

    (getHoverRegion [this text-viewer offset]
      (let [[offset length] (hover-region editor offset)]
        (Region. offset length)))))