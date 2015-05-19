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
(ns ccw.compile
  "This namespace is here only to force require some namespaces that otherwise
   would not necessarily have been compiled transitively"
   (:require [cider.nrepl] ; cider.nrepl is not compiled through ccw.core.launch since it is only dynamically required
   ))

