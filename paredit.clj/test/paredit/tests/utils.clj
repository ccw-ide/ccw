(ns paredit.tests.utils
  (:require [paredit.parser :refer [parse]])
  (:require [paredit.loc-utils :as l])
  (:require [paredit.text-utils :as t])
  (:require [paredit.core :as paredit])
  (:require [clojure.string :as s])
  (:require [clojure.zip :as z])
  (:require [clojure.pprint :refer (pprint)]))

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
    :else 
      (apply vector (:tag tree) (map clean-tree (:content tree)))))

(defn print-tree [t]
  (pprint (if (string? t) 
            (-> t tree clean-tree)
            (-> t clean-tree))))

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

(defn apply-edits [s edits]
  (reduce (fn [s {:keys [text offset length]}]
            (t/str-replace s offset length text))
    s (sort (comp - paredit/compare-edits) edits)))

(defn text+command->spec [{:keys [text] :as t} command]
  (cond
    (:selection command)
    (let [{:keys [selection edits]} command
          text (apply-edits text edits)
          [from to] (paredit/update-selection selection edits)]
      (if (= from to)
        (str (subs text 0 from) "|" (subs text from))
        (str (subs text 0 from) "|" (subs text from to) "|" (subs text to))))
    (nil? command) (text->spec t)
    :else
    (do #_(prn 'COMMAND command) #^:legacy (text->spec command))))
