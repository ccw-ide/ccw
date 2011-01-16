;adaptations paredit pour pouvoir echanger avec parsley:
;
;  1. renommer les tags des noeuds: des keywords :atom, :list, etc.
;  2. virer :end-offset, :offset, :line, :col
;  3. regler le "pb" du :root ??



 ; note : hiredman's reader http://github.com/hiredman/clojure/blob/readerII/src/clj/clojure/reader.clj#L516
; still TODO :
; 1. done - make parser and function behaviour similar for terminals atom and spaces 
; 1.a  (and move the special handling of zipping terminals up on :eof from default-handler to default-maker ?)
; 2. correctly handle clojure specificities : #{} #^ #"" ' ` @ ^ #' #_ #() ~ ~@ foo# #!
; 3. correctly handle the premature :eof on non closed structures (a cause of error)
; 4. correctly handle parsetree errors (wrong closing of bracket (this one done), ... TODO finish the exhaustive list)
; 5. make the parser restartable
; 6. make the parser incremental 
; 7. refactor the code so that the handling of advancing offset, line, column ... is mutualized (be aware of not introducing regressions in the handling of atoms and spaces terminals)
; point 6. is optional :-)
; point 3. may be viewed as a special case of point 4 ?

; bugs:
; \newlinb should be an error, not \n + symbol ewlinb

; miscellaneous TODO
; * add an explicit error message to :parser-state :ko (unbalanced parens)

(ns paredit.parser
  (:use clojure.test)
  (:use clojure.contrib.core)
  (:use paredit.regex-utils)
	(:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter :as zf])
  (:use net.cgrand.parsley :reload)
  (:require [net.cgrand.parsley.lrplus :as lr+]))

#_(set! *warn-on-reflection* true)

