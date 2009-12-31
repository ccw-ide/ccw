(ns paredit.parser
	(:use clojure.test))

(set! *warn-on-reflection* true)

(def *brackets* {\( \) \{ \} \[ \]})
(def *opening-brackets* (set (keys *brackets*)))
(def *closing-brackets* (set (vals *brackets*)))

"\"parser state \" datastructure: (not really a parser currently, contains just the stack of containing balanced expression up to
 the top level)
 { :parents [ {:type <[ or ( or { or \"> :line :offset :col} ... ]
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
	          (cond
	            (*opening-brackets* c)
	              (cond 
	                (= \" (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                (= \; (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                :else
		                (recur 
		                  (inc offset) line (inc col)
		                  (conj parents {:type c :offset offset :line line :col col})))
	            (*closing-brackets* c)
	              (cond 
	                (= \" (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                (= \; (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                :else
		                (recur 
		                  (inc offset) line (inc col)
		                  (pop parents)))
	            (= (first "\r") c)
	              (recur (inc offset) line col parents) ; we do not increment the column    
	            (= \newline c)
	              (cond 
	                (= \" (-> parents peek :type))
		                (recur (inc offset) (inc line) (int 0) parents)
	                (= \; (-> parents peek :type))
	                  ; we take care of going out of comments, if we are in comments !
		                (recur (inc offset) (inc line) (int 0) (pop parents))
	                :else
		                (recur (inc offset) (inc line) (int 0) parents))
	            #_(#{\space \tab \,} c)
	              #_(cond 
	                (= \" (-> parents peek :type))
	                (= \; (-> parents peek :type))
	                :else
	              )
	            (= \" c)
	              (cond 
	                (= \" (-> parents peek :type))
	                  (if (= offset 0)
	                    (recur (inc offset) line (inc col) (conj parents {:type c :line line :col col :offset offset}))
	                    (if (= \\ (.charAt text (dec offset)))
	                      (recur (inc offset) line (inc col) parents)
	                      (recur (inc offset) line (inc col) (pop parents))))
	                (= \; (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                :else
	                  (recur (inc offset) line (inc col) (conj parents {:type c :line line :col col :offset offset})))
	            (= \; c)
	              (cond 
	                (= \" (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                (= \; (-> parents peek :type))
	                  (recur (inc offset) line (inc col) parents)
	                :else
	                  (recur (inc offset) line (inc col) (conj parents {:type c :offset offset :col col :line line})))
	            :else
	              (recur (inc offset) line (inc col) parents)))))))

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

#_(deftest test-format-code
  (testing "parens combinatorics"
    (are [x y] (= x (format-code y))
"" ""
"" nil
"()" "()"
"[]" "[]"
"{}" "{}"
"#{}" "#{}"
#_"()" #_"()   "
#_"()" #_"(  )"
#_"()" #_"   ()"
#_"()" #_"  (   , ,  \t)"
#_"(())" #_"(())"
#_"(())" #_"   (   (   )  )"

#_"
(
  ())" 
#_"
(
())"

#_"(\n  ())" "(\n         (       ))"

#_"
((
   ()))"         
#_"
((
()))"

#_"
(
  ((
     ())))" 
#_"
   (\n(       (\n( ) ) )   ) ,"

#_"
((
 ))"
#_"
((
))"

#_"
[[
 ]]"
#_"
[[
]]"

#_"abc"
#_"abc"

#_"(def toto [] (println 123))"
#_"(def toto [] (println 123))"
    )))
(def rt run-tests)

  