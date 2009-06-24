;*******************************************************************************
;* Copyright (c) 2009 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Laurent PETIT - initial API and implementation
;*******************************************************************************/
; Totally footprint free embedded evaluation server
; (do not have any namespace / symbol presence)
(ns clojuredev.debug.serverrepl)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; library code

(defn on-thread [f] (.start (new Thread f)))

(defn create-server 
  "creates and returns a server socket on port, will pass the client
   socket to accept-socket on connection" 
  [accept-socket port file-name]
  (on-thread 
    #(loop []
            ;(println "port=" port " ; file-name=" file-name)
			(let [ss (java.net.ServerSocket. (if (= port -1) 0 port))]
			  (when (= port -1)
			    (with-open [fw (java.io.FileWriter. file-name)]
			      (.write fw (str (.getLocalPort ss)))))
			  (loop []
			    (when-not (.isClosed ss)
			      (try 
			        (let [s (.accept ss)]
			          (on-thread (fn [] 
			                       (with-open [s s]
			                         (try
			                           (accept-socket s)
			                           (catch Exception e (println "create-server:accept-socket: unexpected exception " (.getMessage e)) (throw e)))))))
			      (catch java.net.SocketException e (println "socket exception " (.getMessage e)))
			      (catch Exception e (println "create-server: unexpected exception " (.getMessage e))))
			      (recur)))
			  (recur))))
	nil)
          
(defn- push-answer [type answer-sexpr dos]
	(let [answer (pr-str answer-sexpr)
	      answer-bytes (.getBytes answer "UTF-8")]
	  (.writeInt dos type) ; 0 = OK, -1 = KO (exception)
	  (.writeInt dos (alength answer-bytes))
	  (.write dos answer-bytes 0 (alength answer-bytes))
	  (.flush dos)))
  
; currently just [file-name line-number message]
(defn- serialize-exception 
  ([e] (serialize-exception e []))
  ([e v]
    (if-not e
      v
		  (let [stack-traces (.getStackTrace e)
		        first-stack (aget stack-traces 0)
		        file-name (.getFileName first-stack)
		        line-number (.getLineNumber first-stack)
		        message (.getMessage e)]
		    (recur (.getCause e)
		           (conj v { "file-name" file-name 
		                     "line-number" line-number 
		                     "message" message }))))))
          
(defn socket-repl 
  "starts a repl thread on the iostreams of supplied socket"
  [s]
    (with-open [dis (new java.io.DataInputStream 
                         (new java.io.BufferedInputStream (.getInputStream s)))]
      (with-open [dos (new java.io.DataOutputStream (.getOutputStream s))]
        (let [question-bytes-length (.readInt dis)
              question-bytes (make-array Byte/TYPE question-bytes-length)]
          (.readFully dis question-bytes 0 question-bytes-length) 
          (try
            (load-string (new String question-bytes "UTF-8"))
            (push-answer 0 (load-string (new String question-bytes "UTF-8")) dos)
            (catch Exception e (push-answer -1 (serialize-exception e) dos)))))))
          
(create-server socket-repl 
              (Integer/valueOf (System/getProperty "clojure.remote.server.port" "-1"))
              (System/getProperty "clojuredev.debug.serverrepl.file.port"))
   
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defn- meta-info [v]
  (reduce (fn [m e] (merge m { (first e) (str (second e)) })) {} (meta v)))

(defn- symbol-info [s]
  (merge { :type "symbol" :name (str s) } (meta-info (find-var s))))
    
(defn- var-info [v]
  (merge { :type "var" :name (str v) } (meta-info v)))

(defn- ns-info 
  ([n] (ns-info n true))
  ([n insert-symbols?]
    (conj
      { :name ((comp str ns-name) n)
        :type "ns"
        :doc (:doc (meta n)) }
      (when insert-symbols?
        [:children 
         (apply vector (sort-by :name (map #(var-info (second %)) (ns-interns n))))]))))

(defn namespaces-info []
  { :name "namespaces" :type "namespaces"
    :children (apply vector (sort-by :name (map ns-info (all-ns)))) })

; The following function (splitted-match) taken from vimClojure with the permission of the author.
; Please look for vimClojure license for more detail
(defn splitted-match
  "Splits pattern and candidate at the given delimiters and matches
  the parts of the pattern with the parts of the candidate. Match
  means „startsWith“ here."
  [pattern candidate delimiters]
  (if-let [delimiters (seq delimiters)]
    (let [delim           (first delimiters)
          pattern-split   (.split pattern delim)
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
;(remove-ns 'clojuredev.debug.serverrepl)   