(ns ccw.editors.clojure.ClojureTopLevelFormsDamager
  (:use [paredit.utils :as utils])
  (:import [org.eclipse.jface.text IRegion ITypedRegion DocumentEvent Region]
           )
  (:gen-class
    :implements [org.eclipse.jface.text.presentation.IPresentationDamager]
    :constructors {[Object] []}
    :methods [^{:static true} [getTokensSeq [Object Object Object] clojure.lang.ISeq]]
    :init init 
    :state state))

#_(set! *warn-on-reflection* true)

(defn state-val [this] (-> this .state deref))

(defn -init
  [editor] [[] (ref {:editor editor :document nil})])

(defn -setDocument [this document]
  (dosync (alter (.state this) assoc :document document)))

(defn parse-tree-get [parse-tree idx]
  #_(println "parse-tree-get[idx:" idx ", parse-tree:" parse-tree "]")
  (try (let [offset ((:content-cumulative-count parse-tree) idx)
             length (:count ((:content parse-tree) idx))]
         [offset (+ offset length)])
    (catch Exception e (println "parse-tree-get: " idx) (throw e))))

(defn parse-tree-count [parse-tree] (count (:content parse-tree)))

(defn parse-tree-content-range [parse-tree text-offset text-length]
  ;(println "parse-tree:" parse-tree)
  (let [start-idx (bin-search [parse-tree-get parse-tree-count]
                              parse-tree 
                              (partial range-contains-in-ex
                                       text-offset))
        stop-idx  (bin-search [parse-tree-get parse-tree-count]
                              parse-tree 
                              (partial range-contains-ex-in 
                                       (+ text-offset 
                                          text-length)))]
    [start-idx stop-idx]))

(defn -getDamageRegion 
  "Creates a damaged region by merging the regions of the top level forms (tlfs)
   (so children of the parse tree root node) which contain the event changes"
  [this
   ^ITypedRegion partition
   ^DocumentEvent event
   documentPartitioningChanged]
  (println (str "getDamageRegion[offset: " (.getOffset event) ", length: " (.length (.getText event))))
  (let [parse-tree (-> this state-val :editor .getParseTree)
        [start-index 
         stop-index] (parse-tree-content-range 
                       parse-tree
                       (.getOffset event)
                       (.length ^String (.getText event)))
        ;stop-index (or stop-index (dec (count (:content parse-tree))))
        ]
    #_(println "    start-index:" start-index)
    #_(println "    stop-index:" stop-index)
    (if (and start-index stop-index)
      (do 
        #_(println (str "    offset:" ((:content-cumulative-count parse-tree) start-index)
                      ", length:"
                      (reduce + (map :count (subvec (:content parse-tree) start-index (inc stop-index))))))
        (Region. ((:content-cumulative-count parse-tree) start-index)
                 (reduce + (map :count (subvec (:content parse-tree) start-index (inc stop-index))))))
      (Region. 0 0)))
  #_partition) 

(defn -getTokensSeq 
  "Given a damaged region created by getDamageRegion, finds back the start index in the
   parse-tree's root children, and creates a tokens seq starting from that"
  [parse-tree offset length]
  (let [[start-index
         stop-index] (parse-tree-content-range
                       parse-tree
                       offset
                       length)]
    #_(println (str "getTokensSeq: offset=" offset ", length=" length ", start-index=" start-index))
    (let [s (concat (mapcat #((:abstract-node %) paredit.parser/tokens-view) (subvec (:content parse-tree) start-index (inc stop-index))) 
                    (list {:token-type :eof :token-length 0}) ;; from paredit.parser/token
                    )]
      ;(println "seq:" s)
      #_(println "start of the seq: " (take 5 s))
      #_(println "end of the seq:" (last s))
      s)))