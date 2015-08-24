;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial implementation (code reviewed by Laurent Petit)
;*******************************************************************************/
(ns ^{:author "Andrea Richiardi"}
  ccw.editors.clojure.folding-support
  "Folding support functions"
  (:refer-clojure :exclude [tree-seq read-string])
  (:require [ccw.core.trace :refer [trace]]
            [clojure.edn :as edn :refer [read-string]]
            [clojure.java.data :refer :all]
            [clojure.zip :as z]
            [paredit.loc-utils :as lu]
            [ccw.editors.clojure.editor-support :as es]
            [ccw.eclipse :refer [string-ccw-pref
                                 preference!]]
            [ccw.swt :as swt :refer [doasync]])
  (:import org.eclipse.e4.core.contexts.IEclipseContext
           org.eclipse.core.databinding.observable.list.WritableList
           org.eclipse.jface.text.IRegion
           org.eclipse.jface.text.Position
           org.eclipse.jface.text.source.Annotation
           org.eclipse.jface.text.source.projection.ProjectionAnnotationModel
           org.eclipse.jface.text.source.projection.ProjectionAnnotation
           ccw.core.StaticStrings
           ccw.editors.clojure.ClojureEditorMessages
           ccw.editors.clojure.IClojureEditor
           ccw.preferences.PreferenceConstants
           ccw.editors.clojure.folding.FoldingModel
           ccw.editors.clojure.folding.FoldingDescriptor))

(defn- any-enabled?
  "Return true if at least one descriptor is enabled, false otherwise."
  [descriptors]
  (not (not-any? :enabled descriptors)))

