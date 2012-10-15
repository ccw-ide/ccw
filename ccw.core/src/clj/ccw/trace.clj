(ns ccw.trace
  (:require [clojure.java.io :as io])
  (:import [ccw CCWPlugin]
           [java.util Properties]))

(def bundle (-> (CCWPlugin/getDefault) .getBundle))

(def symbolic-name (.getSymbolicName bundle))

(def trace-options
  (with-open [io (-> (CCWPlugin/getDefault) 
                   .getBundle
                   (.getEntry "/.options")
                   io/input-stream)]
    (into {} (doto (Properties.)
               (.load io)))))

(def trace-options-keys (set (keys trace-options)))

(defmacro format [trace-option format-string & format-args]
  (let [trace-option (if (keyword? trace-option) 
                       (.substring (str trace-option) 1)
                       trace-option)]
    (when-not (trace-options-keys (str symbolic-name "/" trace-option))
      (throw (RuntimeException. 
               (str "Compilation error: call to ccw.trace/trace"
                    " with non existent trace-option: " trace-option))))
    `(when (and
             (CCWPlugin/DEBUG)
             (CCWPlugin/isTraceOptionEnabled ~(str "/" trace-option)))
       (-> (CCWPlugin/TRACE) (.trace ~(str "/" trace-option)
                               (format ~format-string ~@format-args))))))
