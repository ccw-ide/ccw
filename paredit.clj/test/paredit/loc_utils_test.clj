(ns paredit.loc-utils-test
  (:require [clojure.test :refer [deftest are is]])
  (:require [paredit.tests.utils :as u])
  (:require [clojure.zip :as z])
  (:require [paredit.text-utils :as t])
  (:require [paredit.parser :refer [parse edit-buffer buffer-parse-tree]])
  (:require [paredit.loc-utils :as l]))

(deftest col-shift-tests 
  (are [spec-before inserted-text spec-after] ; set spec-after to nil if no shift intended
                                              ; set spec-after to modif to spec-before
                                              ; because eclipse expects non-overlapping modifs
                                              ; of the initial text
    
    (let [{:keys [text offset length]} (u/spec->text spec-before)
          buffer (edit-buffer nil 0 0 text)
          parse-tree (buffer-parse-tree buffer 0)
          {[modif] :modifs 
           offset  :offset
           length  :length} (l/col-shift {:parse-tree parse-tree
                                          :buffer buffer}
                                         {:offset offset
                                          :length length
                                          :text inserted-text})
          expected-text-after (when spec-after (u/spec->text spec-after))
          text-after (when modif {:text (t/str-replace text
                                                       (:offset modif)
                                                       (:length modif)
                                                       (:text modif))
                                  :offset offset
                                  :length length})]
      (is (= expected-text-after text-after)))
    
      ; this is a little weird: the caret is placed to the final 
      ; position in the text, but the text represents only the 
      ; modif for shifting the rest
    
       "|\nb" "a" nil
       "| |a\n b" "" nil
    
       "|a\nb"  " "  nil
       "(|foo\nbar)"  " "  nil
       "(|foo\n bar)"  " "  nil
       "(|foo\n  bar)"  " "  nil
       "(|foo\n\n  bar)"   " "   nil
       "(|foo\n \n  bar)"   " "   nil

       "(|foo\n\n  (bar\n    baz))"  " " nil

       "| |a\n b"   ""   nil
     
       " |\n b"   "a"   nil
    
       " |\n b\n c"   " " nil
       "|(\n) b\n  c"   " "   nil

       "|\n(\na)"  " " nil     
    
       ";\n| |(\n )"   ""   ";\n| (\n)"  
    
       "|\na"   ";"   nil 
    
       "(|)\n()" " " nil 

       "|a\nb" ";" nil

       "(|a) (b\n      c)"   " "   "(a|) (b\n       c)" 
     
       "(|a)\nb\nc"   " "   nil
     
       "(\n  a)|\n|"  ""   nil
     
       "(\n  a)|b|" "" nil
     
       "( |(a\n    b)\n  c)"   " "   "( (|a\n     b)\n  c)"
       ))
