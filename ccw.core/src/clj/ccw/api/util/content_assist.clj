(ns ccw.api.util.content-assist
  "General purpose Utility fn for content assist"
  (:require
    [clojure.string :as str]
    [clojure.test :as test]))

;; TODO find the prefix via the editor's parse tree !
(defn prefix-info 
  [ns offset ^String prefix]
  (let [[n1 n2] (str/split prefix #"/")
        [n1 n2] (cond
                  (and (nil? n2)
                       (not (.endsWith prefix "/"))) [n2 n1]
                  :else [n1 n2])]
    {:curent-namespace ns
     :offset offset
     :prefix prefix
     :namespace n1 
     :prefix-name n2}))

;; TODO find the prefix via the editor's parse tree !
(defn invalid-symbol-char? [^Character c]
  (let [invalid-chars
        #{ \(, \), \[, \], \{, \}, \', \@,
          \~, \^, \`, \#, \" }]
    (or (Character/isWhitespace c)
        (invalid-chars c))))

;; TODO find the prefix via the editor's parse tree !
(test/deftest test-invalid-symbol-char?
  (test/are [char] (invalid-symbol-char? char)
            \(, \newline, \@, \space)
  (test/are [char] (not (invalid-symbol-char? char))
            \a, \-))

;; TODO find the prefix via the editor's parse tree !
(defn compute-prefix-offset
  [^String string offset]
  (if-let [start (some
                   #(when (invalid-symbol-char? (.charAt string %)) %)
                   (range (dec offset) -1 -1))]
    (inc start)
    0))

;; TODO find the prefix via the editor's parse tree !
(test/deftest test-compute-prefix-offset
  (test/are [result string offset] 
            (= result (compute-prefix-offset string offset))
    0 ""       0
    0 "a"      0
    0 "a"      1
    0 "abc"    2
    0 " abc"   0
    1 " abc"   1
    1 " abc"   2
    1 " abc"   4
    1 " .abc"  4
    1 "\n.abc" 4))
