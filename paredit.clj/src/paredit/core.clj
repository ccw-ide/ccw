(ns paredit.core
  (:use [paredit.parser :exclude [pts]])
  (:use clojure.set)
  (:use [clojure.core.incubator :only [-?>]])
  (:require [paredit.string :as str])
  (:require [paredit.text-utils :as t])
  (:require [clojure.zip :as z])
  (:require [clojure.string :as s])
  (:require [paredit.loc-utils :as l :refer :all]))

#_(set! *warn-on-reflection* true)
;;; adaptable paredit configuration
(def ^String ^:dynamic *newline* "\n")
;;; adaptable paredit configuration

(def ^:dynamic *real-spaces* #{(str \newline) (str \tab) (str \space)})
(def ^:dynamic *extended-spaces* (conj *real-spaces* (str \,)))
(def ^:dynamic *open-brackets* (conj #{"(" "[" "{"} nil)) ; we add nil to the list to also match beginning of text 
(def ^:dynamic *close-brackets* (conj #{")" "]" "}"} nil)) ; we add nil to the list to also match end of text
(def ^:dynamic *form-macro-chars* #{(str \#) (str \~) "~@" (str \') (str \`) (str \@) "^" "#'" "#_" "#!"})
(def ^:dynamic *not-in-code* #{:string :string-body "\"\\" :comment :char :regex :regex-body})

(defn compare-edits
  "Compare disjoint edits. Edits being assumed disjoint or equal, only their midpoints are compared."
  [a b]
  (- (+ (* 4 (:offset a)) (* 2 (:length a)) (if (:before a) 0 1))
    (+ (* 4 (:offset b)) (* 2(:length b)) (if (:before b) 0 1))))

(defn disjoint-edits [edits]
  (for [[before after] (partition-by (juxt :offset :length) (sort compare-edits edits))]
    (if after
      (assoc before :text (str (:text before) (:text after)))
      before)))

(defn update-selection [[offset end-offset] edits]
  ;; only insertion edits (whose length is zero) can broaden a selection
  (let [edits (into (sorted-set-by compare-edits) edits)
        start+ {:offset offset :length 0 :before false}
        end- {:offset end-offset :length 0 :before true}
        before-sel (subseq edits < start+)
        shift (reduce (fn [shift {:keys [text length]}]
                        (+ shift (- (count text) length)))
                0 before-sel)
        offset' (+ offset shift)
        shift (reduce (fn [shift {:keys [text length]}]
                        (+ shift (- (count text) length)))
                shift (subseq edits >= start+ <= end-))
        end-offset' (+ end-offset shift)]
    [offset' end-offset']))

(defmacro with-memoized [func-names & body]
  `(binding [~@(mapcat 
                 (fn [func-name] [func-name `(memoize ~func-name)])
                 func-names)]
     ~@body))

(declare ^:dynamic normalized-selection)
(defmacro with-important-memoized [& body]
  `(with-memoized 
     [start-offset
      end-offset
      loc-text
      loc-col
      loc-for-offset
      leave-for-offset
      loc-containing-offset
      contains-offset?
      normalized-selection
      node-text]
     ~@body))

(defn ^:dynamic normalized-selection
  "makes a syntaxically correct selection, that is the returned nodes are siblings.
   returns a vector of 2 locs.
   If the selection is empty, the first loc will give the start (get it via a call to 'loc-start on it)
   and the second loc will be nil.
   If the selection is not empty, the second loc will give the end (get it via a call to 'loc-end on it).
   Pre-requisites: length >=0, offset >=0. rloc = root loc of the tree"
  [rloc offset length]
  (let [left-leave (parse-leave (leave-for-offset rloc offset false))
        right-leave (parse-leave (leave-for-offset rloc (+ offset length) true))
        right-leave (cond 
                      (root-node-tag? (loc-tag right-leave))
                        (parse-leave (leave-for-offset rloc (dec (+ offset length)))) 
                      (not= (+ offset length) (start-offset right-leave))
                        (parse-node right-leave) 
                      (nil? (seq (previous-leaves right-leave)))
                        (parse-node right-leave)
                      :else
                        (parse-node (first (previous-leaves right-leave))))]
    (if (or
          (= [0 0] [offset length])
          (and 
            (= 0 length)
            (= (start-offset left-leave) offset)))
      [left-leave (when-not (zero? length) right-leave)]
      (let [left-leave (parse-node left-leave)
            right-leave (parse-node right-leave)
            left-depth (loc-depth left-leave)
            right-depth (loc-depth right-leave)
            min-depth (min left-depth right-depth)
            left-leave (nth (iterate z/up left-leave) (- left-depth min-depth))
            right-leave (nth (iterate z/up right-leave) (- right-depth min-depth))]
        (first 
          (filter 
            (fn [[l r]] (= (z/up l) (z/up r))) 
            (iterate 
              (fn [[l r]] [(z/up l) (z/up r)])
              [left-leave right-leave])))))))

(defn- structure-loc? [loc]
  (not (#{:whitespace :comment} (loc-tag loc))))

(defn structural-selection
  ([rloc offset length] (structural-selection rloc offset length structure-loc?))
  ([rloc offset length ok-loc?]
    (if (zero? length)
      (let [loc (leave-for-offset rloc offset false)
            r (parse-node loc)]
        (if (= (start-offset loc) offset)
          (let [l (parse-node (leave-for-offset rloc offset true))
                loc (if (<= (start-offset r) (start-offset l))
                      (if (ok-loc? l) l r)
                      (if (ok-loc? r) r l))]
            [loc loc])
          [r r]))
      (let [[l r] (normalized-selection rloc offset length)]
        (if (or (punct-loc? l) (punct-loc? r))
          (let [p (z/up l)]
            [p p])
          [l r])))))

(defn parsed-in-tags? 
  [parsed tags-set]
  (tags-set (-> parsed :parents peek :tag)))

(defn parse-stopped-in-code?
  ; TODO the current function is not general enough, it just works for the offset
  ; the parse stopped at  
  "true if character at offset offset is in a code
   position, e.g. not in a string, regexp, literal char or comment"
  [parsed]
  (not (parsed-in-tags? parsed *not-in-code*)))

(defn in-code? 
  [loc] 
  (when-let [loc-tag (loc-tag (parse-node loc))]
    (not (*not-in-code* loc-tag))))
  
(defmulti paredit (fn [k & args] k))

(defn insert-balanced
  [[o c] t chars-with-no-space-before chars-with-no-space-after]
  (let [add-pre-space? (not (contains? chars-with-no-space-before 
                                       (t/previous-char-str t 1 #_(count o))))
        add-post-space? (not (contains? chars-with-no-space-after 
                                        (t/next-char-str t)))
        ins-str (str (if add-pre-space? " " "")
                     (str o c)
                     (if add-post-space? " " ""))
        offset-shift (if add-post-space? -2 -1)]
    (-> t (t/insert ins-str) (t/shift-offset offset-shift))))

(declare wrap-with-balanced)

(defn open-balanced
  [parsed [o c] {:keys [^String text offset length] :as t} 
   chars-with-no-space-before chars-with-no-space-after]
  (if (zero? length) 
    (let [offset-loc (-> parsed parsed-root-loc (leave-for-offset offset))]
      (if (in-code? offset-loc)
        (insert-balanced [o c] t chars-with-no-space-before chars-with-no-space-after)
        (-> t (t/insert (str o)))))
    (wrap-with-balanced parsed [o c] t)))

(defn close-balanced
  [parsed [o c] {:keys [^String text offset length] :as t} 
   chars-with-no-space-before chars-with-no-space-after]
    (let [offset-loc (-> parsed parsed-root-loc (loc-for-offset offset true))]
      (if (in-code? offset-loc)
        (let [up-locs (take-while identity (iterate z/up offset-loc))
              match (some #(when (= c (peek (:content (z/node %)))) %) up-locs)]
          (if match
            (let [c-loc (-> match z/down z/rightmost)
                  nb-delete (reduce #(+ %1 (loc-count %2)) 0
                                    (->> (rlefts c-loc)
                                      (take-while #(= :whitespace (loc-tag %)))))
                  t (if (> nb-delete 0)
                      (t/delete t (- (start-offset c-loc) nb-delete) nb-delete)
                      t)] ; z/left because there is the closing node
              (-> t (t/set-offset (- (end-offset match) nb-delete))))
            (if (or (:broken? parsed)
                    (= :net.cgrand.parsley/unfinished (:tag parsed)))
              (-> t (t/insert (str c)))
              t)))
        (-> t (t/insert (str c))))))

(defmethod paredit 
  :paredit-open-round
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized 
    (open-balanced parse-tree ["(" ")"] t 
      (union (conj (into *real-spaces* *open-brackets*) "#") *form-macro-chars*)
      (into *extended-spaces* *close-brackets*))))
    
(defmethod paredit 
  :paredit-open-square
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized (open-balanced parse-tree ["[" "]"] t
    (union (into *real-spaces* *open-brackets*) *form-macro-chars*)
    (into *extended-spaces* *close-brackets*))))
    
(defmethod paredit 
  :paredit-open-curly
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized (open-balanced parse-tree ["{" "}"] t
    (union (conj (into *real-spaces* *open-brackets*) "#") *form-macro-chars*)
    (into *extended-spaces* *close-brackets*))))
    
(defmethod paredit 
  :paredit-close-round
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized (close-balanced parse-tree ["(" ")"] t
    nil nil)))

(defmethod paredit 
  :paredit-close-square
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized (close-balanced parse-tree ["[" "]"] t
    nil nil)))

(defmethod paredit 
  :paredit-close-curly
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized (close-balanced parse-tree ["{" "}"] t
    nil nil)))

(defn- pad-left [{:keys [text offset] :as edit} parse-tree]
  (let [loc (-> parse-tree parsed-root-loc (loc-for-offset offset true))]
    (if (and (= (end-offset loc) offset) (not (when-let [c (some-> parse-tree parsed-root-loc (char-before offset))]
                                                (or (Character/isWhitespace c) (= \# c)))))
      (assoc edit :text (str " " text))
      edit)))

(defn- pad-right [{:keys [text offset] :as edit} parse-tree]
  (let [loc (-> parse-tree parsed-root-loc (loc-for-offset offset false))]
    (if (and (= (start-offset loc) offset) (not (some-> parse-tree parsed-root-loc (char-after offset) Character/isWhitespace)))
      (assoc edit :text (str text " "))
      edit)))

(defmethod paredit ; TODO convert to new style
  :paredit-doublequote
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t}]
  (with-important-memoized 
    (let [offset-loc (-> parse-tree parsed-root-loc (loc-for-offset offset))] 
      (cond
        (in-code? offset-loc)
          (if (zero? length)
            {:selection [offset offset] :edits [(pad-left {:text "\"" :length 0 :offset offset :before true} parse-tree)
                                                (pad-right {:text "\"" :length 0 :offset offset :before false} parse-tree)]}
            (wrap-with-balanced parse-tree ["\"" "\""] t)) ; TODO should quote
        (not (#{:string, :string-body
                :regex :regex-body} (loc-tag offset-loc)))
          {:selection [offset offset] :edits [{:text "\"" :length 0 :offset offset :before true}]}
        (and (= "\\" (t/previous-char-str t)) (not= "\\" (t/previous-char-str t 2)))
          (-> t (t/insert (str \")))
        (= "\"" (t/next-char-str t))
          (t/shift-offset t 1)
        :else
          (-> t (t/insert (str \\ \")))))))

(defmethod paredit 
  :paredit-forward-delete
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
   (if (zero? (count text))
     t
     (with-important-memoized 
       (if parse-tree
         (let [offset-loc (-> parse-tree parsed-root-loc (loc-for-offset offset))
               handled-forms (conj *brackets-tags* :meta)
               in-handled-form (handled-forms (loc-tag offset-loc))
               open-punct-length #(.length ^String (z/node (first (next-leaves offset-loc))))]
           (if-not in-handled-form
             (t/delete t offset 1)
             (cond 
               (= offset (start-offset offset-loc))
                 (if (< 1 (open-punct-length))
                   (t/delete t offset 1)
                   (t/shift-offset t 1))
               (< 0 (- offset (start-offset offset-loc)) (open-punct-length))
                 (t/shift-offset t 1)
               (= offset (dec (end-offset offset-loc)))
                 (if (> (-> offset-loc z/node :content count) 2)
                   (t/shift-offset t 1)
                   (-> t ; delete the form 
                     (t/delete (start-offset offset-loc) (loc-count offset-loc))
                     (t/shift-offset (- (open-punct-length))))))))
         (t/delete t offset 1)))))

(defmethod paredit 
   :paredit-backward-delete
   [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
   (if (zero? (count text))
     t
     (with-important-memoized 
       (if parse-tree
         (let [offset (dec offset)
               offset-loc (-> parse-tree parsed-root-loc (loc-for-offset offset))
               handled-forms *brackets-tags*
               in-handled-form (handled-forms (loc-tag offset-loc))]
           (cond 
             (and in-handled-form (= offset (+ (start-offset offset-loc) (dec (-> offset-loc z/down loc-count)))))
               (if (> (-> offset-loc z/node :content count) 2)
                 (t/shift-offset t -1)
                 (-> t ; delete the form 
                   (t/delete (start-offset offset-loc) (loc-count offset-loc))
                   (t/shift-offset (- (-> offset-loc z/down loc-count)))))
             (and in-handled-form (= offset (dec (end-offset offset-loc))))
               (t/shift-offset t -1)
             :else
               (-> t (t/delete offset 1) (t/shift-offset -1))))
         (-> t (t/delete offset 1) (t/shift-offset -1))))))

(def lisp-forms
  (into #{}
    (mapcat
      (fn [sym] [(str sym) (str "clojure.core/" sym)]) 
      '(let fn binding proxy reify extend extend-protocol extend-type bound-fn 
         if if-not if-let when when-not when-let when-first condp case loop dotimes
         for while do doto try catch locking dosync doseq dorun doall
         -> -?> ->> future ns clojure.core/ns gen-class gen-interface
         as-> cond-> cond->> some-> some->>))))

(defn lisp-form? 
  "Returns logical true if the String probably names a special form or macro var"
  [^String s]
  (let [name (name (symbol s))]
    (or
      (.startsWith s ".")
      (.startsWith name "def")
      (.startsWith name "with")
      (.startsWith name "let")
      (lisp-forms s))))


(defn inline-implementation? 
  "Returns logical true if the loc does not correspond to a function call, but
   rather to a protocol/record inline implementation in defrecord, defprotocol,
   extend-*, etc." 
  [loc]
  (when-let [pcalled (-?> loc z/up z/up z/node paredit.parser/called)]
    (#{"defrecord", "extend-protocol", "extend-type", "proxy",
       "deftype", "reify"} pcalled)))

(defn indent-column 
  "pre-condition: line-offset is already the starting offset of a line"
  [root-loc line-offset force-two-spaces-indent]
  (let [loc (loc-for-offset root-loc (dec line-offset))]
    (loop [loc (z/left loc) seen-loc nil indent 0]
      (cond
        (nil? loc)
          indent
        (punct-loc? loc)
          ; we reached the start of the parent form, indent depending on the form's type
          (if (#{"(" "#("} (loc-text loc))
            (cond
              (nil? seen-loc) 
                (+ (loc-col loc) (loc-count loc) 1)
              (or force-two-spaces-indent (lisp-form? (loc-text (first seen-loc)))
                  (inline-implementation? (first seen-loc)))
                (+ (loc-col loc) (loc-count loc) 1)
              (second seen-loc)
                (loc-col (second seen-loc))
              :else 
                (+ (loc-col loc) (loc-count loc) 1))
            (+ (loc-col loc) (loc-count loc)))
        (= :whitespace (loc-tag loc))
          ; we see a space
          (if (.contains ^String (loc-text loc) "\n")
            (if seen-loc
              (+ indent (dec (-> ^String (loc-text loc) (.substring (.lastIndexOf ^String (loc-text loc) "\n")) .length)))
              (recur (z/left loc) nil 0))
            (recur (z/left loc) seen-loc (+ indent (-> ^String (loc-text loc) .length))))
        :else
          (recur (z/left loc) (conj seen-loc loc) 0)))))

(defn text-selection
  "returns a vector [offset length] from a normalized-selection"
  [nsel]
  (let [[l r] nsel
        offset (start-offset l)
        length (if (nil? r) 0 (- (end-offset r) offset))]
    [offset length]))

(defn locs-to-sel
  "Returns [offset length] from two locs, the second one being optional."
  [l r]
  (let [start (start-offset l)]
    [start (if r (- (end-offset r) start) 0)]))

(defmethod paredit
  :paredit-expand-left
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
    (let [[l r] (structural-selection rloc (dec offset) (inc length) (constantly true))]
      (-> t (assoc :offset (start-offset l))
             (assoc :length (- (end-offset r) (start-offset l))))))))

(defn default-behaviour-sel [parent l r]
  [(start-offset parent) (end-offset parent)])

(defn children-then-punct-sel [parent l r]
  (let [pl (-> parent z/down z/right)
        pr (-> pl z/rightmost z/left)]
    (cond
      (or
        (<= (count (z/children parent)) 2) ; TODO if we had :punct nodes, we could just check
                                           ; if only :punct nodes are present ...
        (and (= l pl) (= r pr)))
        [(start-offset parent) (end-offset parent)]
      :else
        [(start-offset pl) (end-offset pr)])))

(defn- broaden-to-siblings [l r]
  (let [f (fn [best-loc loc]
            (if loc
              (let [node (z/node loc)]
                (if (or (string? node) (= :whitespace (:tag node))) best-loc loc))
              (reduced best-loc)))]
    [(reduce f l (iterate z/left l)) (reduce f r (iterate z/right r))]))

(defn- broaden
  "Broadens this selection by at least one character."
  [l r]
  (let [start (start-offset l)
        end (end-offset r)]
    (loop [l l r r broadened false]
      (cond
        (or (not= start (start-offset l)) (not= end (end-offset r)))
        [l r]
        broadened
        (if-let [p (z/up l)]
          (recur p p false)
          [l r])
        :else
        (let [[l r] (broaden-to-siblings l r)]
          (recur l r true))))))

(defmethod paredit
  :paredit-expand-up
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
    (let [[l r] (structural-selection rloc offset length)]
      (if-not (= [offset length] (locs-to-sel l r))
        (assoc t :offset (start-offset l) 
                 :length (if (nil? r) 0 (- (end-offset r) (start-offset l))))
        (let [[l r] (broaden l r)
              [start-offset length] (locs-to-sel l r)]
          (assoc t :offset start-offset
            :length length))))
    t)))

(defmethod paredit
  :paredit-expand-right
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
    (let [[l r] (structural-selection rloc offset (inc length))]
      (-> t (assoc :offset (start-offset l))
        (assoc :length (- (end-offset r) (start-offset l)))))
    t)))

(defn enforce-structural-selection [{:keys #{parse-tree buffer} :as parse-state} {:keys [offset length] :as t} action]
  (when-let [[l r] (some-> parse-tree (parsed-root-loc true) (structural-selection offset length))]
    (let [[offset' length'] (locs-to-sel l r)]
      (if (and (= offset offset') (= length length'))
        (action l r parse-state t)
        (let [p (or (z/up (parse-node l)) l)]
          {:selection [offset' (+ offset' length')]
           :edits []})))))

(defmethod paredit
  :paredit-raise-sexp
  [cmd parse-state t]
  (with-important-memoized
    (enforce-structural-selection parse-state t
      (fn [l r parse-state t]
        (let [[l' r'] (broaden l r)]
          {:selection [(start-offset l') (end-offset r')]
           :edits [{:offset (start-offset l') :length (- (start-offset l) (start-offset l')) :text ""}
                   {:offset (end-offset r) :length (- (end-offset r') (end-offset r)) :text ""}]}
          ; TODO fix col-shift and reintroduce it
          #_(if-let [new-t (l/col-shift parse-state (-> new-t :modifs first) to-raise-offset replace-offset)]
             (-> new-t
               (assoc-in [:length] (count to-raise-text))
               (assoc-in [:offset] replace-offset))
             new-t))))))

(defmethod paredit
  :paredit-split-sexp
  [cmd {:keys [parse-tree buffer] :as parse-state} {:keys [^String text offset length] :as t}]
  (with-important-memoized
    (if (zero? length)
      (when-let [rloc (-?> parse-tree (parsed-root-loc true))]
        (let [[l r] (normalized-selection rloc offset length)
                  parent (cond
                           (and (#{:comment :string} (loc-tag l)) (> offset (start-offset l))) l ; stay at the same level, and let the code take the correct open/close puncts, e.g. \" \"
                           (and (= :whitespace (loc-tag l)) (= offset (start-offset l)) (= :comment (loc-tag (z/left l)))) (z/left l)
                           :else (if-let [nl (z/up (if (start-punct? l) (parse-node l) (parse-leave l)))] nl (parse-leave l)))
                  [open-punct ^String close-punct] (let [tag (loc-tag parent)]
                                                     (case tag
                                                       :comment [(str "\n" (str/repeat " " (loc-col parent)) ";"
                                                                   (->> parent z/node :content second
                                                                     (re-find #"^;* *"))) ""]
                                                       ((juxt *tag-opening-brackets* *tag-closing-brackets*) tag)))]
              (when close-punct
                (let [[replace-offset replace-length]
                      (if (or (= :comment (loc-tag parent))
                            (and
                              (not= :whitespace (loc-tag l))
                              (or
                                (= :string (loc-tag l))
                                (not (and
                                       (= [offset length] (locs-to-sel l r))
                                       (= offset (start-offset (parse-node l))))))))
                        [offset 0]
                        (let [start (or (some #(when-not (#{:whitespace :comment} (loc-tag %)) (end-offset %)) (previous-leaves l)) offset)
                              end (or (some #(when-not (#{:whitespace :comment} (loc-tag %)) (start-offset %)) (next-leaves l)) offset)]
                          [start (- end start)]))]
                  {:selection [offset offset]
                   :edits [{:offset replace-offset :length 0 :text close-punct :before true}
                           {:offset (+ replace-offset replace-length) :length 0 :text open-punct :before false}]}))))
        (enforce-structural-selection parse-state t
          (fn [l r parse-state t]
            (when-some [[open close] ({:list ["(" ")"] :vector  ["[" "]"] :map ["{" "}"] :set ["#{" "}"]}
                                       (-> l z/up loc-tag))]
              (let [l' (or (last (take-while #(#{:whitespace :comment} (-> % z/node :tag))
                                   (iterate z/left (z/left l)))) l)
                    r' (or (last (take-while #(#{:whitespace :comment} (-> % z/node :tag))
                                   (iterate z/right (z/right r)))) r)]
                {:selection [(start-offset l) (end-offset r)]
                 :edits [(if (punct-loc? (z/left l'))
                           {:offset (start-offset (z/left l')) :length (count open) :text "" :before true}
                           {:offset (start-offset l') :length 0 :text close :before true})
                         (if (punct-loc? (z/right r'))
                           {:offset (end-offset r') :length (count close) :text "" :before false}
                           {:offset (end-offset r') :length 0 :text open :before false})]})))))))

(defmethod paredit
  :paredit-join-sexps
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized 
    (if (not= 0 length)
      t
      (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
        (let [[l _] (normalized-selection rloc offset length)
              lf (first (remove #(= :whitespace (loc-tag %)) (previous-leaves l)))
              rf (first (remove #(= :whitespace (loc-tag %)) (cons l (next-leaves l))))]
          (if (or (nil? lf) (nil? rf) (start-punct? lf) (end-punct? rf))
            t
            (let [ln (parse-node lf)
                  rn (parse-node rf)] 
              (if-not (and
                        (= (loc-tag ln) (loc-tag rn)))
                t
                (let [replace-offset (- (end-offset ln) (if-let [punct ^String (*tag-closing-brackets* (loc-tag ln))] (.length punct) 0))
                      replace-length (- (+ (start-offset rn) (if-let [punct ^String (*tag-closing-brackets* (loc-tag rn))] (.length punct) 0)) replace-offset)
                      replace-text   (if ((conj *atom* :string) (loc-tag ln)) "" " ")
                      new-offset (if (= offset (start-offset rn)) (+ replace-offset (.length replace-text)) replace-offset)]
                  (-> t (assoc-in [:text] (t/str-replace text replace-offset replace-length replace-text))
                    (assoc-in [:offset] new-offset)
                    (update-in [:modifs] conj {:offset replace-offset :length replace-length :text replace-text})))))))
        t))))

(defn wrap-with-balanced
  [parsed [^String o c] {:keys [^String text offset length] :as t}]
  (when-let [rloc (-?> parsed (parsed-root-loc true))]
    (let [[left-leave right-leave] (normalized-selection rloc offset length)]
      (if-not (= [offset length] (locs-to-sel left-leave right-leave))
        (when-not (or (in-code? (loc-containing-offset rloc offset)) ; should rather check if no code node belongs to the selection etc etc
                      (in-code? (loc-containing-offset rloc (+ offset length))))
          {:selection [(+ offset length) (+ offset length)]
           :edits [{:text o :offset offset :length length :before true}]})
        (let [start (start-offset left-leave)
              end (or (-?> right-leave end-offset) (.length text))]
          {:selection [start end]
           :edits [{:text o :offset start :length 0 :before true}
                   {:text c :offset end :length 0 :before false}]})))))

(defmethod paredit
  :paredit-wrap-quote
  [cmd {:keys #{parse-tree buffer}} t]
  (-> t 
    (update-in [:text] t/str-insert (:offset t) \')
    (update-in [:offset] inc)
    (update-in [:modifs] conj {:text "'", :offset (:offset t), :length 0})))

(defmethod paredit
  :paredit-wrap-square
  [cmd {:keys #{parse-tree buffer}} t]
  (with-important-memoized (wrap-with-balanced parse-tree ["[" "]"] t)))

(defmethod paredit
  :paredit-wrap-curly
  [cmd {:keys #{parse-tree buffer}} t]
  (with-important-memoized (wrap-with-balanced parse-tree ["{" "}"] t)))

(defmethod paredit
  :paredit-wrap-round
  [cmd {:keys #{parse-tree buffer}} t]
  (with-important-memoized (wrap-with-balanced parse-tree ["(" ")"] t)))

(defmethod paredit
  :paredit-newline
  [cmd {:keys #{parse-tree buffer}} {:keys [text offset length] :as t} & {:keys [force-two-spaces-indent]}]
  ; no call to with-important-memoized because we almost immediately delegate to :paredit-indent-line
  (let [text (-> text (t/str-remove offset length) (t/str-insert offset "\n"))
        r (paredit :paredit-indent-line 
                   (let [buffer (edit-buffer buffer offset length "\n")
                         parse-tree (buffer-parse-tree buffer :intermediate-id)] 
                     {:parse-tree parse-tree, :buffer buffer})
                   {:text text 
                    :offset (inc offset) 
                    :length 0 
                    :modifs [{:text *newline* :offset offset :length length}]}
                   :force-two-spaces-indent force-two-spaces-indent)]
    (if (-?> r :modifs count (= 2))
      (let [m1 (get-in r [:modifs 0])
            m2 (get-in r [:modifs 1])
            r  (assoc-in r [:modifs] [{:text (str (:text m1) (:text m2)) :offset offset :length (+ (:length m1) (:length m2))}])
            r  (assoc-in r [:offset] (+ (.length ^String (get-in r [:modifs 0 :text])) offset))]
        r)
      r)))

(defn paredit-indent-line
  "common fn: does not call with-important-memoized"
  [{:keys #{parse-tree buffer}} 
   {:keys [^String text offset length] :as t}
   & {:keys [force-two-spaces-indent]}]
  (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
    (let [line-start (t/line-start text offset)
          line-stop (t/line-stop text offset)
          loc (loc-for-offset rloc line-start)]
      (if (and (#{:string, :string-body} (loc-tag loc)) (< (start-offset loc) line-start))
        t
        (let [indent (indent-column rloc line-start force-two-spaces-indent)
              cur-indent-col (- 
                               (loop [o line-start]
                                 (if (>= o (.length text)) 
                                   o
                                   (let [c (.charAt text o)]
                                     (cond
                                       (#{\return \newline} c) o ; test CR/LF before .isWhitespace !
                                       (Character/isWhitespace c) (recur (inc o))
                                       (= \, c) (recur (inc o))
                                       :else o))))
                               line-start)
              to-add (- indent cur-indent-col)]
          (cond
            (zero? to-add) t
            :else (let [t (update-in t [:modifs] conj {:text (str/repeat " " indent) :offset line-start :length cur-indent-col})
                        t (update-in t [:text] t/str-replace line-start cur-indent-col (str/repeat " " indent))]
                    (cond 
                      (>= offset (+ line-start cur-indent-col)) 
                        (update-in t [:offset] + to-add)
                      (<= offset (+ line-start indent))
                        t
                      :else
                        (update-in t [:offset] + (max to-add (- line-start 
                                                                offset)))))))))
    t))

(defmethod paredit
  :paredit-indent-line
  [cmd 
   {:keys #{parse-tree buffer} :as parse-tree-buffer} 
   {:keys [^String text offset length] :as t}
   & options]
  (with-important-memoized 
    (apply paredit-indent-line parse-tree-buffer t options)))

(defn- nb-lines 
  "How many new lines between start-offset and stop-offset?"
  [s start-offset stop-offset]
  (count (re-seq #"\n" (subs s start-offset stop-offset))))

(defn result->input
  "Given the return value of a paredit call of the form 
     {:modifs {..} :text .. :length .. :offset ..}, 
   and the buffer value before the call,
   return the map {:parse-tree new-parse-tree, :buffer new-buffer}
   Right now, only works if :modifs has a single element."
  [t-result buffer]
  (let [{:keys [text offset length] :or {offset 0 length 0 text ""}} (first (:modifs t-result))
        new-buffer (edit-buffer buffer offset length text)
        new-parse-tree (buffer-parse-tree new-buffer 0)]
    {:parse-tree new-parse-tree
     :buffer new-buffer}))

(defn indent-line 
  "Input: {:parse-tree parse-tree :buffer buffer}, t
   Output: [[new-parse-tree new-buffer] new-t]"
  [{:keys [pt, buffer] :as pt-buffer} t & {:keys [force-two-spaces-indent]}]
  (let [t-result (paredit-indent-line
                   pt-buffer
                   t
                   :force-two-spaces-indent force-two-spaces-indent)
        new-pt-buffer (result->input t-result buffer)]
    [new-pt-buffer t-result]))

(defmethod paredit
  :paredit-indent-selection
  [cmd 
   {:keys #{parse-tree buffer} :as parse-tree-buffer} 
   {:keys [^String text offset length] :as t}
   & {:keys [force-two-spaces-indent]}]
  (with-important-memoized 
    (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
      (let [nb-lines (nb-lines text offset (+ offset length))
            [pt-b-0 tr-0] (indent-line 
                            parse-tree-buffer 
                            (assoc t :length 0) 
                            :force-two-spaces-indent force-two-spaces-indent)]
        (if (zero? nb-lines)
          (assoc tr-0 :length 0)
          (let [indent-next (fn [[pt-b-0 tr-0]]
                              (let [next-line-offset (t/next-line-start (:text tr-0) (:offset tr-0))
                                    [pt-b-1 tr-1] (indent-line
                                                    pt-b-0
                                                    (-> tr-0
                                                      (dissoc :modifs)
                                                      (assoc :offset next-line-offset))
                                                    :force-two-spaces-indent force-two-spaces-indent)]
                                [pt-b-1 (assoc tr-1 :length 0)])) 
                [pt-b-n tr-n] (nth (iterate indent-next [pt-b-0 tr-0]) nb-lines)]
            (assoc tr-n
                   :offset (:offset tr-0)
                   :length 0
                   :modifs [(t/text-diff text (:text tr-n))]))))
      t)))
 
(defmethod paredit
  :paredit-indent
  [cmd parse-tree-buffer t & {:keys [force-two-spaces-indent]}]
  (paredit (if (zero? (:length t))
             :paredit-indent-line
             :paredit-indent-selection)
           parse-tree-buffer
           t
           :force-two-spaces-indent force-two-spaces-indent))

(defn update-lines
  "line-updater-factory-fn is a f which takes the lines to transform as its input"
  [{:keys [^String text offset length] :as t}
   lines-updater]
  (let [start offset
        stop (+ start length)
        lines-start (t/line-start text start)
        lines-stop (if (and (pos? length) 
                            (= stop (t/line-start text stop))) 
                     stop
                     (t/line-stop text stop)) ; do not select the last line 
                                              ; if nothing selected in it
        lines-text (.substring text lines-start lines-stop)
        lines (t/full-lines lines-text)
        new-lines (lines-updater lines)
        new-lines-text (apply str new-lines)
        new-text (t/str-replace text lines-start (.length lines-text) new-lines-text)
        shifts (map #(- (count %1) (count %2)) new-lines lines)
        [offset-shift length-shift]
          (if (zero? length)
            [(if (= lines-start start) (max 0 (first shifts)) (first shifts)), 0]
            (if (= lines-start start)
              [0 (apply + shifts)]
              [(first shifts) (apply + (rest shifts))]))]
    {:text new-text
     :offset (+ offset offset-shift)
     :length (+ length length-shift)
     :modifs [{:text new-lines-text :offset lines-start :length (.length lines-text)}]}))

(def ^:dynamic lines-comment-prefix ";")

(defn inc-line-comment [line] (str lines-comment-prefix line))
(defn inc-lines-comments [lines] (map inc-line-comment lines))

(defn dec-line-comment [line] (.substring line (count lines-comment-prefix)))
(defn dec-lines-comments [lines] (map dec-line-comment lines))

(defn line-start-comment? [line] (.startsWith line lines-comment-prefix))

(defn line-toggle-comment [line]
  ((if (line-start-comment? line) dec-line-comment inc-line-comment)
    line))

(defn line-toggle-comments [lines]
  (let [cnt (count lines)
        commented-lines (count (filter line-start-comment? lines))]
    (if (<= commented-lines (- cnt commented-lines))
      (map #(if (line-start-comment? %) % (inc-line-comment %)) lines)
      (map #(if (line-start-comment? %) (dec-line-comment %) %) lines))))

(defmethod paredit
  :paredit-inc-line-comment
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (update-lines t inc-lines-comments))

(defmethod paredit
  :paredit-dec-line-comment
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (update-lines t dec-lines-comments))

(defmethod paredit
  :paredit-toggle-line-comment
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (update-lines t line-toggle-comments))

(defn escape-string-content
  "Meant to escape text to be pasted into a String literal.
   Escapes content of s: adds backslashes before each found double quote or 
   backslash."
  [s]
  (when s 
    (s/escape s {\" "\\\"", \\ "\\\\"})))

(defn inside-string-literal? 
  "In the source code parse-tree is the representation of, is offset positioned
   inside a String literal (that is within a String literal double quotes)?"
  [parse-tree offset]
  (let [offset-loc (-> parse-tree parsed-root-loc (loc-containing-offset offset))] 
      (#{:string, :string-body} (loc-tag offset-loc))))

(defn smart-paste 
  "Takes a parse-tree, an editor state, a text to paste.
   If the cursor is inside a String literal, then double-quotes and backslashes
   found inside to-paste are escaped with a backslash, so that the resulting
   text to paste is properly escaped for being inserted inside a String literal.
   If be-smart? is false, will copy to-paste as is, not doing any smart thing."
  [{:keys #{parse-tree buffer}}
   {:keys [^String text offset length] :as t}
   to-paste
   be-smart?]
  (let [to-paste (if (and be-smart? (inside-string-literal? parse-tree offset))
                   (escape-string-content to-paste)
                   to-paste)
        new-text (t/str-replace text offset length to-paste)
        new-offset (+ offset (.length to-paste))]
    {:text new-text
     :offset new-offset
     :length 0
     :modifs [{:offset offset :length length :text to-paste}]}))

(defmethod paredit
  :paredit-splice-sexp
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized
    (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
      (let [[l r] (normalized-selection rloc offset length)
            ul (if (*tag-closing-brackets* (loc-tag l))  ;; if on closing punct, must select parent differently
                     (loc-for-offset rloc offset)
                     (if-let [nl (z/up (parse-node l))]
                       nl l))
            parent (parse-node ul)
            parent-so (start-offset parent)
            parent-eo (end-offset parent)
            replace-offset parent-so
            text-to-replace (.substring text parent-so parent-eo)
            replace-length (.length text-to-replace)

            [inner-so inner-eo] (let [pl (-> parent z/down z/right)
                                      pr (-> pl z/rightmost z/left)]
                                  [(start-offset pl) (end-offset pr)]) ;; all-children-but-punct

            new-offset (- offset (- inner-so parent-so))
            replace-text (.substring text inner-so inner-eo)

            ret (-> t
                    (assoc-in [:text] (t/str-replace text replace-offset replace-length replace-text))
                    (assoc-in [:offset] new-offset)
                    (update-in [:modifs] conj {:offset parent-so :length replace-length :text replace-text}))]
        ret)
      t)))

;;;
;;; paredit-forward-slurp-sexp
;;;
;;; recurse up until the current node is not the rightmost node under parent
;;; move the sibling to this nodes right into this node as last element (removing it from parent)
;;; get new string from parent (re-indenting according to lisp rules)
;;;

(defn- take-to-non-punct
  ([dir-fn loc]
     (take-to-non-punct dir-fn loc '()))
  ([dir-fn loc ret]
     (if (or (punct-loc? loc) (gspaces (loc-tag loc)))
       (recur dir-fn (dir-fn loc) (cons loc ret))
       (cons loc ret))))

(def non-puncts-to-left
  (partial take-to-non-punct z/left))

(def non-puncts-to-right
  (partial take-to-non-punct z/right))


(defn- up-to-right-sibling
  [loc]
  (if (= loc (-> loc z/rightmost non-puncts-to-left first))
    (when-let [u (z/up loc)]
      (recur u))
    loc))

(defn- up-to-left-sibling
  [loc]
  (if (= loc (-> loc z/leftmost non-puncts-to-right first))
    (when-let [u (z/up loc)]
      (recur u))
    loc))

(defmethod paredit
  :paredit-forward-slurp-sexp
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized
    (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
      (let [[l r] (normalized-selection rloc offset length)
            starting-loc (if (*tag-closing-brackets* (loc-tag l))  ;; if on closing punct, must select parent differently
                           (loc-for-offset rloc offset)
                           (if-let [nl (z/up (parse-node l))]
                             nl l))]
        (if-let [slurper (up-to-right-sibling starting-loc)]
          (let [slurpees (non-puncts-to-right (z/right slurper))
                slurp-text (apply str (map loc-text (reverse slurpees)))
                slurp-to-loc (first slurpees)
                slurp-to-eo (end-offset slurp-to-loc)
                slurper-node (parse-node slurper)
                slurper-so (start-offset slurper-node)
                slurper-eo (end-offset slurper-node)
                replace-offset (dec slurper-eo)
                text-to-replace (.substring text replace-offset slurp-to-eo)
                close-punct (*tag-closing-brackets* (loc-tag slurper))
                replace-text (str slurp-text close-punct)
                replace-length (.length text-to-replace)
                ;; TODO potentially need to re-indent the slurped-in text (if multi-line)
                ret (-> t
                        (assoc-in [:text] (t/str-replace text replace-offset replace-length replace-text))
                        (update-in [:modifs] conj {:offset replace-offset
                                                   :length replace-length
                                                   :text replace-text}))]
            ret)
          t))
      t)))

(defmethod paredit
  :paredit-backward-slurp-sexp
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized
    (if-let [rloc (-?> parse-tree (parsed-root-loc true))]
      (let [[l r] (normalized-selection rloc offset length)
            starting-loc (if (*tag-closing-brackets* (loc-tag l))  ;; if on closing punct, must select parent differently
                           (loc-for-offset rloc offset)
                           (if-let [nl (z/up (parse-node l))]
                             nl l))]
        (if-let [slurper (up-to-left-sibling starting-loc)]
          (let [slurpees (non-puncts-to-left (z/left slurper))
                slurp-text (apply str (map loc-text slurpees))
                slurp-to-loc (first slurpees)
                slurp-to-so (start-offset slurp-to-loc)
                slurper-node (parse-node slurper)
                slurper-so (start-offset slurper-node)
                slurper-eo (end-offset slurper-node)
                replace-offset slurp-to-so
                replace-to-offset (inc slurper-so)
                text-to-replace (.substring text slurp-to-so replace-to-offset)
                open-punct (*tag-opening-brackets* (loc-tag slurper))
                replace-text (str open-punct slurp-text)
                replace-length (.length text-to-replace)
                ;; TODO potentially need to re-indent the slurped-in text (if multi-line)
                ret (-> t
                        (assoc-in [:text] (t/str-replace text replace-offset replace-length replace-text))
                        (update-in [:modifs] conj {:offset replace-offset
                                                   :length replace-length
                                                   :text replace-text}))]
            ret)
          t))
      t)))

(defmethod paredit
  :paredit-forward-barf-sexp
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  (with-important-memoized
    (let [rloc (-?> parse-tree (parsed-root-loc true))
          [l r] (when rloc
                  (normalized-selection rloc offset length))]
      (if (and rloc l (in-code? l))
        (let [starting-loc (if (*tag-closing-brackets* (loc-tag l))  ;; if on closing punct, must select parent differently
                             (loc-for-offset rloc offset)
                             (if-let [nl (z/up (parse-node l))]
                               nl l))
              ;; TODO what does this do with single elem lists?
              rightmost (-> starting-loc z/down z/rightmost z/left non-puncts-to-left)
              more-non-puncts (-> (first rightmost) z/left non-puncts-to-left rest)
              barfees (concat more-non-puncts rightmost)
              barf-text (apply str (map loc-text barfees))
              barf-start-loc (first barfees)
              barf-so (start-offset barf-start-loc)
              barf-eo (end-offset starting-loc)
              text-to-replace (.substring text barf-so barf-eo)
              close-punct (*tag-closing-brackets* (loc-tag starting-loc))
              replace-text (str close-punct barf-text)
              replace-length (.length text-to-replace)
              replace-offset barf-so
              ret (-> t
                      (assoc-in [:text] (t/str-replace text replace-offset replace-length replace-text))
                      (update-in [:modifs] conj {:offset replace-offset
                                                 :length replace-length
                                                 :text replace-text}))]
          ret)
        t))))

(defmethod paredit
  :paredit-backward-barf-sexp
  [cmd {:keys #{parse-tree buffer}} {:keys [^String text offset length] :as t}]
  ;; (with-important-memoized)
  (let [rloc (-?> parse-tree (parsed-root-loc true))
        [l r] (when rloc
                (normalized-selection rloc offset length))]
    (if (and rloc l (in-code? l))
      (let [starting-loc (if (*tag-closing-brackets* (loc-tag l))  ;; if on closing punct, must select parent differently
                               (loc-for-offset rloc offset)
                               (if-let [nl (z/up (parse-node l))]
                                 nl l))
            lefts (-> starting-loc z/down z/leftmost z/right non-puncts-to-right)
            leftmost (reverse lefts)
            more-non-puncts (-> (first lefts) z/right non-puncts-to-right rest reverse)
            barfees (concat leftmost more-non-puncts)
            barf-text (apply str (map loc-text barfees))
            barf-end-loc (first (reverse barfees))
            barf-so (start-offset starting-loc)
            barf-eo (end-offset barf-end-loc)
            text-to-replace (.substring text barf-so barf-eo)
            open-punct (*tag-opening-brackets* (loc-tag starting-loc))
            replace-text (str barf-text open-punct)
            replace-length (.length text-to-replace)
            replace-offset barf-so
            ret (-> t
                    (assoc-in [:text] (t/str-replace text replace-offset replace-length replace-text))
                    (update-in [:modifs] conj {:offset replace-offset
                                               :length replace-length
                                               :text replace-text}))]
        ret)
      t)))
