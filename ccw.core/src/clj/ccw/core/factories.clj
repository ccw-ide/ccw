(ns ccw.core.factories
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

(defn make-factory
  "Creates a factory function for creating instances of the given java
   class name using the constructor with given argument types.
   The types of the arguments must be specified in a symbol sequence,
   which must be emtpy when the no arg constructor is to be used.
   The fn produced uses type hints to enable compiler/jit
   optimizations, at the expense of making this factory method slower
   so it's best to store its result in a var.
   A Bundle instance can optionally be passed for loading the class.
   Example:
     (def int-factory (make-factory \"Integer\" ['String]))
     (int-factory \"42\")
   See http://stackoverflow.com/a/3752276 for original inspiration."
  ([classname types]
    (let [args (map #(with-meta (symbol (str "x" %2)) {:tag %1}) types (range)) ]
      (eval `(fn [~@args] (new ~(symbol classname) ~@args)))))
  ([bundle classname types]
    (let [zclass (.. bundle (loadClass classname))
          constructor (.getConstructor zclass (into-array Class (map resolve types)))]
      (fn [& args] (.newInstance constructor (into-array Object args))))))