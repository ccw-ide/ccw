;*******************************************************************************
;* Copyright (c) 2010 Tuomas KARKKAINEN and John HARROP.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Tuomas KARKKAINEN - initial API and implementation
;*    John HARROP - original code for Clojure formatting
;*******************************************************************************/

(ns ccw.editors.clojure.ClojureFormat
  (:use [clojure.test])
  (:gen-class
    :init init
    :state state
    :methods [[formatCode [String] String]]))

(defn -init [])

(declare format-code)
(defn -formatCode [this string]
  (format-code string))

; from http://groups.google.com/group/clojure/browse_thread/thread/6a16bb89340f46d8/fb03dffa410e919a

(defn format-code [string]
  (loop [s string col 0 dstack [] out [] space nil incl false
         insl false incm false lwcr false sups true cmindent nil]
    (if-let [c (first s)] ; test comment 1 { ; )
      (let [r (rest s)    ; test comment 2 ( [
            begins "([{"
            ends ")]}" ; test comment 3 " ;
            delim-indents (zipmap begins [1 0 0]) ; test comment 4
            delim-ends (zipmap ends begins)
            sups-char? #{\' \` \~ \@ \#} ; characters to suppress
            indent (fn []                ; spaces after
                     (if (or (empty? r) (empty? dstack))
                       [\newline]
                       (into [\newline]
                         (repeat
                           (let [[delim pos] (peek dstack)]
                             (+ pos (delim-indents delim)))
                           \space))))
            conc (fn [c]
                   (if sups [c] (conj space c)))
            pop-d (fn [stack c]
                    (if (empty? stack)
                      []
                      (let [ps (pop stack)]
                        (if (empty? ps)
                          []
                          (if (= (first (peek stack)) (delim-ends c))
                            ps
                            (recur ps c))))))]
        (cond
          incm (let [nl (or (= c \newline) (= c \return))
                     spc (Character/isWhitespace c)
                     cc (if nl
                          (indent)
                          (if spc
                            (if-not sups [c])
                            [c]))]
                 (recur
                   r (if nl (dec (count cc)) (+ col (count cc)))
                   dstack (into out cc) nil false false
                   (if-not nl incm) (= c \return) nl cmindent))
          (= c \") (let [cc (if (or incl insl sups) [c] [\space c])]
                     (recur
                       r (+ col (count cc)) dstack (into out cc)
                       (if (and insl (not incl)) [\space]) false
                       (if incl insl (not insl)) false false false
                       cmindent))
          (or incl insl) (recur
                           r (if (or (= c \newline) (= c \return))
                               0
                               (inc col))
                           dstack (conj out c) nil
                           (and insl (not incl) (= c \\)) insl false
                           false false cmindent)
          (= c \;) (let [padding (if sups 0 1)
                         padding (if cmindent
                                   (max padding (- cmindent col))
                                   padding)
                         padding (repeat padding \space)
                         padding (concat padding [\; \space])]
                     (recur
                       r (+ col (count padding)) dstack
                       (into out padding) nil false false true false
                       true (+ col (count padding) -2)))
          (= c \\) (let [cc (if sups [c] [\space c])]
                     (recur
                       r (+ col (count cc)) dstack (into out cc) nil
                       true false false false false cmindent))
          (or
            (and (= c \newline) (not lwcr))
            (= c \return)) (let [i (indent)]
                             (recur
                               r (dec (count i)) dstack (into out i)
                               nil false false false (= c \return)
                               true nil))
          (= c \newline) (recur
                           r col dstack out nil false false false
                           false true cmindent)
          (Character/isWhitespace c) (recur
                                       r col dstack out [\space]
                                       false false false false sups
                                       cmindent)
          (delim-indents c) (let [cc (if sups [c] [\space c])
                                  cn (count cc)]
                              (recur
                                r (+ col cn)
                                (conj dstack [c (+ col cn)])
                                (into out cc) nil false false false
                                false true cmindent))
          (delim-ends c) (recur
                           r (inc col) (pop-d dstack c) (conj out c)
                           [\space] false false false false false
                           cmindent)
          :else (let [cc (conc c)]
                  (recur
                    r (+ col (count cc)) dstack (into out cc) nil
                    false false false false (sups-char? c)
                    cmindent))))
      (apply str out))))

(deftest code-formatting
  (is (= "#\"something\"" (format-code "#\"something\"")))
  (is (= "(resourcefully/put url {\"Content-Type\" \"multipart/form-data\" \"Encoding\" \"UTF-8\"} request)))" (format-code "(resourcefully/put url {\"Content-Type\" \"multipart/form-data\" \"Encoding\" \"UTF-8\"} request)))")))
  (is (= "#_(unread)" (format-code "#_(unread)"))))
