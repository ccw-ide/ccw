(ns paredit.tests.utils
  (:require [paredit.parser :refer [parse]])
  (:require [paredit.loc-utils :as l])
  (:require [paredit.text-utils :as t])
  (:require [clojure.string :as s])
  (:require [clojure.zip :as z]))

(defn tree 
  "creates parse-tree"
  [text]
  (-> text
    parse 
    (l/parsed-root-loc true)
    (z/node)))

(defn clean-tree 
  "more human readable parse tree"
  [tree]
  (cond
    (string? tree) tree
    :else (-> tree 
            (select-keys [:tag :content])
            (update-in [:content] #(map clean-tree %)))))

(defn spec->text 
  "Converts a text spec: \"a |bit| of text\"
   to a text map: {:text \"a bit of text\" :offset 2 :length 3}
   
   A text spec is of the form \"some |text\" or \"some |other| text\".
   A single pipe in the spec marks the offset of the cursor, and is not 
   considered part of the spec.
   Two pipes in the spec mark a text selection, and are not considered part of 
   the spec.

   A text map has keys :text, :offset and :length

   Examples:
   \"fooo\"   -> {:text \"fooo\", :offset 0, :length 0}
   \"f|ooo\"  -> {:text \"fooo\", :offset 1, :length 0}
   \"f|oo|o\" -> {:text \"fooo\", :offset 1, :length 2}" 
  [^String text-spec]
  (let [offset (.indexOf text-spec "|")
        second-pipe (dec (.indexOf text-spec "|" (inc offset)))]  
    {:text (s/replace text-spec "|" "")
     :offset offset
     :length (if (> second-pipe 0) (- second-pipe offset) 0)}))

(defn text->spec
  "Converts a text map: {:text \"a bit of text\" :offset 2 :length 3}
   to a text spec: \"a |bit| of text\" 

   Examples:
   {:text \"fooo\", :offset 0, :length 0} -> \"fooo\" 
   {:text \"fooo\", :offset 1, :length 0} -> \"f|ooo\" 
   {:text \"fooo\", :offset 1, :length 2} -> \"f|oo|o\"" 
  [text]
  (let [spec (t/str-insert (:text text) (:offset text) "|")
        spec (if (zero? (:length text)) spec (t/str-insert spec (+ 1 (:offset text) (:length text)) "|"))]
    spec))
