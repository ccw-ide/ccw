; *******************************************************************************
; * Copyright (c) 2009 Laurent PETIT.
; * All rights reserved. This program and the accompanying materials
; * are made available under the terms of the Eclipse Public License v1.0
; * which accompanies this distribution, and is available at
; * http://www.eclipse.org/legal/epl-v10.html
; *
; * Contributors: 
; *    Laurent PETIT - initial API and implementation
; *    Tuomas KARKKAINEN - find-symbol
; *******************************************************************************/
(ns ccw.debug.serverrepl)

; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; library code

; currently just [file-name line-number message]
(defn serialize-exception
  [e]
  (loop [e e
         v []]
    (if-not e
      v
      (let [stack-traces (.getStackTrace e)]
        (recur (.getCause e)
               (conj v (if (> (alength stack-traces) 0)
                         (let [first-stack (aget stack-traces 0)
                               file-name (.getFileName first-stack)
                               line-number (.getLineNumber first-stack)
                               message (.getMessage e)]
                           {"file-name" file-name
                            "line-number" line-number
                            "message" message})
                         {"file-name" "<none>"
                          "line-number" 0
                          "message" "<none>"})))))))

(defmacro with-exception-serialization
  [& body]
  `(try
     ~@body
     (catch Exception e#
       {"response-type" -1
        "response" (serialize-exception e#)})))

; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defprotocol Serializable
  "Protocol for preparing objects before they are sent over the wire"
  (prepare-serialize [this]))

(extend-protocol Serializable
  nil
    (prepare-serialize [this] nil)
  Object
    (prepare-serialize [this] this)
  clojure.lang.LazySeq
    (prepare-serialize [this] (seq this)))

(defn serialize [o]
  (str (prepare-serialize o)))

(defn meta-info [v]
  (reduce (fn [m e] (merge m {(first e) (serialize (second e))})) {} (meta v)))

(defn symbol-info [s]
  (merge {:type "symbol" :name (serialize s)} (meta-info (find-var s))))

(defn var-info [v]
  (merge {:type "var" :name (serialize v)} (meta-info v)))

(defn- ns-info
  ([n] (ns-info n true))
  ([n insert-symbols?]
    (conj
      {:name ((comp str ns-name) n)
       :type "ns"
       :doc (:doc (meta n))}
      (when insert-symbols?
        [:children
         (apply vector (sort-by :name (map #(var-info (second %)) (ns-interns n))))]))))

(defn namespaces-info []
  {:name "namespaces" :type "namespaces"
   :children (apply vector (sort-by :name (map ns-info (all-ns))))})

; The following function (splitted-match) taken from vimClojure with the permission of the author.
; Please look for vimClojure license for more detail
(defn splitted-match
  "Splits pattern and candidate at the given delimiters and matches
  the parts of the pattern with the parts of the candidate. Match
  means \"startsWith\" here."
  [pattern candidate delimiters]
  (if-let [delimiters (seq delimiters)]
    (let [delim (first delimiters)
          pattern-split (.split pattern delim)
          candidate-split (.split candidate delim)]
      (and (<= (count pattern-split) (count candidate-split))
        (reduce (fn [a b] (and a b)) (map (fn [a b] (splitted-match a b (rest delimiters)))
                                       pattern-split
                                       candidate-split))))
    (.startsWith candidate pattern)))

(defn matching-ns
  "seq of namespaces which match the prefix
  clojure.co matches clojure.core, ...
  c.c also matches clojure.core, ..."
  [prefix]
  (filter #(splitted-match prefix (str %) ["\\."]) (all-ns)))

(defn code-complete [ns-str prefix only-publics]
  (when-let [nss (matching-ns ns-str)]
    (let [search-fn (if only-publics ns-publics ns-map)
          ns-symbols (fn [ns] (search-fn ns))
          symbols (mapcat ns-symbols nss)]
      (into [] (map (fn [[k v]] [k (str v) (if (var? v) (var-info v) nil)])
                 (filter #(or (.startsWith (first %) prefix)
                            (splitted-match prefix (first %) ["-"]))
                   (map (fn [n] [(str (key n)) (val n)])
                     symbols)))))))

(defn code-complete-ns [prefix]
  (let [result-ns (matching-ns prefix)]
    (into [] (map (fn [ns] [(str ns) (str ns) (ns-info ns)]) result-ns))))

(defn imported-class
  "returns the symbol name corresponding to the java type name
  passed as a parameter if it is imported in the namespace,
  or nil if no corresponding class imported"
  [ns-name type-name]
  (when-let [found-type (ffirst (filter #(= type-name (str (first %)))
                                  (ns-imports (find-ns (symbol ns-name)))))]
    (str found-type)))
; (remove-ns 'ccw.debug.serverrepl)   

(defn find-symbol [s current-ns qualified-ns]
  (let [a ((ns-aliases (symbol current-ns)) (symbol qualified-ns))]
    (map (fn [[k v]] (str v)) (select-keys (meta
                                             (cond
                                               (= "null" qualified-ns) ((symbol s) (ns-map (the-ns (symbol current-ns))))
                                               a (ns-resolve a (symbol s))
                                               :else (resolve (symbol qualified-ns s))))
                                [:ns :name :line :file]))))

(defn starts-with-filter
  "Filter completions starting with prefix"
  [^String completion prefix]
  (when (.startsWith completion prefix)
    (repeat (count prefix) 0)))

(defn contains-filter 
  "Filter completions containing prefix"
  [^String completion ^String prefix]
  (when (.contains completion prefix)
    (let [start (.indexOf completion prefix)]
      (cons start (repeat (dec (count prefix)) 0)))))

(defn textmate-filter 
  "Filter completions containing"
  ([^String completion prefix] (textmate-filter completion prefix []))
  ([^String completion ^String prefix pos]
    (if-not (.isEmpty prefix)
      (let [c (.charAt prefix 0)
            i (.indexOf completion (str c))]
        (when-not (neg? i)
          (recur (.substring completion (inc i))
                 (.substring prefix 1)
                 (conj pos i))))
      pos)))

(defn textmate-distance
  "Distance between completion and prefix is the number of chars
   separating the first match and the last match"
  [completion prefix] 
  (apply + (rest (textmate-filter completion prefix))))

(require '[clojure.string :as s])
(defn decompose-omni 
  "Decomposes s (a String) into a vector
   of vectors: first by splitting at dots,
   then at hyphens."
  [s]
  (->> (s/split s #"\.")
    (map #(s/split % #"-"))))

(defn index-of-distance
  "Return the 'distance' of x from s,
   or nil if irrelevant." [^String x ^String s]
  (let [i (.indexOf x s)]
    (when (not= -1 i) i)))

(defn compose-comparators
  "Create a comparator made of comparators. When called for x & y, 
   applies first comparator to x and y. If x equals y, applies next
   comparator, etc."
  [& comparators]
  (letfn [(chain [[c & tail] x y]
            (let [r (c x y)]
              (if (and (zero? r) tail)
                (recur tail x y)
                r)))]
         (fn [x y] (chain comparators x y))))

(defn length-comparator
  [x y]
  (- (count x) (count y)))

(defn distance-comparator
  [s distance-fn if-same-distances-comparator
   & [if-nil-distances-comparator]]
  (fn [x y]
    (let [idxx (distance-fn x s)
          idxy (distance-fn y s)]
      (cond
        (and idxx idxy)
          (if (= idxx idxy)
            (if-same-distances-comparator x y)
            (- idxx idxy))
        (and idxx (not idxy)) -1
        (and idxy (not idxx)) 1
        :else
        ((or if-nil-distances-comparator if-same-distances-comparator) x y)))))

;; TODO instead of this monolithic piece of code
;; see if we can instead compose comparators
(defn textmate-comparator
  "Construct a comparator which will base its comparison first
   on starts-with
   then on contains
   then on omni-completion (<- TODO) with starts-with
   then on omni-completion (<- TODO) with contains
   then on omni-completion (<- TODO) with textmate-distance
   then on textmate-distance (number of chars separating the first 
   and last matching chars)."
  [^String s]
  (distance-comparator
    s
    index-of-distance
    (compose-comparators length-comparator compare)
    (distance-comparator
      s
      textmate-distance
      (compose-comparators length-comparator compare))))


;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;
(ns ccw.complete
  (:require [clojure.main])
  (:import [java.util.jar JarFile] [java.io File])
  (:require [clojure.string :as s]))

(set! *warn-on-reflection* true)
;; Code adapted from swank-clojure (http://github.com/jochu/swank-clojure)

(defn names 
  "Return the String names of the elements of c"
  [c] (map (comp name first) c))

(defn namespaces
  "Returns a list of potential namespace completions for a given namespace.
   Return a seq of [name ns] keys. name the symbol naming the symbol, ns the namespace object."
  [ns]
  (concat (map (fn [ns] [(ns-name ns) ns]) (all-ns)) (into [] (ns-aliases ns))))

(defn namespaces-names
  [ns]
  (names (namespaces ns)))

(defn ns-public-vars
  [ns]
  (ns-publics ns))

(defn ns-public-vars-names
  "Returns a list of potential public var name completions for a given namespace"
  [ns]
  (names (keys (ns-public-vars ns))))

(defn ns-vars
  [ns]
  (for [[sym val] (ns-map ns) :when (var? val)]
    [sym val]))

(defn ns-vars-names
  "Returns a list of all potential var name completions for a given namespace"
  [ns]
  (names (ns-vars ns)))

(defn ns-classes
  "Returns a list of potential class name completions for a given namespace"
  [ns]
  (into [] (ns-imports ns)))

(defn ns-classes-names
  "Returns a list of potential class name completions for a given namespace"
  [ns]
  (names (ns-classes ns)))

;; should we consider adding catch finally to the list (even if technically
;; they are not special forms)?
(def special-forms
  (map #(vector % %) '[def if do let quote var fn loop recur throw try monitor-enter monitor-exit dot new set!]))

(defn- static? [^java.lang.reflect.Member member]
  (java.lang.reflect.Modifier/isStatic (.getModifiers member)))

;;shouldn't that be (not (static?)) instead of (static?) ?
(defn ns-java-methods
  [ns]
  (for [^java.lang.Class class (vals (ns-imports ns)) 
        method (.getMethods class) :when (static? method)]
    [(symbol (str "." (.getName ^java.lang.reflect.Method method))) method]))

(defn ns-java-methods-names
  "Returns a list of potential java method name completions for a given namespace"
  [ns]
  (names (ns-java-methods ns)))

(defn static-members
  [^java.lang.Class class]
  (for [member (concat (.getMethods class) (.getDeclaredFields class)) :when (static? member)]
    [(symbol (str (.getName (.getDeclaringClass ^java.lang.reflect.Member member)) "/" (.getName ^java.lang.reflect.Member member)))
       member]))

(defn static-members-names
  "Returns a list of potential static members for a given class"
  [^java.lang.Class class]
  (names (static-members class)))

(defn path-files [^String path]
  (cond (.endsWith path "/*")
        (for [^java.io.File jar (.listFiles (File. path)) :when (.endsWith (.getName jar) ".jar")
              file (path-files (.getPath jar))]
          file)

        (.endsWith path ".jar")
        (try (for [^java.util.jar.JarEntry entry (enumeration-seq (.entries (JarFile. path)))]
               (.getName entry))
             (catch Exception e))

        :else
        (for [^java.io.File file (file-seq (File. path))]
          (.replace (.getPath file) path ""))))

(def classfiles
  (for [prop ["sun.boot.class.path" "java.ext.dirs" "java.class.path"]
        path (.split (System/getProperty prop) File/pathSeparator)
        ^String file (path-files path) 
        :when (and (.endsWith file ".class") (not (.contains file "__")))]
    file))

(defn- classname [file]
  (.. ^String file (replace File/separator ".") (replace ".class" "")))

(def top-level-classes
  (future
    (doall
     (for [file classfiles :when (re-find #"^[^\$]+\.class" file)]
       (let [classname (classname file)]
         [(symbol classname) classname])))))

(def nested-classes
  (future
    (doall
     (for [file classfiles :when (re-find #"^[^\$]+(\$[^\d]\w*)+\.class" file)]
       (let [classname (classname file)]
         [(symbol classname) classname])))))

(defn resolve-class [sym]
  (try (let [val (resolve sym)]
         (when (class? val) val))
       (catch Exception e
         (when (not= ClassNotFoundException
                     (class (clojure.main/repl-exception e)))
           (throw e)))))

(defn prefix-kind
  "Return :scoped, :class or :var if /, . or nothing found in the prefix."
  [^String prefix]
  (cond (.contains prefix "/")   :scoped
        (.startsWith prefix ".") :interop
        (.contains prefix ".")   :class
        :else                    :var))

;; Let potential-completions return result be more structured:
;; { :namespaces, :static-members, :vars, :instance-members :java-methods, :classes,
;;   :special-forms }
(defmulti potential-completions
  (fn [^String prefix ns] (prefix-kind prefix)))

(defmethod potential-completions :scoped
  [^String prefix ns]
  (let [scope (symbol (first (.split prefix "/")))]
    (if-let [class (resolve-class scope)]
      {:static-members (static-members class)}
      (when-let [ns (or (find-ns scope) (scope (ns-aliases ns)))]
        {:vars (map (fn [[s m]] [(symbol (str scope "/" (name s))) m]) (ns-public-vars ns))}))))

(defmethod potential-completions :class
  [^String prefix ns]
  {:namespaces (namespaces ns)
   :classes    (if (.contains prefix "$")
                 @nested-classes
                 @top-level-classes)
   })

(defmethod potential-completions :interop
  [_ ns]
  {:java-methods (ns-java-methods ns)})

(defmethod potential-completions :var
  [_ ns]
  {:special-forms special-forms
   :namespaces (namespaces ns)
   :vars (ns-vars ns)
   :classes (ns-classes ns)})

(defmulti filter-completion
  (fn [match-symbol ^String prefix filter-fn] (prefix-kind prefix)))

(defmethod filter-completion :scoped
  [match-symbol ^String prefix filter-fn]
  (let [[scope prefix] (.split prefix "/")
        scope-length (inc (count scope))
        [h & t :as f] (filter-fn match-symbol (or prefix ""))]
    (if h
      (cons (+ h scope-length) t)
      f)))

(defmethod filter-completion :var
  [match-symbol ^String prefix filter-fn]
  (filter-fn match-symbol (or prefix "")))

(defmethod filter-completion :class
  [match-symbol ^String prefix filter-fn]
  (filter-fn match-symbol (or prefix "")))

(defmethod filter-completion :interop
  [match-symbol ^String prefix filter-fn]
  (filter-fn match-symbol (or prefix "")))

(defn completions*
  "Return a sequence of matching completions given a prefix string 
   and an optional current namespace."
  ([prefix] (completions* prefix *ns*))
  ([prefix ns & {:keys #{filter} 
                 :or    {filter ccw.debug.serverrepl/starts-with-filter}}]
     (for [[kind completions] (potential-completions prefix ns)
           [match-symbol match-object :as completion] completions
           :let [f (filter-completion (name match-symbol) prefix filter)]
           :when f]
       {:kind kind
        :completion match-symbol
        :match match-object
        :filter f})))

(defn completions
  "Return a sequence of matching completions given a prefix string 
   and an optional current namespace."
  ([prefix] (completions prefix *ns*))
  ([prefix ns limit
    & {:keys #{filter comparator renderer} 
       :or    {filter ccw.debug.serverrepl/starts-with-filter
               comparator compare
               renderer :completion}}]
    (map renderer
         (take limit
               (sort-by
                 (comp name :completion)
                 comparator
                 (completions* prefix ns :filter filter))))))

(defn ccw-renderer 
  "ccw renderer adds metadata, and transforms data into serializables"
  [completion]
  (-> completion
    (assoc :metadata 
           (ccw.debug.serverrepl/meta-info (:match completion)))
    (update-in [:completion] str)
    (update-in [:match] str)))

(defn ccw-completions
  ([prefix] (ccw-completions prefix *ns* 50))
  ([prefix ns limit]
    (let [completions (completions* prefix ns 
                                    :filter ccw.debug.serverrepl/textmate-filter)]
      (map ccw-renderer
           (take limit
                 (sort-by
                   (comp name :completion)
                   (if (<= (count completions) limit)
                     (ccw.debug.serverrepl/textmate-comparator prefix)
                     (ccw.debug.serverrepl/distance-comparator
                       prefix
                       ccw.debug.serverrepl/index-of-distance
                       compare
                       compare))
                   completions))))))

;; To filter with "textmate-like" logic, and also sort with
;; "textmate-like" logic, use like this: 
(comment
  (let [prefix "run"] 
    (completions prefix *ns*
                 :comparator (ccw.debug.serverrepl/textmate-comparator prefix)
                 :filter ccw.debug.serverrepl/textmate-filter)))
