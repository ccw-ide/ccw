(ns paredit.loc-utils
  (:use paredit.parser)
  (:require [clojure.zip :as z])
  (:require [paredit.text-utils :as t])
  (:require [clojure.string :as s]))

#_(set! *warn-on-reflection* true)
(defn xml-vzip
  "Returns a zipper for xml elements (as from xml/parse),
  given a root element"
  {:added "1.0"}
  [root]
  (z/zipper (complement string?) 
            (fn [node] (or (:content node) []))
            (fn [node children]
              (make-parse-tree-node 
                (:tag node)
                (vec children)))
            root))

(defn down-nth
  "Returns the loc of the child at index idx of the node at this loc, or
  nil if no children.
  Node children MUST be vectors for down-nth to work. And in this case,
  getting the loc of the idx'st child will be done in constant time."
  {:added "1.0"}
  [loc idx]
    (letfn [(split [cs idx] ; cs must be a non-empty vector 
              (when cs
                [(subvec cs 0 idx) (cs idx) (subvec cs (inc idx))]))]
      
      (when (and (z/branch? loc) 
                 (seq (z/children loc)))
        (let [[node path] loc
              [l c r] (split (z/children loc) idx)]
          (with-meta [c {:l l
                         :pnodes (if path (conj (:pnodes path) node) [node]) 
                         :ppath path 
                         :r (seq r) ;; This call to seq is very important,
                                    ;; :r is meant to contain a seq, 
                                    ;; and thus must be nil if empty 
                        }] (meta loc))))))

