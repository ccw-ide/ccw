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
    
       "|\nb" "a" nil
       "| |a\n b" "" "|a\n b"
    
       "|a\nb"  " "  " |a\nb"
       "(|foo\nbar)"  " "  "( |foo\nbar)"
       "(|foo\n bar)"  " "  "( |foo\n bar)"
       "(|foo\n  bar)"  " "  "( |foo\n  bar)"
       "(|foo\n\n  bar)"   " "   "( |foo\n\n  bar)"
       "(|foo\n \n  bar)"   " "   "( |foo\n \n  bar)"

       "(|foo\n\n  (bar\n    baz))"  " " "( |foo\n\n  (bar\n    baz))"

       "| |a\n b"   ""   "|a\n b"
     
       " |\n b"   "a"   nil;" a|\n b"
    
       " |\n b\n c"   " " nil;"  |\n b\n c"
       "|(\n) b\n  c"   " "   " |(\n) b\n  c"

       "|\n(\na)"  " " nil     
    
       ";\n| |(\n )"   ""   ";\n|(\n)"  
    
       "|\na"   ";"   nil 
    
       "(|)\n()" " " nil 

       "|a\nb" ";" nil

       "(|a) (b\n      c)"   " "   "( |a) (b\n       c)" 
     
       "(|a)\nb\nc"   " "   "( |a)\nb\nc"
     
       "(\n  a)|\n|"  ""   nil
     
       "(\n  a)|b|" "" nil
     
       "( |(a\n    b)\n  c)"   " "   "(  |(a\n     b)\n  c)"
       ))
