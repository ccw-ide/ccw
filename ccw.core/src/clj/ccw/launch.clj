(ns ccw.launch
  (:require [ccw.eclipse :as e])
  (:import [org.eclipse.jdt.launching JavaRuntime
                                      IVMInstallType
                                      IRuntimeClasspathEntry
                                      IJavaLaunchConfigurationConstants]
           [org.eclipse.debug.ui IDebugUIConstants
                                 DebugUITools]
           [org.eclipse.debug.core ILaunchManager
                                   DebugPlugin
                                   ILaunchConfiguration
                                   IDebugEventSetListener
                                   DebugEvent
                                   ILaunchesListener2
                                   IStreamListener]
           [org.eclipse.debug.core.model IProcess]
           [org.eclipse.debug.core.sourcelookup ISourcePathComputer]))

;;;; UTILITY FOR JDT
  ;; Specify a classpath. This is more or less easy depending on where it comes from
  ;; General idea: create IRuntimeClasspathEntry(ies) then get their memento (serialized form),
  ;;               add them to a classpath list, set this list on the working copy,
  ;;               AAANNND if you do so set DEFAULT_CLASSPATH to false or else
  ;;                       if you specified a project its classpath will be used instead
  ;; NOTE:
;  An IRuntimeClasspathEntry can be used to describe any of the following:
;
;    a project in the workspace
;    an archive (jar or zip file) in the workspace
;    an archive in the local file system
;    a folder in the workspace
;    a folder in the local file system
;    a classpath variable with an optional extension
;    a system library (also known as a classpath container, which is a related group of archives, such as the jars associated with a JDK)

(defn- lib-entry 
  "Given a lib - archive (jar/zip) or absolute path - any IPathCoercible -,
   return a JDT classpath entry"
  [lib-path]
  (doto (JavaRuntime/newArchiveRuntimeClasspathEntry (e/path lib-path))
    (.setClasspathProperty IRuntimeClasspathEntry/USER_CLASSES)))

(defn- jre-container-entry 
  "Given a Classpath container name, create corresponding JDT classpath entry"
  [jre-container-name]
  (JavaRuntime/newRuntimeContainerClasspathEntry
    (org.eclipse.core.runtime.Path. jre-container-name)
    IRuntimeClasspathEntry/STANDARD_CLASSES))

(defmulti classpath-entry
  "For the given entry-type, create a JDT classpath entry"
  (fn [entry-type entry-params] entry-type))

(defmethod classpath-entry :lib
  [entry-type entry-params]
  (lib-entry (:path entry-params)))

(defmethod classpath-entry :jre-container
  [entry-type entry-params]
  (jre-container-entry (:name entry-params)))

;;;;;;;;;;; CORE

(def launch-modes
  "Pre-existing modes a launch configuration can be launched in."
  {:run   ILaunchManager/RUN_MODE
   :debug ILaunchManager/DEBUG_MODE})

(def launch-configuration-types
  "Pre-defined types of launch configurations"
  {:ccw 
     "ccw.launching.clojure"
   :java 
     IJavaLaunchConfigurationConstants/ID_JAVA_APPLICATION
   :remote-java
     IJavaLaunchConfigurationConstants/ID_REMOTE_JAVA_APPLICATION})

(def vm-connector
  "Identifiers for socket attaching and socket listening connectors"
  {:socket-attach-vm-connector IJavaLaunchConfigurationConstants/ID_SOCKET_ATTACH_VM_CONNECTOR
   :socket-listen-vm-connector IJavaLaunchConfigurationConstants/ID_SOCKET_LISTEN_VM_CONNECTOR})

(def attrs-map
  "Launch configuration pre-existing attributes."
  {
   :private IDebugUIConstants/ATTR_PRIVATE
   :launch-in-background IDebugUIConstants/ATTR_LAUNCH_IN_BACKGROUND
   
   :environment-variables ILaunchManager/ATTR_ENVIRONMENT_VARIABLES
   :append-environment-variables ILaunchManager/ATTR_APPEND_ENVIRONMENT_VARIABLES
   
   :java/vm-install-type IJavaLaunchConfigurationConstants/ATTR_VM_INSTALL_TYPE
   :java/vm-install-name IJavaLaunchConfigurationConstants/ATTR_VM_INSTALL_NAME
   :java/main-type-name  IJavaLaunchConfigurationConstants/ATTR_MAIN_TYPE_NAME
   :java/program-arguments IJavaLaunchConfigurationConstants/ATTR_PROGRAM_ARGUMENTS
   :java/vm-arguments IJavaLaunchConfigurationConstants/ATTR_VM_ARGUMENTS
   :java/working-directory IJavaLaunchConfigurationConstants/ATTR_WORKING_DIRECTORY
   :java/classpath IJavaLaunchConfigurationConstants/ATTR_CLASSPATH
   :java/default-classpath IJavaLaunchConfigurationConstants/ATTR_DEFAULT_CLASSPATH
   :java/project-name IJavaLaunchConfigurationConstants/ATTR_PROJECT_NAME

   ;; specific for remote java
   :java/vm-connector IJavaLaunchConfigurationConstants/ATTR_VM_CONNECTOR
   :java/connect-map IJavaLaunchConfigurationConstants/ATTR_CONNECT_MAP

   ;; specific for debugging inside source code
   :source-path-computer-id ISourcePathComputer/ATTR_SOURCE_PATH_COMPUTER_ID
   :source-locator-id ILaunchConfiguration/ATTR_SOURCE_LOCATOR_ID
   })

#_(defn default-jre "Default JRE installed in workbench" [] (JavaRuntime/getDefaultVMInstall))

