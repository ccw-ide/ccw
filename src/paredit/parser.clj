(ns paredit.parser
  (:use clojure.test)
  (:use clojure.contrib.core)
  (:use paredit.regex-utils)
	(:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter :as zf])
  (:use net.cgrand.parsley)
  (:require [net.cgrand.parsley.lrplus :as lr+])
  (:require [net.cgrand.regex :as r]))

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


(defn root-node-tag?
  "Temporary hack until parsley correctly generates root node tags from incremental buffer"
  [t]
  (= :root 
     (when t (-> t name keyword))))

(def open-list "(")
(def open-vector "[")
(def open-map "{")
(def open-set "#{")
(def open-quote \')
(def open-meta "^")
(def open-deref \@)         ;"#(?:[\{\(\'\^\"\_\!])" 
(def open-syntax-quote \`)
(def open-fn "#(")
(def open-var "#'")
(def open-deprecated-meta "#^")
(def open-string \")
(def open-regex "#\"")
(def open-unquote-splicing "~@")
(def open-unquote #"~(?!@)")
(def open-anon-arg "%")
(def open-keyword #":{1,2}")
(def open-discard "#_")
(def whitespace #"(?:,|\s)+")
(def open-comment #"(?:\#\!|;)")
(def open-char "\\")
(def symbol-exclusion #"[^\(\[\#\{\\\"\~\%\:\,\s\!\;\'\@\`;0-9]")
(def ^{:private true} prefixes
  #{open-list open-vector open-map open-set open-quote open-meta open-deref open-syntax-quote
    open-fn open-var open-deprecated-meta open-string open-regex open-unquote-splicing
    open-unquote open-anon-arg open-keyword open-discard whitespace open-comment
    open-char})
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
    :list [open-list :expr* ")"]
    :chimera #{ [open-list  :expr* #{"]" "}" eof}] 
                [open-vector  :expr* #{")" "}" eof}]
                [open-map :expr* #{")" "]" eof}]
                [open-fn :expr* #{"]" "}" eof}]
                [open-set :expr* #{")" "]" eof}]
                (unspaced open-string #"(?:\\.|[^\\\"])++(?!\")" :? eof)
                (unspaced open-regex #"(?:\\.|[^\\\"])++(?!\")" :? eof)
                [open-quote #{"]" "}" ")" eof}]
                [open-meta :expr :? #{"]" "}" ")" eof}]
                [open-deprecated-meta :expr :? #{"]" "}" ")" eof}]
                [open-deref #{"]" "}" ")" eof}]
                [open-syntax-quote #{"]" "}" ")" eof}]
                [open-var #{"]" "}" ")" eof}]
                [open-discard #{"]" "}" ")" eof}]
                [open-unquote-splicing #{"]" "}" ")" eof}]
                [open-unquote #{"]" "}" ")" eof}]
                (unspaced open-char eof)
                }
    :vector [open-vector :expr* "]"]
    :map [open-map :expr* "}"]
    :set [open-set :expr* "}"]
    :quote [open-quote :expr]
    :meta [open-meta :expr :expr]
    :deref [open-deref :expr]
    :syntax-quote [open-syntax-quote :expr]
    :var [open-var :expr]
    :fn [open-fn :expr* ")"]
    :deprecated-meta [open-deprecated-meta :expr :expr]
    :unquote-splicing [open-unquote-splicing :expr]
    :unquote [open-unquote :expr]
    :string (unspaced open-string #"(?:\\.|[^\\\"])++(?=\")" :? \")
    :regex  (unspaced open-regex #"(?:\\.|[^\\\"])++(?=\")" :? \")
    :symbol
      #"(?:[\-\+](?![0-9])[^\^\(\[\#\{\\\"\~\%\:\,\s\;\'\@\`\)\]\}]*)|(?:[^\^\(\[\#\{\\\"\~\%\:\,\s\;\'\@\`\)\]\}\-\+;0-9][^\^\(\[\#\{\\\"\~\%\:\,\s\;\'\@\`\)\]\}]*|#(?![\{\(\'\^\"\_\!])[^\^\(\[\#\{\\\"\~\%\:\,\s\;\'\@\`\)\]\}]*)#?"
    :keyword (unspaced open-keyword #"[^\(\[\{\'\^\@\`\~\"\\\,\s\;\)\]\}]*"); factorize with symbol
    :int #"(?:[-+]?(?:0(?!\.)|[1-9][0-9]*+(?!\.)|0[xX][0-9A-Fa-f]+(?!\.)|0[0-7]+(?!\.)|[1-9][0-9]?[rR][0-9A-Za-z]+(?!\.)|0[0-9]+(?!\.))(?!/))"
    :ratio #"[-+]?[0-9]+/[0-9]*"
    :float #"[-+]?[0-9]+\.[0-9]*+(?:[eE][-+]?+[0-9]+)?+M?"
    :anon-arg (unspaced open-anon-arg #"(?:[0-9|\&])?+")
    :char (unspaced open-char #"(?:newline|space|tab|backspace|formfeed|return|u[0-9|a-f|A-F]{4}|o[0-3]?+[0-7]{1,2}|.)")
    :whitespace whitespace
    :comment (unspaced open-comment #"[^\n]*")
    :discard [open-discard :expr]
    ))

(defn parse
  ([^String text]
    (sexp text))
  ([^String text offset]
    (sexp text)))

(defn parse-tree
  [state]
  state)

(comment 
(require '[net.cgrand.parsley.lrplus :as l])
(require '[net.cgrand.parsley.fold :as f])
(require '[paredit.loc-utils :as lu])
(defn dser-ser-inc 
  "Demonstration of parsley's incremental features"
  [d]
  (println "----------------------------------------")
  (let [steps (do (println "steps computing") (time (first (reduce (fn [[steps l] r] 
                               (let [step (sexp l r)] 
                                 [(conj steps step) step])) 
                             [[] l/zero]
                             (concat d [nil])))))
        stitch (do (println "reducing steps") (time (reduce f/stitch steps)))]
    (f/make-node :root (nth stitch 2)))))

(comment
  ;; {:v {:count}, :l, :r}
(defprotocol BST
  (bst-node ([this]) ([this l r v])))

(defrecord Leaf [v]
  BST
  (bst-node [this] this))
(defn node-val [l r]
  {:count (+ (-> l bst-node :v :count) (-> r bst-node :v :count))})
(defrecord Node [l r v]
  BST
  (bst-node ([this] this)
            ([this l r] (Node. l r (node-val l r))))))