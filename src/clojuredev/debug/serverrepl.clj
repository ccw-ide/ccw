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
  [accept-socket port]
  (on-thread 
    #(loop []
			(let [ss (new java.net.ServerSocket port)]
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
              (Integer/valueOf (System/getProperty "clojure.remote.server.port" "8503")))
   
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defn- meta-info [v]
  (reduce (fn [m e] (merge m { (first e) (str (second e)) })) {} (meta v)))

(defn- symbol-info [s]
  (merge { :type "symbol" :name (str s) } (meta-info (find-var s))))
    
(defn- var-info [v]
  (merge { :type "var" :name (str v) } (meta-info v)))

(defn- ns-info [n]
  { :name ((comp str ns-name) n)
    :type "ns"
    :children (apply vector (sort-by :name (map #(var-info (second %)) (ns-interns n)))) })

(defn namespaces-info []
  { :name "namespaces" :type "namespaces"
    :children (apply vector (sort-by :name (map ns-info (all-ns)))) })

(defn code-complete [ns-str prefix only-publics]
  (when-let [ns (find-ns (symbol ns-str))]
    (let [search-fn (if only-publics ns-publics ns-map)]
      (into [] (map (fn [[k v]] [k (str v) (if (var? v) (var-info v) nil)]) (filter #(.startsWith (first %) prefix) (map #(vector (str (key %)) (val %)) (search-fn ns))))))))
   
;(remove-ns 'clojuredev.debug.serverrepl)   