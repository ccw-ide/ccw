(ns paredit.loc-utils-test
  (:require [clojure.test :refer [deftest are is]])
  (:require [paredit.tests.utils :as u])
  (:require [clojure.zip :as z])
  (:require [paredit.parser :refer [parse]])
  (:require [paredit.loc-utils :as l]))

(deftest newline?-tests
  (are 
    [spec expected]
    (is (= expected (let [{:keys [text offset]} (u/spec->text spec)
                          loc (-> text 
                                   parse
                                   l/parsed-root-loc
                                   (l/loc-for-offset offset))]
                      (l/newline? loc))))
    "|foo\nbar" false ; it's not a newline in the sense
                      ; that's it's not a new line following another one
    "foo|\nbar" true
    "foo\n|bar" false

    "foo| \n bar" true
    "foo\n |bar" false

    "(|foo\nbar)" false
    "(foo|\nbar)" true
    "(foo\n|bar)" false
    
    "(|foo;bar\nbaz)" false
    "(foo|;bar\nbaz)" false
    "(foo;bar\n|baz)" true ; remember, comments "own" newlines

    "(|foo;bar\n baz)" false
    "(foo|;bar\n baz)" false
    "(foo;bar\n| baz)" true ; remember, comments "own" newlines
    "(foo;bar\n |baz)" false ; remember, comments "own" newlines

    "(|foo;bar\n\n baz)" false
    "(foo|;bar\n\n baz)" false
    "(foo;bar\n|\n baz)" true ; remember, comments "own" newlines
    "(foo;bar\n\n |baz)" false ; remember, comments "own" newlines
    
    ";\n|foo" true
    ))

(deftest shift-nl-whitespace-tests
  (are [spec delta expected]
    
    (is (= expected (let [{:keys [text offset]} (u/spec->text spec)
                          actual (-> text 
                                   parse
                                   l/parsed-root-loc
                                   (l/loc-for-offset offset)
                                   (l/shift-nl-whitespace delta)
                                   z/root
                                   l/node-text)]
                      actual)))
    
    "foo|\nbar"  2 "foo\n  bar"
    "foo|\nbar"  1 "foo\n bar"
    "foo|\nbar"  0 "foo\nbar"
    "foo|\nbar" -1 "foo\nbar"
    "foo|\nbar" -2 "foo\nbar"
    
    "foo;comment\n|  bar"  2 "foo;comment\n    bar"
    "foo;comment\n|  bar"  1 "foo;comment\n   bar"
    "foo;comment\n|  bar"  0 "foo;comment\n  bar"
    "foo;comment\n|  bar" -1 "foo;comment\n bar"
    "foo;comment\n|  bar" -2 "foo;comment\nbar"
    "foo;comment\n|  bar" -3 "foo;comment\nbar"
    
    "foo;comment\n|bar"  2 "foo;comment\n  bar"
    "foo;comment\n|bar"  1 "foo;comment\n bar"
    "foo;comment\n|bar"  0 "foo;comment\nbar"
    "foo;comment\n|bar" -1 "foo;comment\nbar"
    
    "foo;comment\n|\nbar"  2 "foo;comment\n\n  bar"
    "foo;comment\n|\nbar"  1 "foo;comment\n\n bar"
    "foo;comment\n|\nbar"  0 "foo;comment\n\nbar"
    "foo;comment\n|\nbar" -1 "foo;comment\n\nbar"
    
    "foo;comment\n|  \n bar"  2 "foo;comment\n  \n   bar"
    "foo;comment\n|  \n bar"  1 "foo;comment\n  \n  bar"
    "foo;comment\n|  \n bar"  0 "foo;comment\n  \n bar"
    "foo;comment\n|  \n bar" -1 "foo;comment\n  \nbar"
    "foo;comment\n|  \n bar" -2 "foo;comment\n  \nbar"
    
    ";\n(|\n )" -1 ";\n(\n)" 
    ))

(deftest propagate-delta-tests
  (are [spec col delta expected]
    
    (let [{:keys [text offset]} (u/spec->text spec)
          [loc _] (-> text 
                       parse
                       l/parsed-root-loc
                       (l/loc-for-offset offset)
                       (l/propagate-delta col delta))]
      (is (= expected (-> loc z/root l/node-text))))
    
    " |a\nb"  0  1 " a\n b"
    "( |foo\nbar)" 1 1 "( foo\nbar)"
    "( |foo\n bar)" 1 1 "( foo\n  bar)"
    "( |foo\n  bar)" 1 1 "( foo\n   bar)"
    "( |foo\n\n  bar)" 1 1 "( foo\n\n   bar)"
    "( |foo\n \n  bar)" 1 1 "( foo\n \n   bar)"

    "( |foo\n\n  (bar\n    baz))" 1 1 "( foo\n\n   (bar\n     baz))"

    "|a\n b"   1 -1 "a\nb"
     
    " a|\n b"  1  1 " a\n b"
    
    "  |\n b\n c" 1 1 "  \n b\n c"
    " |(\n) b\n  c" 0 1 " (\n ) b\n   c"

    " |\n(\na)" 0 1 " \n(\na)"     
    
    ";\n|(\n )" 1 -1 ";\n(\n)"  
    
    ";|\na" 0 1 ";\na" 
    ))

