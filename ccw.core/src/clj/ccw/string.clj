(ns ^{:doc "String manipulation utilities"}
     ccw.string
  (:refer-clojure :exclude [replace take drop])
  (:require [clojure.string :as s]))

(def replace #'s/replace)

(def split #'s/split)

(defn take [^String s n]
  (if (< (count s) n)
    s
    (.substring s 0 n)))

(defn drop [^String s n]
  (if (< (count s) n)
    ""
    (.substring s n)))
