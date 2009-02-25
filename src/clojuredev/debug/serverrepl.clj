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
  (println "create-server begin")
  (on-thread 
    #(loop []
      (println "create-server: main loop begin")
			(let [ss (new java.net.ServerSocket port)]
			  (loop []
			    (println "create-server: serving loop begin")
			    (when-not (.isClosed ss)
			      (try 
			        (let [s (.accept ss)]
			          (on-thread (fn [] 
			                       (with-open [s s]
			                         (try
			                           (accept-socket s)
			                         (catch Exception e (println "create-server:accept-socket: unexpected exception " (.getMessage e)) (throw e)))))))
			      (catch java.net.SocketException e
			        (println "socket exception " (.getMessage e)))
			      (catch Exception e (println "create-server: unexpected exception " (.getMessage e))))
			      (recur)))
			  (recur))))
    (println "create-server end")
	nil)
          
(defn- push-answer [type answer-sexpr dos]
  (println "push-answer: begin")
	(let [answer (pr-str answer-sexpr)
	      answer-bytes (.getBytes answer "UTF-8")]
	  (println "push-answer begin: must write\"" answer "\"")
	  (.writeInt dos type) ; 0 = OK, -1 = KO (exception)
	  (println "push-answer: type written - " type)
	  (.writeInt dos (alength answer-bytes))
	  (println "push-answer: length written - " (alength answer-bytes))
	  (.write dos answer-bytes 0 (alength answer-bytes))
	  (println "push-answer: answer written - \" " (pr-str answer-sexpr))
	  (.flush dos)
	  (println "push-answer: output flushed"))
	(println "push-answer: end"))
  
; currently just [file-name line-number message]
(defn- serialize-exception 
  ([e] (serialize-exception e []))
  ([e v]
    (println "serialize-exception: begin")
    (if-not e
      (do (println "serialize-exception: end") v)
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
    (println "socket-repl: begin") 
    (with-open [dis (new java.io.DataInputStream 
                         (new java.io.BufferedInputStream (.getInputStream s)))]
      (println "socket-repl: created dis")
      (with-open [dos (new java.io.DataOutputStream (.getOutputStream s))]
        (println "socket-repl: created dos")
        (let [question-bytes-length (do (println "waiting for question length") (.readInt dis))
              question-bytes (do (println "question length: " question-bytes-length ". waiting for question text") (make-array Byte/TYPE question-bytes-length))]
          (println "socket-repl: question-bytes read: " question-bytes)
          (.readFully dis question-bytes 0 question-bytes-length) 
          (println "socket-repl: question-bytes read by readFully")
          (try
            (println "socket-repl: before push-answer - '" (new String question-bytes "UTF-8") "'")
            (println "before load-string")
            (load-string (new String question-bytes "UTF-8"))
            (println "after load-string")
            (push-answer 0 (load-string (new String question-bytes "UTF-8")) dos)
            (println "push-answer successfully called")
            (catch Exception e (println "exception catched in socket repl") (.printStackTrace e) (push-answer -1 (serialize-exception e) dos))))))
    (println "socket-repl: end"))
          
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
    :children (apply vector (map #(var-info (second %)) (ns-interns n))) })

(defn namespaces-info []
  { :name "namespaces" :type "namespaces"
    :children (apply vector (map ns-info (all-ns))) })

(defn code-complete [ns-str prefix]
  (when-let [ns (find-ns (symbol ns-str))]
    (into [] (map (fn [[k v]] [k (str v) (if (var? v) (var-info v) nil)]) (filter #(.startsWith (first %) prefix) (map #(vector (str (key %)) (val %)) (ns-map ns)))))))
   
;(remove-ns 'clojuredev.debug.serverrepl)   