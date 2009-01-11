(ns clojuredev.clientrepl)

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
        (print str))
      (read rdr))))

(defmacro invoke [& s-expr]
  `(invoke-fn '(do ~@s-expr)))