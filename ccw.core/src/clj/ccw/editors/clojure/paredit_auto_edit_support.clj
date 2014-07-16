(ns ccw.editors.clojure.paredit-auto-edit-support
  (:use [clojure.core.incubator :only [-?>]])  
  (:import [org.eclipse.jface.text DocumentCommand]
           [org.eclipse.jface.preference IPreferenceStore]
           [ccw.editors.clojure IClojureEditor]))

(defn init
  "State Initialization for a new AutoEditInstance"
  [editor preference-store] (ref {:editor editor :prefs-store preference-store}))

(defn ccw-prefs
  ^IPreferenceStore []
  (.getCombinedPreferenceStore (ccw.CCWPlugin/getDefault)))

(defn boolean-ccw-pref 
  "Get the value of a boolean Preference set for CCW"
  ([pref-key] (boolean-ccw-pref (ccw-prefs) pref-key))
  ([ccw-prefs pref-key] (.getBoolean ccw-prefs pref-key)))

(defn add-command! [command modif]
  (.addCommand command (:offset modif)
                       (:length modif)
                       (:text modif)
                       nil))



(defn into-command
  "Populate a simple (eg no nested commands) DocumetCommand with a collection of edits."
  [^DocumentCommand command edits]
  {:pre [(= (.getCommandCount command) 1) (seq edits)]
   :post [(= (.getCommandCount command) (count edits))]}
  (let [[{:keys [text offset length]} & more-edits] edits]
    (doto command
      (-> .text (set! text))
      (-> .offset (set! offset))
      (-> .length (set! length)))
    (doseq [{:keys [text offset length]} more-edits]
      (.addCommand command offset length text nil))))

(defn compare-edits
  "Compare disjoint edits. Edits being assumed disjoint or equal, only their midpoints are compared."
  [a b]
  (- (+ (:offset a) (:length a)) (+ (:offset b) (:length b))))

(defn update-selection [[offset end-offset] edits]
  ;; only insertion edits (whose length is zero) can broaden a selection
  (let [edits (into (sorted-set-by compare-edits) edits)
        start {:offset offset :length 0}
        end {:offset end-offset :length 0}
        before-sel (subseq edits < start)
        shift (reduce (fn [shift {:keys [text length]}]
                        (+ shift (- (count text) length)))
                0 before-sel)
        [offset' shift] (if-let [{:keys [text broaden]} (get edits start)]
                          (let [shift' (+ shift (count text))]
                            [(+ offset (if broaden shift shift')) shift'])
                          [(+ offset shift) shift])
        shift (reduce (fn [shift {:keys [text length]}]
                        (+ shift (- (count text) length)))
                shift (subseq edits > start < end))
        end-offset' (if-let [{:keys [text broaden]} (get edits end)]
                      (+ end-offset shift (if broaden (count text) 0))
                      (+ end-offset shift))]
    [offset' end-offset']))

(defn apply-modif!
  "Apply modification in result to command for editor."
  [^IClojureEditor editor ^DocumentCommand command result]
  (if (:selection result)
    (let [{edits :edits selection :selection} result
          [caret-offset end-offset] (update-selection selection edits)]
      (into-command command edits)
      (set! (.shiftsCaret command) false)
      (set! (.caretOffset command) caret-offset)
      (.selectAndReveal editor caret-offset (- end-offset caret-offset)))
    ^:legacy
    (when (and result (not= :ko (-> result :parser-state))) ; is there still such a thing as :parser-state?
      (if-let [edits (seq (:modifs result))]
        (do
          (into-command command edits)
          ;; TODO fix code below where :offset/:length are sometimes an edit and sometimes a selection
          (set! (.shiftsCaret command) false)
          (set! (.caretOffset command) (:offset result))
          (when-not (zero? (:length result)) ;;; WHY when-not zero?
            (.selectAndReveal editor (:offset result) (:length result))))
        (do
          #_(set! (.offset command) (:offset result))
          (set! (.length command) 0)
          (set! (.text command) "")
          (set! (.doit command) false))))))
