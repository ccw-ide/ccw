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
            :content
            (fn [node children]
              (assoc node :content children))
            root))

(defn split [cs idx]
  (when cs
    [(subvec cs 0 idx) (cs idx) (subvec cs (inc idx))]))

(defn vdown
  "Returns the loc of the child at index idx of the node at this loc, or
  nil if no children"
  {:added "1.0"}
  [loc idx]
    (when (z/branch? loc)
      (let [[node path] loc
            [l c r :as cs] (split (z/children loc) idx)]
        (when cs
          (with-meta [c {:l l
                         :pnodes (if path (conj (:pnodes path) node) [node]) 
                         :ppath path 
                         :r r}] (meta loc))))))

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

(defn up-to-depth
  "finds from the loc the ancestor loc at the given depth."
  [loc depth]
  (let [delta (- (loc-depth loc) depth)]
    (cond 
      (zero? delta) loc
      :else (nth (iterate z/up loc) delta))))

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

(defn leave-loc-for-offset-common
  "returns a zipper location or nil if does not contain the offset"
  [loc offset]
  (if (not (z/branch? loc))
    (if (< offset (count (z/node loc))) loc (root-loc loc))
    (let [[cloc offset] 
            (loop [start (int 0) end (int (count (-> loc z/node :content)))]
              (if (<= end start)
                (if (= start (count (-> loc z/node :content)))
                  ; no loc found (end of text, will return root-loc instead)
                  (let [last-leave (last 
                                     (take-while 
                                       #(and (z/branch? %) (z/children %))
                                       (iterate (comp z/rightmost z/down) 
                                                (z/rightmost loc))))]
                    [last-leave 0])                  
                  
                  [(vdown loc start) (- offset (-> loc z/node :content-cumulative-count (get start)))])
                (let [n (int (+ start (quot (- end start) 2)))
                      n-offset (-> loc z/node :content-cumulative-count (get n))
                      n-node (-> loc z/node :content (get n))
                      n-count (if (string? n-node) (count n-node) (or (:count n-node) 0))] 
                  (cond
                    (< offset n-offset)
                      (recur start (dec n))
                    (< offset (+ n-offset n-count))
                      [(vdown loc n) (- offset n-offset)]
                    :else
                      (recur (inc n) end)))))]
      (if (zero? offset) cloc (recur cloc offset)))))

(defn ^:dynamic leave-for-offset
  [loc offset]
  (if-let [l (leave-loc-for-offset-common loc offset)]
    l
    (root-loc loc)))

(defn ^:dynamic loc-for-offset 
  "returns a zipper location or nil if does not contain the offset"
  [loc offset] 
    (when-let [l (leave-loc-for-offset-common loc offset)]
      (parse-node l)))

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

(defn comment? 
  "Is loc at a comment node?"
  [loc] (and loc (= :comment (:tag (z/node loc)))))

(defn after-comment?
  "Is loc at a node following a comment node?"
  [loc] (comment? (z/left loc)))

(defn whitespace? 
  "Is loc at a :whitespace node?"
  [loc]
  (and loc (= :whitespace (:tag (z/node loc)))))

(defn contains-newline?
  "Does loc text contain at least one \newline char?"
  [loc]
  (.contains (loc-text loc) "\n"))

(defn whitespace-newline?
  "Is loc a newline with withespace AND containing newline char?"
  [loc]
  (and (whitespace? loc) (contains-newline? loc)))

(defn root? 
  "Is loc the root?"
  [loc] (and loc (nil? (z/up loc))))

(defn newline? 
  "Is loc the start of a newline?
   Can be:
   - a :whitespace loc containing an \n
   - a :whitespace loc containing no \n (after a :comment)
   - any other loc starting the line, after a :comment
   This can get tricky, you'll have to consider the 3
   cases in your code, but that's the way things are currently
   implemented, sorry."
  [loc]
  (and loc (or (after-comment? loc)
               (whitespace-newline? loc))))

(defn shift-nl-whitespace 
  "Loc is at a line start. Add delta (may be negative) 
   whitespaces to it."
  [loc delta]
  (if (whitespace? loc)
    (z/replace 
      loc
      (assoc-in (z/node loc)
                [:content]
                [(let [text (loc-text loc)
                       prefix (subs text 0 (inc (.lastIndexOf text "\n")))
                       rest   (subs text (count prefix))]
                   (str prefix (t/adjust-padding rest delta \space)))]))
    (if (pos? delta)
      (z/insert-left 
        loc
        {:tag :whitespace
         :content [(t/repeat delta \space)]})
      loc)))

(defn line-start-col 
  "For loc l, representing a whitespace start of a line, at which col
   does l 'start' (e.g. does not have only whitespaces)?" 
  [l]
  (let [text (loc-text l)]
    (if (.contains text "\n")
      (- (count text)
         (inc (.lastIndexOf text "\n")))
      (count text))))

(defn path-count [loc] 
  (count (z/path loc)))

(defn propagate-delta [loc col delta]
  (if (or (comment? loc) (whitespace-newline? loc))
    [loc :stop]
    (let [depth (path-count loc)
          [loc st] (loop [l loc]
                     (if (> depth (path-count l))
                       [l :continue]
                       (let [[l st] 
                             (cond 
                               (newline? l)
                                 (if (and (whitespace? l)
                                          (> col (line-start-col l)))
                                   [l :stop]
                                   [(shift-nl-whitespace l delta) :continue])
                               :else
                                 [l :continue])]
                         (cond
                           (= :stop st)        [l :stop]
                           (nil? (z/next l))   [l :continue]
                           (z/end? (z/next l)) [l :stop]
                           :else               (recur (z/next l))))))]
      (if (= :stop st)
        [loc :stop]
        (if-let [next-loc (if-let [p (z/up loc)] (z/right p))]
          (recur next-loc
                 (loc-col next-loc) ; FIXME may not work if :cumulated-count is not correct 
                 ; OR MAY RETURN old col
                 delta)
          [loc :stop])))))

(defn next-node-loc
  "Get the loc for the node on the right, or if at the end of the parent,
   on the right of the parent. Skips punct nodes. Return nil if at the far end."
  [loc]
  (if-let [r (z/right loc)]
    (if (punct-loc? r)
      (recur r)
      r)
    (when-let [p (z/up loc)]
      (recur p))))

(defn find-loc-to-shift
  "Starting with loc, find to the right, and to the right of parent node, etc.
   a non-whitespace loc. If a newline is found before, return nil."
  [loc]
  (let [continue-search (fn [loc] (and loc (not (whitespace-newline? loc))))
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
  [{:keys [parse-tree buffer]} modif]
  (let [text-before (node-text parse-tree)
        parse-tree (-> buffer
                     (edit-buffer (:offset modif) (:length modif) (:text modif))
                     (buffer-parse-tree 0))
        text (node-text parse-tree)
        offset (+ (:offset modif) (count (:text modif)))
        offset-before (+ (:offset modif) (:length modif))
        col (t/col text offset)
        delta (- col
                 (t/col text-before offset-before))
        rloc (parsed-root-loc parse-tree)
        loc (loc-for-offset rloc offset)
        loc (find-loc-to-shift loc)]
    (when  loc
      (let [col (- (loc-col loc) delta)
            [shifted-loc _] (propagate-delta loc col delta)
            shifted-text (node-text (z/root shifted-loc))
            loc-diff (t/text-diff text shifted-text)
            diff (update-in 
                   loc-diff
                   [:offset] + (:length modif) (- (count (:text modif))))]
        (when-not (empty-diff? diff)
          {:modifs [diff] :offset offset :length 0})))))
