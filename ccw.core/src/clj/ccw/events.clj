(ns ccw.events
  (:require [ccw.e4.model]
            [ccw.core.trace :as t])
  (:import [org.osgi.service.event EventHandler]))

(def DATA
  "IEventBroker/DATA"
  "org.eclipse.e4.data")

(defn event-broker []
  (ccw.e4.model/context-key @ccw.e4.model/app :event-broker))

(defn as-topic [t]
  (cond
    (instance? String t) t
    (keyword? t) (-> t name (.replaceAll "\\." "/"))))

(defn- topic-as-keyword [t]
  (-> t (.replaceAll "\\/" ".") keyword))

(defn send-event [topic data]
  (t/format :events "will send %s, data: %s" (as-topic topic) data)
  ;; we force the data type to smth other than a Map so
  ;; we get consistent behavior in the handler
  (.send (event-broker) (as-topic topic) [data]))

(defn post-event [topic data]
  (t/format :events "will post %s, data: %s" (as-topic topic) data)
  ;; we force the data type to smth other than a Map so
  ;; we get consistent behavior in the handler
  (.post (event-broker) (as-topic topic) [data]))

(defn unsubscribe
  "event-handler-var is either a var with ::event-handler metadata attached to it,
   or a true org.osgi.service.event.EventHandler.
   If it is not one of those cases, then no unsubscription is done.
   Returns true if unsubscription don"
  [event-handler-var]
  (cond
    (var? event-handler-var)
      (when-let [event-handler (some-> event-handler-var meta ::event-handler)]
        (alter-meta! event-handler-var dissoc ::event-handler)
        (.unsubscribe (event-broker) event-handler))
    (instance? EventHandler event-handler-var)
      (.unsubscribe (event-broker) event-handler-var)))

(defn subscribe
  ([topic event-handler-var] (subscribe topic false event-handler-var))
  ([topic require-ui? event-handler-var]
    (t/format :events "subscribing to topic %s" (as-topic topic))
    (unsubscribe event-handler-var)
    (let [event-handler
          (reify EventHandler
            (handleEvent [this event]
              (event-handler-var
                (topic-as-keyword (.getTopic event))
                ;; call to (get 0) to extract the data from the vector
                (first (.getProperty event DATA)))))]
      (when (.subscribe (event-broker)
              (as-topic topic)
              nil
              event-handler
              (boolean require-ui?))
        (when (var? event-handler-var)
          (alter-meta! event-handler-var assoc ::event-handler event-handler))
        event-handler))))
