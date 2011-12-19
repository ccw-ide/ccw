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
(ns compile)

(defn all []
  (dorun
    (map
      compile
      [
       'lancet
       'leiningen.clean
       'leiningen.core
       'leiningen.help
       'leiningen.jar
       'leiningen.pom
       'leiningen.uberjar
       'leiningen.version
       'leiningen.compile
       'leiningen.deps
       'leiningen.install
       'leiningen.new
       'leiningen.test
       'leiningen.upgrade
       ])))

