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
  ccw.editors.clojure.hover-support
  "Implements the pluggable hover framework for ClojureEditor. A hover
  descriptor holds the configuration info. It is exposed to the java
  world through the HoverDescriptor bean (see to-java, from-java)."
  (:refer-clojure :exclude [read-string])
  (:import ccw.CCWPlugin
           ccw.TraceOptions
           ccw.core.StaticStrings
           [ccw.editors.clojure.hovers HoverModel
                                       HoverDescriptor]
           ccw.preferences.PreferenceConstants
           ccw.util.EditorUtility
           java.util.ArrayList
           org.eclipse.core.databinding.observable.Realm
           [org.eclipse.core.databinding.observable.list ObservableList
                                                         WritableList]
           [org.eclipse.core.runtime CoreException
                                     IRegistryEventListener
                                     IExtensionRegistry
                                     IExtension
                                     IExtensionPoint]
           [org.eclipse.e4.core.contexts IEclipseContext
                                         ContextInjectionFactory]
           org.eclipse.jface.databinding.swt.SWTObservables
           [org.eclipse.jface.text Region
                                   ITextHover
                                   ITextHoverExtension
                                   ITextHoverExtension2
                                   ITextViewer
                                   ITextViewerExtension2
                                   IDocument]
           org.eclipse.swt.SWT)
  (:require [ccw.bundle :refer [available? bundle]]
            [ccw.core.trace :refer [trace]]
            [ccw.eclipse :refer [preference
                                 preference!
                                 ccw-combined-prefs
                                 workbench-editor]]
            [ccw.editors.clojure.editor-support :refer [source-viewer]]
            [ccw.extensions :refer [configuration-elements
                                    attributes->map
                                    element->map
                                    element-name
                                    element-children
                                    element-attribute
                                    create-executable-ext
                                    element-valid?
                                    add-extension-listener
                                    mock-element]]
            [ccw.interop :refer [simple-name]]
            [ccw.repl.view-helpers :refer [workbench-display]]
            [clojure.edn :as edn :refer [read-string]]
            [clojure.java.data :refer :all]
            [clojure.string :refer [blank? join]]
            [clojure.test :refer [deftest is run-tests]]))

(set! *warn-on-reflection* true)

(defn- create-hover!
  "Creates a hover instance from the input extension element by calling
  .createExecutableExtension on the \"run\" tag. See
  schema/cljEditorTextHovers.exsd for the hover extension point.
  
  Two conditions need to be met:
  1) The CCW bundle is available
  2) The element has tag activate=true.
  3) Thu \"run\" tag specifies a class that extends either
  IExecutableExtension or IExecutableExtensionFactory."
  [element]
  {:pre [(not (nil? element))]
   :post [(not (nil? %))]}
  (when (or (available? (bundle CCWPlugin/PLUGIN_ID))
            (Boolean/valueOf ^String (element-attribute element "activate")))
    (try
      (create-executable-ext element "run")
      (catch CoreException e
        (CCWPlugin/logError e)))))

(defn- hover-element->descriptor
  "Build a descriptor from a hover IConfigurationElement taken from plugin.xml."
  [hover-element]
  (into {:element hover-element
         :enabled true}
        (attributes->map hover-element)))

