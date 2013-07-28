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
    [ccw.editors.clojure IClojureEditor PareditAutoAdjustWhitespaceStrategy]))
   
#_(set! *warn-on-reflection* true)

(defn init
  [editor preference-store] (ref {:editor editor :prefs-store preference-store}))

; TODO l'offset fonctionne pas si plusieurs niveaux d'indentation
; TODO backspace ne fonctionne pas devant une form top level

;- l'offset offset a été décalé de delta caractères
;- soit loc la loc pour offset
;- soit loc-p la loc parente de loc
;- appliquer le décalage delta à tous les débuts de ligne inclus dans loc-p, à partir d'offset
;- s'arrêter après épuisement des lignes contenues dans loc-p, ou si une des lignes a du contenu plus à gauche que delta
;- si au moins une ligne plus à gauche que delta : c'est fini
;- sinon, on recommence avec le père de loc-p, en partant de l'offset de fin (initial) de loc-p, et avec delta

; HYPOTHESE : l'offset qui se decale marque le debut d'un noeud (= offset-start)
;             SI PAS VRAI => ON FAIT RIEN
;             mais non, pas toujours possible, par ex. si on ajoute
;             un espace au milieu d'espaces, ou une lettre au milieu d'un symbole
;             => la regle c'est: si (not= offset-start) => on prend le frere suivant
;
; ATTENTION, dans les freres il y aura des :comment qui vont poser probleme
;            il faut les traiter specialement, je le crains
;
; Plutôt que de faire des duplications, plutôt implementer un decalage
; rapide par offset (pas optimise pour l'instant) pour se retrouver sur le
; bon noeud. Ou implémenter un zipper intelligent qui calcule ce qu'il faut
; pour les offsets (au moment de la construction du noeud, rajouter
; les meta donnees :cumulative-count)
;
; =>SIMPLIFIER l'algorithme en utilisant shift-whitespace sur les freres de l'offset,
;              puis éventuellement les freres du pere si la fin du père a été poussée à gauche, etc.
;              et toujours garder le fait que que dès qu'on tombe sur un élément plus à "gauche" qu'offset,
;              on arrête la propagation dans les freres (et ça bloque aussi la progagation dans les peres, du coup)
;              (si on fait pas ça, alors ouille, on parcourra systématiquement tout le fichier)


; BUGS:
; - a comment "stops" the propagation of the shift if after the comment we're
;     at the first column
; - inserting parens or double quotes or chars shifts the whole content: baad
; - Suppr. at the start of a document => String index out of range: -1
; TEST CASES:
; - after a comment

(defn next-node-loc
  "Get the loc for the node on the right, or if at the end of the parent,
   on the right of the parent. Skips punct nodes. Return nil if at the far end."
  [loc]
  (if-let [r (zip/right loc)]
    (if (lu/punct-loc? r)
      (recur r)
      r)
    (when-let [p (zip/up loc)]
      (recur p))))

(defn find-loc-to-shift
  "Starting with loc, find to the right, and to the right of parent node, etc.
   a non-whitespace loc. If a newline is found before, return nil."
  [loc]
  (let [continue-search (fn [loc] 
                          ;(println "analysing loc" (pr-str (clean-tree (zip/node loc))))
                          (let [r (and loc (not (lu/whitespace-newline? loc)))]
                           ; (println "result of analyse: continue-search" r)
                            r))
        locs (take-while continue-search (iterate next-node-loc loc))]
    (first (remove lu/whitespace? locs))))

(defn empty-diff?
  "Is the text diff empty (nothing replaced and nothing added)?" 
  [diff]
  (and (zero? (:length diff))
       (zero? (count (:text diff)))))

(defn whitespace-end-of-line?
  "For text s, starting at offset, is the remaining of the
   line only made of whitespace?"
  [s offset]
  (let [eol-offset (tu/line-stop s offset)
        eol (subs s offset eol-offset)]
    (s/blank? eol)))

;(use 'paredit.tests.utils)

(defn customizeDocumentCommand 
  "Work only if no command has been added via (.addCommand)"
  [^PareditAutoAdjustWhitespaceStrategy this, #^IDocument document, #^DocumentCommand command]
  (when (and (.doit command)
             (not (.isInEscapeSequence editor)))
    (let [^IClojureEditor editor (-> this .state deref :editor)
          {:keys [parse-tree buffer]} (.getParseState editor)
          text-before (lu/node-text parse-tree)
          ;_ (println "text-before:" (str "'" text-before "'"))
          parse-tree (-> buffer
                       (p/edit-buffer (.offset command) (.length command) (.text command))
                       (p/buffer-parse-tree 0))
          text (lu/node-text parse-tree)
          ;_ (println "text:" (str "'" text "'"))
          offset (+ (.offset command) (count (.text command)))
          offset-before (+ (.offset command) (.length command))
          col (tu/col text offset)
          delta (- col
                   (tu/col text-before offset-before))
        ; _ (println "delta:" delta)
         rloc (lu/parsed-root-loc parse-tree)
         loc (lu/loc-for-offset rloc offset)
        ; _ (if loc (println "loc for offset:" 
        ;                    (pr-str (clean-tree (zip/node loc)))))
         loc (if (or 
                   (= (lu/start-offset loc) offset)
                   (whitespace-end-of-line? text offset))
               loc
               (next-node-loc loc))
         loc (find-loc-to-shift loc)
         ;_ (when-not loc (println "no loc found"))
         ]
      (when  loc
        (let [col (- (lu/loc-col loc) delta)
           ;   _ (println "final loc node:" (pr-str (clean-tree (zip/node loc))))
           ;   _ (println "col" col)
           ;   _ (println "(lu/loc-col loc)" (lu/loc-col loc))
              [shifted-loc _] (lu/propagate-delta loc col delta)
              shifted-text (lu/node-text (zip/root shifted-loc))
              ;_ (println "shifted-text:" (with-out-str (pr shifted-text)))
              ;_ (println "text        :" (with-out-str (pr text)))
              loc-diff (tu/text-diff text shifted-text)
              ;_ (println "loc-diff" (with-out-str (pr loc-diff)))
              diff (update-in 
                     loc-diff
                     [:offset] + (.length command) (- (count (.text command))))
              ;_ (println "diff" (with-out-str (pr diff)))
              ]
          (when-not (empty-diff? loc-diff)
            (.addCommand command 
              (:offset diff)
              (:length diff)
              (:text diff)
              nil)
            (set! (.shiftsCaret command) false)
            (set! (.caretOffset command) offset)))))))

