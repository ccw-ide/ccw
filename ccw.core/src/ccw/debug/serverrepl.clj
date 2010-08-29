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
      (let [stack-traces (.getStackTrace e)
            first-stack (aget stack-traces 0)
            file-name (.getFileName first-stack)
            line-number (.getLineNumber first-stack)
            message (.getMessage e)]
        (recur (.getCause e)
          (conj v {"file-name" file-name
                   "line-number" line-number
                   "message" message}))))))

(defmacro with-exception-serialization
  [& body]
  `(try
     ~@body
     (catch Exception e#
       {"response-type" -1
        "response" (serialize-exception e#)})))

; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defn- meta-info [v]
  (reduce (fn [m e] (merge m {(first e) (str (second e))})) {} (meta v)))

(defn- symbol-info [s]
  (merge {:type "symbol" :name (str s)} (meta-info (find-var s))))

(defn- var-info [v]
  (merge {:type "var" :name (str v)} (meta-info v)))

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

(defn- matching-ns
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