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

(ns ccw.editors.clojure.handlers
  (:require [paredit.core :as pc]
            [ccw.editors.clojure.clojure-hyperlink-detector :as hlu] ; hlu as HyperLinkUtil
            [ccw.editors.clojure.PareditAutoEditStrategyImpl :as pimpl]
            [ccw.editors.clojure.editor-support :as ed]) 
  (:use [clojure.core.incubator :only [-?>]])  
  (:import
    [org.eclipse.ui.handlers HandlerUtil]
    [ccw.util PlatformUtil]
    [ccw.editors.clojure IClojureEditor
                            SourceRange]))
   
(defn
  editor
  "Return the Clojure editor, if any, associated with the event."
  [event] 
  (PlatformUtil/getAdapter (HandlerUtil/getActivePart event) IClojureEditor))

(defn- set-mode [^IClojureEditor editor mode]
  (when mode (swap! (.getState editor) assoc :mode mode)))

;; TODO remove duplications with appli-paredit-command
(defn- apply-paredit-selection-command [^IClojureEditor editor command-key]
  (let [{:keys #{length offset}} (bean (.getSignedSelection editor))
        caret-at-left (if-not (zero? length)
                        (neg? length)
                        (:bias @(.getState editor)))
        length (if caret-at-left (- length) length)
        offset (if caret-at-left (- offset length) offset)
        text  (.get (.getDocument editor))]
        (when-let [r (apply pc/paredit 
                       command-key
                       (.getParseState editor) 
                       {:text text :offset offset :length length :caret-at-left caret-at-left}
                       (pimpl/paredit-options command-key))]
          (let [[new-offset new-length] (if-let [[from to] (:selection r)]
                                          [from (- to from)]
                                          ^:legacy [(:offset r) (:length r)])
                updates (fn [state]
                          (update-in state [:selection-history] conj [offset length]))
                updates (cond
                          (not (zero? new-length))
                          (comp updates #(assoc % :bias (neg? new-length)))
                          (not= (+ new-length new-offset) (+ length offset))
                          (comp updates #(assoc % :bias (< (+ new-length new-offset) (+ length offset))))
                          :else updates)
                updates (if-let [mode (:mode r)]
                          (comp updates #(assoc % :mode mode))
                          updates)]
            (swap! (.getState editor) updates)
            (binding [ed/*random-selection* false]
               (.selectAndReveal editor new-offset new-length))))))

(defn do-select [editor offset]
  (let [r (pc/struct-select (.getParseState editor) offset)
        [from to] (:selection r)]
    (set-mode editor (:mode r))
    (binding [ed/*random-selection* false]
      (.selectAndReveal editor from (- to from)))))

(defn editor-text 
  "Return the current text, cursor offset and selection length
   in the Editor editor, in the form
   {:text text, :offset offset, :length length}"
  [editor]
  (assoc (bean (.getUnSignedSelection editor))
         :text (.get (.getDocument editor))))

(defn ^org.eclipse.text.edits.TextEdit to-text-edit
  "Creates a TextEdit from a collection of edits."
  [edits]
  (let [text-edit (org.eclipse.text.edits.MultiTextEdit.)
        edits (pc/disjoint-edits edits)]
    (doseq [{:keys [text offset length]} edits]
      (.addChild text-edit (org.eclipse.text.edits.ReplaceEdit. offset length text)))
    text-edit))

;; TODO remove duplication with PareditAutoEditStrategy (or not)
(defn- apply-paredit-command 
  "f is a function which takes the parse state, and the editor state as a
   map of :text, :offset and :length keys.
   f returns a map with the modifications as a sequential of :text, :offset, :set
   maps under the key :modifs, as well as global final offset in the :offset key,
   and selection in the :length key"
  [editor f]
  (let [result (f (.getParseState editor) (editor-text editor))]
    (if (:selection result)
      (let [{edits :edits selection :selection} result
            [caret-offset end-offset] (pc/update-selection selection edits)]
        (->
          (.getDocument editor)
          (org.eclipse.jface.text.RewriteSessionEditProcessor.
            (to-text-edit edits)
            (bit-or org.eclipse.text.edits.TextEdit/CREATE_UNDO org.eclipse.text.edits.TextEdit/UPDATE_REGIONS))
          .performEdits)
        (set-mode editor (:mode result))
        (.selectAndReveal editor caret-offset (- end-offset caret-offset)))
      ^:legacy
      (when-let [modif (-?> result :modifs first)] ;; TODO what if more than one modif in :modifs ?
        (let [{:keys #{length offset text}} modif
              document (-> editor .getDocument)]
          (.replace document offset length text)
          (.selectAndReveal editor (:offset result) (:length result)))))))

(defn- paredit-fn 
  ([command-key] (paredit-fn command-key nil))
  ([command-key options] #(apply pc/paredit command-key %1 %2 options)))

(defn forward-slurp [_ event] 
  (apply-paredit-command (editor event) 
                         (paredit-fn :paredit-forward-slurp-sexp)))

(defn backward-slurp [_ event] 
  (apply-paredit-command (editor event) 
                         (paredit-fn :paredit-backward-slurp-sexp)))

(defn forward-barf [_ event] 
  (apply-paredit-command (editor event) 
                         (paredit-fn :paredit-forward-barf-sexp)))

(defn backward-barf [_ event] 
  (apply-paredit-command (editor event) 
                         (paredit-fn :paredit-backward-barf-sexp)))

(defn raise [_ event] 
  (apply-paredit-command (editor event) 
                         (paredit-fn :paredit-raise-sexp)))
(defn split [_ event]
  (apply-paredit-command (editor event)
                         (paredit-fn :paredit-split-sexp)))
(defn splice [_ event]
  (apply-paredit-command (editor event)
                         (paredit-fn :paredit-splice-sexp)))
(defn join [_ event]
  (apply-paredit-command (editor event)
                         (paredit-fn :paredit-join-sexps)))
(defn leaf-left [_ event]
  (apply-paredit-selection-command (editor event)
                         :leaf-left))
(defn leaf-right [_ event]
  (apply-paredit-selection-command (editor event)
                         :leaf-right))
(defn leaf-up [_ event]
  (apply-paredit-selection-command (editor event)
                         :leaf-up))
(defn expand-left [_ event] 
  (apply-paredit-selection-command (editor event)
                                   :paredit-expand-left))
(defn expand-right [_ event] 
  (apply-paredit-selection-command (editor event) 
                                   :paredit-expand-right))
(defn expand-up [_ event] 
  (apply-paredit-selection-command (editor event)
                                   :paredit-expand-up))
(defn indent-selection [_ event]
  (apply-paredit-command 
    (editor event) 
    (paredit-fn :paredit-indent (pimpl/paredit-options :paredit-indent))))

(defn toggle-structural-edition-mode [_ event]
  (.toggleStructuralEditionMode (editor event)))

(defn toggle-show-rainbow-parens [_ event]
  (.toggleShowRainbowParens (editor event)))

(defn toggle-line-comment [_ event]
  (apply-paredit-command (editor event)
                         (paredit-fn :paredit-toggle-line-comment)))

(defn clipboard-text [display]
  (let [clipboard (org.eclipse.swt.dnd.Clipboard. display)]
    (try
      (let [text-transfer (org.eclipse.swt.dnd.TextTransfer/getInstance)
            text (.getContents clipboard text-transfer)]
        text)
      (finally (.dispose clipboard)))))

(defn 
  smart-paste
  "Applies paredit.cljs's smart-paste iff the editor is in strict mode and
   we're not currently in the context of an escape sequence.
   Expects to be run from the GUI thread"
  ([_ event] (smart-paste (editor event)))
  ([editor]
    (apply-paredit-command 
      editor
      #(pc/smart-paste
         %1
         %2
         (clipboard-text (org.eclipse.swt.widgets.Display/getCurrent))
         (and
           (.isEscapeInStringLiteralsEnabled editor)
           (not (:esc @(.getState editor))))))))

(defn- swap!'
  "Like swap! but returns the value of the atom before update."
  [a f & args]
  (loop []
    (let [v @a
          v' (apply f v args)]
      (if (compare-and-set! a v v')
        v
        (recur)))))

(defn do-select-last [editor]
  (let [{:keys [selection-history]} (swap!' (.getState editor) update-in
                                      [:selection-history] #(if (empty? %) % (pop %)))
        [offset length] (peek selection-history)]
    (binding [ed/*random-selection* false]
      (.selectAndReveal editor offset length))))

(defn select-last [_ event] 
  (do-select-last (editor event)))

(defn open-declaration [_ event]
  (let [editor (editor event)
        caret-offset (-> editor .getUnSignedSelection .getOffset)]
    (if-let [[{open :open}] (hlu/detect-hyperlinks caret-offset editor)]
      (open))))

(defn content-assist [_ event]
  (when-let [editor (editor event)]
    ; TODO validateEditorInputState () : if editor read-only ...
    (org.eclipse.swt.custom.BusyIndicator/showWhile
      (.getDisplay (HandlerUtil/getActiveShell event))
      #(-> editor 
         (.getAdapter org.eclipse.jface.text.ITextOperationTarget) 
         (.doOperation org.eclipse.jface.text.source.ISourceViewer/CONTENTASSIST_PROPOSALS)))))

(defn structedit-key-event [^org.eclipse.swt.events.VerifyEvent event ^ccw.editors.clojure.ClojureSourceViewer source-viewer parse-state document]
  #_(binding [*out* out]
    (prn (.character event) (java.lang.Integer/toHexString (int (.character event)))
      (bit-and (bit-xor org.eclipse.swt.SWT/MODIFIER_MASK org.eclipse.swt.SWT/SHIFT) (.stateMask event)))
    #_(-> parse-state :parse-tree lu/parsed-root-loc (lu/leave-loc-for-offset-common (-> source-viewer .getSignedSelection .getOffset) true) lu/parse-leave clojure.zip/node :tag prn)
    #_(-> parse-state :parse-tree lu/parsed-root-loc (lu/leave-loc-for-offset-common (-> source-viewer .getSignedSelection .getOffset)) lu/parse-leave clojure.zip/node :tag prn)
    #_(prn (.getSignedSelection source-viewer)))
  nil)

(defn fire-structedit-event [source-viewer command-name]
  (apply-paredit-selection-command source-viewer (keyword command-name)))