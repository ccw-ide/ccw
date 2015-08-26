(ns ccw.repl.visible-in-all-perspectives
  "Ensures that open REPLs are visible in all perspectives"
  (:require [ccw.eclipse :as e]
            [ccw.events  :as evt])
  (:import ccw.CCWPlugin
           ccw.repl.REPLView
           org.eclipse.ui.IWorkbenchPage))

(defonce open-repl-view-references (atom #{}))

(defn repl-view-adder [t {:keys [perspective-descriptor]}]
  (doseq [repl-ref @open-repl-view-references]
    (.showView (e/workbench-active-page)
      REPLView/VIEW_ID
      (.getSecondaryId (.getPart repl-ref false))
      IWorkbenchPage/VIEW_CREATE)))

(defn add-part [t {:keys [part-reference]}]
  (when (= REPLView/VIEW_ID (.getId part-reference))
    (swap! open-repl-view-references conj part-reference)))

(defn remove-part [t {:keys [part-reference]}]
  (when (= REPLView/VIEW_ID (.getId part-reference))
    (swap! open-repl-view-references disj part-reference)))

(defn start []
  (evt/subscribe :perspective.activated #'repl-view-adder)
  (evt/subscribe :part.opened #'add-part)
  (evt/subscribe :part.closed #'remove-part)
  (evt/subscribe :part.hidden #'add-part)
  (evt/subscribe :part.activated #'add-part))
