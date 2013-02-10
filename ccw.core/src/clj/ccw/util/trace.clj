(ns ccw.util.trace
  (:require [clojure.java.io :as io])
  (:import [java.util Properties]))

(defmacro def-trace-options-keys [bundle]
  `(let [bundle# ~bundle]
     (def ~'symbolic-name (.getSymbolicName bundle#))
     (def ~'trace-options
       (with-open [io# (-> bundle#
                         (.getEntry "/.options")
                         io/input-stream)]
         (into {} (doto (Properties.)
                    (.load io#)))))
     (def ~'trace-options-keys (set (keys ~'trace-options)))))

(defn trace-option-str [trace-option]
  (if (keyword? trace-option) 
    (.substring (str trace-option) 1)
    trace-option))

(defmacro mk-trace-macros
  "Install all necessary macros in the calling namespace for leveraging the
   Eclipse Tracing API.
   bundle-call is the code necessary to get the bundle for which tracing support
   must be added.
   get-tracer-call is the code necessary to get an instance of ccw.util.ITracer.
   This means an initialization still needs to be done in the bundle Activator.

   The macros that are installed wrap the org.eclipse.osgi.service.debug.DebugTrace
   utility methods:

   (trace trace-option message)
   (trace trace-option message throwable)
   (trace-dump-stack trace-option)
   (trace-entry trace-option & method-args)
   (trace-exit trace-option & result-value)
  "
  [bundle-call get-tracer-call]
  (let [caller-ns (name (ns-name *ns*))
        caller-tracer (symbol caller-ns "tracer")
        caller-tracer-call (symbol caller-ns "tracer-call")
        caller-trace (symbol caller-ns "trace")]
    `(do
       (def-trace-options-keys ~bundle-call)
       
       (defn ~'^ccw.util.ITracer tracer [] ~get-tracer-call)
       
       (defmacro ~'tracer-call [trace-option# & body#]
         (let [trace-option# (trace-option-str trace-option#)]
           (when-not (~'trace-options-keys (str ~'symbolic-name "/" trace-option#))
             (throw (RuntimeException. 
                      (str "Compilation error: call to ccw.trace/trace"
                           " with non existent trace-option: " trace-option#))))
           `(when (.isEnabled (~'~caller-tracer) ~trace-option#)
              ~@body#)))
       
       (defmacro ~'trace 
         ([trace-option# string#]
           `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                   (.trace (~'~caller-tracer) 
                                     (trace-option-str ~trace-option#)
                                     (into-array Object [~string#]))))
         ([trace-option# string# throwable#]
           `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                   (.trace (~'~caller-tracer) 
                                     (trace-option-str ~trace-option#)
                                     ~throwable#
                                     (into-array Object [~string#])))))
       
       (defmacro ~'trace-dump-stack 
         ([trace-option#]
           `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                   (.traceDumpStack (~'~caller-tracer) 
                                     (trace-option-str ~trace-option#)))))
       
       (defmacro ~'trace-entry
         ([trace-option# & method-args#]
           (cond
             (nil? (seq method-args#))
               `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                       (. (~'~caller-tracer) 
                                         ~'~'traceEntry
                                         (trace-option-str ~trace-option#)))
             :else
               `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                       (. (~'~caller-tracer) 
                                         ~'~'traceEntry
                                         (trace-option-str ~trace-option#)
                                         (into-array Object [~@method-args#]))))))
       
       (defmacro ~'trace-exit
         ([trace-option# & result#]
           (if (seq result#)
             `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                     (. (~'~caller-tracer) 
                                       ~'~'traceExit
                                       (trace-option-str ~trace-option#)
                                       ~(first result#)))
             `(~'~caller-tracer-call ~(trace-option-str trace-option#)
                                     (. (~'~caller-tracer) 
                                       ~'~'traceExit
                                       (trace-option-str ~trace-option#))))))
       
       (defmacro ~'format [trace-option# format-string# & format-args#]
         `(~'~caller-trace ~(trace-option-str trace-option#) (format ~format-string# ~@format-args#))))))