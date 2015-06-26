(ns ccw.file
  "Small utility functions for manipulating java.io.Files
   in the context of Counterclockwise"
  (:require [clojure.java.io :as io]))

(defn exists?
  "Does f correspond to an existing resource on the filesystem?
   If yes, returns f."
  [f] (and f (.exists f) f))

(defn directory? 
  "Does f exist and is it a directory?
   If yes, returns f.
   nil-safe (returns nil)."
  [f] (and f (.isDirectory f) f))

(defn file? 
  "Does f exist and is it a file?
   If yes, returns f.
   nil-safe (returns nil)."
  [f] (and f (.isFile f) f))

(defn absolute-path
  "Return the absolute path (filesystem/OS dependent) of f"
  [f] (and f (.getAbsolutePath f)))

(defn name
  "Return the file name, aka the last part of the path"
  [f] (.getName f))

(defn canonical-path
  "Return the canonical path for f"
  [f] (.getCanonicalPath f))