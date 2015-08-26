(ns ccw.core.event-bus
  "Register missing events for workbench, windows, pages, perspectives, etc.
   to the global event bus"
  (:require [ccw.eclipse :as e]
            [ccw.events :as evt]))

(defonce window-listener
  (reify org.eclipse.ui.IWindowListener
    (windowActivated [this window]
      (evt/send-event :window.activated {:window window}))
    (windowDeactivated [this window]
      (evt/send-event :window.deactivated {:window window}))
    (windowClosed [this window]
      (evt/send-event :window.closed {:window window}))
    (windowOpened [this window]
      (evt/send-event :window.opened {:window window}))))

(defonce page-listener
  (reify org.eclipse.ui.IPageListener
    (pageActivated [this page]
      (evt/send-event :page.activated {:page page}))
    (pageClosed [this page]
      (evt/send-event :page.closed {:page page}))
    (pageOpened [this page]
      (evt/send-event :page.opened {:page page}))))

(defonce perspective-listener
  (reify org.eclipse.ui.IPerspectiveListener4
    (perspectiveActivated [this page perspective-descriptor]
      (evt/send-event :perspective.activated
        {:page page :perspective-descriptor perspective-descriptor}))
    (perspectiveChanged [this page perspective-descriptor change-id]
      (evt/send-event :perspective.changed
        {:page page :perspective-descriptor perspective-descriptor :change-id change-id}))
    (perspectiveChanged [this page perspective-descriptor part-ref change-id]
      (evt/send-event :perspective.changed
        {:page page :perspective-descriptor perspective-descriptor :part-ref part-ref :change-id change-id}))
    (perspectiveOpened [this page perspective-descriptor]
      (evt/send-event :perspective.opened
        {:page page :perspective-descriptor perspective-descriptor}))
    (perspectiveClosed [this page perspective-descriptor]
      (evt/send-event :perspective.closed
        {:page page :perspective-descriptor perspective-descriptor}))
    (perspectiveDeactivated [this page perspective-descriptor]
      (evt/send-event :perspective.deactivated
        {:page page :perspective.descriptor perspective-descriptor}))
    (perspectiveSavedAs [this page old-perspective-descriptor new-perspective-descriptor]
      (evt/send-event :perspective.saved-as
        {:page page :old-perspective-descriptor old-perspective-descriptor :new-perspective-descriptor new-perspective-descriptor}))
    (perspectivePreDeactivate [this page perspective-descriptor]
      (evt/send-event :perspective.pre-deactivate
        {:page page :perspective-descriptor perspective-descriptor}))))

(defonce workbench-listener
  (reify org.eclipse.ui.IWorkbenchListener
    (preShutdown [this workbench forced?]
      (evt/send-event :workbench.pre-shutdown
        {:workbench workbench :forced? forced?}))
    (postShutdown [this workbench]
      (evt/send-event :workbench.post-shutdown
        {:workbench workbench}))))

(defonce command-execution-listener
  (reify org.eclipse.core.commands.IExecutionListener
    (notHandled [this command-id exception]
      (evt/send-event :command.not-handled
        {:command-id command-id :exception exception}))
    (postExecuteFailure [this command-id exception]
      (evt/send-event :command.post-execute-failure
        {:command-id command-id :exception exception}))
    (postExecuteSuccess [this command-id return-value]
      (evt/send-event :command.post-execute-success
        {:command-id command-id
         :return-value return-value}))
    (preExecute [this command-id execution-event]
      (evt/send-event :command.pre-execute
        {:command-id command-id
         :execution-event execution-event}))))

(defonce part-listener
  (reify org.eclipse.ui.IPartListener2
    (partActivated [this part-reference]
      (evt/send-event :part.activated
        {:part-reference part-reference}))
    (partBroughtToTop [this part-reference]
      (evt/send-event :part.brought-to-top
        {:part-reference part-reference}))
    (partClosed [this part-reference]
      (evt/send-event :part.closed
        {:part-reference part-reference}))
    (partDeactivated [this part-reference]
      (evt/send-event :part.deactivated
        {:part-reference part-reference}))
    (partOpened [this part-reference]
      (evt/send-event :part.opened
        {:part-reference part-reference}))
    (partHidden [this part-reference]
      (evt/send-event :part.hidden
        {:part-reference part-reference}))
    (partVisible [this part-reference]
      (evt/send-event :part.visible
        {:part-reference part-reference}))
    (partInputChanged [this part-reference]
      (evt/send-event :part.input-changed
        {:part-reference part-reference}))))

(defn register-window-listeners [window]
  (.addPageListener window page-listener)
  (.addPerspectiveListener window perspective-listener)
  (-> window
    (.getService org.eclipse.ui.commands.ICommandService)
    (.addExecutionListener command-execution-listener))
  (-> window
    (.getPartService)
    (.addPartListener part-listener)))

(defn window-opened [_ window] (register-window-listeners window))

(defn start
  "Start new event handlers for new kinds of events on the event bus"
  []
  (.addWorkbenchListener (e/workbench) workbench-listener)
  (.addWindowListener (e/workbench) window-listener)
  (doseq [window (e/workbench-windows)]
    (register-window-listeners window))
  (evt/subscribe :window.opened :require-ui #'window-opened))
