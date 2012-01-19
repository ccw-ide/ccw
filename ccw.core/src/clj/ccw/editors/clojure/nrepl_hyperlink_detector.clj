(ns ccw.editors.clojure.nrepl-hyperlink-detector
  (:require [clojure.zip :as z]
            [paredit.loc-utils :as lu]
            [clojure.tools.nrepl :as repl]
            [ccw.editors.clojure.editor-support :as editor]
            [ccw.editors.clojure.hyperlink :as hyperlink])
  (:use [clojure.core.incubator :only [-?>]])
  (:import 
    [org.eclipse.jface.text BadLocationException
                            IRegion
                            Region
                            ITextViewer] 
    [org.eclipse.jface.text.hyperlink IHyperlink
                                      IHyperlinkDetector]
    [ccw.editors.clojure IClojureEditor
                         ClojureEditorMessages
                         IHyperlinkConstants
                         AbstractHyperlinkDetector]
    [ccw.debug           ClojureClient]
    [ccw                 ClojureCore]))

(def *ID* (IHyperlinkConstants/ClojureHyperlinkDetector_ID)) 
(def *TARGET_ID* (IHyperlinkConstants/ClojureHyperlinkDetector_TARGET_ID))  

(defn editor [this] (.getClassAdapter this IClojureEditor))

;; FIXME share it with console hyperlink
(def ^:private pattern #"nrepl://([^':',' ']+):(\d+)")

;; FIXME rewrite this to use lower level stuff (java matchers) rather than
;;       reinvent them with more object allocations and more lines of code
;; FIXME add unit tests!
(defn find-match-for-offset [pattern string offset] 
  (loop [matches (re-seq pattern string)]
    (when-let [[m _ _ :as match] (first matches)]
      (let [m-off (.indexOf string m)
            match? (<= m-off offset (dec (+ m-off (.length m))))]
        (if match? 
          [m-off (.length m) match]
          (recur (rest matches)))))))

(defn detect-hyperlinks
  [offset document]
  ;(println "nrepl hyperlink")
  (let [region (.getLineInformationOfOffset document offset)
        [line-offset line-length] [(.getOffset region) (.getLength region)]
        line (.get document line-offset line-length)]
    ;(println "line:" line)
    (when-let [[offset length [_ host port]] (find-match-for-offset pattern line (- offset line-offset))]
      ;(println "nrepl hyperlink:" :offset (+ line-offset offset) :length length)
      [{:offset (+ line-offset offset) :length length
        :open #(ccw.repl.REPLView/connect host (Integer/parseInt port))}])))

(defn factory [ _ ]
  (proxy [AbstractHyperlinkDetector]
         []
    (detectHyperlinks [textViewer region canShowMultipleHyperlinks?]
      (when-let [hyperlinks (detect-hyperlinks (.getOffset region) (.getDocument textViewer))] 
        (into-array IHyperlink (map (fn [{:keys #{offset length open}}] 
                                      (hyperlink/make 
                                        (Region. offset length) 
                                        open))
                                    hyperlinks))))))