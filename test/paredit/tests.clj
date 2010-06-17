(ns paredit.tests
  (:use paredit.core-commands)
  (:use paredit.core)
  (:use [paredit.parser :exclude [pts]])
  (:require [paredit.text-utils :as t])
  (:use clojure.contrib.def)
  (:use clojure.test)
  (:require [clojure.contrib.str-utils2 :as str2])
  (:use clojure.contrib.core)
  (:require [clojure.zip :as zip])
  (:use paredit.loc-utils))

(defn text-spec-to-text 
  "Converts a text spec to text map" 
  [^String text-spec]
  (let [offset (.indexOf text-spec "|")
        second-pipe (dec (.indexOf text-spec "|" (inc offset)))]  
    {:text (str2/replace text-spec "|" "")
     :offset offset
     :length (if (> second-pipe 0) (- second-pipe offset) 0)}))

(defn text-to-text-spec
  "Converts a text map to text spec"
  [text]
  (let [spec (t/str-insert (:text text) (:offset text) "|")
        spec (if (zero? (:length text)) spec (t/str-insert spec (+ 1 (:offset text) (:length text)) "|"))]
    spec))

(deftest normalized-selection-tests
  (are [text offset length expected-offset expected-length]
    (= [expected-offset expected-length] (-?> (parse text) (parsed-root-loc true) (normalized-selection offset length) (text-selection)))
    "foo bar baz" 4 0 4 0
    "foo bar baz" 4 3 4 3
    "foo bar baz" 4 6 4 7
    "foo bar baz" 4 7 4 7 
    "foo (bar baz)" 5 3 5 3 
    "foo (bar baz)" 5 4 5 4 
    "foo (bar baz)" 5 5 5 7 
    "foo (bar baz)" 5 7 5 7 
    "foo (bar baz)" 4 0 4 0
    "foo bar (baz) foo" 4 9 4 9
    "foo bar (baz) foo" 4 10 4 10
    "foo bar (baz) foo" 4 11 4 13 ;
    "foo (bar baz) foo" 4 2 4 9
    "foo bar" 2 5 0 7
    "foo (bar baz)" 4 9 4 9
    "foo (bar baz)" 4 4 4 9
    "foo (bar baz)" 9 4 4 9 
    "foo (bar baz)" 0 0 0 0
    "(foo bar)" 8 0 8 0
    "foo (bar baz)" 12 1 4 9 
    ))


(defn test-command [title-prefix command]
  (testing (str title-prefix " " (second command) " (\"" (first command) "\")")
    (doseq [[input expected] (get command 2)]
      (spy "++++++++++++++++++++")
      (spy (text-spec-to-text input))
      (is (= expected (text-to-text-spec (paredit (second command) (text-spec-to-text input))))))))

(deftest paredit-tests
  (doseq [group *paredit-commands*]
    (testing (str (first group) ":")
      (doseq [command (rest group)]
        (test-command "public documentation of paredit command" command)
        (test-command "additional non regression tests of paredit command " (assoc command 2 (get command 3)))))))

(deftest spec-text-tests
  (are [spec text] (and 
                     (= text (text-spec-to-text spec))
                     (= spec (text-to-text-spec text)))
    "foo |bar" {:text "foo bar" :offset 4 :length 0}
    "|" {:text "" :offset 0 :length 0}
    "|foo" {:text "foo" :offset 0 :length 0}
    "foo|" {:text "foo" :offset 3 :length 0}
    "foo |bar| foo" {:text "foo bar foo" :offset 4 :length 3}))

(deftest loc-for-offset-tests
  (are [text offset expected-tag] (= expected-tag (-?> (parse text) (parsed-root-loc true) (loc-for-offset offset) (zip/node) :tag))
    "foo (bar baz) baz" 12 :list ;nil
    "hello" 0 :atom
    "hello" 1 :atom
    "hello" 5 nil ;:root
    "a b" 0 :atom
    "a b" 1 :whitespace
    "a b" 2 :atom
    "foo \"bar\" foo" 3 :whitespace
    "foo \"bar\" foo" 4 :string))

(deftest leave-for-offset-tests
  (are [text offset expected-tag ?expected-node]
    (let [l (-?> (parse text) (parsed-root-loc true) (leave-for-offset offset))] 
      (and
        (= expected-tag (loc-tag l))
        (or (nil? ?expected-node) (= ?expected-node (zip/node l)))))
    "foo (bar baz) baz" 12 :list ")"
    "hello" 0 :atom nil
    "hello" 1 :atom nil
    "hello" 5 :root nil
    "a b" 0 :atom nil
    "a b" 1 :whitespace nil
    "a b" 2 :atom nil
    "foo \"bar\" foo" 3 :whitespace nil
    "foo \"bar\" foo" 4 :string nil
    ))

(deftest loc-containing-offset-tests
  (are [text offset expected-tag] (= expected-tag (-?> (parse text) (parsed-root-loc true) (loc-containing-offset offset) (zip/node) :tag))
    "hello" 1 :atom
    "foo bar" 3 :root
    "foo bar" 4 :root
    "hello" 5 :root
    "a b" 0 :root
    "a b" 1 :root
    "a b" 2 :root
    "foo \"bar\" foo" 3 :root
    "foo \"bar\" foo" 4 :root
    ))
(defn pts []
  (normalized-selection-tests)
  (t/line-stop-tests)
  (spec-text-tests)
  (paredit-tests)
  #_(loc-for-offset-tests)
  (leave-for-offset-tests)
  (loc-containing-offset-tests))

(defvar *text* (atom {:text "" :offset 0 :length 0})
  "defines a text, with :offset being the cursor position,
   and :length being a possible selection (may be negative)")