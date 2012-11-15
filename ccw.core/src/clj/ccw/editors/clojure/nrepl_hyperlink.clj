(ns ccw.editors.clojure.nrepl-hyperlink
  (:require [ccw.util.string :as s])
  (:use [clojure.test]))

;; TODO share it with editor hyperlink
(def ^:private pattern #"nrepl://([^':',' ']+):(\d+)")

(defn match-found [event console]
  (let [offset (.getOffset event)
        length (.getLength event)
        document (.getDocument console)
        s (.get document offset length)
        [[url]] (re-seq pattern s)
        hyperlink (reify org.eclipse.ui.console.IHyperlink
                    (linkActivated [this] (ccw.repl.REPLView/connect url true))
                    (linkExited [this])
                    (linkEntered [this]))]
    (.addHyperlink console hyperlink offset length)))

(defn make []
  (let [state (atom nil)]
    (reify org.eclipse.ui.console.IPatternMatchListenerDelegate
      (connect [this console]  (dosync (reset! state console)))
      (disconnect [this]       (reset! state nil))
      (matchFound [this event] (match-found event @state)))))

(defn factory "plugin.xml hook" [ _ ] (make))

;(def sample-string "at clojure.contrib.repl_ln$_main__7172.doInvoke(repl_ln.clj:140")
;
;(deftest offset-and-length-are-determined
;  (is (= [48 15] (offset-and-length sample-string)))
;  (is (= "repl_ln.clj:140" (s/take (s/drop sample-string 48) 15)))
;  (is (= "repl_ln.clj:14" (s/take (s/drop sample-string 48) 14))))
;
;(def test-data (find-datas sample-string))
;
;(deftest string-is-parsed
;  (is (= "clojure.contrib.repl-ln" (:ns test-data)))
;  (is (= "clojure/contrib/repl_ln.clj" (:file test-data)))
;  (is (= 140 (:line test-data))))
