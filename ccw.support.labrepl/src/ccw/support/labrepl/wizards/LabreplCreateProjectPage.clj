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
(ns ccw.support.labrepl.wizards.LabreplCreateProjectPage
  (:gen-class
   :extends org.eclipse.ui.dialogs.WizardNewProjectCreationPage
   :init myinit
   :post-init mypostinit
   :state state))

(defn- -myinit
  [page-name]
  [[page-name] (ref {})])

(defn- -mypostinit
  [_ page-name])
