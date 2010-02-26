; *******************************************************************************
; * Copyright (c) 2010 Tuomas KARKKAINEN.
; * All rights reserved. This program and the accompanying materials
; * are made available under the terms of the Eclipse Public License v1.0
; * which accompanies this distribution, and is available at
; * http://www.eclipse.org/legal/epl-v10.html
; *
; * Contributors: 
; *    Tuomas KARKKAINEN - initial API and implementation
; *******************************************************************************/

(ns ccw.editors.antlrbased.StacktraceHyperlink
  (:require [clojure.contrib.str-utils2 :as s])
  (:use [clojure.test])
  (:gen-class
    :init init
    :state state
    :implements [org.eclipse.ui.console.IPatternMatchListenerDelegate]
    :methods [[doStuff [String] clojure.lang.PersistentArrayMap]]))

(defn -init []
  [[] (ref {})])

(defn -connect [this console]
  (dosync (alter (.state this) assoc :console console)))

(defn- find-datas [s]
  {:line (Integer/valueOf (second (re-find #":([0-9]+)" s)))
   :file (str (s/replace (second (re-find #"at ([\w\.]+)\$" s)) "." "/") ".clj")
   :ns (s/replace (second (re-find #"at ([\w\.]+)\$" s)) "_" "-")})

(defn- open-file [s]
  (let [x (find-datas s)]
    (ccw.ClojureCore/openInEditor (:ns x) (:file x) (:line x))))

(defn- offset-and-length [s]
  (map + [1 0] (map count (s/split s #"\("))))

(defn -matchFound [this event]
  (let [console (@(.state this) :console)
        offset (.getOffset event)
        s (.get (.getDocument console) offset (.getLength event))
        [o l] (offset-and-length s)
        hyperlink (proxy [org.eclipse.ui.console.IHyperlink] []
                    (linkActivated [] (open-file s))
                    (linkExited [])
                    (linkEntered []))]
    (.addHyperlink console hyperlink (+ offset o) l)))

(def sample-string "at clojure.contrib.repl_ln$_main__7172.doInvoke(repl_ln.clj:140")

(deftest offset-and-length-are-determined
  (is (= [48 15] (offset-and-length sample-string)))
  (is (= "repl_ln.clj:140" (s/take (s/drop sample-string 48) 15)))
  (is (= "repl_ln.clj:14" (s/take (s/drop sample-string 48) 14))))

(def test-data (find-datas sample-string))

(deftest string-is-parsed
  (is (= "clojure.contrib.repl-ln" (:ns test-data)))
  (is (= "clojure/contrib/repl_ln.clj" (:file test-data)))
  (is (= 140 (:line test-data))))

; (println "actual:" test-data)
; (run-tests)

