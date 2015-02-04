(ns ccw.editors.clojure.paredit-auto-edit-support
  (:import [org.eclipse.jface.text DocumentCommand]
           [ccw.editors.clojure IClojureAwarePart]))

(defn init
  "State Initialization for a new AutoEditInstance"
  [part preference-store] (ref {:part part :prefs-store preference-store}))

(defn add-command! [command modif]
  (.addCommand command (:offset modif)
                       (:length modif)
                       (:text modif)
                       nil))

(defn apply-modif!
  "Apply modification in result to command for editor."
  [^IClojureAwarePart part ^DocumentCommand command result]
  (when (and result (not= :ko (-> result :parser-state)))
    (if-let [modif (some-> result :modifs first)]
      (do
        (set! (.offset command) (-> result :modifs first :offset))
        (set! (.length command) (-> result :modifs first :length))
        (set! (.text command) (-> result :modifs first :text))
        (doseq [modif (rest (-> result :modifs))]
          (add-command! command modif)))
      (do
        (set! (.offset command) (:offset result))
        (set! (.length command) 0)
        (set! (.text command) "")
        (set! (.doit command) false)))
    (set! (.shiftsCaret command) false)
    (set! (.caretOffset command) (:offset result))
    (when-not (zero? (:length result)) ;;; WHY when-not zero?
      (.selectAndReveal part (:offset result) (:length result)))))
