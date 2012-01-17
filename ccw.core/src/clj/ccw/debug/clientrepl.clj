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
(ns ccw.debug.clientrepl)

(import '(java.net ServerSocket Socket SocketException)
        '(java.io InputStreamReader OutputStreamWriter)
        '(clojure.lang LineNumberingPushbackReader))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; library code
 
(def *default-repl-port* 8503)

(defn remote-load [s]
  ;(println "remote-load: begin")
  (with-open [client (new Socket "localhost" *default-repl-port*)]
    ;(println "remote-load: opened socket on port " *default-repl-port*)
    (with-open [dis (new java.io.DataInputStream 
                         (new java.io.BufferedInputStream (.getInputStream client)))]
      ;(println "remote-load: opened input and output stream to socket")
      (with-open [dos (new java.io.DataOutputStream (.getOutputStream client))]
        ;(println "remote-load: opened data outputstream to socket")
        (let [s-bytes (.getBytes s "UTF-8")]
          (.writeInt dos (alength s-bytes))
          (.write dos s-bytes 0 (alength s-bytes))
          (.flush dos)
          ;(println "remote-load: question written to output stream : "\" s "\"")
          (let [response-type (.readInt dis) ; 0 = OK, -1 = KO (exception)
                response-bytes-length (.readInt dis)
                response-bytes (make-array Byte/TYPE response-bytes-length)]
            ;(println "remote-load: answer read: nb bytes of the answer:" response-bytes-length)
            (.readFully dis response-bytes 0 response-bytes-length)
            ;(println "remote-load: answer read: answer content:" (new String response-bytes "UTF-8"))
            ;(println "remote-load: end")
            { "response-type" response-type
              "response" (new String response-bytes "UTF-8")}))))))

(defn remote-load-read [s]
  ;(println "remote-load-read: begin")
  (let [result (remote-load s)]
    ;(println "result: " result)
    ;(println "remote-load-read: end")
    { "response-type" (result "response-type")
      "response" (read-string (result "response")) }))

(defn local-load-read [s]
  (load-string s))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defn ns-info []
  (remote-load "(ns-info)"))

(defn ns-sym [s]
  "returns a vector with the namespace in first position and the symbol in second position.
   if there is no namespace, first position will contain \"\"."
  (let [[s n] (-> s (.split "/") reverse)] [(or n "") s]))

(defn qualify-sym [ns s]
  "returns a symbol with ns as a namespace if s does not yet have a namespace.
  ns and s are strings"
  (let [n-s (ns-sym s)]
    (if (= "" (first n-s)) (str ns "/" s) s)))
    
(defn symbol-type [ns-name s-name]
	(try 
	  (let [s             (symbol (qualify-sym ns-name s-name))
	        macro?        #((meta (find-var %)) :macro)]
		  (cond 
		    (special-symbol? (symbol s-name)) "SPECIAL_FORM"
		    (nil? (find-var s)) nil
		    (macro? s)        "MACRO"
		    :else             "FUNCTION"))
    (catch IllegalArgumentException e nil)))

(def clojure-core-namespaces 
	'("clojure.core" "clojure.main" "clojure.set" "clojure.xml" "clojure.zip"
	  "clojure.inspector" "clojure.parallel"))

(defn core-symbol-type [s-name]
  (first (filter identity (map #(symbol-type % s-name) clojure-core-namespaces))))
  
; FUNCTION, MACRO, SPECIAL_FORM, GLOBAL_VAR
(defn clojure-symbol-types [] 
	(let [namespaces (map find-ns clojure-core-namespaces)]
	  (mapcat (fn [sym] [(str sym) (symbol-type sym)]) namespaces)))  ; TODO implement this method!
