(ns ccw.core.launch
  (:require [clojure.tools.nrepl.server :refer [start-server stop-server default-handler]]
            [clojure.tools.nrepl.ack :refer [handle-ack]]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [ccw.eclipse :refer [property-ccw-nrepl-port property-ccw-nrepl-cider-enable]])
  (:import ccw.CCWPlugin
           ccw.core.StaticStrings
           java.lang.System))

(defn default-nrepl-server-listener 
  "Default listener printing on *out* the nrepl url"
  [{:keys [event-type port project]}]
  (when (= :creation event-type)
    (printf "nREPL server created nrepl://127.0.0.1:%s" port)
    (when project
      (printf " for project '%s'" project))
    (println)))

(defonce ^{:doc
  "Atom containing a Map of listener name -> listener fn.
   Listener fns will be called when interesting events occur wrt nrepl servers
   managed by Counterclockwise, like that: (a-listener-fn event-map).
   Events currently handled: :creation, with keys :event-type :creation, 
                             :port (Number, required) and :project (String, optional)" }
  nrepl-servers-listeners 
  (atom {:ccw/default #'default-nrepl-server-listener}))


(defn on-nrepl-server-instanciated
  "Called when a new nREPL server has been instanciated through Counterclockwise.
   port: the port to use to connect an nREPL client to the server
   project: the project name. May be nil."
  [port project]
  (doseq [[n l] @nrepl-servers-listeners]
    (try
      (l {:event-type :creation, :port port :project project})
      (catch Exception e
        (CCWPlugin/logError
          (format "Error while notifying nrepl listener '%s' of %s event with params %s"
                  n (:type l) (dissoc l :type))
          e)))))

;; Embedded nRepl
(defonce ^{:doc "Atom containing the map returned by start-server."}
  ccw-nrepl-server-map (atom nil))

(defn ccw-nrepl-start-if-necessary
  "Starts a nRepl if there is none started, reading the info from
  eclipse's runtime properties. The result is then stored in the
  ccw-nrepl-server-map atom."
  []
  (when (= nil @ccw-nrepl-server-map)
    ;; TODO use ClojureOSGi.withBundle instead
    (let [nrepl-port (property-ccw-nrepl-port)
          handler (if (property-ccw-nrepl-cider-enable)
                    (handle-ack cider-nrepl-handler)
                    (handle-ack (default-handler)))]
      (if (= 0 nrepl-port)
        (CCWPlugin/log (str "nRepl port will be automatically selected"))
        (CCWPlugin/log (str "nRepl port will be: " nrepl-port)))
      (reset! ccw-nrepl-server-map (start-server :port nrepl-port :handler handler))
      (CCWPlugin/log (str "Started ccw nREPL server: nrepl://127.0.0.1:" nrepl-port)))))

(defn ccw-nrepl-port
  []
  (:port @ccw-nrepl-server-map))

(defn ccw-nrepl-stop
  "Stops the instance of the running nRepl."
  []
  (when-let [server-map @ccw-nrepl-server-map]
    (stop-server server-map)
    (reset! ccw-nrepl-server-map nil)
    (CCWPlugin/log (str "Stopped ccw nREPL server: nrepl://127.0.0.1:" (:port server-map)))))
