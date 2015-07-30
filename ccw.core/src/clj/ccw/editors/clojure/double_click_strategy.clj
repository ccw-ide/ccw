(ns ccw.editors.clojure.double-click-strategy
  "Implements the IDoubleClickStrategy so that when in code, double clicking
   acts as if the command 'expand right' had been triggered"
  (:require [paredit.core :as pc]
            [ccw.editors.clojure.handlers :as handlers]))

(defn double-clicked [viewer default-strategy]
  (let [parse-tree (-> viewer .getParseState :parse-tree)
        offset (-> viewer .getUnSignedSelection .getOffset)]
    (if (pc/in-code-at-offset? parse-tree offset)
      (handlers/apply-paredit-selection-command viewer :paredit-expand-right)    
      (.doubleClicked default-strategy viewer))))