#_(defn jre-types "Currently existing JRE Types" [] (JavaRuntime/getVMInstallTypes))

#_(defn jres 
  "Specific instances of a type of JREs retrieved via IVMInstall"
  [jre-type]
  (for [type jre-types jre (seq (.getVMInstalls type))] jre))
    
(def default-jre-container-name "Name of the default JRE container" JavaRuntime/JRE_CONTAINER)

(defn default-launch
  "Somes default values for launch configurations: GUID name, java type"
  []
  {:name (.toString (java.util.UUID/randomUUID))
   :type-id :java})

(defmulti ^:private compute-value 
  "For the given attribute, compute the value to use to create the Launch configuration
   instance given the value configuration."
  (fn [attribute value-configuration] attribute)
  :default ::default-dispatch-value)

(defmethod compute-value ::default-dispatch-value
  [_ value-configuration] value-configuration)

(defmethod compute-value :java/classpath
  [_ value-configuration]
  (let [entry-fn (fn [{type :entry-type :as p}] 
                   (classpath-entry type p))
        entries (map entry-fn value-configuration)]
    (into [] (map #(.getMemento %) entries))))

#_(defn jre-attributes 
  "Given a jre (IVMInstall instance), return a fragment of launch configuration map 
   with relevant attributes for defining the jre"
  [jre]
  {:java/vm-install-type (-> jre .getVMInstallType .getId)
   :java/vm-install-name (.getName jre)})

(defn- set-attribute! 
  "Sets one attribute/value pair for working-copy.
   If attr is a key, it is transformed into proper String via attrs-map mapping.
   Value is transformed into expected Launch framework objects via compute-value multimethod.
   Return the working-copy."
  [working-copy attr value]
  (let [value (compute-value attr value)
        attr  (attrs-map attr attr)]
    (when (string? attr) (.setAttribute working-copy attr value))
    working-copy))

(defn- set-attributes! [working-copy attrs-map]
  "Sets all attribute/value pairs from attrs-map in working copy, calling
   set-attributes! on each pair.
   Return the working copy."
  (doseq [[k v] attrs-map]
    (set-attribute! working-copy k v))
  working-copy)

(defn valid&unique-name
  "Create a valid and unique name for the launch configuration"
  [s]
  (-> (DebugPlugin/getDefault)
     .getLaunchManager
     (.generateLaunchConfigurationName s)))

(defn- new-working-copy
  "Create a launch configuration working copy with given name and type-id"
  [& {:keys #{name type-id} :or {type-id :java}}]
  (let [name (valid&unique-name (or name (.toString (java.util.UUID/randomUUID))))
        manager (.getLaunchManager (DebugPlugin/getDefault))
        type (.getLaunchConfigurationType manager (launch-configuration-types type-id type-id))]
    (.newInstance type nil name)))

(defn mk-launch-configuration-working-copy 
  "Create a working copy given the launch configuration spec in map m."
  [m]
  (let [m (merge (default-launch) m)
        wc (new-working-copy :name (:name m) :type-id (:type-id m))
        attrs (dissoc m :name :type-id)
        wc (set-attributes! wc attrs)]
    wc))

(defn launch-configuration 
  "Create a launch configuration given a working copy.
   Working copy can be 2 things:
   - a real ILaunchConfigurationWorkingCopy instance
   - a specification for a launch configuration in a Map"
  [working-copy]
  (.doSave (cond 
             (map? working-copy) 
               (mk-launch-configuration-working-copy working-copy) 
             :else working-copy)))

(defn launch 
  "Launch configuration (either a ILaunchConfigurationWorkingCopy, either a Map spec)
   in the given mode"
  ([mode configuration]
    (DebugUITools/launch
      (launch-configuration configuration)
      (launch-modes mode mode)))
  ([mode configuration {:keys [result-listener] :as options}]
    (let [lc (launch-configuration configuration)]
      (when result-listener
        (let [right-launch? (fn [launch]
                              (and (= (.getName lc)
                                     (-> launch .getLaunchConfiguration .getName))
                                (first (seq (.getProcesses launch)))))
              listener (let [state (atom {:add-listener? true
                                          :str-out (StringBuilder.)
                                          :str-err (StringBuilder.)
                                          :exit-value nil})]
                         (reify ILaunchesListener2
                           (launchesRemoved [this launches])
                           (launchesAdded [this launches])
                           (launchesChanged [this launches]
                             (when-let [process (and (:add-listener? @state)
                                                  (some right-launch? launches))]
                               (swap! state assoc :add-listener? false)
                               (-> process .getStreamsProxy .getOutputStreamMonitor
                                 (.addListener
                                   (reify IStreamListener
                                     (streamAppended [this text monitor]
                                       (.append (:str-out @state) text)))))
                               (-> process .getStreamsProxy .getErrorStreamMonitor
                                 (.addListener
                                   (reify IStreamListener
                                     (streamAppended [this text monitor]
                                       (.append (:str-err @state) text)))))))
                           (launchesTerminated [this launches]
                             (when-let [process (some right-launch? launches)]
                               (swap! state assoc :exit-value (.getExitValue process))
                               (future (result-listener
                                         (.toString (:str-out @state))
                                         (.toString (:str-err @state))
                                         (:exit-value @state))))
                             (-> (DebugPlugin/getDefault)
                               .getLaunchManager (.removeLaunchListener this)))))]
          (-> (DebugPlugin/getDefault) .getLaunchManager (.addLaunchListener listener))))
      (DebugUITools/launch lc (launch-modes mode mode)))))

(def debug "Launch a configuration in debug mode" (partial launch :debug))
(def run   "Launch a configuration in run   mode" (partial launch :run))
