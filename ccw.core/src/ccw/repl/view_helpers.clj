(ns ccw.repl.view-helpers
  (:require [clojure.tools.nrepl :as repl]
    [ccw.repl.cmdhistory :as history])
  (:use (clojure.contrib def core))
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

(defn beep
  []
  (-> (PlatformUI/getWorkbench)
     .getDisplay
     .beep))

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

(defn- cursor-at-end
  "Puts the cursor at the end of the text in the given widget."
  [^StyledText widget]
  (.setCaretOffset widget (.getCharCount widget)))

(defn log
  [^StyledText log ^String s type]
  (ui-sync
    (let [s (.replaceAll s "\\s+\\Z" "")
          charcnt (.getCharCount log)]
      (.append log s)
      (when (and (seq s) (not (Character/isWhitespace (last s))))
        (.append log "\n"))
      (doto log
        cursor-at-end
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

(defn configure-repl-view
  [& [repl-view :as args]]
  (let [[history retain-expr-fn] (history/get-history (-?> repl-view
                                                        .getLaunch
                                                        ccw.launching.LaunchUtils/getProjectName))
        ^StyledText input-widget (.viewerWidget repl-view)
        ; a bunch of atoms are just fine, since access to them is already serialized via the SWT event thread
        history (atom history)
        current-step (atom -1)
        retained-input (atom nil)]
    ;; TODO need to make this a customizable action or something
    (.addKeyListener (.viewerWidget repl-view)
      (proxy [org.eclipse.swt.events.KeyAdapter] []
        (keyPressed [^org.eclipse.swt.events.KeyEvent e]
          (when-let [history-shift (get {[SWT/CTRL SWT/ARROW_UP] inc
                                         [SWT/CTRL SWT/ARROW_DOWN] dec}
                                     [(.stateMask e) (.keyCode e)])]
            (swap! current-step history-shift)
            (cond
              (>= @current-step (count @history)) (do (swap! current-step dec) (beep))
              (neg? @current-step) (do (reset! current-step -1)
                                     (when @retained-input
                                       (doto input-widget
                                         (.setText @retained-input)
                                         cursor-at-end)
                                       (reset! retained-input nil)))
              :else (do
                      (when-not @retained-input
                        (reset! retained-input (.getText input-widget)))
                      (doto input-widget
                        (.setText (@history (dec (- (count @history) @current-step))))
                        cursor-at-end)))))))
    
    (comp (apply partial eval-expression args)
      retain-expr-fn
      (fn [expr]
        (reset! retained-input nil)
        (reset! current-step -1)
        (swap! history #(subvec (conj % expr)
                          (-> % count (- history/max-history) (max 0))))
        expr))))