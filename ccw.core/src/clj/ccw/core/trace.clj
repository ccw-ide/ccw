(ns ccw.core.trace
  (:require ccw.trace))

(ccw.trace/mk-trace-macros
  (.getBundle (ccw.CCWPlugin/getDefault))
  (ccw.CCWPlugin/getTracer))
