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

(ns ^{:author "Andrea Richiardi" }
  ccw.editors.clojure.hovers.debug-hover
  (:import [org.eclipse.jface.text Region
                                   ITextHover
                                   ITextHoverExtension
                                   ITextHoverExtension2]
           [ccw.editors.clojure IClojureEditor]
           [ccw CCWPlugin])
  (:require [ccw.core.trace :refer [trace]]
            [ccw.interop :refer [simple-name]]))

(defn create-debug-hover [& params]
  (reify
    ITextHover
    (getHoverInfo [this text-viewer hover-region]
      (trace :support/hover (str "[DEBUG-HOVER] " (simple-name this) ".getHoverInfo called with:\n"
                                 "text-viewer -> " (.toString text-viewer) "\n"
                                 "region -> " (.toString hover-region) "\n")))

    (getHoverRegion [this text-viewer offset]
      (trace :support/hover (str "[DEBUG-HOVER] " (simple-name this) ".getHoverRegion called with:\n"
                                 "text-viewer -> "(.toString text-viewer) "\n"
                                 "offset -> " offset "\n")))

    ITextHoverExtension
    (getHoverControlCreator [this]
      (trace :support/hover (str "[DEBUG-HOVER] " (simple-name this) ".getHoverControlCreator called.")))

    ITextHoverExtension2
    (getHoverInfo2 [this text-viewer hover-region]
      (trace :support/hover (str "[DEBUG-HOVER] " (simple-name this) ".getHoverInfo2 called with:\n"
                                 "text-viewer -> " (.toString text-viewer) "\n"
                                 "region -> " (.toString hover-region) "\n")))))