(def *spy?* (atom false))
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; utility code
(defn 
  start-like
  "returns true if s1 is a prefix of s2 or s2 is a prefix of s1.
   Examples:
   (= true (start-like \"bar and foo\" \"bar\"))
   (= true (start-like \"bar\" \"bar and foo\"))
   (= false (start-like \"bar\" \"baz\"))
   (= true (start-like \"bar\" \"bar\"))
   (= true (start-like \"ba\" \"bar\"))
   (= true (start-like \"bar\" \"ba\"))
   (= true (start-like \"b\" \"bar\"))
   (= true (start-like \"bar\" \"b\"))
   (= true (start-like \"\" \"bar\"))
   (= true (start-like \"bar\" \"\"))"
  [^String s1 ^String s2]
    (or (.startsWith s1 s2) (.startsWith s2 s1))
    #_(.startsWith s1 (.substring s2 0 (min (.length s2) (.length s1)))))

(deftest test-start-like
  (testing "start-like"
    (are [expected s1 s2]  (= expected (start-like s1 s2))
      true "bar and foo" "bar"
      true "bar" "bar and foo"

      false "bar" "baz"
      false "baz" "bar"

      true "bar" "bar"

      true "ba" "bar"
      true "bar" "ba"

      true "bar" "b"
      true "bar" "b"

      true "bar" ""
      true "" "bar")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; the parser code
(def *brackets* {"(" ")", "{" "}", "[" "]", "\"" "\"", "#\"" "\"", "#{" "}", "#(" ")"})
(def *tag-closing-brackets* {:list ")", :map "}", :vector "]", :string "\"", :regex "\"", :set "}", :fn ")"})
(def *tag-opening-brackets* {:list "(", :map "{", :vector "[", :string "\"", :regex "#\"", :set "#{", :fn "#("})
(def *brackets-tags* #{:list :map :vector :string :set :fn :regex})
(def ^{:private true} *opening-bracket-tags* {"(" :list, "{" :map, "[" :vector, "\"" :string, "#\"" :regex, "#{" :set, "#(" :fn})
(def *opening-brackets* (set (keys *brackets*)))
(def *closing-brackets* (set (vals *brackets*)))
(def *spaces* #{(str \space) (str \tab) (str \newline) (str \return) (str \,)})

(def *atom* #{:symbol :keyword :int :float :ratio :anon-arg})

(def *atoms* (conj *atom* :whitespace))

(defn empty-node? [node]
  (or 
    (= :whitespace (:tag node))
    (every? #(and (not (string? %)) (= :whitespace (:tag %))) (:content node))))

(defn char-at 
  "if index is out of bounds, just returns nil"
  [^String s index]
  (when (< -1 index (.length s))
    (.charAt s index)))
    
(defn eof [s eof?]
  (when (and (= 0 (.length s)) eof?) [0 eof]))

(defn bracket-end [s eof?]
  (lr+/match #{")" "]" "}" eof} s eof?))

(def gspaces #{:whitespace :comment :discard})
(def only-code (partial remove (comp gspaces :tag)))
(defn code-children [e] (only-code (:content e)))
(defn sym-name
  "returns the symbol name" [e] (and (#{:symbol} (:tag e)) (apply str (:content e))))
(defn call-of [e c] (and (#{"("} (nth (code-children e) 0)) (#{c} (sym-name (nth (code-children e) 1))) e))
(defn call-args [e] (-> (code-children e) nnext butlast))
(defn form 
  "removes the meta(s) to get to the form" 
  [e]
  (if-not (#{:meta} (:tag e))
    e
    (recur (nth (code-children e) 2))))

(def sexp
  (parser {:root-tag :root
           :main :expr*
           :space (unspaced gspaces :*)}
    :expr- #{
             :list
             :vector
             :map
;             :odd-map
             :set
             :quote
             :meta
             :deref
             :syntax-quote
             :var
             :fn
             :deprecated-meta
             :unquote-splicing
             :unquote
             :string
             :regex
             :symbol 
             :keyword 
             :int 
             :float 
             :ratio 
             :anon-arg
             :char
             ;:unexpected-close
             :chimera
             }
    :list ["(" :expr* ")"] ;#{")" "]" "}" eof}]
    :chimera #{ ["("  :expr* #{"]" "}" eof}] 
                ["["  :expr* #{")" "}" eof}]
                ["{" :expr* #{")" "]" eof}]
                ["#(" :expr* #{"]" "}" eof}]
                ["#{" :expr* #{")" "]" eof}]
                (unspaced \"    #"(?:\\.|[^\\\"])++(?!\")" :? eof)
                (unspaced "#\"" #"(?:\\.|[^\\\"])++(?!\")" :? eof)
                ;(unspaced \" #"(?:\\.|[^\\\"])*+" eof)
                }
    :vector ["[" :expr* "]"] ;#{")" "]" "}" eof}]
    :map ["{" :expr* "}"] ;#{")" "]" "}" eof}]
;    :map ["{" [:expr :expr]:* "}"]
 ;   :odd-map ["{" [:expr :expr]:* :expr "}"]
    :set ["#{" :expr* "}"] ;#{")" "]" "}" eof}]
    :quote [\' :expr]
    :meta ["^" :expr :expr]
    :deref [\@ :expr]
    :syntax-quote [\` :expr]
    :var ["#'" :expr]
    :fn ["#(" :expr* ")"]
    :deprecated-meta ["#^" :expr :expr]
    :unquote-splicing ["~@" :expr]
    :unquote [#"~(?!@)" :expr]
    :string (unspaced \"    #"(?:\\.|[^\\\"])++(?=\")" :? \")
    :regex  (unspaced "#\"" #"(?:\\.|[^\\\"])++(?=\")" :? \")
    :symbol 
      ;#"(?:\.|\/|\&|(?:(?:[a-z|A-Z|\*|\!|\-(?![0-9])|\_|\?|\>|\<|\=|\$]|\+(?![0-9]))(?:(?:(?:[a-z|A-Z|\*|\!|\-(?![0-9])|\_|\?|\>|\<|\=|\$]|\+(?![0-9]))|[0-9]|\.|\#(?!\()))*(?:\:(?:(?:(?:[a-z|A-Z|\*|\!|\-(?![0-9])|\_|\?|\>|\<|\=|\$]|\+(?![0-9]))|[0-9]|\.|\#(?!\()))+)*)(?:\/(?:(?:[a-z|A-Z|\*|\!|\-(?![0-9])|\_|\?|\>|\<|\=|\$]|\+(?![0-9]))(?:(?:(?:[a-z|A-Z|\*|\!|\-(?![0-9])|\_|\?|\>|\<|\=|\$]|\+(?![0-9]))|[0-9]|\.|\#(?!\()))*(?:\:(?:(?:(?:[a-z|A-Z|\*|\!|\-(?![0-9])|\_|\?|\>|\<|\=|\$]|\+(?![0-9]))|[0-9]|\.|\#(?!\()))+)*))?)"
      (let [symbol-head 
              #"(?:[a-z|A-Z|\*|\!]|\-(?![0-9])|[\_|\?|\>|\<|\=|\$]|\+(?![0-9]))"
              ; other characters will be allowed eventually, but not all macro characters have been determined
            symbol-rest 
              (interpol-regex #"(?:`symbol-head`|[0-9]|\.|\#(?!\())")
              ; "." : multiple successive points is allowed by the reader (but will break at evaluation)
              ; "#" : normally # is allowed only in syntax quote forms, in last position
            symbol-name
              (interpol-regex #"(?:`symbol-head``symbol-rest`*(?:\:`symbol-rest`++)*+)")
              ]
        (interpol-regex #"(?:\.|\/|\&|`symbol-name`(?:\/`symbol-name`)?)"))
        ;:symbol- #"[:]?([\D&&[^/]].*/)?([\D&&[^/]][^/]*)"  
    ; from old definition of symbol :symbol- #"[\%|\&||\.|\/|.*"
    :int #"[-+]?(?:0(?!\.)|[1-9][0-9]*+(?!\.)|0[xX][0-9A-Fa-f]++(?!\.)|0[0-7]++(?!\.)|[1-9][0-9]?[rR][0-9A-Za-z]++(?!\.)|0[0-9]++(?!\.))"
    :ratio #"[-+]?[0-9]++/[0-9]++"
    :float #"[-+]?[0-9]++\.[0-9]*+(?:[eE][-+]?+[0-9]++)?+M?+"
    :anon-arg #"%(?:[0-9|\&])?+" ; (?![_|\(])
    :keyword (unspaced #":{1,2}" #"[^\(\[\{\'\^\@\`\~\"\\\,\s\;\)\]\}]*+")
            ;:atom #"[a-z|A-Z|0-9|\!|\$|\%|\&|\*|\+|\-|\.|\/|\:|\<|\=|\>|\?|\_][a-z|A-Z|0-9|\!|\$|\%|\&|\*|\+|\-|\.|\/|\:|\<|\=|\>|\?|\_|\#]*"
            ;:atom #"[a-z|A-Z|0-9|\!|\$|\%|\&|\*|\+|\-|\.|\/|\:|\<|\=|\>|\?|\_].*"
    ;;;; CAS DU +toto+ -toto-
    :char #"\\(?:newline|space|tab|backspace|formfeed|return|u[0-9|a-f|A-F]{4}|o[0-3]?+[0-7]{1,2}|.)"
    :whitespace #"(?:,|\s)++"
    :comment #"(?:\#\!|;)[^\n]*+"
    :discard ["#_" :expr]
    ;:unexpected-close #{#"}" #"\)" #"]"}
    ))

(defn parse
  ([^String text]
    (sexp text))
  ([^String text offset]
    (sexp text)))

(defn parse-tree
  [state]
  state)
