(ns ccw.api.parse-tree
  "Experimental"
  (:require [paredit.loc-utils :as lu]
            [clojure.zip :as z]
            [paredit.parser :as p]))

(defn call? 
  "is element e a function/macro-call?"
  [e] (= "(" (first (p/code-children e))))

(defn callee 
  "the called symbol node (without metadata"
  [e] (second (p/code-children e)))

(defn parent-call
  "return the loc for the parent call, or nil"
  [loc]
  (let [parents (take-while (comp not nil?) (iterate z/up loc))]
    (first (filter (comp call? z/node) parents))))

(defn call-context-loc
  "for viewer, at offset offset, return the loc containing the encapsulating
   call, or nil"
  [parse-tree offset]
  (when (pos? offset)
    (let [loc (lu/loc-containing-offset 
                (lu/parsed-root-loc parse-tree)
                offset)
          maybe-call-loc (some-> loc parent-call)]
      (when (some-> maybe-call-loc z/node call?)
        maybe-call-loc))))

(defn call-symbol
  "for viewer, at offset 12, return the symbol name if the offset
   is inside a function/macro call, or nil"
  [loc]
  (when loc (-> loc z/node callee p/remove-meta p/sym-name)))
