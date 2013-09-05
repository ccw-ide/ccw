(ns ccw.editors.clojure.PareditAutoAdjustWhitespaceStrategyImpl
  (:require [paredit [core :refer [paredit]]])
  (:require [clojure.core.incubator :refer [-?>]])
  (:require [ccw.editors.clojure.paredit-auto-edit-support :as support])
  (:require [paredit.loc-utils :as lu])
  (:require [paredit.text-utils :as tu])
  (:require [paredit.parser :as p])
  (:require [clojure.string :as s])
  (:require [clojure.zip :as zip])
  (:import
    [org.eclipse.jface.text IAutoEditStrategy
                            IDocument
                            DocumentCommand]
    [org.eclipse.jface.preference IPreferenceStore]
    [ccw.editors.clojure IClojureEditor PareditAutoAdjustWhitespaceStrategy]
    [ccw.preferences PreferenceConstants]))
   

#_(set! *warn-on-reflection* true)

(defn customizeDocumentCommand 
  "Work only if no command has been added via (.addCommand)"
  [^PareditAutoAdjustWhitespaceStrategy this, #^IDocument document, #^DocumentCommand command]
  (let [^IClojureEditor editor (-> this .state deref :editor)
        prev-caret-offset (.caretOffset command)]
    (when (and (.doit command)
               (not (.isInEscapeSequence editor))
               (support/boolean-ccw-pref PreferenceConstants/EXPERIMENTAL_AUTOSHIFT_ENABLED))
      (when-let [{[modif] :modifs offset :offset} 
                 (lu/col-shift (.getParseState editor)
                               {:offset (.offset command)
                                :length (.length command) 
                                :text   (.text command)})]
        (support/add-command! command modif) 
        (set! (.shiftsCaret command) false)
        (set! (.caretOffset command) 
              ;; if prev-caret-offset not set, then no paredit command has been
              ;; applied => let the offset be what AutoAdjust strategy devised
              (if (= -1 prev-caret-offset)
                offset
                prev-caret-offset))))))

