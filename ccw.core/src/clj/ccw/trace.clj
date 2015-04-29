(ns ccw.trace
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
  (str "/" (if (keyword? trace-option)
             (.substring (str trace-option) 1)
             trace-option)))
       

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
       
       (defmacro ~'tracer-call [trace-option-string# & body#]
         (when-not (~'trace-options-keys (str ~'symbolic-name trace-option-string#))
           (throw (RuntimeException. 
                   (str "Compilation error: call to ccw.trace/trace"
                        " with non existent trace-option: " trace-option-string#))))
         `(when (.isEnabled (~'~caller-tracer) ~trace-option-string#)
            ~@body#))
       
       (defmacro ~'trace 
         ([trace-option# string#]
          (let [trace-option-string# (trace-option-str trace-option#)]
            `(~'~caller-tracer-call ~trace-option-string#
                                    (.trace (~'~caller-tracer)
                                            ~trace-option-string#
                                            (into-array Object [~string#]))) ))
         ([trace-option# string# throwable#]
          (let [trace-option-string# (trace-option-str trace-option#)] 
            `(~'~caller-tracer-call ~trace-option-string#
                                    (.trace (~'~caller-tracer) 
                                            ~trace-option-string#
                                            ~throwable#
                                            (into-array Object [~string#]))))))
       
       (defmacro ~'trace-dump-stack 
         ([trace-option#]
          (let [trace-option-string# (trace-option-str trace-option#)]
            `(~'~caller-tracer-call ~trace-option-string#
                                    (.traceDumpStack (~'~caller-tracer)
                                                     ~trace-option-string#)))))
       
       (defmacro ~'trace-entry
         ([trace-option# & method-args#]
          (let [trace-option-string# (trace-option-str trace-option#)]
            (cond
              (nil? (seq method-args#))
              `(~'~caller-tracer-call ~trace-option-string#
                                      (. (~'~caller-tracer)
                                         ~'~'traceEntry
                                         ~trace-option-string#))
              :else
              `(~'~caller-tracer-call ~trace-option-string#
                                      (. (~'~caller-tracer) 
                                         ~'~'traceEntry
                                         ~trace-option-string#
                                         (into-array Object [~@method-args#])))))))
       (defmacro ~'trace-exit
         ([trace-option# & result#]
          (if (seq result#)
            (let [trace-option-string# (trace-option-str trace-option#)]
              `(~'~caller-tracer-call ~trace-option-string#
                                      (. (~'~caller-tracer) 
                                         ~'~'traceExit
                                         ~trace-option-string#
                                         ~(first result#)))
              `(~'~caller-tracer-call ~trace-option-string#
                                      (. (~'~caller-tracer) 
                                         ~'~'traceExit
                                         ~trace-option-string#))))))
       
       (defmacro ~'format [trace-option# format-string# & format-args#]
           `(~'~caller-trace ~trace-option# (format ~format-string# ~@format-args#))))))
