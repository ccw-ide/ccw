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

#_(defn default-parser-hook
  ; todo : later on, move offset+line+col+parents inside a proper deftyped type
  [#^String text offset line col parents hook-state]
  [(< offset (.length text))
   { :parents parents :offset offset :line line :col col }])

(defn make-stop-offset-based-parser-hook
  [stop-offset]
  (fn stop-offset-based-parser-hook
  ; todo : later on, move offset+line+col+parents inside a proper deftyped type
    [#^String text offset line col parents parser-state hook-state]
    [(< offset stop-offset) nil]))

(defn default-parser-result-maker
  [#^String text offset line col parents parser-state hook-state]
  { :parents parents :offset offset :line line :col col })
   
(defn parse 
	"TODO: currently the parser assumes a well formed document ... Define a policy if the parser encounters and invalid text
	 TODO: make the parser restartable at a given offset given a state ...
	 TODO: make the parser fully incremental (via chunks of any possible size ...)"	
  ([#^String text] (parse text (.length text)))
  ([#^String text stop-offset] (parse text nil (make-stop-offset-based-parser-hook stop-offset) default-parser-result-maker))
	([#^String text initial-hook-state parser-hook parser-result-maker]
	  (loop [offset  (int 0)
	         line    (int 0)
	         col     (int 0)
	         parents [{:type nil :offset 0 :line 0 :col 0}]
	         hook-state initial-hook-state]
	      (let [parser-state (if (>= offset (.length text)) :eof :ok) ; TODO soon an additional :parser-error state !
	            [continue? hook-state] (parser-hook text offset line col parents parser-state hook-state)] 
  	      (if (or (not continue?) (= :eof parser-state))
  	        (parser-result-maker text offset line col parents parser-state hook-state)
  	        (let [c (.charAt text offset)] ; c: current char
  	          (condp = (-> parents peek :type)
  	            \" 
                  (cond
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) parents hook-state)
                    (= \" c)
                      (if (= offset 0)
                        (recur (inc offset) line (inc col) (conj parents {:type c :line line :col col :offset offset}) hook-state)
                        (if (= \\ (.charAt text (dec offset)))
                          (recur (inc offset) line (inc col) parents hook-state)
                          (recur (inc offset) line (inc col) (pop parents) hook-state)))
                    :else
                      (recur (inc offset) line (inc col) parents hook-state))
                \;	          
                  (cond
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) (pop parents) hook-state)
                    :else
                      (recur (inc offset) line (inc col) parents hook-state))
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
                        (recur (inc offset) line (inc col) parents hook-state)
                        (recur (inc offset) line (inc col) (pop parents) hook-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\tab")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\tab"))
                        (recur (inc offset) line (inc col) parents hook-state)
                        (recur (inc offset) line (inc col) (pop parents) hook-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\space")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\space"))
                        (recur (inc offset) line (inc col) parents hook-state)
                        (recur (inc offset) line (inc col) (pop parents) hook-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\backspace")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\backspace"))
                        (recur (inc offset) line (inc col) parents hook-state)
                        (recur (inc offset) line (inc col) (pop parents) hook-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\formfeed")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\formfeed"))
                        (recur (inc offset) line (inc col) parents hook-state)
                        (recur (inc offset) line (inc col) (pop parents) hook-state))
                    (start-like (.substring text (-> parents peek :offset)) "\\return")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\return"))
                        (recur (inc offset) line (inc col) parents hook-state)
                        (recur (inc offset) line (inc col) (pop parents) hook-state))
                    ; TODO I don't like the fact the next two conditions on \r and \newline are repeated from the default case
                    ;      clearly a level of indirection is missing regarding the update of line, offset and col
                    (= (first "\r") c)
                      (recur (inc offset) line col parents hook-state) ; we do not increment the column    
                    (= \newline c)
                      (recur (inc offset) (inc line) (int 0) parents hook-state)
                    :else
                      (recur (inc offset) line (inc col) (pop parents) hook-state))
                ; last falling case: we are in plain code, neither in a string, regexp or a comment
                (cond
                  (*opening-brackets* c)
                    (recur 
                      (inc offset) line (inc col)
                      (conj parents {:type c :offset offset :line line :col col})
                       hook-state)
                  (*closing-brackets* c)
                    (recur 
                      (inc offset) line (inc col)
                      (pop parents)
                       hook-state)
                  (= (first "\r") c)
                    (recur (inc offset) line col parents hook-state) ; we do not increment the column    
                  (= \newline c)
                    (recur (inc offset) (inc line) (int 0) parents hook-state)
                  (= \" c)
                    (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col }) hook-state)
                  (= \; c)
                    (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col}) hook-state)
                  (= \\ c)
                    (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col}) hook-state)
                  :else
                    (recur (inc offset) line (inc col) parents hook-state))
                  
                  #_(cond   ; leaved as a template if new cases have to be handled
                    (*opening-brackets* c)
                    (*closing-brackets* c)
                    (= (first "\r") c)
                    (= \newline c)
                    (= \" c)
                    (= \; c)
                    (= \\ c)
                    :else
                      (recur (inc offset) line (inc col) parents hook-state)))))))))
	          
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

  