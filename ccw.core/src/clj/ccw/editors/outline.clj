;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Laurent PETIT - initial API and implementation
;*******************************************************************************/

(ns ^{:author "Laurent Petit"}
  ccw.editors.outline
  "Clojure back-end for the java class ClojureOutlinePage"
  (:refer-clojure :exclude [read])
  (:require [ccw.core.trace :as t])
  (:import clojure.lang.LineNumberingPushbackReader)
  (:import java.io.StringReader)
  (:import clojure.lang.LispReader$ReaderException)
  (:import ccw.CCWPlugin))

(defn silent-data-reader
  "just return the underlying data structure"
  [tag form] form)

(defn read
  "Reads a form, and returns eof if a ReaderException occurs"
  [reader eof]
  (try
    (clojure.core/read reader false eof)
    (catch LispReader$ReaderException e
      ; once a syntax error occurs (often because of a namespaced keyword)
      ; there's little chance that the rest of the data will be worthwhile...
      (t/trace :log/trace "Failed to read outline 'til the end" e)
      ;(CCWPlugin/logWarning "Failed to read outline 'til the end", e)
      eof)))

(defn read-forms [s]
  (binding [*read-eval* false
           *default-data-reader-fn* silent-data-reader]
    (with-open [pushback-reader (LineNumberingPushbackReader. (StringReader. s))]
      (let [eof (Object.)]
        (into [] (map #(if (instance? java.util.List %) % [%])
                   (take-while
                     (partial not= eof)
                     (repeatedly #(read pushback-reader eof)))))))))
