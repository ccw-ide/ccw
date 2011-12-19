;*******************************************************************************
;* Copyright (c) 2010 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Laurent PETIT - initial API and implementation
;*******************************************************************************/
(ns
  ^{:doc
    "Helper functions for the Clojure Editor.

     Related to parse tree and text buffer:
       - the state holds a ref, which is a map containing keys
         :text                    the full text corresponding to the incremental text buffer
         :incremental-text-buffer the incrementally editable text buffer
         :previous-parse-tree     the previous parse-tree, or nil if no previous parse-tree
         :parse-tree              the parse-tree related to the :text-buffer
         :build-id                the build id, identifying a \"version\" of the parse-tree
                                    (used for determining deltas between 2 updates)
   "}
  ccw.editors.clojure.EditorSupport
  (:require [paredit.parser :as p]
            [paredit.loc-utils :as lu])
  (:import [org.eclipse.jdt.ui PreferenceConstants])
  (:gen-class
    :methods [^{:static true} [updateTextBuffer [Object String Object Object String] Object]
              ^{:static true} [getParseState [String Object] Object]
              ^{:static true} [getParseTree [Object] Object]
              ^{:static true} [getPreviousParseTree [Object] Object]
              ^{:static true} [startWatchParseRef [Object Object] Object]
              ^{:static true} [disposeSourceViewerDecorationSupport [Object] org.eclipse.ui.texteditor.SourceViewerDecorationSupport]
              ^{:static true} [configureSourceViewerDecorationSupport [Object Object] Object]]))

#_(set! *warn-on-reflection* true)

(defn- safe-edit-buffer [buffer offset len text final-text]
  (try
    (p/edit-buffer buffer offset len text)
    (catch Exception e
      (println (str "--------------------------------------------------------------------------------" \newline
                    "Error while editing parsley buffer. offset:" offset ", len:" len ", text:'" text "'" \newline
                    "buffer text:'" (-> buffer (p/buffer-parse-tree 0) lu/node-text) "'"))
      (p/edit-buffer nil 0 0 final-text))))

(defn -updateTextBuffer [r final-text offset len text]
  (let [r (if (nil? r) (ref nil) r), text (or text ""), build-id (if-let [old (:build-id @r)] (inc old) 0)]
    (dosync
      (when-not (= final-text (:text @r))
        (let [buffer (safe-edit-buffer (:incremental-text-buffer @r) offset len text final-text)
              parse-tree (p/buffer-parse-tree buffer build-id)]
          (if-not true #_(= final-text (lu/node-text parse-tree)) ;;;;;;;;; TODO remove this potentially huge perf sucker!
            (do
              (println (str
                         "Doh! the incremental update did not work well. "
                         \newline
                         "offset:" offset ", " "len:" len ", " (str "text:'" text "'")
                         \newline
                         "final-text passed as argument:'" final-text "'" ", but text recomputed from parse-tree: '" (lu/node-text parse-tree) "'"
                         \newline
                         "What happened ? Will throw away the current buffer and start with a fresh one..."))
              (let [buffer (p/edit-buffer nil 0 -1 final-text)
                    parse-tree (p/buffer-parse-tree buffer build-id)]
                (ref-set r {:text final-text, :incremental-text-buffer buffer, :previous-parse-tree (:parse-tree @r), :parse-tree parse-tree, :build-id build-id})))
            (ref-set r {:text final-text, :incremental-text-buffer buffer, :previous-parse-tree (:parse-tree @r), :parse-tree parse-tree :build-id build-id}))
          )))
    r))

(defn -startWatchParseRef [r editor]
  (add-watch r :track-state (fn [_ _ _ new-state]
                              (.setStructuralEditionPossible editor
                                (let [possible? (not (nil? (:parse-tree new-state)))
                                      possible? (or possible? (.isEmpty (:text new-state)))]
                                  possible?)))))

(defn -getParseTree [parse-state] (:parse-tree parse-state))

(defn -getParseState
  "text is passed to check if the contents of r is still up to date or not.
   If not, text will also be used to recompute r on-the-fly."
  [text r]
  (let [rv @r]
    (if (= text (:text rv))
      {:parse-tree (:parse-tree rv), :buffer (:incremental-text-buffer rv)}
      (do
        (println (str "cached parse-tree miss: expected text='" (:text rv) "'" ", text received: '" text "'"))
        (-updateTextBuffer r text 0 -1 text)
        (recur text r)))))

;; Now, I don't like the fact that getting the current and the previous parse tree may lead to incorrect code
;; since they both are dereferencing a ref instead of decomposing a consistent snapshot of a ref
(defn -getPreviousParseTree
  [r]
  (:previous-parse-tree @r))

(defn -disposeSourceViewerDecorationSupport [s]
  (when s
    (doto s .uninstall .dispose)
    nil))

(defn -configureSourceViewerDecorationSupport [support viewer]
		;; TODO more to pick in configureSourceViewerDecorationSupport of AbstractDecoratedTextEditor, if you want ...
  (doto support
		(.setCharacterPairMatcher (.getPairsMatcher viewer))
		(.setMatchingCharacterPainterPreferenceKeys
      PreferenceConstants/EDITOR_MATCHING_BRACKETS
      PreferenceConstants/EDITOR_MATCHING_BRACKETS_COLOR)))