(ns ccw.static-analysis
  (:require [paredit.parser :as p]))

(defn find-namespace [tree]
  (p/sym-name
    (p/form
      (first
        (p/call-args
          (some
            #(or
               (p/call-of % "ns")
               (p/call-of % "in-ns"))
            (p/code-children tree)))))))