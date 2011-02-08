(ns paredit.core-commands
  (:use clojure.contrib.def))

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

(defvar  
  paredit-mode-map {} 
  "Keymap for the paredit minor mode.")

(defn check-parens "TODO LAP: implement it !" [text] true)
(defn can-enable-paredit? [text] (check-parens text))

(def 
  ^{ :doc "
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
	                 "(foo \"bar |baz\" quux)" "(foo \"bar (|baz\" quux)"
                  }
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
	                 "\\\\| " "\\\\ (|) "
                  }]
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
	                 "foo \\|" "foo \\)|"
                  ; tests with the new :chimera
                   "({foo |bar])" "({foo bar])|"
                   "({[foo |bar)})" "({[foo bar)|})"
                  }]
	    #_["M-)"       :paredit-close-round-and-newline
	                {"(defun f (x|  ))"
	                 "(defun f (x)\n  |)"
	                 "; (Foo.|"
	                 "; (Foo.)|"}]
      ["["         :paredit-open-square
                 {"(a b |c d)"  "(a b [|] c d)"
                  "(foo \"bar |baz\" quux)" "(foo \"bar [|baz\" quux)" 
                  }
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
                   "foo \\|" "foo \\]|"
                   ; tests with the new :chimera
                   "({foo |bar])" "({foo bar]|)"
                   "({(foo |bar]))" "({(foo bar]|))"
                   "({[foo |bar)})" "({[foo ]|bar)})"
                   "[foo (bar [baz {bleh |blah}))]" "[foo (bar [baz {bleh blah}))]|"
                   }]
      ["{"         :paredit-open-curly
                 {"(a b |c d)"  "(a b {|} c d)"
                  "(foo \"bar |baz\" quux)" "(foo \"bar {|baz\" quux)" 
                  }
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
                   "foo \\|" "foo \\}|"
                   "({(foo |bar}))" "({(foo bar}|))"
                   }]
      ["\""        :paredit-doublequote
                  { "(frob grovel |full lexical)" "(frob grovel \"|\" full lexical)",
                   "(frob grovel \"|\" full lexical)" "(frob grovel \"\"| full lexical)",
                   "(foo \"bar |baz\" quux)" "(foo \"bar \\\"|baz\" quux)",
                   ";|ab" ";\"|ab", 
                   "(frob grovel \"foo \\|bar\" full lexical)"
                     "(frob grovel \"foo \\\"|bar\" full lexical)",
                   "(frob grovel \"foo \\\\|bar\" full lexical)"
                     "(frob grovel \"foo \\\\\\\"|bar\" full lexical)",
                   "\"fo\\\"o\" \"b|ar\"" "\"fo\\\"o\" \"b\\\"|ar\"",
                     "\"\\\\\" \"b|ar\"" "\"\\\\\" \"b\\\"|ar\"",
                   "\"\\\\\\\"|a\"" "\"\\\\\\\"\\\"|a\"",
                   "\"fo|o\"" "\"fo\\\"|o\"",
                   ;"#\"fo|o\"" "#\"fo\\\"|o\"",
                   ;;;"#\"foo\"" "#\"foo\""

                   "#|" "#\"|\"" ; specific to clojure regexs

                   }]
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
                 "(foo \"|\" bar)" "(foo | bar)"
                 "(foo (a|) bar)" "(foo (a|) bar)"
                 "(foo [a|] bar)" "(foo [a|] bar)"
                 "(foo {a|} bar)" "(foo {a|} bar)"

                 "(foo #{|} bar)" "(foo | bar)"
                 "(foo #{a|} bar)" "(foo #{a|} bar)"
                 "(foo #{a |d} bar)" "(foo #{a |} bar)"
                 "(|#{foo bar})" "(#{|foo bar})"

                 "(foo #(|) bar)" "(foo | bar)"
                 "(foo #(a|) bar)" "(foo #(a|) bar)"
                 "(foo #(a |d) bar)" "(foo #(a |) bar)"
                 "(|#(foo bar))" "(#(|foo bar))"

                 "(foo #\"|\" bar)" "(foo | bar)"
                 "(foo #\"a|\" bar)" "(foo #\"a|\" bar)"
                 "(foo #\"a |d\" bar)" "(foo #\"a |\" bar)"
                 "(|#\"foo bar\")" "(#\"|foo bar\")"

                 "(foo \"a|\" bar)" "(foo \"a|\" bar)" 
                 "(|(foo bar))" "((|foo bar))"
                 "(|[foo bar])" "([|foo bar])"
                 "(|{foo bar})" "({|foo bar})"
                 
                 "|" "|"
                 }]
      ["BackDel" :paredit-backward-delete
                {
                 "(\"zot\" q|uux)" "(\"zot\" |uux)",
                 "(\"zot\"| quux)" "(\"zot|\" quux)",
                 "(\"zot|\" quux)" "(\"zo|\" quux)",

                 "(#\"zot\"| quux)" "(#\"zot|\" quux)",
                 "(#\"zot|\" quux)" "(#\"zo|\" quux)",
                 
                 "(foo (|) bar)" "(foo | bar)",
                 "(foo #(|) bar)" "(foo | bar)",
                 "(foo #{|} bar)" "(foo | bar)",
                 
                 "(foo bar)|" "(foo bar|)",
                 "(foo bar|)" "(foo ba|)",
                 
                 "|" "|"
                 
                 "\"\"|" "\"|\""
                 "\"|\"" "|"
                 "#\"\"|" "#\"|\""
                 "#\"|\"" "|"
                 
                 "#(foo bar)|" "#(foo bar|)",
                 "#(foo bar|)" "#(foo ba|)",
                 "#{foo bar}|" "#{foo bar|}",
                 "#{foo bar|}" "#{foo ba|}",
                 "#(|)" "|",
                 "#{|}" "|"
                 }]
      ;#_["C-k"     :paredit-kill
      ;          {"(foo bar)|     ; Useless comment!"
      ;           "(foo bar)|",
      ;          "(|foo bar)     ; Useful comment!"
      ;           "(|)     ; Useful comment!",
      ;          "|(foo bar)     ; Useless line!"
      ;           "|",
      ;          "(foo \"|bar baz\"\n     quux)"
      ;           "(foo \"|\"\n     quux)"}]
      ]
    
    ["Depth-Changing Commands"
     ["M-("       :paredit-wrap-round
      {"(foo |bar baz)" "(foo (|bar) baz)",
       ";hel|lo" ";hel(|lo",
       "a |\"hi\"" "a (|\"hi\")",
       "a |\"hi\"|" "a (|\"hi\"|)",
       "foo |bar| foo" "foo (|bar|) foo",
       "foo |bar baz| foo" "foo (|bar baz|) foo",
       "foo (|bar| baz) foo" "foo ((|bar|) baz) foo"
       "foo (|bar baz|) foo" "foo ((|bar baz|)) foo"
       ;; not-yet "foo |(bar| baz) foo" "foo |(bar| baz) foo"
       ;; not-yet "foo (bar| baz)| foo" "foo (bar| baz)| foo"
       "foo |(bar baz)| foo" "foo (|(bar baz)|) foo"
       "foo |(bar\n;comment\n baz)| foo" "foo (|(bar\n;comment\n baz)|) foo"
       ;; not-yet "foo |bar ba|z foo" "foo |bar ba|z foo",
       "foo \"|bar ba|z\" foo" "foo \"(|z\" foo",
       ;; not-yet "foo |\"bar ba|z\" foo" "foo |\"bar ba|z\" foo",
       "foo |bar|" "foo (|bar|)"
       "foo |(bar)|" "foo (|(bar)|)"
       "bar |`foo| baz" "bar (|`foo|) baz"
       }]
     ;["M-s"       :paredit-splice-sexp
     ;           {"(foo (bar| baz) quux)"
     ;            "(foo bar| baz quux)"}]
     ;[("M-<up>" "ESC <up>")
     ;           paredit-splice-sexp-killing-backward
     ;           ("(foo (let ((x 5)) |(sqrt n)) bar)"
     ;            "(foo (sqrt n) bar)")]
     ;(("M-<down>" "ESC <down>")
     ;           paredit-splice-sexp-killing-forward
     ;           ("(a (b c| d e) f)"
     ;            "(a b c f)"))
     ["M-r"       :paredit-raise-sexp
                {"(dynamic-wind in (lambda () |body|) out)" "(dynamic-wind in |body out)"
                 "(dynamic-wind in |body| out)" "|body" 
                 "(foo bar|)" "(foo bar|)"
                 "(foo |bar)" "|bar"
                 "(foo |(bar))" "|(bar)"
                 "(foo |(bar]|)" "|(bar]"
                 }]
     ]
    
    ["Selection"
     ["Shift+Alt+Left" :paredit-expand-left
                {
                 "foo bar| baz" "foo |bar| baz"
                 "foo bar |baz" "foo bar| |baz"
                 "foo ba|r baz" "foo |bar| baz"
                 "foo1 bar b|a|z" "foo1 bar |baz|"
                 "foo2 bar ba|z|" "foo2 bar |baz|"
                 "foo3 bar |baz|" "foo3 bar| baz|"
                 "foo bar| baz|" "foo |bar baz|"
                 "foo |bar baz|" "foo| bar baz|"
                 "|(foo bar baz)|" "|(foo bar baz)|"
                 ;;not-yet "|fo|o bar baz" "|foo bar baz|" 
                 ;;not-yet "|foo| bar baz" "|foo bar baz|" 
                 ;;not-yet "|foo |bar baz" "|foo bar baz|" 
                 ;;not-yet "|foo b|ar baz" "|foo bar baz|" 
                 "foo (bar| baz)" "foo (|bar| baz)"
                 "foo b|ar| baz" "foo |bar| baz"
                 "foo1 (|bar| baz)" "foo1 |(bar baz)|"
                 "foo \"bar |baz\"" "foo |\"bar baz\"|"
                 "foo;ba|r\nbaz" "foo|;bar|\nbaz"
                 "foo (bar [ba|z] |foo)" "foo (bar |[baz] |foo)"
                 "foo (bar [ba|z]) (foo [bar (b|az)])" "foo |(bar [baz]) (foo [bar (baz)])|"
                 "foo |(bar [baz (b|am)])" "foo |(bar [baz (bam)])|"
                 "(foo bar|)" "(foo |bar|)"
                 "fooz foo |(bar)| baz" "fooz foo| (bar)| baz"
                 "fooz foo| (bar)| baz" "fooz |foo (bar)| baz"
                 ;with :chimera
                 "(foo bar|]" "(foo |bar|]"
                 "(foo {bar)|]" "(foo |{bar)|]"
                 }]
     ["Shift+Alt+Right" :paredit-expand-right
                {
                 "foo bar| baz" "foo bar| |baz"
                 "foo4 bar |baz" "foo4 bar |baz|"
                 "foo ba|r baz" "foo |bar| baz"
                 "foo5 bar b|a|z" "foo5 bar |baz|"
                 "foo6 bar ba|z|" "foo6 bar |baz|"
                 ;;not-yet "foo bar |baz|" "|foo bar baz|"
                 ;;not-yet "foo bar| baz|" "|foo bar baz|"
                 ;;not-yet "foo |bar baz|" "|foo bar baz|"
                 "|foo bar baz" "|foo| bar baz";;
                 "|f|oo bar baz" "|foo| bar baz"
                 "|foo| bar baz" "|foo |bar baz"
                 "|foo |bar baz" "|foo bar| baz"
                 "|foo b|ar baz" "|foo bar| baz"
                 "foo (bar| baz)" "foo (bar| |baz)"
                 "foo (bar |baz)" "foo (bar |baz|)"
                 "foo b|ar| baz" "foo |bar| baz"
                 "foo2 (bar baz|)" "foo2 |(bar baz)|"
                 "foo3 (bar |baz|)" "foo3 |(bar baz)|"
                 "foo \"bar |baz\"" "foo |\"bar baz\"|"
                 "foo;ba|r\nbaz" "foo|;bar|\nbaz"
                 "foo (bar [ba|z] |foo)" "foo (bar |[baz] |foo)"
                 "foo (bar [ba|z]) (foo [bar (b|az)])" "foo |(bar [baz]) (foo [bar (baz)])|"
                 "foo |(bar [baz (b|am)])" "foo |(bar [baz (bam)])|"
                 ;with :chimera
                 "(foo |bar]" "(foo |bar|]"
                 "(foo |{bar)]" "(foo |{bar)|]"
                 }]
     ["Shift+Alt+Up" :paredit-expand-up
                {
                 "abc defgh|i " "abc |defghi| "
                 "|abc| defghi " "|abc defghi |"
                 "foo bar| baz" "|foo bar baz|"
                 "foo bar |baz" "|foo bar baz|"
                 "foo ba|r baz" "foo |bar| baz"
                 "foo7 bar b|a|z" "foo7 bar |baz|"
                 "foo8 bar ba|z|" "foo8 bar |baz|"
                 "foo9 bar |baz|" "|foo9 bar baz|"
                 "foo bar| baz|" "|foo bar baz|"
                 "foo |bar baz|" "|foo bar baz|"
                 "|foo bar baz" "|foo bar baz|"
                 "|f|oo bar baz" "|foo| bar baz"
                 "|foo| bar baz" "|foo bar baz|"
                 "|foo |bar baz" "|foo bar baz|" 
                 "|foo b|ar baz" "|foo bar| baz"
                 "foo4 (bar| baz)" "foo4 |(bar baz)|"
                 "foo5 (bar |baz)" "foo5 |(bar baz)|"
                 "foo b|ar| baz" "foo |bar| baz"
                 "foo6 (bar baz|)" "foo6 |(bar baz)|"
                 "foo7 (bar |baz|)" "foo7 |(bar baz)|"
                 "foo \"bar |baz\"" "foo |\"bar baz\"|"
                 "foo;ba|r\nbaz" "foo|;bar|\nbaz"
                 "foo (bar [ba|z] |foo)" "foo (bar |[baz] |foo)"
                 "foo (bar [ba|z]) (foo [bar (b|az)])" "foo |(bar [baz]) (foo [bar (baz)])|"
                 "foo |(bar [baz (b|am)])" "foo |(bar [baz (bam)])|"
                 "foo ([|bar])" "foo (|[bar]|)"
                 "foo ([b|ar])" "foo ([|bar|])"
                 "foo ([b|a|r])" "foo ([|bar|])"
                 "foo ([|bar|])" "foo (|[bar]|)"
                 "foo (|[bar]|)" "foo |([bar])|"
                 ;with :chimera
                 "(foo |bar]" "|(foo bar]|"
                 "(foo |{bar)]" "|(foo {bar)]|"
                 }]
     ]
    ["Miscellaneous"             
      ["Tab"     :paredit-indent-line
                {"[a\n|b]"  "[a\n |b]"
                 "([a1\n|b])"  "([a1\n  |b])"
                 "([a1b\n  |b])" "([a1b\n  |b])"
                 "(a\n |)" "(a\n  |)"
                 "(a b c\nd| e)" "(a b c\n  d| e)"
                 "|(toto)" "|(toto)"
                 "(a\n  ;sdfdf\n  |b)" "(a\n  ;sdfdf\n  |b)"
                 "[a\n \"b\n |\"]" "[a\n \"b\n |\"]"
                 "[a\n|\"a\"]" "[a\n |\"a\"]"
                 "(a\n\t|b)" "(a\n  |b)"
                 "(\n|\n)" "(\n  |\n)"
                 "(\n |\n)" "(\n  |\n)"
                 "(\n  |\n)" "(\n  |\n)"
                 "(\n   |\n)" "(\n  |\n)"
                 "(\n , |\n)" "(\n  |\n)"
                 "  {\n|a}" "  {\n   |a}"
                 " (\n|    ab c)" " (\n|   ab c)"
                 " (\n |   ab c)" " (\n |  ab c)"
                 " (\n  |  ab c)" " (\n  | ab c)"
                 " (\n   | ab c)" " (\n   |ab c)"
                 " (\n    |ab c)" " (\n   |ab c)" 
                 " (\n    a|b c)" " (\n   a|b c)"  
                 " (\n    |    ab c)" " (\n|   ab c)" 
                 " (\n      |  ab c)" " (\n |  ab c)"
                 " (\n  |ab c)" " (\n   |ab c)"
                 " (\n| ab c)" " (\n|   ab c)"  
                 " (\n  | ab c)" " (\n  | ab c)"
                 "(a\n |b" "(a\n  |b" 
                 ;;;"foo (let [n (frobbotz)] \n|(display (+ n 1)\nport))\n        bar"
                 ;;;(str "foo (let [n (frobbotz)]"
                 ;;;   "\n      |(display (+ n 1)"
                 ;;;   "\n        port))\n        bar"
                 ;;   )
                 "   a\n       |" "   a\n   |"
                 ")|s" ")|s"
                 ")\n|s" ")\n|s"
                 "#(a\n|)" "#(a\n   |)"
                 ; with chimera
                 "(a\n|(])" "(a\n  |(])"
                 "(a\n|" "(a\n  |"
                 "(a\n|]" "(a\n  |]"
                 " #(a\n|]" " #(a\n    |]"
                 "(ab\n|cd|def)" "(ab\n  |cd|def)"
                 }]
      [#"C-j"     :paredit-newline
                {"(ab|cd)" "(ab\n  |cd)"
                 ;"(ab|ce)\r\n" "(ab\r\n  |ce)\r\n"
                 "(ab|     cd)" "(ab\n  |cd)"
                 "   a|" "   a\n   |"
                 "(ab|cd|ef)" "(ab\n  |ef)"
                 ;"foo (let [n (frobbotz)] |(display (+ n 1)\nport))\n        bar"
                 ;(str "foo (let [n (frobbotz)]"
                 ;   "\n      |(display (+ n 1)"
                 ;   "\n        port))\n        bar")
                 }]
      ["M-S"    :paredit-split-sexp
                {"(hello  |  world)" "(hello)| (world)",
                 "\"Hello, |world!\"" "\"Hello, \"| \"world!\"",
                 "(hel|lo)" "(hel)| (lo)",
                 "[hello |world]" "[hello]| [world]",
                 "{hello brave |new world}" "{hello brave}| {new world}",
                 "{|}" "{}| {}"
                 "(foo|)" "(foo)| ()"
                 "({|})" "({}| {})"
                 "(defn hello |[world])" "(defn hello)| ([world])"
                 }]
      ["M-J"    :paredit-join-sexps
                {"(hello)| (world)" "(hello| world)",
                 "\"Hello, \"| \"world!\"" "\"Hello, |world!\"",
                 "hello-\n|  world" "hello-|world"
                 "({:foo :bar}| {:baz :fooz})" "({:foo :bar| :baz :fooz})"
                 "({:foo :bar} |{:baz :fooz})" "({:foo :bar |:baz :fooz})"
                 "({:foo :bar} {|:baz :fooz})" "({:foo :bar} {|:baz :fooz})"
                 "({:baz :fooz|} {:foo :bar})" "({:baz :fooz|} {:foo :bar})"
                 }]
    ]
  ])
