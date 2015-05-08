(ns ccw.editors.clojure.PareditAutoAdjustWhitespaceStrategyImpl
  (:require [paredit [core :refer [paredit]]]
            [clojure.core.incubator :refer [-?>]]
            [ccw.eclipse :refer [boolean-ccw-pref]]
            [paredit.loc-utils :as lu]
            [paredit.text-utils :as tu]
            [paredit.parser :as p]
            [clojure.string :as s]
            [clojure.zip :as zip])
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
               (boolean-ccw-pref PreferenceConstants/EXPERIMENTAL_AUTOSHIFT_ENABLED))
      (when-let [{[modif] :modifs offset :offset}
                 (lu/col-shift (.getParseState editor)
                               {:offset (.offset command)
                                :length (.length command) 
                                :text   (.text command)})]
        (set! (.offset command) (-> modif :offset))
        (set! (.length command) (-> modif :length))
        (set! (.text command) (-> modif :text))
        (when (neg? prev-caret-offset)
          (set! (.shiftsCaret command) false)
        (set! (.caretOffset command) offset))))))

