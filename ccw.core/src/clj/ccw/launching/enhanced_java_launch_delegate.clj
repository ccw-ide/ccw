;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Laurent PETIT - initial implementation
;*******************************************************************************/
(ns ccw.launching.enhanced-java-launch-delegate
  "Small enhancements over JavaLaunchDelegate:
   - Fix OS X not propagating environment variables: add additional PATHs to the PATH"
  (:import [org.eclipse.core.runtime Platform]
           [org.eclipse.debug.core DebugPlugin]
           [org.eclipse.osgi.service.environment Constants]))

(defn osx?
  "Is the running platform Mac OS X ?"
  []
  (= (Platform/getOS) Constants/OS_MACOSX))

(defn build-osx-base-environment
  "Builds the base environment variables map. This function would not
   work on windows (not as sophisticated as the one found in LaunchManager"
  []
  (assert (osx?))
  (let [launch-manager (-> (DebugPlugin/getDefault) .getLaunchManager)]
    (into {} (.getNativeEnvironmentCasePreserved launch-manager))))

(defn env-array->env-map
  "Transforms an Array of '{k}={v}' Strings to {k v} map"
  [env]
  (letfn [(key-val [s] (let [[_ k v] (re-find #"([^=]*)=(.*)" s)]
                         [k v]))]
    (->> env
      (map key-val)
      (reduce (fn [m [k v]] (assoc m k v)) {}))))

(defn env-map->array
  "The Eclipse API wants an Array of String, so let's give it what it wants"
  [env-map]
  (into-array String
    (->> env-map (map (fn [[k v]] (str k "=" v))))))

(defn get-environment
  "Appends some classic missing PATH environment variable values in OS X."
  [delegate configuration]
  (let [env (.superGetEnvironment delegate configuration)]
    (if-not (osx?)
      env
      (let [env-map (or (some-> env env-array->env-map)
                        (build-osx-base-environment))]
        (-> env-map
          (update-in ["PATH"]
            (fnil str "/usr/bin:/bin:/usr/sbin:/sbin")
            ":/usr/local/bin") ;; "/usr/local/bin" is the one missing in some OS X configs
          env-map->array)))))
