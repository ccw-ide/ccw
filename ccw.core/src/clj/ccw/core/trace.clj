(ns ccw.core.trace
  (:require ccw.trace))

(ccw.trace/mk-trace-macros
  (.getBundle (ccw.CCWPlugin/getDefault))
  (ccw.CCWPlugin/getTracer))

(defmacro trace-execution-time
  "Use the trace mechanism to trace execution time of `body` for
   the given `trace-option` (a keyword), with the custom `msg` message"
  [trace-option msg & body]
  `(let [t0#  (System/currentTimeMillis)
         res# (do ~@body)
         t1#  (System/currentTimeMillis)]
     (t/format ~trace-option "%s took %dms" ~msg (- t1# t0#))
     res#))
