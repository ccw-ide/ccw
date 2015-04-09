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
  (:import clojure.lang.LineNumberingPushbackReader)
  (:import java.io.StringReader)
  (:import clojure.lang.LispReader$ReaderException)
  (:import ccw.CCWPlugin))

(defn silent-data-reader
  "just return the underlying data structure"
  [tag form] form)

(defn read-forms [s]
  (binding [*read-eval* false
           *default-data-reader-fn* silent-data-reader]
    (with-open [pushback-reader (LineNumberingPushbackReader. (StringReader. s))]
      (let [eof (Object.)]
        (into [] (take-while
                   (partial not= eof)
                   (repeatedly #(read pushback-reader false eof))))))))
