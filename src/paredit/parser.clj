(ns paredit.parser
  (:use clojure.test)
  (:use clojure.contrib.core)
  (:use paredit.regex-utils)
	(:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter :as zf])
  (:use net.cgrand.parsley)
  (:require [net.cgrand.parsley.lrplus :as lr+]))

#_(set! *warn-on-reflection* true)

(def *brackets-tags* #{:list :map :vector :string :set :fn :regex})
(def *tag-closing-brackets* {:list ")", :map "}", :vector "]", :string "\"", :regex "\"", :set "}", :fn ")"})
(def *tag-opening-brackets* {:list "(", :map "{", :vector "[", :string "\"", :regex "#\"", :set "#{", :fn "#("})
(def *atom* #{:symbol :keyword :int :float :ratio :anon-arg})

#_(def ^{:private true} *brackets* {"(" ")", "{" "}", "[" "]", "\"" "\"", "#\"" "\"", "#{" "}", "#(" ")"})
#_(def ^{:private true} *opening-bracket-tags* {"(" :list, "{" :map, "[" :vector, "\"" :string, "#\"" :regex, "#{" :set, "#(" :fn})
#_(def ^{:private true} *opening-brackets* (set (keys *brackets*)))
#_(def ^{:private true} *closing-brackets* (set (vals *brackets*)))
#_(def ^{:private true} *spaces* #{(str \space) (str \tab) (str \newline) (str \return) (str \,)})
#_(def *atoms* (conj *atom* :whitespace))

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
             :chimera
             }
    :list ["(" :expr* ")"]
    :chimera #{ ["("  :expr* #{"]" "}" eof}] 
                ["["  :expr* #{")" "}" eof}]
                ["{" :expr* #{")" "]" eof}]
                ["#(" :expr* #{"]" "}" eof}]
                ["#{" :expr* #{")" "]" eof}]
                (unspaced \"    #"(?:\\.|[^\\\"])++(?!\")" :? eof)
                (unspaced "#\"" #"(?:\\.|[^\\\"])++(?!\")" :? eof)
                }
    :vector ["[" :expr* "]"]
    :map ["{" :expr* "}"]
    :set ["#{" :expr* "}"]
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
      (let [symbol-head 
              #"(?:[a-z|A-Z|\*|\!]|\-(?![0-9])|[\_|\?|\>|\<|\=|\$]|\+(?![0-9]))"
            symbol-rest 
              (interpol-regex #"(?:`symbol-head`|[0-9]|\.|\#(?!\())")
            symbol-name
              (interpol-regex #"(?:`symbol-head``symbol-rest`*(?:\:`symbol-rest`++)*+)")
              ]
        (interpol-regex #"(?:\.|\/|\&|`symbol-name`(?:\/`symbol-name`)?)"))
    :int #"[-+]?(?:0(?!\.)|[1-9][0-9]*+(?!\.)|0[xX][0-9A-Fa-f]++(?!\.)|0[0-7]++(?!\.)|[1-9][0-9]?[rR][0-9A-Za-z]++(?!\.)|0[0-9]++(?!\.))"
    :ratio #"[-+]?[0-9]++/[0-9]++"
    :float #"[-+]?[0-9]++\.[0-9]*+(?:[eE][-+]?+[0-9]++)?+M?+"
    :anon-arg #"%(?:[0-9|\&])?+"
    :keyword (unspaced #":{1,2}" #"[^\(\[\{\'\^\@\`\~\"\\\,\s\;\)\]\}]*+")
    :char #"\\(?:newline|space|tab|backspace|formfeed|return|u[0-9|a-f|A-F]{4}|o[0-3]?+[0-7]{1,2}|.)"
    :whitespace #"(?:,|\s)++"
    :comment #"(?:\#\!|;)[^\n]*+"
    :discard ["#_" :expr]
    ))

(defn parse
  ([^String text]
    (sexp text))
  ([^String text offset]
    (sexp text)))

(defn parse-tree
  [state]
  state)
