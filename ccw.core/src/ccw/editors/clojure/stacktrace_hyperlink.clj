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

(ns ccw.editors.clojure.stacktrace-hyperlink
  (:require [ccw.util.string :as s])
  (:use [clojure.test]))

(defn- find-datas [s]
  {:line (Integer/valueOf (second (re-find #":([0-9]+)" s)))
   :file (str (s/replace (second (re-find #"at ([\w\.]+)\$" s)) "." "/") ".clj")
   :ns (s/replace (second (re-find #"at ([\w\.]+)\$" s)) "_" "-")})

(defn- open-file [s]
  (let [x (find-datas s)]
    (ccw.ClojureCore/openInEditor (:ns x) (:file x) (:line x))))

(defn- offset-and-length [s]
  (map + [1 0] (map count (s/split s #"\("))))

(defn make []
  (let [state (atom nil)]
    (reify org.eclipse.ui.console.IPatternMatchListenerDelegate
      (connect [this console]
        (dosync (reset! state console)))
      (disconnect [this])
      (matchFound [this event]
                  (let [console @state
                        offset (.getOffset event)
                        s (.get (.getDocument console) offset (.getLength event))
                        [o l] (offset-and-length s)
                        hyperlink (reify org.eclipse.ui.console.IHyperlink
                                    (linkActivated [this] (open-file s))
                                    (linkExited [this])
                                    (linkEntered [this]))]
                    (.addHyperlink console hyperlink (+ offset o) l))))))

(defn factory "plugin.xml hook" [ _ ] (make))

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
