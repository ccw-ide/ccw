(ns paredit.parser
	(:use clojure.test))

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
	[text stop-offset]
  (let [r (StringBuilder.) ; result string builder
        append-spaces (fn [n-spaces] (dotimes [_ n-spaces] (.append r " ")))
       ] 
    (loop [
           s (seq text) ; current string, stream of chars
           { :keys [parents offset line col] :as state} { :parents [{:type nil :offset 0 :line 0 :col 0}] :offset 0 :line 0 :col 0}]
        (if (or (not s) (>= offset stop-offset))
          state
          (let [c (first s)] ; c: current char
            (cond
              (*opening-brackets* c)
                (cond 
                  (= \" (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  (= \; (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  :else
		                (recur 
		                  (next s) 
		                  (assoc state 
		                    :parents (conj parents {:type c :line line :col col :offset offset})
		                    :offset (inc offset)
		                    :col (inc col))))
              (*closing-brackets* c)
                (cond 
                  (= \" (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  (= \; (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  :else
		                (recur 
		                  (next s)
		                  (assoc state 
		                    :parents (pop parents)
		                    :offset (inc offset)
		                    :col (inc col))))
              (= (first "\r") c)
                (recur (next s) (assoc state :offset (inc offset))) ; we do not increment the column    
              (= \newline c)
                (cond 
                  (= \" (-> parents pop :type))
		                (recur (next s) (assoc state :offset (inc offset) :line (inc line) :col 0))
                  (= \; (-> parents pop :type))
                    ; we take care of going out of comments, if we are in comments !
		                (recur (next s) (assoc state :parents (if (= \; (-> parents pop :type)) (pop parents) parents) :offset (inc offset) :line (inc line) :col 0))
                  :else
		                (recur (next s) (assoc state :offset (inc offset) :line (inc line) :col 0)))
              #_(#{\space \tab \,} c)
                #_(cond 
                  (= \" (-> parents pop :type))
                  (= \; (-> parents pop :type))
                  :else
                )
              (= \" c)
                (cond 
                  (= \" (-> parents pop :type))
                    (if (= offset 0)
                      (recur (next s) (assoc state :offset (inc offset) :col (inc col) :parents (conj {:type c :line line :col col :offset offset})))
                      (if (= \\ (.charAt text (dec offset)))
                        (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                        (recur (next s) (assoc state :parents (pop parents) :offset (inc offset) :col (inc col)))))
                  (= \; (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  :else
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col) :parents (conj parents {:type c :line line :col col :offset offset}))))
              (= \; c)
                (cond 
                  (= \" (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  (= \; (-> parents pop :type))
                    (recur (next s) (assoc state :offset (inc offset) :col (inc col)))
                  :else
                    (recur (next s) (assoc state :parents (conj parents {:type c :offset offset :col col :line line}) state :offset (inc offset) :col (inc col))))
              :else
                (recur (next s) (assoc state :offset (inc offset) :col (inc col)))))))))    

(defn parse-core []
  (time (let [s (slurp "/home/lpetit/projects/clojure/src/clj/clojure/core.clj")] (parse s (.length s)) (.length s))))

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

  