(defn- enabled-tags
  "Collect and return a set of the loc tags that are associated with
  enabled descriptors."
  [descriptors]
  (into #{} (reduce concat (map :loc-tags (filter :enabled descriptors)))))

(defn- locs-with-tags
  "Given an already parsed tree (root loc in paredit) and a set of tags,
  returns the seq of locs which contains them or nil if no match was
  found."
  [rloc tag-set]
  (seq (filter #(get tag-set (lu/loc-tag %1)) (lu/next-leaves rloc))))

(defn- loc->position
  [loc]
  (let [start-offset (lu/start-offset loc)
        end-offset (lu/end-offset loc)]
    (Position. start-offset (- end-offset start-offset))))

(defn- pos->vec
  "Given a org.eclipse.jface.text.Position, build a vector [offset
  length]."
  [^Position pos]
  [(.offset pos) (.length pos)])

(defn- seq->pos
  "Given a seq of two elements which represent offset and length, build
  a org.eclipse.jface.text.Position."
  [coll]
  (Position. (first coll) (second coll)))

;; (defn enclose?
  ;; "Return true if pos1 encloses pos2. This implementation returns false
  ;; if the Positions have same start/end offset (e.g.: [2 40] does not
  ;; include [2 30] and [4 10] does not include [5 9])."
  ;; [^Position pos1 ^Position pos2]
  ;; (let [of1 (.offset pos1)
        ;; of2 (.offset pos2)
        ;; ln1 (.length pos1)
        ;; ln2 (.length pos2)]
    ;; (cond
      ;; (<= of2 of1) false
      ;; (>= (+ of2 ln2) (+ of1 ln1)) false
      ;; :else true)))

;; (defn- overlap?
  ;; "Return true if at least one position overlaps with another inside the
  ;; positions parameter. False otherwise.
  ;; Note1: The cases where the position vector is empty or contains only
  ;; one element are considered not overlapping.
  ;; Note2: if pos1 and pos2 have just boundaries in common they do not
  ;; overlap."
  ;; ([positions] (overlap? positions false))
  ;; ([positions overlapping?]
   ;; (cond
     ;; overlapping? overlapping?
     ;; (<= (count positions) 1) overlapping?
     ;; :else (let [pos1 (first positions)
                 ;; pos-rest (rest positions)
                 ;; pos1-against-rest (for [pos2 pos-rest]
                                     ;; (.overlapsWith pos1 (.offset pos2) (.length pos2)))]
             ;; (recur pos-rest (not (every? false? pos1-against-rest)))))))

;;;;;;;;;;;;;;;;;;;;;;;
;;; Parsing helpers ;;;
;;;;;;;;;;;;;;;;;;;;;;;

(defn- inc-token-occ
  "Increment the token :occurrences in the map."
  [token-map token]
  (update-in token-map [token :occurrences] inc))

(defn- dec-token-occ
  "Decrement the token :occurrences in the map."
  [token-map token]
  (update-in token-map [token :occurrences] dec))

(defn- update-token-map
  "Initialize the token map assigning {:loc token-loc and :occurrences}
  to the input token."
  [token-map token token-loc]
  (let [old-token (get token-map token)]
    (if old-token
      (inc-token-occ token-map token)
      (assoc token-map token {:loc token-loc :occurrences 1}))))

(defn- token-data-info
  "Convert token data {:loc :occurrences ...} into a human readable
  map which will contain useful info only. Useful for debugging."
  [token-data]
  (let [l (:loc token-data)
        occ (:occurrences token-data)]
    {:text (lu/loc-text l)
     :occs occ
     :start (lu/start-offset l)
     :end (lu/end-offset l)}))

(defn- next-is-newline?
  "Given a loc, return true if the next loc is a newline (according to
  paredit's loc-utils). False otherwise."
  [loc]
  (lu/newline? (z/next loc)))

(def ^:private son-of-a-multi-line-loc?
  "Return true if the input loc is children of a multi-line loc, that is,
  its zip/up is on multiple lines, according to paredit
  single-line-loc?. False otherwise."
  (complement (comp lu/single-line-loc? z/up)))

(defn- folding-range
  "Compute the folding range given the loc of the opening token and
  the loc of the closing token. Returns org.eclipse.jface.text.Position
  or nil if something goes wrong."
  [open-token-loc close-token-loc]
  (let [open-token-end (lu/end-offset open-token-loc)
        close-token-end (lu/end-offset close-token-loc)]
    (trace :editor/text (str "computing folding for '" (lu/loc-text open-token-loc) "' -> offset end at " open-token-end
                             " and '" (lu/loc-text close-token-loc) "' -> offset start at " close-token-end))
    ; TODO - AR handle custom positions instances (see jdt's JavaElementPosition)
    (if (> close-token-end open-token-end)
      (Position. open-token-end (- close-token-end open-token-end))
      nil)))

(declare match-paren
         match-double-apex
         ;; match-bracket
         )

(defn- parse-locs
  "Give a loc seq, extract the locs that are going to be used for folding.
  Returns another seq or nil. This function has been implemented for
  using through the tail-call optimized trampoline and carries two
  accumulating parameters.
  1) open-token-loc-map will accumulate the open token
  data (:loc :occurrences ...)
  2) positions will accumulate the positions used for folding"
  [loc-seq open-token-map positions]
  (if (seq loc-seq)
    (let [loc (first loc-seq)
          loc-token (lu/loc-text loc)]
      (trace :editor/text (str "current -> token " loc-token " start " (lu/start-offset loc) " end " (lu/end-offset loc) "\n"
                          "token-map -> " (pr-str (map #(token-data-info (second %1)) open-token-map))))
      #(case loc-token
         "(" (match-paren (rest loc-seq) (update-token-map open-token-map "(" loc) positions)
         "\"" (match-double-apex (rest loc-seq) (update-token-map open-token-map "\"" loc) positions)
         ")" (match-paren loc-seq open-token-map positions) ))
    positions))

(defn- match-paren
  [loc-seq open-token-map positions]
  (if (seq loc-seq)
    (let [loc (first loc-seq)
          loc-token (lu/loc-text loc)]
      (trace :editor/text (str "current -> token " loc-token " start " (lu/start-offset loc) " end " (lu/end-offset loc) "\n"
                          "token-map -> " (pr-str (map #(token-data-info (second %1)) open-token-map))))
      #(case loc-token
         ;; Match
         ")" (let [open-token-data (get open-token-map "(")]
               (if (= (:occurrences open-token-data) 1)
                 (parse-locs (rest loc-seq) (dissoc open-token-map "(")
                             (conj positions (folding-range (:loc open-token-data) loc)))
                 (match-paren (rest loc-seq) (dec-token-occ open-token-map "(") positions)))
         ;; Other cases
         "(" (match-paren (rest loc-seq) (inc-token-occ open-token-map "(") positions)
         "\"" (match-double-apex (rest loc-seq) (update-token-map open-token-map "\"" loc) positions)))
    positions))

(defn- match-double-apex
  [loc-seq open-token-map positions]
  (if (seq loc-seq)
    (let [loc (first loc-seq)
          loc-token (lu/loc-text loc)]
      (trace :editor/text (str "current -> token " loc-token " start " (lu/start-offset loc) " end " (lu/end-offset loc) "\n"
                          "token-map -> " (pr-str (map #(token-data-info (second %1)) open-token-map))))
      #(case loc-token
         ;; Match
         "\"" (if-let [open-token-data (get open-token-map "\"")]
                (if (= (:occurrences open-token-data) 1)
                  (parse-locs (rest loc-seq) (dissoc open-token-map "\"")
                              (conj positions (folding-range (:loc open-token-data) loc)))
                  (match-double-apex (rest loc-seq) (dec-token-occ open-token-map "\"") positions)))
         ;; Other cases
         "(" (match-paren (rest loc-seq) (inc-token-occ open-token-map "(") positions)))
    positions))

(defn- folding-positions
  "Compute a set of positions (offset, length, wrapped in a
  org.eclipse.jface.text.Position) of the folding point according to the
  folding descriptors."
  [parse-state descriptors]
  (when (any-enabled? descriptors)
    (let [parse-tree (es/getParseTree parse-state)
          root-loc (lu/parsed-root-loc parse-tree)
          loc-tags (enabled-tags descriptors)
          loc-seq (locs-with-tags root-loc loc-tags)
          multi-line-locs (filter son-of-a-multi-line-loc? loc-seq)] ; TODO - get set from preference
      (into #{} (keep identity (trampoline parse-locs multi-line-locs {} ()))))))

;;;;;;;;;;;;;;;;;;;
;;; Java Interop ;;
;;;;;;;;;;;;;;;;;;;

(defmethod to-java [FoldingDescriptor clojure.lang.APersistentMap] [clazz props]
  (doto (FoldingDescriptor.
         (name (:id props))
         (:label props)
         (:enabled props)
         (:description props)
         (:loc-tags props))))

(defmethod from-java FoldingDescriptor [^FoldingDescriptor instance]
  {:id (keyword (.getId instance))
   :label (.getLabel instance)
   :enabled (.isEnabled instance)
   :description (.getDescription instance)
   :loc-tags (.getTags instance)})

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Preference helpers ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- read-descriptors-from-preference!
  "Load what is in the preferences, returns the descriptors accordingly."
  []
  (edn/read-string {:eof ""} (string-ccw-pref PreferenceConstants/EDITOR_FOLDING_DESCRIPTORS)))

(defn- persist-descriptors!
  "Persist the input descriptors in the preferences."
  [descriptors]
  (preference! PreferenceConstants/EDITOR_FOLDING_DESCRIPTORS (pr-str descriptors)))

;;;;;;;;;;;;;;
;;; Public ;;;
;;;;;;;;;;;;;;

(defn compute-folding!
  "Update the ProjectionAnnotationModel's positions (offset, length,
  wrapped in a org.eclipse.jface.text.Position) used for folding in the
  input IClojureEditor. The UI part is executed using swt/doasync."
  [^IClojureEditor editor ^IRegion region]
  (let [descriptors (read-descriptors-from-preference!)]
    (if (any-enabled? descriptors)
      (let [positions (folding-positions (.getParseState editor) descriptors)
            additions (zipmap (repeatedly #(ProjectionAnnotation.)) positions)] 
        (trace :editor/text (str "computed additions: " additions)) 
        ;; TODO - AR incrementally handle additions, removals
        (let [result-promise (swt/doasync
                              (when-let [^ProjectionAnnotationModel model (some-> editor (.getProjectionAnnotationModel))]
                                (.removeAllAnnotations model)
                                (.modifyAnnotations model nil additions nil)
                                (.markDamagedAndRedraw editor)))]
          (when (instance? Throwable @result-promise)
            (trace :editor/text "Exception caught while updating the editor" @result-promise))))
      (trace :editor/text (str "Both projection and descriptors were disabled, skipping folding computation")))))

(defn init-injections
  "Set a new instance of FoldingModel in the input context."
  [^IEclipseContext context]
  (trace :editor/text (str "Initializing folding injections..."))
  (.set context StaticStrings/CCW_CONTEXT_VALUE_FOLDINGMODEL
        (reify
          FoldingModel
          (getObservableDescriptors [this]
            (WritableList.
             (map #(to-java FoldingDescriptor %1) (read-descriptors-from-preference!))
             FoldingDescriptor))

          (persistDescriptors [this list]
            (let [descriptors (map #(from-java %1) list)]
              (persist-descriptors! descriptors))))))
