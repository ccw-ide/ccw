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

(ns ccw.editors.antlrbased.EditorSupport 
  (:require [paredit.parser :as p])
  (:gen-class
    :methods [^{:static true} [updateParseRef [String Object] Object]
              ^{:static true} [getParser [String Object] Object]]))

(defn -updateParseRef [text r]
  (let [r (if (nil? r) (ref nil) r)] 
    (dosync (ref-set r {:text text :parser (future (p/parse text))}))
    r))

(defn -getParser [text r]
  (if (= text (-> r deref :text))
    (-> r deref :parser deref)
    (do
      (println "cached parser miss !")
      (-updateParseRef text r)
      (-> r deref :parser deref))))