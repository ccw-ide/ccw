; note : hiredman's reader http://github.com/hiredman/clojure/blob/readerII/src/clj/clojure/reader.clj#L516
; still TODO :
; 1. make parser and function behaviour similar for terminals atom and spaces (and move the special handling of zipping terminals up on :eof from default-handler to default-maker ?)
; 2. correctly handle clojure specificities : #{} #^ #"" ' ` @ #^ ^ #' #_ #() ~ ~@ foo#
; 3. correctly handle the premature :eof on non closed structures (a cause of error)
; 4. correctly handle parsetree errors (wrong closing of bracket, ... TODO finish the exhaustive list)
; 5. make the parser restartable
; 6. make the parser incremental 
; 7. refactor the code so that the handling of advancing offset, line, column ... is mutualized (be aware of not introducing regressions in the handling of atoms and spaces terminals)
; point 6. is optional :-)
; point 3. may be viewed as a special case of point 4 ?

(ns paredit.parser
	(:use clojure.test))

(set! *warn-on-reflection* true)

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
  [#^String s1 #^String s2]
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

(def *brackets* {\( \) \{ \} \[ \]})
(def *opening-brackets* (set (keys *brackets*)))
(def *closing-brackets* (set (vals *brackets*)))
(def *spaces* #{\space \tab \newline \return})

      
(defn zip-one [#^String text offset line col parents parser-state accumulated-state]
  (let [level (-> accumulated-state peek (assoc :end-offset offset))
        parent-level (-> accumulated-state pop peek)
        brothers (get parent-level :content [])
        parent-level (assoc parent-level :content (conj brothers level))]
    (-> accumulated-state pop pop (conj parent-level))))

; todo : later on, move offset+line+col+parents inside a proper deftyped type
(defn default-accumulator
  [#^String text offset line col parents parser-state accumulated-state]
    [#^String text offset line col parents parser-state accumulated-state]
    (cond
      (> (count parents) (count accumulated-state))
        ; enter a sublevel
        (if (and (= :eof parser-state) (= \a (-> parents peek :tag))) ; todo move in default-make-result ?
          (zip-one 
            text offset line col parents parser-state 
            (-> accumulated-state (conj (peek parents))))
          (-> accumulated-state (conj (peek parents)))) 
      (or
        (< (count parents) (count accumulated-state))
        (and (= :eof parser-state) (= \a (-> accumulated-state peek :tag))))
        ; exit a sublevel
          ; on stocke l'offset de fin du niveau, on l'enregistre en tant
          ; que fils du parent, on depope le niveau
        (zip-one text offset line col parents parser-state accumulated-state)
      :else
        accumulated-state))
        
(defn make-default-continue?-fn
  [stop-offset]
  (fn default-continue?-fn
    [#^String text offset line col parents parser-state accumulated-state]
    (< offset stop-offset)))

(defn default-make-result
  [#^String text offset line col parents parser-state accumulated-state]
  { :parents parents :offset offset :line line :col col :accumulated-state accumulated-state})
   
(defn parse 
	"TODO: currently the parser assumes a well formed document ... Define a policy if the parser encounters and invalid text
	 TODO: make the parser restartable at a given offset given a state ...
	 TODO: make the parser fully incremental (via chunks of any possible size ...)"	
  ([#^String text] (parse text (.length text)))
  ([#^String text stop-offset] (parse text [] default-accumulator (make-default-continue?-fn stop-offset) default-make-result))
	([#^String text initial-accumulated-state accumulator-fn continue?-fn make-result-fn]
	  (loop [offset  (int 0)
	         line    (int 0)
	         col     (int 0)
	         parents [{:tag nil :offset 0 :line 0 :col 0}]
	         accumulated-state initial-accumulated-state]
	      (let [parser-state (if (>= offset (.length text)) :eof :ok) ; TODO soon an additional :parser-error state !
	            accumulated-state (accumulator-fn text offset line col parents parser-state accumulated-state)
	            continue? (continue?-fn text offset line col parents parser-state accumulated-state)] 
  	      (if (or (not continue?) (= :eof parser-state))
  	        (make-result-fn text offset line col parents parser-state accumulated-state)
  	        (let [c (.charAt text offset) ; c: current char
  	              parent-type (-> parents peek :tag)]
  	          (condp = parent-type
  	            \" 
                  (cond
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state)
                    (= \" c)
                      (if (= offset 0)
                        (recur (inc offset) line (inc col) (conj parents {:tag c :line line :col col :offset offset}) accumulated-state)
                        (if (= \\ (.charAt text (dec offset)))
                          (recur (inc offset) line (inc col) parents accumulated-state)
                          (recur (inc offset) line (inc col) (pop parents) accumulated-state)))
                    :else
                      (recur (inc offset) line (inc col) parents accumulated-state))
                \;	          
                  (cond
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) (pop parents) accumulated-state)
                    :else
                      (recur (inc offset) line (inc col) parents accumulated-state))
                \space
                  (cond 
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state)
                    (= \return c)
                      (recur (inc offset) line col parents accumulated-state)
                    (*spaces* c) ; we know it's not a space related to line jump
                      (recur (inc offset) line (inc col) parents accumulated-state)
                    :else ; we know we're going out of spaces
                      (recur offset line col (pop parents) accumulated-state))
                \\
                  (cond   ; leaved as a template if new cases have to be handled
                    ; TODO refactor the following stuff. or keep for performance ? (bleh)
                    (start-like (.substring text (-> parents peek :offset)) "\\newline")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\newline"))
                        (recur (inc offset) line (inc col) parents accumulated-state)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\tab")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\tab"))
                        (recur (inc offset) line (inc col) parents accumulated-state)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\space")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\space"))
                        (recur (inc offset) line (inc col) parents accumulated-state)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\backspace")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\backspace"))
                        (recur (inc offset) line (inc col) parents accumulated-state)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\formfeed")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\formfeed"))
                        (recur (inc offset) line (inc col) parents accumulated-state)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\return")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\return"))
                        (recur (inc offset) line (inc col) parents accumulated-state)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                    ; TODO I don't like the fact the next two conditions on \r and \newline are repeated from the default case
                    ;      clearly a level of indirection is missing regarding the update of line, offset and col
                    (= (first "\r") c)
                      (recur (inc offset) line col parents accumulated-state) ; we do not increment the column    
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state)
                    :else
                      (recur (inc offset) line (inc col) (pop parents) accumulated-state))
                ; last falling case: we are in plain code, neither in a string, regexp or a comment or a space
                (cond
                  (*opening-brackets* c)
                    (if (= \a parent-type)
                      (recur offset line col (pop parents) accumulated-state)
                      (recur 
                        (inc offset) line (inc col)
                        (conj parents {:tag c :offset offset :line line :col col})
                         accumulated-state))
                  (*closing-brackets* c)
                    (if (= \a parent-type)
                      (recur offset line col (pop parents) accumulated-state)
                      (recur 
                        (inc offset) line (inc col)
                        (pop  parents)
                        accumulated-state))
                  (= \" c)
                    (if (= \a parent-type)
                      (recur offset line col (pop parents) accumulated-state)
                      (recur 
                        (inc offset) line (inc col) 
                        (conj parents {:tag c :offset offset :line line :col col }) 
                        accumulated-state))
                  (= \; c)
                    (if (= \a parent-type)
                      (recur offset line col (pop parents) accumulated-state)
                      (recur 
                        (inc offset) line (inc col) 
                        (conj parents {:tag c :offset offset :line line :col col}) 
                        accumulated-state))
                  (= \\ c)
                    (if (= \a parent-type)
                      (recur offset line col (pop parents) accumulated-state)
                      (recur (inc offset) line (inc col) 
                      (conj parents {:tag c :offset offset :line line :col col}) 
                      accumulated-state))
                  (*spaces* c)
                    (if (= \a parent-type)
                      (recur offset line col (pop parents) accumulated-state)
                      (recur 
                        offset line col 
                        (conj parents {:tag \space :offset offset :line line :col col}) accumulated-state))
                  :else
                    (recur (inc offset) line (inc col) 
                      (if (= \a parent-type) 
                        parents 
                        (conj parents {:tag \a :offset offset :line line :col col}))
                          accumulated-state)))))))))
	          
(defn parse-lib 
  ([lib] (parse-lib lib false))
  ([lib show?]
    (let [#^String s (slurp (str "/home/lpetit/projects/clojure/src/clj/clojure/" lib ".clj"))
          res (time (parse s))]
      (when show? res))))
(defn parse-core []
  (parse-lib "core" false))
(defn parse-set []
	(parse-lib "set" false))
