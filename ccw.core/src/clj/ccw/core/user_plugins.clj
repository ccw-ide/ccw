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
    (doseq [c (doall to-remove)] ; prevents ConcurrentModificationExceptions in .remove
      (println "user-plugins gc, element to remove:" c)
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

(defn clean-key-bindings!
  "Find all key-bindings with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
  [app load-key]
  (let [to-remove (for [binding-table (m/binding-tables app)
                        :let [key-bindings (m/bindings binding-table)]
                        key-binding key-bindings
                        :when (and
                                (.contains (m/tags key-binding) "ccw")
                                (not= load-key (get (m/transient-data key-binding) "ccw/load-key")))]
                    [key-binding key-bindings])]
    (doseq [[kb kbs] (doall to-remove)] ;; doall prevents ConcurrentModificationExceptions when calling .remove
      (println "user-plugins gc, key-binding to remove: " kb)
      (.remove kbs kb))))

(defn clean-elements!
 "Find all elements with tag 'ccw', and remove all those that don't
   have 'ccw/load-key' transient key with value load-key"
 [app load-key]
 (let [model-service (m/context-key app :model-service)
       elts (.findElements model-service
              app
              nil ; don't find by id
              nil ; don't find a specific subclass of MUIElement
              ["ccw"])
       to-remove (remove #(not= load-key (get (m/transient-data %) "ccw/load-key")) elts)]
   (doseq [e (doall to-remove)] ; prevents ConcurrentModificationExceptions in .remove
     (println "user-plugins gc, element to remove:" e)
     (-> e .getParent .getChildren (.remove e)))))

(defn clean-model!
  "Find all elements with tag 'ccw', and remove all those that
   dont have 'ccw/load-key' transient key with value load-key."
  ([app]
    ;; generate a fresh load-key. The net result is a guarantee that all 'ccw'
    ;; tagged elements will be removed mercilessly
    (clean-model! app (str (java.util.UUID/randomUUID))))
  ([app load-key]
    (clean-commands! app load-key)
    (clean-handlers! app load-key)
    (clean-key-bindings! app load-key)
    (clean-elements! app load-key)))

(defn with-bundle [bundle urls f]
  (ccw.util.osgi.ClojureOSGi/withBundle 
    bundle 
    (reify ccw.util.osgi.RunnableWithException
      (run [this] (f)))
    urls))

(defn plugin-folder? [d]
  (some #(and (f/file? %) (.endsWith (.getName %) ".clj"))
        (.listFiles (io/file d))))

(defn user-plugins [d]
  (if (and d (plugin-folder? d))
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

(defn plugins-root-dir 
  "Return the user plugins dir (`~/.ccw/`) if it exists and is a directory.
   Or return nil."
  []
  (when-let [d (f/directory? (io/file (System/getProperty "user.home")
                                      ".ccw"))]
    d))

(defn start-user-plugins
  "Find user plugins, load them, and then clean the Eclipse Application model
   to remove traces of user plugin artifacts that have not yet been loaded
   (meaning they are now considered garbage).
   If no user plugin is found, remove all user plugin artifacts from the model."
  []
  (if-let [user-plugins (some-> (plugins-root-dir) user-plugins)]
    (binding [dsl/*load-key* (str (java.util.UUID/randomUUID))]
      (try
        (doseq [p user-plugins]
          (start-user-plugin p))
        (ccw.CCWPlugin/log (str "Loaded User Plugins"))
        (catch Exception e
          (ccw.CCWPlugin/logError "Error while loading User Plugins" e))
        (finally (clean-model! @m/app dsl/*load-key*))))
    (clean-model! @m/app)))