(defn ^:dynamic node-text [n]
  (if (string? n)
    n
    (apply str (map #'node-text (:content n)))))

(defn ^:dynamic loc-text [loc]
  (node-text (z/node loc)))

(defn loc-count [loc]
 (if (z/branch? loc)
   (or (:count (z/node loc)) 0) 
   (count (z/node loc))))

(defn ^String loc-tag [loc]
  (and loc 
    (:tag (z/node (if (string? (z/node loc)) (z/up loc) loc)))))

(defn same-parent? [loc & locs]
  (let [loc-parent-path (butlast (z/path loc))]
    (every? #(= (butlast (z/path %)) loc-parent-path) locs)))

(defn loc-depth 
  "returns the depth in the tree of the given loc"
  [loc]
  (count (z/path loc)))

(defn punct-loc?
  "true if the loc corresponds to punctuation."
  [loc]
  (and
    loc
    (string? (z/node loc)) 
    (not ((conj *atom* :whitespace :comment :char :string :regex) (loc-tag (z/up loc))))))

(defn root-loc [loc] (if-let [up (z/up loc)] (recur up) loc))

(defn rlefts
  "like clojure.z/lefts, but in reverse order (optimized lazy sequence)"
  [loc]
  (rest (take-while identity (iterate z/left loc))))

(defn next-leaves
  "seq of next leaves locs" ;; TODO correct this aberration: next-leaves includes the current leave ... (or change the name ...)
  [loc]
  (and loc (remove z/branch? (take-while (complement z/end?) (iterate z/next loc)))))

(defn previous-leaves
  "seq of previous leaves locs"
  [loc]
  (and loc (remove z/branch? (take-while (complement nil?) (iterate z/prev (z/prev loc))))))

;; TODO we should be able to locate the offset by first looking at the loc index, 
;; and then get the :content-cumulative-count, etc.
(defn ^:dynamic start-offset [loc]
  (loop [loc loc offset 0] 
    (cond
      (nil? loc) offset
      :else
        (if-let [l (z/left loc)]
          (recur l (+ offset (loc-count l)))
          (recur (z/up loc) offset)))))

(defn ^:dynamic end-offset [loc]
  (+ (start-offset loc) (loc-count loc)))

(defn ^:dynamic loc-col [loc]
  (loop [loc (z/prev loc) col 0]
    (cond
      (nil? loc) 
        col
      (string? (z/node loc))
        (if (.contains ^String (z/node loc) "\n")
          (+ col (dec (-> ^String (z/node loc) (.substring (.lastIndexOf ^String (z/node loc) "\n")) .length)))
          (recur (z/prev loc) (+ col (loc-count loc))))
      :else
        (recur (z/prev loc) col))))

(defn loc-end-col [loc]
  (let [loc-text (loc-text loc)
        last-nl-idx (.lastIndexOf loc-text "\n")]
    (if (neg? last-nl-idx)
      (+ (loc-col loc) (count loc-text))
      (- (count loc-text) (inc last-nl-idx)))))
  
(defn loc-parse-node [loc] ; wrong name, and also, will return (foo) if located at ( or at ) ... so definitely wrong name ...
  (if (string? (z/node loc))
    (z/up loc)
    loc))

(defn parse-leave
  "returns a leave which corresponds to a parse information: either a (punct-loc?) (beware: a bare String, not a node with meta-data,
   or a parse atom" 
  [loc]
  (cond 
    (punct-loc? loc) loc
    (string? (z/node loc)) (z/up loc)
    :else loc))

(defn parse-node
  "transforms the loc in a parse-leave, and if a punct, returns the parent loc"
  [loc]
  (let [loc (parse-leave loc)] 
    (if (punct-loc? loc) (z/up loc) loc)))

(defn parsed-root-loc
  ([parsed] (parsed-root-loc parsed false))
  ([parsed only-valid?]
    ;(let [valid? (= 1 (-> parsed :accumulated-state count))]
    (xml-vzip parsed)))

(defn ^:dynamic contains-offset?
  "returns the loc itself if it contains the offset, else nil"
  [loc offset]
   (let [start (start-offset loc)
         end (+ (loc-count loc) start)] 
     (and
       (<= start offset (dec end))
       loc)))

(defn- bisect [v pred x]
  (loop [start 0 end (count v)]
    (if (<= (- end start) 1)
      start
      (let [i (quot (+ start end) 2)
            y (nth v i)]
        (if (pred x y)
          (recur start i)
          (recur i end))))))

(defn leave-loc-for-offset-common
  "returns a zipper location or nil if does not contain the offset"
  ([loc offset]
    (leave-loc-for-offset-common loc offset false))
  ([loc offset left-bias]
    (leave-loc-for-offset-common loc offset left-bias (fn [loc _] loc)))
  ([loc offset left-bias f]
    (if (z/branch? loc)
      (let [content-cumulative-count (-> loc z/node :content-cumulative-count)]
        (when (seq content-cumulative-count) ; only the root should be empty
          (let [n (bisect content-cumulative-count (if left-bias <= <) offset)]
            (recur (down-nth loc n) (- offset (nth content-cumulative-count n)) left-bias f))))
      (f loc offset))))

(defn char-before [loc offset]
  (leave-loc-for-offset-common loc offset true (fn [loc offset] (nth (z/node loc) (dec offset) nil))))

(defn char-after [loc offset]
  (leave-loc-for-offset-common loc offset false (fn [loc offset] (nth (z/node loc) offset nil))))

(defn ^:dynamic leave-for-offset
  ([loc offset] (leave-for-offset loc offset false))
  ([loc offset left-bias]
    (if-let [l (leave-loc-for-offset-common loc offset left-bias)]
      l
      (root-loc loc))))

(defn ^:dynamic loc-for-offset
  "returns a zipper location or nil if does not contain the offset"
  ([loc offset] (loc-for-offset loc offset false))
  ([loc offset left-bias]
    (when-let [l (leave-loc-for-offset-common loc offset left-bias)]
      (parse-node l))))

(defn ^:dynamic loc-containing-offset
  [loc offset]
  (if-let [l (leave-loc-for-offset-common loc offset)]
    (loop [l l]
      (cond
        (= (root-loc loc) l) l
        (= offset (start-offset l)) (recur (z/up l))
        :else l))
    (root-loc loc)))

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

(defn top-level-loc 
  "Returns the top level loc"
  [loc]
  (first 
    (filter 
      #(= :root (loc-tag (z/up %)))
      (iterate z/up loc))))

(defn root? 
  "Is loc the root?"
  [loc] (and loc (nil? (z/up loc))))

(defn loc-tag? 
  "Is loc's :tag tag?"
  [loc tag] (and loc (= tag (:tag (z/node loc)))))

(defn comment? 
  "Is loc at a comment node?"
  [loc] (loc-tag? loc :comment))

(defn whitespace? 
  "Is loc at a :whitespace node? (including nodes for which newline? returns true"
  [loc] (loc-tag? loc :whitespace))

(defn newline? 
  "Is loc at a newline node? (a :whitespace node which is a newline)"
  [loc] (and (whitespace? loc)
             (= "\n" (loc-text loc))))

(defn single-line-whitespace?
  "Is loc a whitespace but not a newline?"
  [loc] (and (whitespace? loc)
             (not= "\n" (loc-text loc))))

(defn starts-line?
  "Is loc at the start of a newline? (at the start of the document, or after a newline)"
  [loc] (or (newline? (z/left loc))
            (and (not (root? loc))
                 (root? (z/prev loc)))))

(defn indent-whitespace?
  "Is loc at a :whitespace node at the start of a line? (an"
  [loc] (and (single-line-whitespace? loc)
             (starts-line? loc)))

(defn shift-nl-whitespace 
  "Loc is at a line start. Add delta (may be negative) spaces before it."
  [loc delta]
  {:pre [(starts-line? loc)]}
  (if (single-line-whitespace? loc)
    (let [adjusted-space (t/adjust-padding (loc-text loc) delta \space)]
      (if (= adjusted-space (loc-text loc))
        loc
        (z/replace loc (make-parse-tree-node :whitespace [adjusted-space]))))
    (if (pos? delta)
      (z/insert-left loc (make-parse-tree-node :whitespace [(t/repeat delta \space)]))
      loc)))

(defn next-node-loc
  "Get the loc for the node on the right, or if at the end of the parent,
   to the right of the parent. Skips punct nodes. Return nil if at the far end."
  [loc]
  (when (and loc (not (z/end? loc)))
    (if-let [r (z/right loc)]
      (if (punct-loc? r)
        (recur r)
        r)
      (when-let [p (z/up loc)]
        (recur p)))))

(defn path-count [loc] 
  (count (z/path loc)))

(defn next-start-line
  "Return the start-line following loc, or nil if none"
  [loc]
  (when-first [nl-leaf (filter (comp starts-line? z/up) (rest (next-leaves loc)))]
    (z/up nl-leaf)))

(defn shift-all-loc 
  "Shift all lines of loc of delta."
  [loc col delta]
  (loop [prev-loc loc]
    (let [loc (next-start-line prev-loc)]
      (cond 
        (nil? loc) (root-loc prev-loc)
        
        (newline? loc) (recur loc)
        
        (and (whitespace? loc) (newline? (z/right loc)))
          (recur (z/right loc))
        
        (or (and (whitespace? loc) (<= col (count (loc-text loc))))
            (zero? col))
          (let [sloc (shift-nl-whitespace loc delta)]
            (if (= sloc loc) 
              (root-loc loc)
              (recur sloc)))
        
        :else (root-loc loc)))))

(defn single-line-loc? [loc] (= -1 (.indexOf (loc-text loc) "\n")))
(defn propagate-delta 
  [loc col delta]
  (if (z/end? (z/next loc))
    (root-loc loc)
    (let [re-rooted-loc (if (loc-tag? loc :root) loc (->> loc z/node xml-vzip))
          loc-end-col-before (loc-end-col re-rooted-loc)
          sloc (shift-all-loc re-rooted-loc col delta)
          loc-end-col-after (loc-end-col sloc)
          loc (z/replace loc (z/node sloc))]
      (if-let [next-loc (and (or (not= loc-end-col-before loc-end-col-after)
                                 (single-line-loc? sloc))
                             (next-node-loc loc))]
        (recur next-loc (loc-end-col loc) delta)
        (root-loc loc)))))

(defn find-loc-to-shift
  "Starting with loc, find to the right, and to the right of parent node, etc.
   a non-whitespace loc. If a newline is found before, return nil."
  [loc]
  (let [continue-search (fn [loc] (and loc (not (newline? loc))))
        locs (take-while continue-search (iterate next-node-loc loc))]
    (first (remove whitespace? locs))))

(defn empty-diff?
  "Is the text diff empty (nothing replaced and nothing added)?" 
  [diff]
  (and (zero? (:length diff))
       (zero? (count (:text diff)))))

(defn whitespace-end-of-line?
  "For text s, starting at offset, is the remaining of the
   line only made of whitespace?"
  [s offset]
  (let [eol-offset (t/line-stop s offset)
        eol (subs s offset eol-offset)]
    (s/blank? eol)))

(defn col-shift 
  ([{:keys [parse-tree buffer] :as st} modif] (col-shift st modif nil nil))
  ([{:keys [parse-tree buffer]} modif offset-before offset]
    (let [text-before (node-text parse-tree)
          parse-tree (-> buffer
                       (edit-buffer (:offset modif) (:length modif) (:text modif))
                       (buffer-parse-tree 0))
          text (node-text parse-tree)
          offset (or offset (+ (:offset modif) (count (:text modif))))
          offset-before (or offset-before (+ (:offset modif) (:length modif)))
          col (t/col text offset)
          col-before (t/col text-before offset-before)
          delta (- col col-before)
          rloc (parsed-root-loc parse-tree)
          loc (loc-for-offset rloc offset)
          loc (if (or 
                    (= (start-offset loc) offset)
                    (whitespace-end-of-line? text offset)
                    (= :comment (loc-tag loc)))
                loc
                (next-node-loc loc))
          loc (find-loc-to-shift loc)]
      (when loc
        (let [col (- (loc-col loc) delta)]
          (when-not (or (neg? col) (< col col-before))
            (let [shifted-loc (propagate-delta loc col delta)
                  shifted-text (node-text (z/root shifted-loc))
                  diff (t/text-diff text-before shifted-text)]
              ;(println "text before:" text-before)
              ;(println "shifted-text:" shifted-text)
              ;(println "diff:" diff)
              (when-not (empty-diff? diff)
                {:modifs [diff] :offset offset :length 0 :text shifted-text}))))))))
