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
  (with-open [client (new Socket "localhost" *default-repl-port*)]
    (with-open [dis (new java.io.DataInputStream 
                         (new java.io.BufferedInputStream (.getInputStream client)))]
      (with-open [dos (new java.io.DataOutputStream (.getOutputStream client))]
        (let [s-bytes (.getBytes s "UTF-8")]
          (.writeInt dos (alength s-bytes))
          (.write dos s-bytes 0 (alength s-bytes))
          (.flush dos)
          (let [response-bytes-length (.readInt dis)
                response-bytes (make-array Byte/TYPE response-bytes-length)]
            (.readFully dis response-bytes 0 response-bytes-length)
            (new String response-bytes "UTF-8")))))))

(defn remote-load-read [s]
  (read-string (remote-load s)))

(defn local-load-read [s]
  (load-string s))
  
(defn local-load [s]
  (str (local-load-read s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; support code  

(defn ns-info []
  (remote-load "(ns-info)"))