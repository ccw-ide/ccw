(ns ccw.editors.clojure.nrepl-hyperlink
  (:require [ccw.core.trace :as t]
            [ccw.api.hyperlink :as hyperlink])
  (:import  [org.eclipse.jface.text IDocument]
            [ccw.editors.clojure IClojureEditor]))

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
  [[offset length] ^IClojureEditor editor]
  (let [document (.getDocument editor)
        region (.getLineInformationOfOffset document offset)
        [line-offset line-length] [(.getOffset region) (.getLength region)]
        line (.get document line-offset line-length)]
    (t/format :editor "line: %s" line)
    (when-let [[offset length [url host port]] (find-match-for-offset pattern line (- offset line-offset))]
      (t/format :editor "nrepl hyperlink: [:offset :length]: [%s %s]" (+ line-offset offset) length)
      [{:region [(+ line-offset offset) length]
        :open #(ccw.repl.REPLView/connect url true)
        :text (format "open %s" url)}])))
