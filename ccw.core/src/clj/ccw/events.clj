(ns ccw.events)

;; remove this once CCW has definitely dropped Eclipse 3.x support
(def DATA
  "IEventBroker/DATA"
  "org.eclipse.e4.data")

(def event-broker
  (delay
    (try 
      (require '[ccw.e4.model])
      (ccw.e4.model/context-key @ccw.e4.model/app :event-broker)
      (catch Exception e
        (println "Event Broker cannot be loaded. Please upgrade to Eclipse 4")
        nil))))

(defn as-topic [t]
  (cond
    (instance? String t) t
    (keyword? t) (-> t name (.replaceAll "\\." "/"))))

(defn- topic-as-keyword [t]
  (-> t (.replaceAll "\\/" ".") keyword))

(defn send-event [topic data]
  (when-let [event-broker @event-broker]
    (println "will send" (as-topic topic) ", data:" data)
    (.send event-broker (as-topic topic) [data])))

(defn post-event [topic data]
  (println "will post" (as-topic topic) ", data:" data)
  (when-let [event-broker @event-broker]
    (.post event-broker (as-topic topic) [data])))

(defn subscribe
  ([topic event-handler-var] (subscribe topic false event-handler-var))
  ([topic require-ui? event-handler-var]
    (println "subscribing to topic" (as-topic topic))
    (when-let [event-broker @event-broker]
      (let [event-handler
            (reify org.osgi.service.event.EventHandler
              (handleEvent [this event]
                (event-handler-var
                  (topic-as-keyword (.getTopic event))
                  (first (.getProperty event DATA)))))]
        (when (.subscribe event-broker
                (as-topic topic)
                nil
                event-handler
                (boolean require-ui?))
          event-handler)))))

(defn unsubscribe [event-handler]
  (when-let [event-broker @event-broker]
    (.unsubscribe event-broker
      event-handler)))
