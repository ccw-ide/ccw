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
(ns clojuredev.debug.clientrepl)

(import '(java.net ServerSocket Socket SocketException)
        '(java.io InputStreamReader OutputStreamWriter)
        '(clojure.lang LineNumberingPushbackReader))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; library code
 
(def *default-repl-port* 8503)

(defn remote-load [s]
  (println "remote-load: begin")
  (with-open [client (new Socket "localhost" *default-repl-port*)]
    (println "remote-load: opened socket on port " *default-repl-port*)
    (with-open [dis (new java.io.DataInputStream 
                         (new java.io.BufferedInputStream (.getInputStream client)))]
      (println "remote-load: opened input and output stream to socket")
      (with-open [dos (new java.io.DataOutputStream (.getOutputStream client))]
        (println "remote-load: opened data outputstream to socket")
        (let [s-bytes (.getBytes s "UTF-8")]
          (.writeInt dos (alength s-bytes))
          (.write dos s-bytes 0 (alength s-bytes))
          (.flush dos)
          (println "remote-load: question written to output stream : "\" s "\"")
          (let [response-type (do (println "remote-load: before response readInt") (.readInt dis)) ; 0 = OK, -1 = KO (exception)
                response-bytes-length (do (println "remote-load: before response content read") (.readInt dis))
                response-bytes (make-array Byte/TYPE response-bytes-length)]
            (println "remote-load: answer read: nb bytes of the answer:" response-bytes-length)
            (.readFully dis response-bytes 0 response-bytes-length)
            (println "remote-load: answer read: answer content:" (new String response-bytes "UTF-8"))
            (println "remote-load: end")
            { "response-type" response-type
              "response" (new String response-bytes "UTF-8")}))))))

(defn remote-load-read [s]
  (println "remote-load-read: begin")
  (let [result (remote-load s)]
    (println "result: " result)
    (println "remote-load-read: end")
    { "response-type" (result "response-type")
      "response" (read-string (result "response")) }))

(defn local-load-read [s]
  (load-string s))
  
(defn local-load [s]
  (str (local-load-read s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defn ns-info []
  (remote-load "(ns-info)"))