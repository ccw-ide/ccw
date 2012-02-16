(ns ^{:doc "String manipulation utilities"}
     ccw.util.string
  (:refer-clojure :exclude [replace take drop])
  (:require [clojure.string :as s]))

(def replace #'s/replace)

(def split #'s/split)

(defn take [s n]
  (if (< (count s) n)
    s
    (.substring s 0 n)))

(defn drop [s n]
  (if (< (count s) n)
    ""
    (.substring s n)))
