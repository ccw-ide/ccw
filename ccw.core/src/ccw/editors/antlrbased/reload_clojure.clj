(ns ccw.editors.antlrbased.reload-clojure
  (:gen-class
    :init init
    :state state
    :extends org.eclipse.jface.action.Action))

(defn -init [])

(defn -run [this]
  (require :reload '(ccw.editors.antlrbased
                      [reload-clojure]
                      [ClojureFormat]
                      [StacktraceHyperlink]
                      [PareditAutoEditStrategy])))


