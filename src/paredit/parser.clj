; note : hiredman's reader http://github.com/hiredman/clojure/blob/readerII/src/clj/clojure/reader.clj#L516
(ns paredit.parser
	(:use clojure.test))

(set! *warn-on-reflection* true)

(def *brackets* {\( \) \{ \} \[ \]})
(def *opening-brackets* (set (keys *brackets*)))
(def *closing-brackets* (set (vals *brackets*)))

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
      true "" "bar"
      )))
      

"\"parser state \" datastructure: (not really a parser currently, contains just the stack of containing balanced expression up to
 the top level)
 { :parents [ {:type <[ or ( or { or \" or \\> :line :offset :col} ... ]
   :offset current-offset :line current-line :col current-col}"

; todo : later on, move offset+line+col+parents inside a proper deftyped type
(defn default-accumulator
  [#^String text offset line col parents parser-state accumulated-state]
    [#^String text offset line col parents parser-state accumulated-state]
    nil)

(defn make-default-continue?-fn
  [stop-offset]
  (fn default-continue?-fn
    [#^String text offset line col parents parser-state accumulated-state]
    (< offset stop-offset)))

(defn default-make-result
  [#^String text offset line col parents parser-state accumulated-state]
  { :parents parents :offset offset :line line :col col })
   
(defn parse 
	"TODO: currently the parser assumes a well formed document ... Define a policy if the parser encounters and invalid text
	 TODO: make the parser restartable at a given offset given a state ...
	 TODO: make the parser fully incremental (via chunks of any possible size ...)"	
  ([#^String text] (parse text (.length text)))
  ([#^String text stop-offset] (parse text nil default-accumulator (make-default-continue?-fn stop-offset) default-make-result))
	([#^String text initial-accumulated-state accumulator-fn continue?-fn make-result-fn]
	  (loop [offset  (int 0)
	         line    (int 0)
	         col     (int 0)
	         parents [{:type nil :offset 0 :line 0 :col 0}]
	         accumulated-state initial-accumulated-state]
	      (let [parser-state (if (>= offset (.length text)) :eof :ok) ; TODO soon an additional :parser-error state !
	            accumulated-state (accumulator-fn text offset line col parents parser-state accumulated-state)
	            continue? (continue?-fn text offset line col parents parser-state accumulated-state)] 
  	      (if (or (not continue?) (= :eof parser-state))
  	        (make-result-fn text offset line col parents parser-state accumulated-state)
  	        (let [c (.charAt text offset)] ; c: current char
  	          (condp = (-> parents peek :type)
  	            \" 
                  (cond
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state)
                    (= \" c)
                      (if (= offset 0)
                        (recur (inc offset) line (inc col) (conj parents {:type c :line line :col col :offset offset}) accumulated-state)
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
                \\
                  (cond   ; leaved as a template if new cases have to be handled
                    #_(*opening-brackets* c)
                    #_(*closing-brackets* c)
                    #_(= (first "\r") c)
                    #_(= \newline c)
                    #_(= \" c)
                    #_(= \; c)
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
                ; last falling case: we are in plain code, neither in a string, regexp or a comment
                (cond
                  (*opening-brackets* c)
                    (recur 
                      (inc offset) line (inc col)
                      (conj parents {:type c :offset offset :line line :col col})
                       accumulated-state)
                  (*closing-brackets* c)
                    (recur 
                      (inc offset) line (inc col)
                      (pop parents)
                       accumulated-state)
                  (= (first "\r") c)
                    (recur (inc offset) line col parents accumulated-state) ; we do not increment the column    
                  (= \newline c)
                    (recur (inc offset) (inc line) (int 0) parents accumulated-state)
                  (= \" c)
                    (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col }) accumulated-state)
                  (= \; c)
                    (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col}) accumulated-state)
                  (= \\ c)
                    (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col}) accumulated-state)
                  :else
                    (recur (inc offset) line (inc col) parents accumulated-state))
                  
                  #_(cond   ; leaved as a template if new cases have to be handled
                    (*opening-brackets* c)
                    (*closing-brackets* c)
                    (= (first "\r") c)
                    (= \newline c)
                    (= \" c)
                    (= \; c)
                    (= \\ c)
                    :else
                      (recur (inc offset) line (inc col) parents accumulated-state)))))))))
	          
(comment
(defn parse-lib 
  ([lib] (parse-lib lib false))
  ([lib show?]
    (let [#^String s (slurp (str "/home/lpetit/projects/clojure/src/clj/clojure/" lib ".clj"))
          res (time (parse s))]
      (when show? res))))
(defn parse-core []
  (parse-lib "core" true))
(defn parse-set []
	(parse-lib "set" true))
)

  