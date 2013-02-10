(ns ccw.editors.clojure.nrepl-hyperlink-detector
  (:require [clojure.zip :as z]
            [paredit.loc-utils :as lu]
            [ccw.editors.clojure.editor-support :as editor]
            [ccw.editors.clojure.hyperlink :as hyperlink])
  (:use [clojure.core.incubator :only [-?>]])
  (:import 
    [org.eclipse.jface.text BadLocationException
                            IRegion
                            Region
                            ITextViewer
                            IDocument] 
    [org.eclipse.jface.text.hyperlink IHyperlink
                                      IHyperlinkDetector]
    [ccw.editors.clojure IClojureEditor
                         ClojureEditorMessages
                         IHyperlinkConstants
                         AbstractHyperlinkDetector]
    [ccw                 ClojureCore]))

(def ID (IHyperlinkConstants/ClojureHyperlinkDetector_ID)) 
(def TARGET_ID (IHyperlinkConstants/ClojureHyperlinkDetector_TARGET_ID))  

(defn editor [^AbstractHyperlinkDetector this] (.getClassAdapter this IClojureEditor))

;; FIXME share it with console hyperlink
(def ^:private pattern #"nrepl://([^':',' ']+):(\d+)")

;; FIXME rewrite this to use lower level stuff (java matchers) rather than
;;       reinvent them with more object allocations and more lines of code
;; FIXME add unit tests!
(defn find-match-for-offset [pattern ^String string offset] 
  (loop [matches (re-seq pattern string)]
    (when-let [[^String m _ _ :as match] (first matches)]
      (let [m-off (.indexOf string m)
            match? (<= m-off offset (dec (+ m-off (.length m))))]
        (if match? 
          [m-off (.length m) match]
          (recur (rest matches)))))))

(defn detect-hyperlinks
  [offset ^IDocument document]
  ;(println "nrepl hyperlink")
  (let [region (.getLineInformationOfOffset document offset)
        [line-offset line-length] [(.getOffset region) (.getLength region)]
        line (.get document line-offset line-length)]
    ;(println "line:" line)
    (when-let [[offset length [_ host port]] (find-match-for-offset pattern line (- offset line-offset))]
      ;(println "nrepl hyperlink:" :offset (+ line-offset offset) :length length)
      [{:offset (+ line-offset offset) :length length
        :open #(ccw.repl.REPLView/connect (format "nrepl://%s:%s" host port) true)}])))

(defn factory [ _ ]
  (proxy [AbstractHyperlinkDetector]
         []
    (detectHyperlinks [^ITextViewer textViewer ^IRegion region canShowMultipleHyperlinks?]
      (when-let [hyperlinks (detect-hyperlinks (.getOffset region) (.getDocument textViewer))] 
        (into-array IHyperlink (map (fn [{:keys #{offset length open}}] 
                                      (hyperlink/make 
                                        (Region. offset length) 
                                        open))
                                    hyperlinks))))))