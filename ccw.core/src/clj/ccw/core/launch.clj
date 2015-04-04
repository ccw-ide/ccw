(ns ccw.core.launch
  (:require [clojure.tools.nrepl.server :refer [start-server stop-server default-handler]]
            [clojure.tools.nrepl.ack :refer [handle-ack]]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [ccw.util :refer [delayed-atom-swap! delayed-atom-reset!]]
            [ccw.eclipse :refer [property-ccw-nrepl-port]])
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

;;;;;;;;;;;
;; nRepl ;;
;;;;;;;;;;;

(defonce ^{:author "Andrea Richiardi" :doc "Atom containing the map returned by start-server."}
  nrepl-server-map-delay (atom nil))

(defn- nrepl-start
  "Starts an nRepl given the port number."
  [port handler]
  (start-server :port port :handler handler))

(defn- nrepl-stop
  "Stops the instance of the running nRepl. Always returns nil."
  [server-map]
  (stop-server server-map))

;; Public API
(defn ccw-nrepl-port
  "Return the currently open nRepl's port"
  []
  (:port (some-> @nrepl-server-map-delay force)))

(defn ccw-nrepl-start-if-necessary
  "Starts a nRepl if there is none started, reading the info from
  eclipse's runtime properties. The result is then stored in the
  ccw-nrepl-server-map atom. Returns the newly opened socket to it."
  []
  (let [nrepl-port (property-ccw-nrepl-port)
        handler (handle-ack cider-nrepl-handler)]
    (when (= 0 nrepl-port)
      (CCWPlugin/log (str "nRepl port will be automatically selected")))
    (let [start-server-delay (delayed-atom-swap! nrepl-server-map-delay
                                                 nrepl-start
                                                 nrepl-port handler)
          was-realized (realized? start-server-delay)
          server-map (force start-server-delay)]
      (if-not was-realized
        (CCWPlugin/log (str "Started ccw nREPL server: nrepl://127.0.0.1:" (:port server-map)))))))

(defn ccw-nrepl-stop
  "Stops the instance of the running nRepl on the underlying atom. Always returns nil."
  []
  (CCWPlugin/log (str "Stopping ccw nREPL server..."))
  (delayed-atom-reset! nrepl-server-map-delay nrepl-stop))
