(ns ccw.repl.view-helpers
  (:require [cemerick.nrepl :as repl])
  (:use clojure.contrib.def)
  (:import ccw.CCWPlugin
    org.eclipse.ui.PlatformUI
    org.eclipse.swt.SWT
    (org.eclipse.swt.custom StyledText StyleRange)))

(defmacro ui-async
  [& body]
  `(-> (PlatformUI/getWorkbench)
     .getDisplay
     (.asyncExec (fn [] ~@body))))

(defmacro ui-sync
  [& body]
  `(-> (PlatformUI/getWorkbench)
     .getDisplay
     (.syncExec (fn [] ~@body))))

(defn- set-style-range
  [style-range-fn start length]
  (let [^StyleRange style (style-range-fn)]
    (set! (.start style) start)
    (set! (.length style) length)
    style))

(defvar- default-log-style (partial set-style-range #(StyleRange.)))

(defvar- log-styles
  (let [colored-style #(let [s (StyleRange.)]
                         (set! (.foreground s) (-> (PlatformUI/getWorkbench)
                                                 .getDisplay
                                                 (.getSystemColor %)))
                         s)]
    {:err (partial set-style-range #(colored-style SWT/COLOR_DARK_RED))
     :out default-log-style
     :value (partial set-style-range #(colored-style SWT/COLOR_DARK_GREEN))}))

(defn log
  [^StyledText log ^String s type]
  (ui-sync
    (let [s (.replaceAll s "\\s+\\Z" "")
          charcnt (.getCharCount log)]
      (.append log s)
      (when (and (seq s) (not (Character/isWhitespace (last s))))
        (.append log "\n"))
      (doto log
        (.setCaretOffset (.getCharCount log))
        .showSelection
        (.setStyleRange ((get log-styles type default-log-style) charcnt (- (.getCharCount log) charcnt)))))))

(defn eval-failure-msg
  [status s]
  (format "Expression %s: %s"
    ({"timeout" "timed out", "interrupted" "was interrupted"} status "failed")
    (-> s
      (.substring 0 (min 30 (count s)))
      (.replaceAll "\\n|\\r" " "))))

(defn eval-expression
  [repl-view log-component {:keys [send]} requests-atom expr]
  (let [response-fn (send expr)
        key [(System/currentTimeMillis) expr]]
    (swap! requests-atom assoc key response-fn)
    (future (try
              (doseq [{:keys [out err value ns status] :as resp} (repl/response-seq response-fn)]
                (ui-sync
                  (when ns
                    (.setCurrentNamespace repl-view ns))
                  (doseq [[k v] resp]
                    (when (#{:out :err :value} k)
                      (log log-component v k)))
                  (case status
                    ("timeout" "interrupted") (log log-component (eval-failure-msg status out) :err)
                    nil)))
              (catch Throwable t
                (CCWPlugin/logError (eval-failure-msg nil expr) t)
                (log log-component (eval-failure-msg nil expr) :err))
              (finally
                (swap! requests-atom dissoc key))))))
