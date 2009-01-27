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
			      (catch java.net.SocketException e
			        (println "socket exception " (.getMessage e)))
			      (catch Exception e (println "create-server: unexpected exception " (.getMessage e))))
			      (recur)))
			  (recur))))
	nil)
          
(defn socket-repl 
  "starts a repl thread on the iostreams of supplied socket"
  [s]
    (with-open [dis (new java.io.DataInputStream 
                         (new java.io.BufferedInputStream (.getInputStream s)))]
      (with-open [dos (new java.io.DataOutputStream (.getOutputStream s))]
        (let [question-bytes-length (.readInt dis)
              question-bytes (make-array Byte/TYPE question-bytes-length)]
          (.readFully dis question-bytes 0 question-bytes-length) 
          (let [answer (pr-str (load-string (new String question-bytes "UTF-8")))
                answer-bytes (.getBytes answer "UTF-8")]
            (.writeInt dos (alength answer-bytes))
            (.write dos answer-bytes 0 (alength answer-bytes))
            (.flush dos))))))
          
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
   
   
;(remove-ns 'clojuredev.debug.serverrepl)   