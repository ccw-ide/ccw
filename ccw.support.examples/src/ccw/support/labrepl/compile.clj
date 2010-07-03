;*******************************************************************************
;* Copyright (c) 2010 Stephan Muehlstrasser.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors: 
;*    Stephan Muehlstrasser - initial API and implementation
;*******************************************************************************/
(ns ccw.support.labrepl.compile)

(defn all []
  (dorun   
    (map
      compile
      [
       'ccw.support.labrepl.wizards.LabreplAntLogger
       'ccw.support.labrepl.wizards.LabreplCreateProjectPage
       'ccw.support.labrepl.wizards.LabreplCreationOperation
       'ccw.support.labrepl.wizards.LabreplCreationWizard
       ])))

