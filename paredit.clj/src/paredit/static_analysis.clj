(ns paredit.static-analysis
  (:require [paredit.parser :as p]
            [paredit.loc-utils :as lu]
            [clojure.zip :as zip]))

(defn find-namespace [tree] 
  (p/sym-name 
    (p/remove-meta 
      (first 
        (p/call-args 
          (some 
            #(or 
               (p/call-of % "ns") 
               (p/call-of % "in-ns"))
            (p/code-children tree)))))))

(defn code-forms 
  "Remove non-executable forms from locs (removes spaces, comments, etc.)"
  [locs]
  (remove (comp p/gspaces lu/loc-tag) locs))

(defn top-level-code-form 
  "Returns the top level code form for offset.
   If offset is between 2 top level forms, return either the form right after
   if it starts exactly at the same offset, or the preceding form."
  [root-loc offset]
  (let [tll (lu/top-level-loc (lu/loc-for-offset root-loc offset))]
    (first (code-forms (take-while identity (iterate zip/left tll))))))
