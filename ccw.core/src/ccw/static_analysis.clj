(ns ccw.static-analysis
  (:require [paredit.parser :as p]))

(defn find-namespace 
  [tree] (p/sym-name (p/form (first (p/call-args (some #(p/call-of % "ns") (p/code-children tree)))))))