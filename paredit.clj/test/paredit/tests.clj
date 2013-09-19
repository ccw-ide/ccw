(ns paredit.tests
  (:use paredit.core-commands)
  (:use paredit.core)
  (:use [paredit.parser :exclude [pts]])
  (:require [paredit.text-utils :as t])
  (:use clojure.test)
  (:require [clojure.string :as str])
  (:use [clojure.core.incubator :only [-?>]])
  (:require [clojure.zip :as zip])
  (:require [paredit.static-analysis :as st])
  (:require [paredit.tests.utils :as u])
  (:use paredit.loc-utils))

(def ^:dynamic *spy?* (atom false))
(defn start-spy [] (reset! *spy?* true))
(defn stop-spy [] (reset! *spy?* false))

(defn spy*
  [msg expr]
  `(let [expr# ~expr]
     (do
       (when  @*spy?* (println (str "::::spying[" ~msg "]:::: " '~expr ":::: '" expr# "'")))
       expr#)))

(defmacro spy 
  ([expr] (spy* "" expr))
  ([msg expr] (spy* msg expr)))

(deftest unescape-string-content-tests
  (are [unescaped expected-escaped]
    (= expected-escaped (escape-string-content unescaped))
    ""                                      ""
    "abcd"                                  "abcd"
    "/"                                     "/"
    "\""                                    "\\\""
    "\\\""                                  "\\\\\\\""
    "<name attr=\"value\">\"text\"</name>" "<name attr=\\\"value\\\">\\\"text\\\"</name>"
    "\\d"                                  "\\\\d"))


(defn test-command [title-prefix command]
  (testing (str title-prefix " " (second command) " (\"" (first command) "\")")
    (doseq [[input expected] (get command 2)]
      (spy (u/spec->text input))
      (let [{text :text :as t} (u/spec->text input)
            buffer (edit-buffer nil 0 -1 text)
            parse-tree (buffer-parse-tree buffer :for-test)]
        (is (= expected
               (u/text->spec (paredit (second command) 
                                           {:parse-tree parse-tree, 
                                            :buffer buffer} 
                                           t))))))))

(deftest paredit-tests
  (doseq [group *paredit-commands*]
    (testing (str (first group) ":")
      (doseq [command (rest group)]
        (test-command "public documentation of paredit command" command)
        (test-command "additional non regression tests of paredit command " (assoc command 2 (get command 3)))))))

(deftest parser-tests
  (is (not= nil (sexp ":"))))

(defn parsetree-to-string [parsetree]
  (->> parsetree 
    clojure.zip/xml-zip 
    paredit.loc-utils/next-leaves 
    (map clojure.zip/node) 
    (apply str)))

(deftest parsetree-tests
  (doseq [s [""
             "(defn "
             "3/4"
             "-3/4"
             "3/"
             ":éà"
             "::éà"
             "or#"
             "^"
             "^foo"
             "#"
             "'"
             "~"
             "~@"
             "@"
             "#_"
             "#^"
             "#^foo"
             "`"
             "#'"
             "#("
             "#!"
             "\\"
             "(foo `)"
             "(foo ^)"
             "(foo #)"
             "(foo ')"
             "(foo ~)"
             "(foo ~@)"
             "(foo @)"
             "(foo #_)"
             "(foo #^)"
             "(foo #')"
             "(foo #()"
             "(foo #!)"
             "(foo \\)"
             "#"
             ]]
    (is (= s (parsetree-to-string (parse s)))))
  (doseq [r ["paredit/compile.clj" 
             "paredit/core_commands.clj"
             "paredit/core.clj"
             "paredit/loc_utils.clj"
             "clojure/core.clj"
             ]]
    (let [s (slurp (.getResourceAsStream (clojure.lang.RT/baseLoader) r))]
      (is (= s (parsetree-to-string (parse s))))))
  (are [text expected-tag] (= expected-tag (-?> text parse :content (get 0) :tag))
       "#" :net.cgrand.parsley/unfinished
       "f#" :symbol
       "#'foo" :var
       "#foo bar" :reader-literal
       "#foo.bar baz" :reader-literal
       "#foo.bar []" :reader-literal
       "#foo 5" :reader-literal))

(deftest static-analysis-tests
  (are [text]
       (= "foo" (-?> text u/tree (st/find-namespace)))
    "(ns foo)"
    ";some comment\n(ns foo)"
    "#!nasty comment\n(ns foo)"
    " \"some string\"(ns foo) "
    "(ns ^:foo foo)"
    "^:foo (ns foo)"
    "(ns ^{:author \"Laurent Petit\"} foo)"
    "^:foo (ns ^:foo foo)"
    "^:foo (ns ^:foo ^{:author \"Laurent Petit\"} foo)"
    "^{:a :b};sdf\n^:bar ^:foo (ns ^:foo ^{:author ;comment\n\"Laurent Petit\"}\n;foo\n#_bleh foo)"))

(deftest code-forms-tests
  (are [before after]
       (= after (->> before 
                       parse
                       parsed-root-loc
                       zip/down
                       (#(take-while identity (iterate zip/right %))) ; children locs
                       st/code-forms
                       (map loc-text)
                       (apply str)))
  "" ""
  "a" "a"
  "(a)"        "(a)"
  "(a)(b)"     "(a)(b)" 
  "(a)\n\n(b)" "(a)(b)"
  "(a);foo\n^:anything(b)" "(a)^:anything(b)"
  "#_(a)" ""
  "#_(a)b c" "bc"))

(deftest top-level-code-form-tests
  (are [code offset sel]
       (= sel (-> code 
                       parse
                       parsed-root-loc
                       (st/top-level-code-form offset)
                       loc-text))
  "(a)" 0 "(a)"
  "(a)" 1 "(a)"
  "(a)" 2 "(a)"
  "(a)" 3 "(a)"
  "(a)\n(b)" 3 "(a)"
  "(a)\n(b)" 4 "(b)"
  "(a)\n(b)" 5 "(b)"
  "(a)\n(b)" 7 "(b)"
  "(a)\n;foo\n\n^:true b" 6 "(a)"
  "(a)\n;foo\n\n^:true b" 9 "(a)"
  "(a)\n;foo\n\n^:true b" 10 "^:true b"
  "(a)\n;foo\n\n^:true b" 17 "^:true b"))

(deftest col-tests
  (are 
    [text-spec expected-col] 
    (let [{:keys [text offset]} (u/spec->text text-spec)]
      (is (= expected-col (t/col text offset))))
    "|foo" 0
    "f|oo" 1
    "foo|" 3
    "foo|\n" 3
    "foo\n|" 0
    "foo\n \n|" 0
    "foo\n \n |" 1))

(defn pts []
  (t/line-stop-tests)
  (paredit-tests)
  (parser-tests)
  (parsetree-tests)
  (unescape-string-content-tests)
  (static-analysis-tests))

(def ^{:dynamic true
       :doc 
          "defines a text, with :offset being the cursor position,
           and :length being a possible selection (may be negative)"}
      *text* (atom {:text "" :offset 0 :length 0}))
