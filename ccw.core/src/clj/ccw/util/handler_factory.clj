(ns ccw.util.handler-factory)

(defn factory [{handler "handler"}]
  (let [handler (symbol handler)]
    (require (-> handler namespace symbol))
    (let [handler (find-var handler)]
      (proxy [org.eclipse.core.commands.AbstractHandler]
             []
        (execute [event] (when handler (handler this event)))))))

