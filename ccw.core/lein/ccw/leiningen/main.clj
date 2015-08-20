(ns ccw.leiningen.main
  "Provides the main function to call when launching a generic leiningen process.
   Function -main will wrap the call to leiningen.core.main/-main.
   Also, Loading this namespace has side effects on Leiningen namespaces, installing
   'hooks (not necessarily via robert.hook) even before leiningen.core.main/-main
   has been called."
  (:require [leiningen.core.main]
            [leiningen.core.eval]
            [leiningen.compile]))

(def ^:dynamic *in-prep* false)

(alter-var-root
  #'leiningen.core.eval/run-prep-tasks
  (fn [old-fn]
    (fn [project & rest]
      (binding [*in-prep* true]
        ;; removing :injections during prep-tasks because
        ;; it can only cause harm (e.g. start user code launching
        ;; futures - thus preventing the compilation task to finish, etc.)
        (let [project (dissoc project :injections)]
          (apply old-fn project rest))))))


(def original-eval-in-subprocess
  (get-method leiningen.core.eval/eval-in :subprocess))

(defn start-socket-server 
  "Start a socker server on a separate thread, to maintain a connection
   with a child process.
   Return the port the socket server is listening to."
  []
  (let [p (promise)]
    (future 
      (let [socket (java.net.ServerSocket. 0)]
        (deliver p (.getLocalPort socket))
        (.accept socket)))
    @p))

(def lost-parent-socket-conn-errno 7331)

(defn socket-client-form 
  "Return a form with code to inject in the subprocess jvm. The code does the 
   following: create socket client on a separate thread, which will connect to
   the leiningen process' server socket, call read (blocking), and finally
   shutdown the JVM when an exception is sent or the read returns."
  [port]
  ;; we do not use a future because a future can prevent the process to stop
  ;; since it uses agent threadpool which does not use daemon threads
  `(doto (Thread.
           (reify Runnable
             (run [this]
               (let [socket# (java.net.Socket. "127.0.0.1" ~port)
                     in# (.getInputStream socket#)]
                 (try 
                   (.read in#)
                   (finally 
                     (println "Lost parent connection" ~port)
                     (System/exit ~lost-parent-socket-conn-errno)))))))
     (.setDaemon true)
     .start))

(defmethod leiningen.core.eval/eval-in :subprocess 
  [project form]
  (if *in-prep* ;; prep tasks (compilation, etc. are finished)
    (original-eval-in-subprocess project form)
    (let [port (start-socket-server)
          enhanced-form (list 'do (socket-client-form port) form)]
      (original-eval-in-subprocess project enhanced-form))))

(defn -main [& raw-args]
  (apply leiningen.core.main/-main raw-args))
