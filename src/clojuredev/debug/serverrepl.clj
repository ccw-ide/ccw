; Totally footprint free embedded evaluation server
; (do not have any namespace / symbol presence)
(ns clojuredev.debug.serverrepl)

(defn on-thread [f] (.start (new Thread f)))

(defn create-server 
  "creates and returns a server socket on port, will pass the client
   socket to accept-socket on connection" 
  [accept-socket port]
  (on-thread #(loop []
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
          
(defn repl
  "runs a repl on ins and outs until eof"
  [ins outs]
          (let [old-out *out*]
          (binding [*ns* (create-ns 'user)
                    *warn-on-reflection* false
                    *out* (new java.io.OutputStreamWriter outs)]
            (let [eof (new Object)
                  r (new clojure.lang.LineNumberingPushbackReader (new java.io.InputStreamReader ins))]
              (loop [e (read r false eof)]
                (when-not (= e eof)
                  (let [result (eval e)]
                    (prn result))
                  (flush)
                  (recur (read r false eof))))))))
                  
(defn socket-repl 
  "starts a repl thread on the iostreams of supplied socket"
  [s]
          (repl (. s (getInputStream)) (. s (getOutputStream))))
          
(create-server socket-repl (Integer/valueOf (System/getProperty "clojure.remote.server.port" "8503")))
   
(defn- meta-info [v]
  (reduce #(assoc %1 (key %2) (str (val %2))) {} (meta v)))

(defn- symbol-info [s]
  { :type "symbol"
    :name (str s)
    :meta (meta-info (find-var s)) })
    

(defn- ns-info [n]
  { :name ((comp str ns-name) n)
    :type "ns"
    :children [] })

    
(defn nss-info []
  (let [ns-names (map (comp str ns-name) (all-ns))
        ns-with-symbols (reduce (fn [m name]
                                  (assoc m name (apply vector (map (fn [s] (str s)) (keys (ns-interns (symbol name)))))))
                                {} ns-names)]
    ns-with-symbols))
   
   
;(remove-ns 'clojuredev.debug.serverrepl)   