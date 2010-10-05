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
  (:gen-class
    :methods [^{:static true} [updateParseRef [String Object] Object]
              ^{:static true} [getParser [String Object] Object]]
    :impl-ns ccw.editors.antlrbased.EditorSupportImpl))
