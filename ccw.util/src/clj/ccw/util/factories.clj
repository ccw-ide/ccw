(ns ccw.util.factories
  (:import [clojure.lang Reflector]))

(defn handler-factory 
  "Creates an org.eclipse.core.commands.IHandler instance by proxying 
   org.eclipse.core.commands.AbstractHandler and overriding its 
   (.execute this event) method with a call to (handler this event)."
  [{handler "handler"}]
  (let [handler (symbol handler)]
    (require (-> handler namespace symbol))
    (let [handler (find-var handler)]
      (proxy [org.eclipse.core.commands.AbstractHandler]
             []
        (execute [event] (when handler (handler this event)))))))

(defn java-factory 
  "Factory for an Eclipse plugin executable extension which calls the no-arg
   constructor of the java class whose fully qualified name is passed in javaClass"
  [{java-class "javaClass"}]
  (println "I was called, hey!")
  (try
    (let [class (-> (Thread/currentThread) 
                  .getContextClassLoader
                  (.loadClass java-class))]
      (let [i (Reflector/invokeConstructor class (make-array Object 0))]
        (println "instance created: " i)
        i))
    (catch Exception e
      (println "sent exception: " (.getMessage e) "," e)
      (.printStackTrace e))))