(defn- assoc-create-hover-closure
  "Create the hover instance, modifying the descriptor if necessary.
  Preconditions: instance key must be nil."
  [descriptor]
  {:pre [(not (nil? descriptor))]}
  (assoc descriptor :create-hover #(create-hover! (descriptor :element))))

(defn- descriptor->types
  [descriptor]
  (cond-> []
    (instance? ITextHoverExtension2 (:instance descriptor)) (conj 'ITextHoverExtension2)
    (instance? ITextHoverExtension (:instance descriptor)) (conj 'ITextHoverExtension)
    (instance? ITextHover (:instance descriptor)) (conj 'ITextHover)))

(def ^:private
  descriptor-persisted-keys
  "The list of persisted properties."
  [:id :enabled :modifier-string])

(defn- create-java-descriptor-list
  "Function that returns a list of Java hover descriptors."
  [map-descriptors]
  (map #(to-java HoverDescriptor %1) map-descriptors))

;;;;;;;;;;;;;;;;;;
;;; Predicates ;;;
;;;;;;;;;;;;;;;;;;

(defn- descriptor-valid?
  [descriptor]
  (element-valid? (:element descriptor)))

;;;;;;;;;;;;;;;;;
;;; Selectors ;;;
;;;;;;;;;;;;;;;;;

(def ^:private debug-hover-id "ccw.editors.clojure.hovers.DebugHover")

(defn- select-debug-descriptor
  "Always selects and returns the Debug Hover."
  [descriptors]
  (first (filter #(= (:id %1) debug-hover-id) descriptors)))

(defn- select-default-descriptor
  "Always selects and returns the enabled Default descriptor, that is the one with
  with blank string modifier/state mask. Returns null instead."
  [descriptors]
  (first (filter
          #(let [state-mask (:state-mask %1)]
             (and (:enabled %1) (or (= SWT/DEFAULT state-mask) (= SWT/NONE state-mask))))
          descriptors)))

(defn- select-hover-by-state-mask
  "Selects the first enabled descriptor with the input state mask. No
  check on the uniqueness of the descriptor is performed.  The return
  value is a hover descriptor, or nil if nothing matching the state mask
  can be found.

  Note: The Eclipse framework passes content-type AND state-mask to
  select a hover. At the moment CCW does not define content types and
  therefore we do not select hovers using the first parameter."
  [state-mask descriptors]
  (first (filter #(and (:enabled %1) (= state-mask (:state-mask %1))) descriptors)))

(defn- select-descriptor
  "This function wraps the input selecting function so that if the
  selected hover is nil the default hover (the one with blank string
  modifier/state mask), if enabled, is returned instead. If even the
  default is not enabled, this function returns nil. The selecting-fn
  must accept the descriptor as parameter and return the selected
  descriptor or nil."
  [selecting-fn descriptors]
  (if-let [selected-descriptor (selecting-fn descriptors)]
    selected-descriptor
    (select-default-descriptor descriptors)))

;;;;;;;;;;;;;;;;
;;; Private  ;;;
;;;;;;;;;;;;;;;;

(def ^:private
  text-hover-extension-point
  "The hover extension point unique identifier."
  "ccw.core.cljEditorTextHovers")

(defn- sanitize-descriptor
  "Sanitize function on a hover descriptor."
  [descriptor]
  (let [state-mask (EditorUtility/computeStateMask (:modifier-string descriptor))
        modifier-string (EditorUtility/getModifierString state-mask)]
    (assoc descriptor :modifier-string modifier-string :state-mask state-mask)))

(defn- read-and-sanitize-descriptor-string
  "Reads a descriptor list from the input string (in edn format, will use read-string."
  [edn-string]
  (map sanitize-descriptor (edn/read-string {:eof ""} edn-string)))

(defn- read-descriptors-from-preference!
  "Loading what it is in the preferences, returns the hover descriptors accordingly."
  []
  (let [pref PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS
        default (some-> (ccw-combined-prefs) (.getDefaultString pref))
        descriptors (read-and-sanitize-descriptor-string (preference pref (or default "")))] 
    descriptors))

(defn- merge-descriptors
  "Merges the key-values of the two hover lists in input. The first list
  gets its key-values replaced by the second in case. See merge."
  [first-descriptor-list second-descriptor-list]
  (if (seq first-descriptor-list) 
    (let [second-list-by-id (into {} (map (juxt :id identity) second-descriptor-list))]
      (map #(merge %1 (get second-list-by-id (:id %1) {})) first-descriptor-list))
    second-descriptor-list))

(defn- persist-java-hover-descriptors!
  "Persists the input hover desrcriptors for later retrieval."
  [java-descriptors]
  (let [descriptors (map #(from-java %1) java-descriptors)]
    (preference! PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS
                 (pr-str (map #(select-keys %1 descriptor-persisted-keys) descriptors)))))

(defn- load-extension-descriptors!
  "Processing closure on the hover data structure."
  []
  (map (comp sanitize-descriptor assoc-create-hover-closure hover-element->descriptor)
       (configuration-elements text-hover-extension-point)))

(defn- load-descriptors!
  "Reads from both preference and extension point the hover descriptors
  and merges together, with the one in the preference always taking
  precedence."
  []
  (merge-descriptors (load-extension-descriptors!) (read-descriptors-from-preference!)))


;;;;;;;;;;;;;
;;; State ;;;
;;;;;;;;;;;;;

(defrecord State [contributed-descriptors
                  observable-descriptors
                  hovers-by-state-mask
                  dirty]
  java.lang.Object
  (toString [_]
    (with-out-str (do (println "*** Descriptors: ")
                      (clojure.pprint/pprint contributed-descriptors)
                      (println "*** Hovers: ")
                      (clojure.pprint/pprint hovers-by-state-mask)
                      (println "*** Dirty: " dirty)))))

(defonce ^:private ^{:doc "Atom containing the state."}
  state-atom (atom (->State nil
                            (WritableList. (ArrayList.) HoverDescriptor)
                            nil
                            true)))

(defn- swap-observable-descriptors
  "Updates the observable-hovers part of the state. Returns new state."
  [old-state descriptors]
  (let [observable-hovers ^ObservableList (:observable-descriptors old-state)
        realm (SWTObservables/getRealm (workbench-display))]
    (.exec realm #(do (.clear observable-hovers) 
                      (.addAll observable-hovers (create-java-descriptor-list descriptors))))
    (assoc old-state :observable-descriptors observable-hovers)))

(defn- set-state-dirty
  "Resets the hover atoms."
  [old-state]
  (assoc old-state :dirty true))

(def ^:private contributed-descriptors
  "Var containing a function returning the contributed descriptors in
  the state."
  #(:contributed-descriptors @state-atom))

(def ^:private observable-descriptors
  "Var containing a function returning the state observable descriptors
  in the state, for the Eclipse data binding framework."
  #(:observable-descriptors @state-atom))

(defn- state-dirty?
  [state]
  (:dirty state))

(defn- swap-descriptors-in-state
  "Fills up the old-state with new descriptors. Sets the dirty flag
  to false. Returns the new state."
  [old-state descriptors]
  (-> old-state
      (assoc :contributed-descriptors descriptors)
      (swap-observable-descriptors descriptors)
      (assoc :dirty false)))

(defn- swap-hover-instance-in-state
  "Fills up the old-state with new hover instance. Does not touch the
  dirty flag."
  [old-state state-mask instance]
  (assoc old-state :hovers-by-state-mask (assoc (:hovers-by-state-mask old-state) state-mask instance)))

(defn- descriptors-update!
  "Updates the descriptors. Swaps in a new state, if necessary, returning the new one."
  []
  (if (state-dirty? @state-atom)
    (let [new-state (swap! state-atom swap-descriptors-in-state (load-descriptors!))]
      (trace :support/hover (str "State dirty, loaded:\n" new-state))
      new-state)
    (do (trace :support/hover (str "State NOT dirty, returning:\n" @state-atom))
        @state-atom)))


;;;;;;;;;;;;;;;;;
;;;    API    ;;;
;;;;;;;;;;;;;;;;;

(defn reset-hover-on-editor!
  "Sets the input hover as default hover of the input editor."
  [content-type state-mask editor]
  {:pre [(not (nil? editor))]}
  (when-let [sv (source-viewer editor)]
    (condp instance? sv
      ITextViewerExtension2 (.setTextHover ^ITextViewerExtension2 sv nil content-type state-mask)
      true (.setTextHover ^ITextViewer sv nil content-type))))

(def ^:private
  reset-default-hover-on-editor!
  "Function that resets the default hover of the input editor."
  (partial reset-hover-on-editor! IDocument/DEFAULT_CONTENT_TYPE ITextViewerExtension2/DEFAULT_HOVER_STATE_MASK))

;;;;;;;;;;;;;;;;;
;;; Listeners ;;;
;;;;;;;;;;;;;;;;;

(defn add-registry-listener
  []
  (trace :support/hover (str "Registering " text-hover-extension-point "extension point registry listener..."))
  (add-extension-listener
   (reify
     IRegistryEventListener
     (^void added [this #^"[Lorg.eclipse.core.runtime.IExtension;" extensions]
       (doseq [^IExtension ex extensions] (trace :support/hover (str "IRegistryEventListener added extension with id: " (.getLabel ex))))
       (trace :support/hover "Resetting hovers...")
       (swap! state-atom set-state-dirty)
       (descriptors-update!)
       ;; I need some protection because here i might not have active editor
       (some-> (CCWPlugin/getClojureEditor) reset-default-hover-on-editor!))

     (^void removed [this #^"[Lorg.eclipse.core.runtime.IExtension;" extensions]
       (doseq [^IExtension ex extensions] (trace :support/hover (str "IRegistryEventListener removed extension with id: " (.getLabel ex))))
       (trace :support/hover "Resetting hovers...")
       (swap! state-atom set-state-dirty)
       (descriptors-update!)
       ;; I need some protection because here i might not have active editor
       (some-> (CCWPlugin/getClojureEditor) reset-default-hover-on-editor!))

     (^void added [this #^"[Lorg.eclipse.core.runtime.IExtensionPoint;" extension-points]
       (doseq [^IExtensionPoint ep extension-points] (trace :support/hover (str "IRegistryEventListener added extension point with id: " (.getLabel ep))))
       (trace :support/hover "Resetting hovers...")
       (swap! state-atom set-state-dirty)
       (descriptors-update!)
       ;; I need some protection because here i might not have active editor
       (some-> (CCWPlugin/getClojureEditor) reset-default-hover-on-editor!))

     (^void removed [this #^"[Lorg.eclipse.core.runtime.IExtensionPoint;" extension-points]
       (doseq [^IExtensionPoint ep extension-points] (trace :support/hover (str "IRegistryEventListener removed extension point with id: " (.getLabel ep))))
       (trace :support/hover "Resetting hovers...")
       (swap! state-atom set-state-dirty)
       (descriptors-update!)
       ;; I need some protection because here i might not have active editor
       (some-> (CCWPlugin/getClojureEditor) reset-default-hover-on-editor!)))

   text-hover-extension-point))

(defn add-preference-listener
  []
  (let [pref-key PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS]
    (trace :support/hover (str "Registering " pref-key " listener..."))
    (ccw.eclipse/add-preference-listener pref-key
                                         #(do
                                            (trace :support/hover (str "Preference " pref-key " has changed. Resetting hovers..."))
                                            (swap! state-atom set-state-dirty)
                                            ;; I need some protection because here i might not have active editor
                                            (some-> (CCWPlugin/getClojureEditor) reset-default-hover-on-editor!)))))

;;;;;;;;;;;;;;;;;;;
;;; Java Interop ;;
;;;;;;;;;;;;;;;;;;;

(defmethod to-java [HoverDescriptor clojure.lang.APersistentMap] [clazz props]
  "The conversion function to the Java world, modifier string and state mask are not checked here."
  (let [ms (props :modifier-string)]
    (doto (HoverDescriptor.
           (props :id)
           (props :label)
           (props :enabled)
           (props :description)
           (EditorUtility/computeStateMask ms)
           ms))))

(defmethod from-java HoverDescriptor [^HoverDescriptor instance]
  "The conversion function from the Java world, modifier string and state mask are not checked here."
  {:id (.getId instance)
   :label (.getLabel instance)
   :enabled (.isEnabled instance)
   :description (.getDescription instance)
   :modifier-string (.getModifierString instance)
   :state-mask (.getStateMask instance)})

;;;;;;;;;;;
;;; API ;;;
;;;;;;;;;;;

(defmacro ^{:private true} do-if-no-value
  "Expands to the lookup of accessor in coll and the conditional execution of the side-effect form.
  The side-effect is executed iff no value can be found in coll. The
  form can access to a local named __a_value (the anaphora, from The Joy of
  Clojure - 8.5.1) which will be bound either to not-found-value or to
  nil. The macro returns the found key or not-found-value."
  [coll accessor side-effect not-found-value]
  `(let [~'__a_value (get ~coll ~accessor ~not-found-value)]
     (if ~'__a_value
       (do ~side-effect ~not-found-value)
       ~'__a_value)))

(defn- create-hover-instance
  "Returns an ITextHover (or extensions) instance given, content-type and state-mask."
  [descriptors content-type state-mask]
  ;; TODO The selection could be improved by indexing the descriptors by state-mask
  (when-let [selected-descriptor (select-descriptor (partial select-hover-by-state-mask state-mask) descriptors)]
    ;; ensure non nil descriptor arrives here
    (let [state-mask (selected-descriptor :state-mask)]
      (do-if-no-value descriptors state-mask
                      (swap! state-atom swap-hover-instance-in-state state-mask __a_value)
                      ((selected-descriptor :create-hover))))))

(defn hover-instance
  "Public API which is called from java."
  [content-type state-mask]
  (create-hover-instance (:contributed-descriptors (descriptors-update!)) content-type state-mask))

(defn init-injections
  "Sets a new instance of HoverModel in the input context."
  [^IEclipseContext context]
  (trace :support/hover (str "Initializing Injections..."))
  (.set context StaticStrings/CCW_CONTEXT_VALUE_HOVERMODEL
        (reify
          HoverModel
          (observableHoverDescriptors [this]
            (:observable-descriptors (descriptors-update!)))

          (persistHoverDescriptors [this list]
            (persist-java-hover-descriptors! (observable-descriptors))))))

(defn configured-state-masks
  "Returns the configured state masks (as array of int) given source viewer and content type.
  This function is called early, when the atom is still empty, and it is
  the very true entry point of the hover framework, therefore we need to
  load the descriptors."
  [_ _]
  (int-array (keep :state-mask (:contributed-descriptors (descriptors-update!)))))
