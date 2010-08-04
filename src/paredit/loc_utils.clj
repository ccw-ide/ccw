(ns paredit.loc-utils
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter :as zf]))

#_(set! *warn-on-reflection* true)

(defn node-text [n]
  (if (string? n)
    n
    (apply str (map #'node-text (:content n)))))

(defn loc-text [loc]
  (node-text (zip/node loc)))

(defn loc-count [loc]
 ;(cond
 ;   (nil? loc) 0
 ;   (string? (zip/node loc)) (.length ^String (zip/node loc))
 ;   (zip/down loc) (apply + (map #'loc-count (concat [(zip/down loc)] (zip/rights (zip/down loc)))))
 ;   :else 0))
  (.length ^String (loc-text loc)))

(defn ^String loc-tag [loc]
  (and loc 
    (:tag (zip/node (if (string? (zip/node loc)) (zip/up loc) loc)))))

(defn same-parent? [loc & locs]
  (let [loc-parent-path (butlast (zip/path loc))]
    (every? #(= (butlast (zip/path %)) loc-parent-path) locs)))

(defn loc-depth 
  "returns the depth in the tree of the given loc"
  [loc]
  (count (zip/path loc)))

(defn up-to-depth
  "finds from the loc the ancestor loc at the given depth."
  [loc depth]
  (let [delta (- (loc-depth loc) depth)]
    (cond 
      (zero? delta) loc
      :else (nth (iterate zip/up loc) delta))))

(defn punct-loc?
  "true if the loc corresponds to punctuation."
  [loc]
  (and
    loc
    (string? (zip/node loc)) 
    (not (#{:whitespace :atom :comment :char :string :regex} (loc-tag (zip/up loc))))))

(defn root-loc [loc] (if-let [up (zip/up loc)] (recur up) loc))

(defn rlefts
  "like clojure.zip/lefts, but in reverse order (optimized lazy sequence)"
  [loc]
  (rest (take-while identity (iterate zip/left loc))))

(defn next-leaves
  "seq of next leaves locs" ;; TODO correct this aberration: next-leaves includes the current leave ... (or change the name ...)
  [loc]
  (and loc (remove zip/branch? (take-while (complement zip/end?) (iterate zip/next loc)))))

(defn previous-leaves
  "seq of previous leaves locs"
  [loc]
  (and loc (remove zip/branch? (take-while (complement nil?) (iterate zip/prev (zip/prev loc))))))

(defn start-offset [loc]
  (loop [loc loc offset 0] 
    (cond
      (nil? loc) offset
      :else
        (if-let [l (zip/left loc)]
          (recur l (+ offset (loc-count l)))
          (recur (zip/up loc) offset)))))

(defn end-offset [loc]
  (+ (start-offset loc) (loc-count loc)))

(defn loc-col [loc]
  (loop [loc (zip/prev loc) col 0]
    (cond
      (nil? loc) 
        col
      (string? (zip/node loc))
        (if (.contains ^String (zip/node loc) "\n")
          (+ col (dec (-> ^String (zip/node loc) (.substring (.lastIndexOf ^String (zip/node loc) "\n")) .length)))
          (recur (zip/prev loc) (+ col (loc-count loc))))
      :else
        (recur (zip/prev loc) col))))
  
(defn loc-parse-node [loc] ; wrong name, and also, will return (foo) if located at ( or at ) ... so definitely wrong name ...
  (if (string? (zip/node loc))
    (zip/up loc)
    loc))

(defn parse-leave
  "returns a leave which corresponds to a parse information: either a (punct-loc?) (beware: a bare String, not a node with meta-data,
   or a parse atom" 
  [loc]
  (cond 
    (punct-loc? loc) loc
    (string? (zip/node loc)) (zip/up loc)
    :else loc))

(defn parse-node
  "transforms the loc in a parse-leave, and if a punct, returns the parent loc"
  [loc]
  (let [loc (parse-leave loc)] 
    (if (punct-loc? loc) (zip/up loc) loc)))

(defn parsed-root-loc
  ([parsed] (parsed-root-loc parsed false))
  ([parsed only-valid?]
    ;(let [valid? (= 1 (-> parsed :accumulated-state count))]
    (zip/xml-zip parsed)))

(defn contains-offset?
  "returns the loc itself if it contains the offset, else nil"
  [loc offset]
   (let [start (start-offset loc)
         end (+ (loc-count loc) start)] 
     (and
       (<= start offset (dec end))
       loc)))

;; TODO when parsley is up and implements (count), then it will be (probably)
;;      more performant to use (count) to jump over nodes
(defn leave-loc-for-offset-common 
  "returns a zipper location or nil if does not contain the offset"
  [loc offset] 
    (loop [locs (seq (next-leaves (root-loc loc))) o 0]
      (if-not locs
        (root-loc loc) ;best-match
        (let [end (+ o (.length ^String (zip/node (first locs))))]
          (if (<= o offset (dec end))
            (first locs)
            (recur (next locs) end))))))

(defn leave-for-offset
  [loc offset]
  (if-let [l (leave-loc-for-offset-common loc offset)]
    l
    (root-loc loc)))

(defn loc-for-offset 
  "returns a zipper location or nil if does not contain the offset"
  [loc offset] 
    (when-let [l (leave-loc-for-offset-common loc offset)]
      (parse-node l)))

(defn loc-containing-offset
  [loc offset]
  (if-let [l (leave-loc-for-offset-common loc offset)]
    (loop [l l]
      (cond
        (= (root-loc loc) l) l
        (= offset (start-offset l)) (recur (zip/up l))
        :else l))
    (root-loc loc)))

#_(defn loc-containing-offset
  ([loc offset]
    (if (= 0 offset)
      (root-loc loc)
      (let [match (some #(contains-offset? % offset) (take-while (complement zip/end?) (iterate zip/next (zip/next (root-loc loc)))))]
        (cond
          (nil? match) (root-loc loc)
          (= offset (start-offset match)) (zip/up match)
          :else match)))))

(defn start-punct?
  "true if the loc is a punct starting a form"
  [loc]
  (and
    (punct-loc? loc)
    (= (start-offset loc) (start-offset (parse-node loc)))))

(defn end-punct?
  "true if the loc is a punct ending a form"
  [loc]
  (and
    (punct-loc? loc)
    (= (end-offset loc) (end-offset (parse-node loc)))))
