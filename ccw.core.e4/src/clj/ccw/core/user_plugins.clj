(ns ccw.core.user-plugins
  (:require [clojure.java.io :as io]
            [ccw.file :as f]
            [ccw.e4.dsl :as dsl]
            [ccw.e4.model :as m]))

(defn clean-type-elements!
  "Find all type elements with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
  [find-type-elements-by-tags application-type-elements app load-key]
  ;(println "clean-type-elements! load-key=" load-key)
  (let [cmds (find-type-elements-by-tags app ["ccw"])
        ;_ (println "ccw type elements:" cmds)
        to-remove (remove #(= load-key
                              (get (m/transient-data %) "ccw/load-key")) 
                          cmds)
        ;_ (println "to-remove:" to-remove)
        app-cmds (application-type-elements app)]
    (doseq [c to-remove]
      (.remove app-cmds c))))

(defn clean-commands!
  "Find all commands with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
  [app load-key]
  (clean-type-elements!
    m/find-commands-by-tags m/commands
    app load-key))


(defn clean-handlers!
  "Find all handlers with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
  [app load-key]
  (clean-type-elements!
    m/find-handlers-by-tags m/handlers
    app load-key))

#_(defn clean-key-bindings!
   "Find all key-bindings with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
   [app load-key]
   (clean-type-elements!
     m/find-handlers-by-tags m/handlers
     app load-key))

#_(defn clean-elements!
   "Find all elements with tag 'ccw', and remove all those that don't
   have 'ccw/load-key' transient key with value load-key"
   [app load-key]
   (clean-type-elements!
     m/find-elements-by-tags
     ))

(defn clean-model!
  "Find all elements with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
  [app load-key]
  (clean-commands! app load-key)
  (clean-handlers! app load-key)
  #_(clean-key-bindings! app load-key)
  #_(clean-elements! app load-key))

(defn with-bundle [bundle urls f]
  (ccw.util.osgi.ClojureOSGi/withBundle 
    bundle 
    (reify ccw.util.osgi.RunnableWithException
      (run [this] (f)))
    urls))

(defn plugin-folder? [d]
  (some #(and f/file? (.endsWith (.getName %) ".clj"))
        (.listFiles (io/file d))))

(defn user-plugins [d]
  (if (plugin-folder? d)
    [d]
    (mapcat user-plugins 
            (filter f/directory?
                    (.listFiles (io/file d))))))

(defn load-user-script [f]
  (try
    (load-file (f/absolute-path (io/file f)))
    (ccw.CCWPlugin/log (str "Loaded User Script " f))
    (catch Exception e
      (ccw.CCWPlugin/logError (str "Exception loading User Script " f) e))))

;; TODO handle load-key per user plugin ... ???
(defn start-user-plugin [d]
  (when-let [scripts (seq (filter #(and f/file? (.endsWith (.getName %) ".clj"))
                                  (.listFiles (io/file d))))]
    (with-bundle
      (.getBundle (ccw.CCWPlugin/getDefault))
      [(io/as-url (io/file d))]
      #(try
         (doseq [script scripts] 
           (load-user-script script))
         (ccw.CCWPlugin/log (str "Loaded User Plugin: " d))
         (catch Exception e
           (ccw.CCWPlugin/logError (str "Error while loading User Plugin " d) e))))))

(defn start-user-plugins []
  (when-let [user-plugins (user-plugins (f/plugins-root-dir))]
    (binding [dsl/*load-key* (str (java.util.UUID/randomUUID))]
      (try
        (doseq [p user-plugins]
          (start-user-plugin p))
        (ccw.CCWPlugin/log (str "Loaded User Plugins"))
        (catch Exception e
          (ccw.CCWPlugin/logError "Error while loading User Plugins" e))
        (finally (clean-model! @m/app dsl/*load-key*))))))
