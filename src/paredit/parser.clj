(ns paredit.parser
	(:use clojure.test))

(set! *warn-on-reflection* true)

(def *brackets* {\( \) \{ \} \[ \]})
(def *opening-brackets* (set (keys *brackets*)))
(def *closing-brackets* (set (vals *brackets*)))

(defn 
  starts-like
  "returns true if content of s1 (starting at index s1-offset) starts with content
   of s2, or if it is a prefix of the content of s2.
   Examples:
   (= true (starts-like \"bar and foo\" \"bar\"))
   (= false (starts-like \"bar\" \"baz\"))
   (= true (starts-like \"bar\" \"bar\"))
   (= true (starts-like \"ba\" \"bar\"))
   (= true (starts-like \"b\" \"bar\"))
   (= true (starts-like \"\" \"bar\"))
   (= true (starts-like \"bar and foo\" \"bar\"))"
  [#^String s1 #^String s2]
    (.startsWith s1 (.substring s2 0 (min (.length s2) (.length s1)))))

(deftest test-starts-like
  (testing "starts-like"
    (are [expected s1 s2]  (= expected (starts-like s1 s2))
      true "bar and foo" "bar"
      false "bar" "baz"
      true "bar" "bar"
      true "ba" "bar"
      true "b" "bar"
      true "" "bar")))
      

"\"parser state \" datastructure: (not really a parser currently, contains just the stack of containing balanced expression up to
 the top level)
 { :parents [ {:type <[ or ( or { or \" or \\> :line :offset :col} ... ]
   :offset current-offset :line current-line :col current-col}"

(defn parse 
	"TODO: currently the parser assumes a well formed document ...
	 TODO: make the parser restartable at a given offset given a state ...
	 TODO: make the parser fully incremental (via chunks of any possible size ...)"	
	([#^String text] (parse text (.length text)))
	([#^String text stop-offset]
	  (loop [offset  (int 0)
	         line    (int 0)
	         col     (int 0)
	         parents [{:type nil :offset 0 :line 0 :col 0}]]
	      (if (>= offset stop-offset)
	        { :parents parents :offset offset :line line :col col }
	        (let [c (.charAt text offset)] ; c: current char
	          (condp = (-> parents peek :type)
	            \" 
                (cond
                  (= \newline c)
                    (recur (inc offset) (inc line) (int 0) parents)
                  (= \" c)
                    (if (= offset 0)
                      (recur (inc offset) line (inc col) (conj parents {:type c :line line :col col :offset offset}))
                      (if (= \\ (.charAt text (dec offset)))
                        (recur (inc offset) line (inc col) parents)
                        (recur (inc offset) line (inc col) (pop parents))))
                  :else
                    (recur (inc offset) line (inc col) parents))
              \;	          
                (cond
                  (= \newline c)
                    (recur (inc offset) (inc line) (int 0) (pop parents))
                  :else
                    (recur (inc offset) line (inc col) parents))
              \\
                (cond   ; leaved as a template if new cases have to be handled
                  #_(*opening-brackets* c)
                  #_(*closing-brackets* c)
                  #_(= (first "\r") c)
                  #_(= \newline c)
                  #_(= \" c)
                  #_(= \; c)
                  ; TODO refactor the following stuff. or keep for performance ? (bleh)
                  (starts-like (.substring text (-> parents peek :offset)) "\\newline")
                    (if (< (inc (- offset (-> parents peek :offset))) (.length "\\newline"))
                      (recur (inc offset) line (inc col) parents)
                      (recur (inc offset) line (inc col) (pop parents)))
                  (starts-like (.substring text (-> parents peek :offset)) "\\tab")
                    (if (< (inc (- offset (-> parents peek :offset))) (.length "\\tab"))
                      (recur (inc offset) line (inc col) parents)
                      (recur (inc offset) line (inc col) (pop parents)))
                  (starts-like (.substring text (-> parents peek :offset)) "\\space")
                    (if (< (inc (- offset (-> parents peek :offset))) (.length "\\space"))
                      (recur (inc offset) line (inc col) parents)
                      (recur (inc offset) line (inc col) (pop parents)))
                  ; TODO I don't like the fact the next two conditions on \r and \newline are repeated from the default case
                  ;      clearly a level of indirection is missing regarding the update of line, offset and col
                  (= (first "\r") c)
                    (recur (inc offset) line col parents) ; we do not increment the column    
                  (= \newline c)
                    (recur (inc offset) (inc line) (int 0) parents)
                  :else
                    (recur (inc offset) line (inc col) (pop parents)))
              ; last falling case: we are in plain code, neither in a string, regexp or a comment
              (cond
                (*opening-brackets* c)
                  (recur 
                    (inc offset) line (inc col)
                    (conj parents {:type c :offset offset :line line :col col}))
                (*closing-brackets* c)
                  (recur 
                    (inc offset) line (inc col)
                    (pop parents))
                (= (first "\r") c)
                  (recur (inc offset) line col parents) ; we do not increment the column    
                (= \newline c)
                  (recur (inc offset) (inc line) (int 0) parents)
                (= \" c)
                  (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col }))
                (= \; c)
                  (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col}))
                (= \\ c)
                  (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :line line :col col}))
                :else
                  (recur (inc offset) line (inc col) parents))
                
                #_(cond   ; leaved as a template if new cases have to be handled
                  (*opening-brackets* c)
                  (*closing-brackets* c)
                  (= (first "\r") c)
                  (= \newline c)
                  (= \" c)
                  (= \; c)
                  (= \\ c)
                  :else
                    (recur (inc offset) line (inc col) parents))))))))
	          
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

  