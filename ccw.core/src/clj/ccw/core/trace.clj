(ns ccw.core.trace
  (:require ccw.util.trace))

(ccw.util.trace/mk-trace-macros
  (.getBundle (ccw.CCWPlugin/getDefault))
  (ccw.CCWPlugin/getTracer))
