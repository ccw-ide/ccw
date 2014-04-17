(ns ccw.editors.clojure.nrepl-hyperlink
  (:require [ccw.string :as s]
            [ccw.eclipse :as e])
  (:use [clojure.test])
  (:import [org.eclipse.ui.console PatternMatchEvent TextConsole]))

;; TODO share it with editor hyperlink
(def ^:private pattern #"nrepl://([^':',' ']+):(\d+)")

(defn deliver-url
  "Deliver the port to every waiter behind the repl-port-promise (if any)."
  [name port]
  (if-let [repl-url-promise (.get (ccw.launching.ClojureLaunchShortcut/launchNameREPLURLPromise) name)]
    (deliver repl-url-promise port)))

(defn console-name
  "Approximation of console/process identity with console's launch configuration name"
  [console]
  (some-> console .getProcess .getLaunch .getLaunchConfiguration .getName))

(defn match-found 
  "state contains the console instance, and a set of seen nrepl links.
   This allows to e.g. only process nrepl links opening once, even if 
   match-found is called several times on the same text."
  [^PatternMatchEvent event ^TextConsole {:keys [console nrepl-urls] :as state}]
  (let [offset (.getOffset event)
        length (.getLength event)
        document (.getDocument console)
        s (.get document offset length)
        [[url]] (re-seq pattern s)
        open-repl-view #(ccw.repl.REPLView/connect url console (some-> console .getProcess .getLaunch) true)
        hyperlink (reify org.eclipse.ui.console.IHyperlink
                    (linkActivated [this] (open-repl-view))
                    (linkExited [this])
                    (linkEntered [this]))]
    (.addHyperlink console hyperlink offset length)
    (when-not (nrepl-urls url)
      (deliver-url (console-name console) url))
    (update-in state [:nrepl-urls] conj url)))

(defn make []
  (let [state (atom nil)]
    (reify org.eclipse.ui.console.IPatternMatchListenerDelegate
      (connect [this console]
        (reset! state {:console console
                       :nrepl-urls #{}}))
      (disconnect [this]
        ;; remove the repl url promise since the console / process is stopped
        (println "not removed: " (ccw.launching.ClojureLaunchShortcut/launchNameREPLURLPromise))
        (println "removing console name" (console-name (:console @state)))
        (.remove 
          (ccw.launching.ClojureLaunchShortcut/launchNameREPLURLPromise) 
          (console-name (:console @state)))
        (println "removed: " (ccw.launching.ClojureLaunchShortcut/launchNameREPLURLPromise))
        (reset! state nil))
      (matchFound [this event] 
        (swap! state (partial match-found event))
        nil))))

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
