; todo 
; done 1. emit text deltas, not plain text replacement (or IDEs will not like it)
; done 2. have a story for invalid parsetrees : just do nothing : currently = paredit deactivated if error from start-of-file to area of paredit's work
; 3. use restartable version of the parser
; 4. make paredit optional in ccw
; 5. prepare a new release of ccw
; 6. write with clojure.zip functions the close-* stuff
; 7. write the string related stuff
; ... ?
; . add support for more clojure-related source code ( #{}, #""... )
; ... and all the other paredit stuff ...

(ns paredit.core
  (:use clojure.contrib.def)
  (:use clojure.test)
  (:use paredit.parser)
  (:use clojure.set)
  (:require clojure.contrib.pprint)
  (:require [clojure.contrib.str-utils2 :as str2])
  (:require [clojure.zip :as zip]))

#_(set! *warn-on-reflection* true)

;;; -*- Mode: Emacs-Lisp; outline-regexp: "\n;;;;+" -*-

;;;;;; Paredit: Parenthesis-Editing Minor Mode
;;;;;; Version 21

;;; Copyright (c) 2008, Taylor R. Campbell
;;;
;;; Redistribution and use in source and binary forms, with or without
;;; modification, are permitted provided that the following conditions
;;; are met:
;;;
;;; * Redistributions of source code must retain the above copyright
;;;   notice, this list of conditions and the following disclaimer.
;;;
;;; * Redistributions in binary form must reproduce the above copyright
;;;   notice, this list of conditions and the following disclaimer in
;;;   the documentation and/or other materials provided with the
;;;   distribution.
;;;
;;; * Neither the names of the authors nor the names of contributors
;;;   may be used to endorse or promote products derived from this
;;;   software without specific prior written permission.
;;;
;;; THIS SOFTWARE IS PROVIDED BY THE AUTHORS ``AS IS'' AND ANY EXPRESS
;;; OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
;;; WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
;;; ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
;;; DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
;;; DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
;;; GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
;;; INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
;;; WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
;;; NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
;;; SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

;;; This file is permanently stored at
;;;   <http://mumble.net/~campbell/emacs/paredit-21.el>.
;;;
;;; The currently released version of paredit is available at
;;;   <http://mumble.net/~campbell/emacs/paredit.el>.
;;;
;;; The latest beta version of paredit is available at
;;;   <http://mumble.net/~campbell/emacs/paredit-beta.el>.
;;;
;;; Release notes are available at
;;;   <http://mumble.net/~campbell/emacs/paredit.release>.

;;; Install paredit by placing `paredit.el' in `/path/to/elisp', a
;;; directory of your choice, and adding to your .emacs file:
;;;
;;;   (add-to-list 'load-path "/path/to/elisp")
;;;   (autoload 'paredit-mode "paredit"
;;;     "Minor mode for pseudo-structurally editing Lisp code."
;;;     t)
;;;
;;; Toggle Paredit Mode with `M-x paredit-mode RET', or enable it
;;; always in a major mode `M' (e.g., `lisp' or `scheme') with:
;;;
;;;   (add-hook M-mode-hook (lambda () (paredit-mode +1)))
;;;
;;; Customize paredit using `eval-after-load':
;;;
;;;   (eval-after-load 'paredit
;;;     '(progn ...redefine keys, &c....))
;;;
;;; Paredit should run in GNU Emacs 21 or later and XEmacs 21.5 or
;;; later.  Paredit is highly unlikely to work in earlier versions of
;;; GNU Emacs, and it may have obscure problems in earlier versions of
;;; XEmacs due to the way its syntax parser reports conditions, as a
;;; result of which the code that uses the syntax parser must mask all
;;; error conditions, not just those generated by the syntax parser.
;;;
;;; Questions, bug reports, comments, feature suggestions, &c., may be
;;; addressed via email to the author's surname at mumble.net or via
;;; IRC to the user named Riastradh on irc.freenode.net in the #paredit
;;; channel.
;;;
;;; Please contact the author rather than forking your own versions, to
;;; prevent the dissemination of random variants floating about the
;;; internet unbeknownst to the author.  Laziness is not an excuse:
;;; your laziness costs me confusion and time trying to support
;;; paredit, so if you fork paredit, you make the world a worse place.
;;;
;;; *** WARNING *** IMPORTANT *** DO NOT SUBMIT BUGS BEFORE READING ***
;;;
;;; If you plan to submit a bug report, where some sequence of keys in
;;; Paredit Mode, or some sequence of paredit commands, doesn't do what
;;; you wanted, then it is helpful to isolate an example in a very
;;; small buffer, and it is **ABSOLUTELY**ESSENTIAL** that you supply,
;;; along with the sequence of keys or commands,
;;;
;;;   (1) the version of Emacs,
;;;   (2) the version of paredit.el[*], and
;;;   (3) the **COMPLETE** state of the buffer used to reproduce the
;;;       problem, including major mode, minor modes, local key
;;;       bindings, entire contents of the buffer, leading line breaks
;;;       or spaces, &c.
;;;
;;; It is often extremely difficult to reproduce problems, especially
;;; with commands like `paredit-kill'.  If you do not supply **ALL** of
;;; this information, then it is highly probable that I cannot
;;; reproduce your problem no matter how hard I try, and the effect of
;;; submitting a bug without this information is only to waste your
;;; time and mine.  So, please, include all of the above information.
;;;
;;; [*] If you are using a beta version of paredit, be sure that you
;;;     are using the *latest* edition of the beta version, available
;;;     at <http://mumble.net/~campbell/emacs/paredit-beta.el>.  If you
;;;     are not using a beta version, then upgrade either to that or to
;;;     the latest release version; I cannot support older versions,
;;;     and I can't fathom any reason why you might be using them.  So
;;;     the answer to item (2) should be either `release' or `beta'.

;;; The paredit minor mode, Paredit Mode, binds a number of simple
;;; keys, notably `(', `)', `"', and `\', to commands that more
;;; carefully insert S-expression structures in the buffer.  The
;;; parenthesis delimiter keys (round or square) are defined to insert
;;; parenthesis pairs and move past the closing delimiter,
;;; respectively; the double-quote key is multiplexed to do both, and
;;; also to insert an escape if within a string; and backslashes prompt
;;; the user for the next character to input, because a lone backslash
;;; can break structure inadvertently.  These all have their ordinary
;;; behaviour when inside comments, and, outside comments, if truly
;;; necessary, you can insert them literally with `C-q'.
;;;
;;; The key bindings are designed so that when typing new code in
;;; Paredit Mode, you can generally use exactly the same keystrokes as
;;; you would have used without Paredit Mode.  Earlier versions of
;;; paredit.el did not conform to this, because Paredit Mode bound `)'
;;; to a command that would insert a newline.  Now `)' is bound to a
;;; command that does not insert a newline, and `M-)' is bound to the
;;; command that inserts a newline.  To revert to the former behaviour,
;;; add the following forms to an `eval-after-load' form for paredit.el
;;; in your .emacs file:
;;;
;;;   (define-key paredit-mode-map (kbd ")")
;;;     'paredit-close-round-and-newline)
;;;   (define-key paredit-mode-map (kbd "M-)")
;;;     'paredit-close-round)
;;;
;;; Paredit Mode also binds the usual keys for deleting and killing, so
;;; that they will not destroy any S-expression structure by killing or
;;; deleting only one side of a parenthesis or quote pair.  If the
;;; point is on a closing delimiter, `DEL' will move left over it; if
;;; it is on an opening delimiter, `C-d' will move right over it.  Only
;;; if the point is between a pair of delimiters will `C-d' or `DEL'
;;; delete them, and in that case it will delete both simultaneously.
;;; `M-d' and `M-DEL' kill words, but skip over any S-expression
;;; structure.  `C-k' kills from the start of the line, either to the
;;; line's end, if it contains only balanced expressions; to the first
;;; closing delimiter, if the point is within a form that ends on the
;;; line; or up to the end of the last expression that starts on the
;;; line after the point.
;;;
;;; The behaviour of the commands for deleting and killing can be
;;; overridden by passing a `C-u' prefix argument: `C-u DEL' will
;;; delete a character backward, `C-u C-d' will delete a character
;;; forward, and `C-u C-k' will kill text from the point to the end of
;;; the line, irrespective of the S-expression structure in the buffer.
;;; This can be used to fix mistakes in a buffer, but should generally
;;; be avoided.
;;;
;;; Paredit performs automatic reindentation as locally as possible, to
;;; avoid interfering with custom indentation used elsewhere in some
;;; S-expression.  Only the advanced S-expression manipulation commands
;;; automatically reindent, and only the forms that were immediately
;;; operated upon (and their subforms).
;;;
;;; This code is written for clarity, not efficiency.  It frequently
;;; walks over S-expressions redundantly.  If you have problems with
;;; the time it takes to execute some of the commands, let me know, but
;;; first be sure that what you're doing is reasonable: it is
;;; preferable to avoid immense S-expressions in code anyway.

;;; This assumes Unix-style LF line endings.

(defmacro defconst [& body] `(clojure.contrib.def/defvar ~@body))

(defconst paredit-version 21)
(defconst paredit-beta-p nil)

(defvar paredit-mode-map {}
  "Keymap for the paredit minor mode.")

(defn check-parens "TODO LAP: implement it !" [text] true)
(defn can-enable-paredit? [text] (check-parens text))

(def 
  #^{ :doc "
    The format for documenting the commands is simple, and a slight varation of
    the original paredit.el format :
    paredit-commands := [ group* ]
    group := [ group-name-str command* ]
    command := [ default-triggering-keys 
                 command-name-keyword 
                 { before-after-documentation-pair* } 
                 { before-after-non-regression-pair* }* ]
    before-after-documentation-pair := before-after-non-regression-pair
    before-after-non-regression-pair := before-text-spec after-text-spec
    before-text-spec := after-text-spec := text-spec
    text-spec := a string, with the caret position indicated by a pipe character |, 
                 and if there is a selected portion of the text, the end of the text
                 selection is marked with another pipe character |"}
  *paredit-commands*
  [
    ["Basic Insertion Commands"
	    ["("         :paredit-open-round
	                {"(a b |c d)"
	                 "(a b (|) c d)"
	                 "(foo \"bar |baz\" quux)"
	                 "(foo \"bar (|baz\" quux)"}
	                {"(a b|c d)" "(a b (|) c d)"
	                 "(|)" "((|))"
	                 "|" "(|)"
	                 "a|" "a (|)"
	                 "(a |,b)" "(a (|),b)"
	                 "(a,| b)" "(a, (|) b)"
	                 "(a,|b)" "(a, (|) b)"
	                 "(a,|)" "(a, (|))"
	                 "\\| " "\\(| "
	                 "~|" "~(|)"
	                 "~@|" "~@(|)"
	                 "\\\\| " "\\\\ (|) "}]
	    [")"         :paredit-close-round
	                {"(a |b)" "(a b)|"
	                 "(a |b) cd" "(a b)| cd"
	                 "(a |b ) cd" "(a b)| cd"
	                 "(a b |c [])" "(a b c [])|"
                   "(a b c [|] )" "(a b c [])|"
	                 "(a b |c   )" "(a b c)|"
                   "( a,  b |[a b ]   )" "( a,  b [a b ])|"
                   "( a,  b [|a b ]   )" "( a,  b [a b ])|"
                   "[ a,  b (|a b )   ]" "[ a,  b (a b)|   ]"
	                 "(a b |c ,  )" "(a b c)|"
	                 "(a b| [d e]" "(a b)| [d e]"
	                 "; Hello,| world!"  "; Hello,)| world!"
	                 "(  \"Hello,| world!\" foo )" "(  \"Hello,)| world!\" foo )"
	                 "  \"Hello,| world!" "  \"Hello,)| world!"
	                 "foo \\|" "foo \\)|"}]
	    #_["M-)"       :paredit-close-round-and-newline
	                {"(defun f (x|  ))"
	                 "(defun f (x)\n  |)"
	                 "; (Foo.|"
	                 "; (Foo.)|"}]
      ["["         :paredit-open-square
                 {"(a b |c d)"  "(a b [|] c d)"
                  "(foo \"bar |baz\" quux)" "(foo \"bar [|baz\" quux)"}
                  {"(a b|c d)" "(a b [|] c d)"
                   "(|)" "([|])"
                   "|" "[|]"
                   "a|" "a [|]"
                   "(a |,b)" "(a [|],b)"
                   "(a,| b)" "(a, [|] b)"
                   "(a,|b)" "(a, [|] b)"
                   "(a,|)" "(a, [|])"
                   "\\| " "\\[| "
                   "\\\\| " "\\\\ [|] "}]
      ["]"         :paredit-close-square
                  {"(define-key keymap [frob|  ] 'frobnicate)"
                   "(define-key keymap [frob]| 'frobnicate)"
                   "; [Bar.|" "; [Bar.]|"
                   "  \"Hello,| world!\" foo" "  \"Hello,]| world!\" foo"
                   "  \"Hello,| world!" "  \"Hello,]| world!"
                   "foo \\|" "foo \\]|"}]
      ["{"         :paredit-open-curly ; this command does not exist in paredit AFAIK
                 {"(a b |c d)"  "(a b {|} c d)"
                  "(foo \"bar |baz\" quux)" "(foo \"bar {|baz\" quux)"}
                  {"(a b|c d)" "(a b {|} c d)"
                   "(|)" "({|})"
                   "|" "{|}"
                   "a|" "a {|}"
                   "#|" "#{|}" ; specific to clojure sets
                   "(a |,b)" "(a {|},b)"
                   "(a,| b)" "(a, {|} b)"
                   "(a,|b)" "(a, {|} b)"
                   "(a,|)" "(a, {|})"
                   "\\| " "\\{| "
                   "\\\\| " "\\\\ {|} "
                   }]
      ["}"         :paredit-close-curly
                  {"{a b |c   }" "{a b c}|"
                   "; Hello,| world!"
                   "; Hello,}| world!"
                   "  \"Hello,| world!\" foo" "  \"Hello,}| world!\" foo"
                   "  \"Hello,| world!" "  \"Hello,}| world!"
                   "foo \\|" "foo \\}|"}]
      ["\""        :paredit-doublequote
                  {"(frob grovel |full lexical)"
                   "(frob grovel \"|\" full lexical)",
                   "(frob grovel \"|\" full lexical)"
                   "(frob grovel \"\"| full lexical)",
                   "(foo \"bar |baz\" quux)"
                   "(foo \"bar \\\"|baz\" quux)",
                   ";|ab" ";\"|ab",
                   "(frob grovel \"foo \\|bar\" full lexical)"
                   "(frob grovel \"foo \\\"|bar\" full lexical)",
                   "(frob grovel \"foo \\\\|bar\" full lexical)"
                   "(frob grovel \"foo \\\\\\\"|bar\" full lexical)",
                   "\"fo\\\"o\" \"b|ar\"" "\"fo\\\"o\" \"b\\\"|ar\"",
                   "\"\\\\\" \"b|ar\"" "\"\\\\\" \"b\\\"|ar\"",
                   "\"\\\\\\\"|a\"" "\"\\\\\\\"\\\"|a\""}]
    ]
    ["Deleting & Killing"
      ["Del"     :paredit-forward-delete
                {"(quu|x \"zot\")" "(quu| \"zot\")",
                 "(quux |\"zot\")" "(quux \"|zot\")",
                 "(quux \"|zot\")" "(quux \"|ot\")",
                 "(foo |(a) bar)" "(foo (|a) bar)"
                 "(foo (|a) bar)" "(foo (|) bar)" 
                 "(foo (|) bar)" "(foo | bar)"
                 "(foo [|] bar)" "(foo | bar)"
                 "(foo {|} bar)" "(foo | bar)"
                 #_"(foo #{|} bar)" #_"(foo | bar)"
                 "(foo \"|\" bar)" "(foo | bar)"
                 "(foo (a|) bar)" "(foo (a|) bar)"
                 "(foo [a|] bar)" "(foo [a|] bar)"
                 "(foo {a|} bar)" "(foo {a|} bar)"
                 #_"(foo #{a|} bar)" #_"(foo #{a|} bar)"
                 "(foo \"a|\" bar)" "(foo \"a|\" bar)"
                 "(|(foo bar))" "((|foo bar))"
                 "(|[foo bar])" "([|foo bar])"
                 "(|{foo bar})" "({|foo bar})"
                 #_"(|#{foo bar})" #_"(#{|foo bar})"
                 }]
      ["BackDel" :paredit-backward-delete
                {"(\"zot\" q|uux)" "(\"zot\" |uux)",
                "(\"zot\"| quux)" "(\"zot|\" quux)",
                 "(\"zot|\" quux)" "(\"zo|\" quux)",
                 "(foo (|) bar)" "(foo | bar)",
                 "(foo bar)|" "(foo bar|)",
                 "(foo bar|)" "(foo ba|)"
                 }]
      #_["C-k"     :paredit-kill
                {"(foo bar)|     ; Useless comment!"
                 "(foo bar)|",
                "(|foo bar)     ; Useful comment!"
                 "(|)     ; Useful comment!",
                "|(foo bar)     ; Useless line!"
                 "|",
                "(foo \"|bar baz\"\n     quux)"
                 "(foo \"|\"\n     quux)"}]]
    ["Miscellaneous"             
      ["Tab"     :paredit-indent-line
                {"foo (let [n (frobbotz)] \n|(display (+ n 1)\nport))\n        bar"
                 (str "foo (let [n (frobbotz)]"
                    "\n      |(display (+ n 1)"
                    "\n        port))\n        bar")}]
      ["C-j"     :paredit-newline
                {"foo (let [n (frobbotz)] |(display (+ n 1)\nport))\n        bar"
                 (str "foo (let [n (frobbotz)]"
                    "\n      |(display (+ n 1)"
                    "\n        port))\n        bar")}]
    ]
  ])

(def *real-spaces* #{(str \newline) (str \tab) (str \space)})
(def *extended-spaces* (conj *real-spaces* (str \,)))
(def *open-brackets* (conj #{"(" "[" "{"} nil)) ; we add nil to the list to also match beginning of text 
(def *close-brackets* (conj #{")" "]" "}"} nil)) ; we add nil to the list to also match end of text

(def *form-macro-chars* #{(str \#) (str \~) (str "~@") (str \') (str \`) (str \@) "#^" "#'" "#_"})

(defn text-spec-to-text 
  "Converts a text spec to text map" 
  [#^String text-spec]
  (let [offset (.indexOf text-spec "|")
        second-pipe (dec (.indexOf text-spec "|" (inc offset)))]  
  {:text (str2/replace text-spec "|" "")
   :offset offset
   :length (if (> second-pipe 0) (- second-pipe offset) 0)}))

(defn text-to-text-spec
  "Converts a text map to text spec"
  [text]
  (let [insert (fn [#^String s i c] (str (.substring s 0 i) c (.substring s i)))
        spec (insert (:text text) (:offset text) "|")
        spec (if (zero? (:length text)) spec (insert spec (+ 1 (:offset text) (:length text)) "|"))]
    spec))

(def *not-in-code* #{"\"" "\"\\" ";" "\\"})

(defn parsed-in-tags?
  [parsed tags-set]
  (tags-set (-> parsed :parents peek :tag)))

(defn in-code?
  ; TODO the current function is not general enough, it just works for the offset
  ; the parse stopped at  
  "true if character at offset offset is in a code
   position, e.g. not in a string, regexp, literal char or comment"
  [parsed]
  (not (parsed-in-tags? parsed *not-in-code*)))

(defn insert
  "insert what at offset. offset shifted by what's length, selection length unchanged"
  [{:keys [#^String text offset length modifs] :as where :or {:modifs []}} #^String what]
  (let [new-offset (+ offset (.length what))]
    (assoc where 
      :text (str (.substring text 0 offset) what (.substring text offset))
      :offset new-offset
      :modifs (conj modifs {:text what, :offset offset, :length 0})))) 

(defn delete
  "removes n chars at offset off. offset not shifted, selection length unchanged"
  ; TODO FIXME : decrease length if now that things are removed, length would make the selection overflow the text
  [{:keys [#^String text offset length modifs] :as where :or {:modifs []}} off n]
  (assoc where 
    :text (str (.substring text 0 off) (.substring text (+ off n)))
    :offset offset
    :modifs (conj modifs {:text "", :offset off, :length n}))) 

(defn shift-offset     
  "shift offset, the selection is also shifted"
  [{:keys [text offset length] :as where} shift]
  (assoc where :offset (+ offset shift))) 

(defn set-offset     
  "sets offset, the selection is also shifted"
  [{:keys [text offset length] :as where} new-offset]
  (assoc where :offset new-offset)) 

; TODO faire comme next-char sur l'utilisation de length
; !! attention pas de gestion de length negative
(defn previous-char-str 
  ([{:keys [#^String text offset length] :as t}] (previous-char-str t 1))
  ([{:keys [#^String text offset length] :as t} n]
    (assert (>= length 0))
    (when (< -1 (- offset n) (.length text))
      (str (.charAt text (- offset n))))))
  
(defn next-char-str [{:keys [#^String text offset length] :as t}]
  (assert (>= length 0))
  (when (< -1 (+ offset length) (.length text))
    (str (.charAt text (+ offset length)))))
  
(defmulti paredit (fn [k & args] k))

(defn insert-balanced
  [[o c] t chars-with-no-space-before chars-with-no-space-after]
  (let [add-pre-space? (not (contains? chars-with-no-space-before 
                                       (previous-char-str t)))
        add-post-space? (not (contains? chars-with-no-space-after 
                                        (next-char-str t)))
        ins-str (str (if add-pre-space? " " "")
                     (str o c)
                     (if add-post-space? " " ""))
        offset-shift (if add-post-space? -2 -1)]
    ;(println (type (previous-char-str t)))
    ;(println (str (apply str \" chars-with-no-space-before) \"))
    ;(println add-pre-space? (str \" (previous-char-str t) \") add-post-space? (str \" ins-str \"))
    (-> t (insert ins-str) (shift-offset offset-shift))))

(defn open-balanced
  [[o c] {:keys [#^String text offset length] :as t} 
   chars-with-no-space-before chars-with-no-space-after]
  (let [parsed (parse text offset)]
    (if (in-code? parsed)
      (do
        ;(println [o c] t chars-with-no-space-before chars-with-no-space-after)
        (insert-balanced [o c] t chars-with-no-space-before chars-with-no-space-after))
      (-> t (insert (str o))))))
  
(defn close-balanced
  [[o c] {:keys [#^String text offset length] :as t} 
   chars-with-no-space-before chars-with-no-space-after]
    (let [parsed (parse text (.length text))
          offset-loc (-> parsed parsed-root-loc (loc-for-offset offset))]       
      (if (and offset-loc (not (*not-in-code* (-> offset-loc zip/node :tag))))
        (let [up-locs (take-while identity (iterate zip/up offset-loc))
              match (some #(when (= o (-> % zip/node :tag)) %) up-locs)]
          (if match
            (let [last-node (-> match zip/down zip/rightmost zip/left zip/node)
                  nb-delete (if (= (str \space) (:tag last-node)) 
                              (- (:end-offset last-node) (:offset last-node))
                              0)
                  t (if (> nb-delete 0) 
                      (delete t (:offset last-node) nb-delete)
                      t)] ; zip/left because there is the closing node
              (-> t (set-offset (- (-> match zip/node (:end-offset)) nb-delete))))
            (-> t (insert (str c)))))
        (-> t (insert (str c))))))

(defmethod paredit 
  :paredit-open-round
  [cmd {:keys [text offset length] :as t}]
  (open-balanced ["(" ")"] t 
    (union (conj (into *real-spaces* *open-brackets*) "#") *form-macro-chars*)
    (into *extended-spaces* *close-brackets*)))
    
(defmethod paredit 
  :paredit-open-square
  [cmd {:keys [text offset length] :as t}]
  (open-balanced ["[" "]"] t
    (union (into *real-spaces* *open-brackets*) *form-macro-chars*)
    (into *extended-spaces* *close-brackets*)))
    
(defmethod paredit 
  :paredit-open-curly
  [cmd {:keys [text offset length] :as t}]
  (open-balanced ["{" "}"] t
    (union (conj (into *real-spaces* *open-brackets*) "#") *form-macro-chars*)
    (into *extended-spaces* *close-brackets*)))
    
(defmethod paredit 
  :paredit-close-round
  [cmd {:keys [text offset length] :as t}]
  (close-balanced ["(" ")"] t
    nil nil))

(defmethod paredit 
  :paredit-close-square
  [cmd {:keys [text offset length] :as t}]
  (close-balanced ["[" "]"] t
    nil nil))

(defmethod paredit 
  :paredit-close-curly
  [cmd {:keys [text offset length] :as t}]
  (close-balanced ["{" "}"] t
    nil nil))

(defmethod paredit
  :paredit-doublequote
  [cmd {:keys [text offset length] :as t}]
  (let [parsed (parse text offset)]
    (cond
      (in-code? parsed)
        (insert-balanced [\" \"] t
          (conj (into *real-spaces* *open-brackets*) \#)
          (into *extended-spaces* *close-brackets*))
      (not (parsed-in-tags? parsed #{"\""}))
        (-> t (insert (str \")))
      (and (= "\\" (previous-char-str t)) (not= "\\" (previous-char-str t 2)))
        (-> t (insert (str \")))
      (= "\"" (next-char-str t))
        (shift-offset t 1)
        #_(close-balanced ["\"" "\""] t nil nil)
      :else
        (-> t (insert (str \\ \"))))))

(defmethod paredit 
  :paredit-forward-delete
  [cmd {:keys [text offset length] :as t}]
  (let [parsed (parse text (.length text))
        parse-ok (not= :ko (:parser-state parsed))]
    (if parse-ok
      (let [offset-loc (-> parsed parsed-root-loc (loc-for-offset offset))
            offset-node (-> offset-loc zip/node)
            handled-forms (conj *open-brackets* "\"")
            in-handled-form (handled-forms (:tag offset-node))]
        (cond 
          (and in-handled-form (= offset (:offset offset-node)))
            (shift-offset t 1)
          (and in-handled-form (= offset (dec (:end-offset offset-node))))
            (if (> (-> offset-node :content count) 2)
              t     ; don't move
              (-> t ; delete the form 
                (delete (:offset offset-node) (- (:end-offset offset-node) (:offset offset-node)))
                (shift-offset -1)))
          :else
            (delete t offset 1)))
      (delete t offset 1))))

(defmethod paredit 
  :paredit-backward-delete
  [cmd {:keys [text offset length] :as t}]
  (let [offset (dec offset)
        parsed (parse text (.length text))
        parse-ok (not= :ko (:parser-state parsed))]
    (if parse-ok
      (let [offset-loc (-> parsed parsed-root-loc (loc-for-offset offset))
            offset-node (-> offset-loc zip/node)
            handled-forms (conj *open-brackets* "\"")
            in-handled-form (handled-forms (:tag offset-node))]
        (cond 
          (and in-handled-form (= offset (:offset offset-node)))
            (if (> (-> offset-node :content count) 2)
              t     ; don't move
              (-> t ; delete the form 
                (delete (:offset offset-node) (- (:end-offset offset-node) (:offset offset-node)))
                (shift-offset -1)))
          (and in-handled-form (= offset (dec (:end-offset offset-node))))
            (shift-offset t -1)
          :else
            (-> t (delete offset 1) (shift-offset -1))))
      (-> t (delete offset 1) (shift-offset -1)))))
      
(defmethod paredit
 :paredit-indent-line
 [cmd {:keys [text offset length] :as t}] t
; (let [current line, enclosing node, parent bracket node]
;   for the current line and all following lines while we're still in the same parent bracket node:
;     (cond
;       (or
;           (start of line in string)
;           (start of line starts with comment in first col)) ; special case because line probably commented out temporarily
;         (do not indent line)
;       :else
;         (do
;            (remove the trailing spaces in the previous line)
;            (compute the number of indentation spaces and correct the current line beginning))) ; (only spaces, no tabs)
;   endfor)
)

(defn test-command [title-prefix command]
  (testing (str title-prefix " " (second command) " (\"" (first command) "\")")
    (doseq [[input expected] (get command 2)]
      (is (= expected (text-to-text-spec (paredit (second command) (text-spec-to-text input))))))))

(deftest paredit-tests
  (doseq [group *paredit-commands*]
		(testing (str (first group) ":")
		  (doseq [command (rest group)]
		    (test-command "public documentation of paredit command" command)
		    (test-command "additional non regression tests of paredit command " (assoc command 2 (get command 3)))))))

(def pts paredit-tests)

(defvar *text* (atom {:text "" :offset 0 :length 0})
  "defines a text, with :offset being the cursor position,
   and :length being a possible selection (may be negative)")
