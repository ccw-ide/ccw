; note : hiredman's reader http://github.com/hiredman/clojure/blob/readerII/src/clj/clojure/reader.clj#L516
; still TODO :
; 1. done - make parser and function behaviour similar for terminals atom and spaces 
; 1.a  (and move the special handling of zipping terminals up on :eof from default-handler to default-maker ?)
; 2. correctly handle clojure specificities : #{} #^ #"" ' ` @ #^ ^ #' #_ #() ~ ~@ foo# #!
; 3. correctly handle the premature :eof on non closed structures (a cause of error)
; 4. correctly handle parsetree errors (wrong closing of bracket (this one done), ... TODO finish the exhaustive list)
; 5. make the parser restartable
; 6. make the parser incremental 
; 7. refactor the code so that the handling of advancing offset, line, column ... is mutualized (be aware of not introducing regressions in the handling of atoms and spaces terminals)
; point 6. is optional :-)
; point 3. may be viewed as a special case of point 4 ?

; bugs:
; \newlinb should be an error, not \n + symbol ewlinb

; miscellaneous TODO
; * add an explicit error message to :parser-state :ko (unbalanced parens)

(ns paredit.parser
	(:use [clojure.test])
	(:require [clojure.zip :as zip]))

#_(set! *warn-on-reflection* true)

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
(def *brackets* {"(" ")", "{" "}", "[" "]", "\"" "\""})
(def *opening-brackets* (set (keys *brackets*)))
(def *closing-brackets* (set (vals *brackets*)))
(def *spaces* #{(str \space) (str \tab) (str \newline) (str \return) (str \,)})
(def *atoms* #{ (str \a) (str \space)})

(defn zip-one [#^String text offset line col parents parser-state accumulated-state]
  (let [level (-> accumulated-state peek)
        level-content (get level :content [])
        level-content (if-let [closing-delimiter (-> level :tag *brackets*)]
                        (if (and (= "\"" closing-delimiter) (> (- offset (get level :offset 0)) 2))
                          [ "\"" (.substring text (inc (get level :offset 0)) (dec offset)) "\""]
                          (conj level-content (str closing-delimiter)))
                        (conj level-content (.substring text (get level :offset 0) offset)))
        level (-> accumulated-state peek (assoc :end-offset offset :content level-content))
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
        (if (and (= :eof parser-state) (*atoms* (-> parents peek :tag))) ; todo move in default-make-result ?
          (zip-one 
            text offset line col parents parser-state 
            (-> accumulated-state (conj (peek parents))))
          (let [new-node (peek parents)
                new-node (assoc new-node :content (if-let [opening-delimiter (*opening-brackets* (:tag new-node))]
                                           [(str opening-delimiter)]
                                           []))]
            (-> accumulated-state (conj new-node)))) 
      (or
        (< (count parents) (count accumulated-state))
        (and (= :eof parser-state) (*atoms* (-> accumulated-state peek :tag))))
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
  { :parents parents 
    :offset offset 
    :line line 
    :col col 
    :accumulated-state (assoc-in accumulated-state [0 :end-offset] offset)
    :parser-state parser-state})

(defn empty-node? [node]
  (or 
    (= (str \space) (:tag node))
    (every? #(and (not (string? %)) (= (str \space) (:tag %))) (:content node))))

(defn char-at 
  "if index is out of bounds, just returns nil"
  [#^String s index]
  (when (< -1 index (.length s))
    (.charAt s index)))
    
   
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
	         parents [{:tag :root :offset 0 :line 0 :col 0}]
	         accumulated-state initial-accumulated-state
	         parser-state :ok]
      (if (= :ko parser-state)
        (make-result-fn text offset line col parents parser-state accumulated-state)   
        (let [parser-state (if (>= offset (.length text)) :eof :ok) ; TODO soon an additional :parser-error state !
              accumulated-state (accumulator-fn text offset line col parents parser-state accumulated-state)
              continue? (continue?-fn text offset line col parents parser-state accumulated-state)] 
  	      (if (or (not continue?) (= :eof parser-state))
  	        (make-result-fn text offset line col parents parser-state accumulated-state)
  	        (let [c (str (.charAt text offset))
  	              parent-type (-> parents peek :tag)]
  	          (condp = parent-type
  	            "\"" 
                  (cond
                    (= (str \newline) c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state :ok)
                    (= "\\" c)
                      (recur (inc offset) line (inc col) (conj parents {:tag "\"\\" :line line :col col :offset offset}) accumulated-state :ok)
                    (= "\"" c)
                      ;(if (= offset 0)
                      ;  (recur (inc offset) line (inc col) (conj parents {:tag c :line line :col col :offset offset}) accumulated-state :ok)
                      ;  (if (and 
                      ;        (= (str \\) (str (.charAt text (dec offset))))
                      ;        (not= (str \\) (str (char-at text (dec (dec offset))))))
                      ;    (recur (inc offset) line (inc col) parents accumulated-state :ok)
                          (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok )
                          ;))
                    :else
                      (recur (inc offset) line (inc col) parents accumulated-state :ok))
                "\"\\"
                  (cond
                    (= (str \newline) c)
                      (recur (inc offset) (inc line) (int 0) (pop parents) accumulated-state :ok)
                    :else
                      (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                (str \;) 	          
                  (cond
                    (= (str \newline) c)
                      (recur (inc offset) (inc line) (int 0) (pop parents) accumulated-state :ok)
                    :else
                      (recur (inc offset) line (inc col) parents accumulated-state :ok))
                (str \space)
                  (cond 
                    (= (str \newline) c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state :ok)
                    (= (str \return) c)
                      (recur (inc offset) line col parents accumulated-state :ok)
                    (*spaces* c) ; we know it's not a space related to line jump
                      (recur (inc offset) line (inc col) parents accumulated-state :ok)
                    :else ; we know we're going out of spaces
                      (recur offset line col (pop parents) accumulated-state :ok))
                (str \\) 
                  (cond
                    ; TODO refactor the following stuff. or keep for performance ? (bleh)
                    (start-like (.substring text (-> parents peek :offset)) "\\newline")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\newline"))
                        (recur (inc offset) line (inc col) parents accumulated-state :ok)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                    (start-like (.substring text (-> parents peek :offset)) "\\tab")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\tab"))
                        (recur (inc offset) line (inc col) parents accumulated-state :ok)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                    (start-like (.substring text (-> parents peek :offset)) "\\space")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\space"))
                        (recur (inc offset) line (inc col) parents accumulated-state :ok)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                    (start-like (.substring text (-> parents peek :offset)) "\\backspace")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\backspace"))
                        (recur (inc offset) line (inc col) parents accumulated-state :ok)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                    (start-like (.substring text (-> parents peek :offset)) "\\formfeed")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\formfeed"))
                        (recur (inc offset) line (inc col) parents accumulated-state :ok)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                    (start-like (.substring text (-> parents peek :offset)) "\\return")
                      (if (< (inc (- offset (-> parents peek :offset))) (.length "\\return"))
                        (recur (inc offset) line (inc col) parents accumulated-state :ok)
                        (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                    ; TODO I don't like the fact the next two conditions on \r and \newline are repeated from the default case
                    ;      clearly a level of indirection is missing regarding the update of line, offset and col
                    (= "\r" c)
                      (recur (inc offset) line col parents accumulated-state :ok) ; we do not increment the column    
                    (= (str \newline) c)
                      (recur (inc offset) (inc line) (int 0) parents accumulated-state :ok)
                    :else
                      (recur (inc offset) line (inc col) (pop parents) accumulated-state :ok))
                ; last falling case: we are in plain code, neither in a string, regexp or a comment or a space
                (cond
                  (*opening-brackets* c)
                    (if (= (str \a) parent-type)
                      (recur offset line col (pop parents) accumulated-state :ok)
                      (recur 
                        (inc offset) line (inc col)
                        (conj parents {:tag c :offset offset :line line :col col})
                        accumulated-state
                        :ok))
                  (*closing-brackets* c)
                    (if (= (str \a) parent-type)
                      (recur offset line col (pop parents) accumulated-state :ok)
                      (if (= c (*brackets* parent-type))
                        (recur 
                          (inc offset) line (inc col)
                          (pop  parents)
                          accumulated-state
                          :ok)
                         ; problem: the closing paren does not match
                        (recur offset line col parents accumulated-state :ko)))
                  (= "\"" c)
                    (if (= (str \a) parent-type)
                      (recur offset line col (pop parents) accumulated-state :ok)
                      (recur 
                        (inc offset) line (inc col) 
                        (conj parents {:tag c :offset offset :line line :col col }) 
                        accumulated-state :ok))
                  (= (str \;) c)
                    (if (= (str \a) parent-type)
                      (recur offset line col (pop parents) accumulated-state :ok)
                      (recur 
                        (inc offset) line (inc col) 
                        (conj parents {:tag c :offset offset :line line :col col}) 
                        accumulated-state
                        :ok))
                  (= (str \\) c)
                    (if (= (str \a) parent-type)
                      (recur offset line col (pop parents) accumulated-state :ok)
                      (recur (inc offset) line (inc col) 
                        (conj parents {:tag c :offset offset :line line :col col}) 
                        accumulated-state
                        :ok))
                  (*spaces* c)
                    (if (= (str \a) parent-type)
                      (recur offset line col (pop parents) accumulated-state :ok)
                      (recur 
                        offset line col 
                        (conj parents {:tag (str \space) :offset offset :line line :col col})
                        accumulated-state
                        :ok))
                  :else
                    (recur (inc offset) line (inc col) 
                      (if (= (str \a) parent-type) 
                        parents 
                        (conj parents {:tag (str \a) :offset offset :line line :col col}))
                      accumulated-state
                      :ok))))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; utility libraries for manipulating the parse-tree

(defn parsed-root-loc
  ([parsed] (parsed-root-loc parsed false))
  ([parsed only-valid?]
    (let [valid? (= 1 (-> parsed :accumulated-state count))]
      (when (or valid? (not only-valid?))
        (-> parsed :accumulated-state first zip/xml-zip)))))

(defn contains-offset?
  "returns the loc itself if it contains the offset, else nil"
  [loc offset]
  (let [n (zip/node loc)]
    (and 
      (not (string? n))
      (cond
        (= :root (:tag n))
          (<= (:offset n) offset (:end-offset n))
        :else
          (and (<= (:offset n) offset) 
               (< offset (:end-offset n))))
      loc)))

(defn loc-for-offset 
  "returns a zipper location or nil if does not contain the offset"
  ([loc offset] (loc-for-offset loc offset nil))
  ([loc offset last-match]
    (if (or (nil? loc) (not (contains-offset? loc offset)) (not (zip/branch? loc)))
      last-match
      (recur 
        (some  
          #(contains-offset? % offset) 
          (take-while identity (iterate zip/right (zip/down loc)))) 
        offset
        loc))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; test functions

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
