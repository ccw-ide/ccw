; Totally footprint free embedded evaluation server
; (do not have any namespace / symbol presence)
(let [on-thread 
       (fn [f] (.start (new Thread f)))
      create-server 
        (fn [accept-socket port]
        ;"creates and returns a server socket on port, will pass the client
        ;socket to accept-socket on connection" 
          ;(println "create-server: begin")
          (on-thread #(loop []
          	            ;(println "1")
                        (let [ss (new java.net.ServerSocket port)]
                          ;(println "2")
                          ;(println "begin accepting connections on " ss)
                          (loop []
                            ;(println "3")
                            (when-not (.isClosed ss)
                              ;(println "4")
                              (try 
                                ;(println "5")
                                (let [s (.accept ss)]
                                  ;(println "6")
                                  (on-thread (fn [] 
                                               (with-open [s s]
                                                 ;(println "7")
                                                 (try
                                                   ;(println "8")
                                                   (accept-socket s)
                                                   ;(println "9")
                                                 (catch Exception e (println "create-server:accept-socket: unexpected exception " (.getMessage e)) (throw e)))))))
                              (catch java.net.SocketException e
                                (println "socket exception " (.getMessage e)))
                              (catch Exception e (println "create-server: unexpected exception " (.getMessage e))))
                              ;(println "10")
                              ;(println "ss " ss " closed, will loop and create a new one")
                              (recur)))
                          (recur))))
            ;(println "create-server: end")
            nil)
      repl
        (fn [ins outs]
        ;"runs a repl on ins and outs until eof"
          ;(println "11")
          (let [old-out *out*]
          ;(println "12")
          (binding [*ns* (create-ns 'user)
                    *warn-on-reflection* false
                    *out* (new java.io.OutputStreamWriter outs)]
            ;(println "13")
            (let [eof (new Object)
                  r (new clojure.lang.LineNumberingPushbackReader (new java.io.InputStreamReader ins))]
              ;(println "14")
              (loop [e (read r false eof)]
                ;(println "15")
                (when-not (= e eof)
                  ;(println "16")
                  ;(binding [*out* old-out] (prn e)) 
                  (let [result (eval e)]
                    ;(println "17")
                    (binding [*out* old-out] (prn result))
                    ;(println "18")
                    (prn result))
                    ;(println "19"))
                  ;(println "20")
                  (flush)
                  ;(println "21")
                  (recur (read r false eof))))))))
      socket-repl 
        (fn [s]
        ;"starts a repl thread on the iostreams of supplied socket"
          (repl (. s (getInputStream)) (. s (getOutputStream))))]
   (create-server socket-repl (Integer/valueOf (System/getProperty "clojure.remote.server.port" "8503"))))