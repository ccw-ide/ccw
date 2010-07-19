(ns paredit.regex-utils
  (:import java.util.regex.Pattern)
  (:require [clojure.contrib.str-utils2 :as str2]))

(defprotocol Patternable (pattern [this] "given this, returns a String corresponding to the pattern"))

(extend-protocol Patternable
  Pattern
  (pattern [this] (.pattern this))
  String
  (pattern [this] this))

(defmacro interpol-regex
  "delim: literal char (or one-char String) for delimiting where the variables are. Optional, defaults to \\`
   regex: literal regex, or an object which implements protocol Patternable
   Usage: (interpol-regex \"foo\") => #\"foo\"
          (interpol-regex #\"foo\" => #\"foo\"
          (let [x #\"baz\\(\" y \"bar\"] (interpol-regex #\"(?:a|`x`|`y`)\") 
            => #\"(?:a|baz\\(|bar)\""
  ([regex] `(interpol-regex \` ~regex))
  ([delim regex]
    (let [escaped-delim (str \\ delim)
          partitioning-pattern (Pattern/compile (str escaped-delim "([^" escaped-delim "]+)" escaped-delim))
          exploded-pattern (str2/partition (pattern regex) partitioning-pattern)]
      `(java.util.regex.Pattern/compile (str ~@(map #(if (string? %) % `(str "(?:" (pattern ~(symbol (get % 1))) ")")) exploded-pattern))))))

