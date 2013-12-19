(ns ccw.core.launch
  (:require [ccw.eclipse :as e]))

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
        (ccw.CCWPlugin/logError
          (format "Error while notifying nrepl listener '%s' of %s event with params %s"
                  n (:type l) (dissoc l :type))
          e)))))
