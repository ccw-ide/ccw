(ns ^{:doc
      "This 'text region damager' is based on the hypothesis that a clojure file, even a big one, is made of a lot of not so big
       top level forms. So, wrt this hypothesis, its goal is to determine, given the previous state, and the modification made to it,
       which top level forms are to be re-colorized.

       Tentative explanation of the algorithm:
       A modification event adresses a start offset, the length of the text that is removed (may be 0 in case of pure 'insertion modification'),
       and the text to insert (may be the empty string in case of pure 'deletion modification'.
       - In a first step, we determine, for the previous state, which top level forms include the start offset and the text to be removed
       - Then we \"extend\" the text region starting with the first character of the first including top level form, and ending with the last character of the last including top level form, by adding to it the length of the inserted text ...
       - ... and then we recompute, for the new state, which top level forms include this text region
       - we return the text region which includes all the found top level forms

       TODO: code needs more refactoring (could be more DRY)
      "}
     ccw.editors.clojure.ClojureTopLevelFormsDamagerImpl
  (:use [paredit.utils :as utils])
  (:import [org.eclipse.jface.text IRegion ITypedRegion DocumentEvent Region
                                   IDocument]
           [ccw.editors.clojure ClojureTopLevelFormsDamager IClojureEditor])
  (:require [ccw.editors.clojure.editor-support :as editor])
  (:require [paredit.parser :as p]))

#_(set! *warn-on-reflection* true)

(defn state-val [^ClojureTopLevelFormsDamager this] (-> this .state deref))

(defn ^IClojureEditor editor [^ClojureTopLevelFormsDamager this]
  (:editor (state-val this)))

(defn init
  [editor] (ref {:editor editor 
                 :document nil}))

(defn setDocument [^ClojureTopLevelFormsDamager this document]
  (dosync (alter (.state this) assoc :document document)))

(defn parse-tree-get [parse-tree idx]
  #_(println "parse-tree-get[idx:" idx ", parse-tree:" parse-tree "]")
  (try (let [offset ((:content-cumulative-count parse-tree) idx)
             length (:count ((:content parse-tree) idx))]
         [offset (+ offset length)])
    (catch Exception e (println "parse-tree-get: " idx) (throw e))))

(defn parse-tree-count [parse-tree] (count (:content parse-tree)))

(defn parse-tree-content-range [parse-tree text-offset text-length]
  #_(println (str "parse-tree-content-range [text-offset text-length]:" [text-offset text-length]))
  #_(println "parse-tree:" parse-tree)
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

(defn getDamageRegion 
  "Creates a damaged region by merging the regions of the top level forms (tlfs)
   (so children of the parse tree root node) which contain the event changes"
  [this
   ^ITypedRegion partition
   ^DocumentEvent event
   documentPartitioningChanged]
  #_(println (str "getDamageRegion[offset: " 
                (.getOffset event)
                ", replace-length:"
                (.getLength event)
                ", length: " 
                (.length (.getText event)) "]"))
  (if  (.isForceRepair (editor this))
    (Region. 0 (-> this editor .getDocument .get .length))
    (let [previous-parse-tree (-> this editor .getPreviousParseTree)
        parse-tree (-> this editor .getParseState (editor/getParseTree))
        [start-offset 
         stop-offset] (if previous-parse-tree
                        (do
                          #_(println "using previous-parse-tree")
                          (let  [[start-index stop-index] (parse-tree-content-range 
                                                            previous-parse-tree
                                                            (.getOffset event)   ; "#_(a)" et suppression de #
                                                            ; "#_((a)(b))" et suppression de #_(
                                                            (.getLength event))]
                            #_(println (str "old [start-index stop-index]:" [start-index stop-index]) )
                            (if (and start-index stop-index)
                              (do
                                [((:content-cumulative-count previous-parse-tree) start-index)
                                 (+
                                   ((:content-cumulative-count previous-parse-tree) start-index)
                                   (reduce + (map :count (subvec (:content previous-parse-tree) start-index (inc stop-index)))))])
                              [0 (+ (-> this state-val ^IDocument (:document) .get .length) (.getLength event) (- (.length ^String (.getText event))))])))
                        (do #_(println "using default values")
                          [0 0]))
        #__            #_(println (str "[start-offset stop-offset]:" [start-offset stop-offset]))]
    (let [[start-index 
           stop-index] (parse-tree-content-range 
                         parse-tree
                         start-offset   ; "#_(a)" et suppression de #
                         ; "#_((a)(b))" et suppression de #_(
                         (+ stop-offset (- start-offset) (.length ^String (.getText event)) (- (.getLength event))))]
      #_(println (str "[start-index stop-index]=" [start-index stop-index]))
      (if (and start-index stop-index)
        (let [start-offset ((:content-cumulative-count parse-tree) start-index)
              stop-offset  (reduce + (map :count (subvec (:content parse-tree) start-index (inc stop-index))))]
          #_(println (str "final computed damage region: start-offset=" start-offset ", stop-offset=" stop-offset))
          (Region. start-offset stop-offset)) ; TODO suboptimal, we could go to content-cumulative count of ...
        (do
          #_(println "damaged region: 0, 0")
          (Region. 0 0))))))) 

(defn getTokensSeq 
  "Given a damaged region created by getDamageRegion, finds back the start index in the
   parse-tree's root children, and creates a tokens seq starting from that"
  [parse-tree offset length]
  (let [[start-index
         stop-index] (parse-tree-content-range
                       parse-tree
                       offset
                       length)]
    (let [s (concat (mapcat #((:abstract-node %) p/tokens-view) (subvec (:content parse-tree) start-index (inc stop-index))) 
                    (list {:token-type :eof :token-length 0}) ;; from paredit.parser/token
                    )]
      s)))