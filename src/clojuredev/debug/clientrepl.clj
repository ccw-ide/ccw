(ns clojuredev.debug.clientrepl)

(import '(java.net ServerSocket Socket SocketException)
        '(java.io InputStreamReader OutputStreamWriter)
        '(clojure.lang LineNumberingPushbackReader))
 
(def *default-repl-port* 8328)

(defn invoke-fn [s-expr]
  (with-open [client (new Socket "localhost" *default-repl-port*)]
    (let [rdr (new LineNumberingPushbackReader 
               (new InputStreamReader (.getInputStream client)))
          wtr (new OutputStreamWriter (.getOutputStream client))]
      (binding [*out* wtr
                *flush-on-newline* true]
        (prn s-expr))
      (read rdr))))

(defn invoke-str [str]
  (with-open [client (new Socket "localhost" *default-repl-port*)]
    (let [rdr (new LineNumberingPushbackReader 
               (new InputStreamReader (.getInputStream client)))
          wtr (new OutputStreamWriter (.getOutputStream client))]
      (binding [*out* wtr
                *flush-on-newline* true]
        (println str) 
        (flush))
      (read rdr))))

(defmacro invoke [& s-expr]
  `(invoke-fn '(do ~@s-expr)))
  

; list all ns : (invoke (map ns-name (all-ns)))
; list all symbols of a ns : (invoke (str (ns-interns 'testns)))
; list all public symbols of a ns : (invoke (str (ns-publics 'testns)))  
; get doc : (invoke (let [out (new java.io.StringWriter)] (binding [*out* out] (doc clojure.set/rename-keys) (.toString out))))

  