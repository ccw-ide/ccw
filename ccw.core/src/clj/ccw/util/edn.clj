(ns ccw.util.edn
  (:require [clojure.edn :as edn]))

(defn read-vector
  "Reads a vector from the EDN string s. Returns an empty vector if s does not
  contain a valid EDN vector."
  [s]
  (let [x (try
            (edn/read-string s)
            (catch Throwable _
              nil))]
    (if (vector? x)
      x
      [])))

