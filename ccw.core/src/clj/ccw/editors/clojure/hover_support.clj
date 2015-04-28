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
  (:import java.util.ArrayList
           [org.eclipse.core.databinding.observable.list ObservableList
                                                         WritableList]
           [org.eclipse.core.runtime CoreException
                                     IRegistryEventListener
                                     IExtensionRegistry
                                     IExtension
                                     IExtensionPoint]
           [org.eclipse.e4.core.contexts IEclipseContext
                                         ContextInjectionFactory]
           [org.eclipse.jface.text Region
                                   ITextHover
                                   ITextHoverExtension
                                   ITextHoverExtension2
                                   ITextViewer
                                   ITextViewerExtension2
                                   IDocument]
           org.eclipse.swt.SWT
           ccw.CCWPlugin
           ccw.core.StaticStrings
           [ccw.editors.clojure.hovers HoverModel
                                       HoverDescriptor]
           ccw.util.EditorUtility
           ccw.preferences.PreferenceConstants
           ccw.TraceOptions)
  (:require [clojure.java.data :refer :all]
            [clojure.string :refer [blank? join]]
            [clojure.test :refer [deftest is run-tests]]
            [ccw.bundle :refer [available? bundle]]
            [ccw.eclipse :refer [preference
                                 preference!
                                 ccw-combined-prefs
                                 workbench-editor]]
            [ccw.editors.clojure.editor-support :refer [source-viewer]]
            [ccw.core.trace :refer [trace]]
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
            [clojure.edn :as edn :refer [read-string]]))

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
  {:pre [(not (nil? element))]}
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
         :instance nil
         :enabled true}
        (attributes->map hover-element)))

(defn- ensure-hover-created!
  "Create the hover instance, modifying the descriptor if
  necessary. Returns a potentially updated descriptor value."
  [descriptor]
  (if (nil? (:instance descriptor))
    (-> (assoc descriptor :instance ((descriptor :create-hover)))
        (dissoc :element))
    descriptor))

(defn- assoc-create-hover-closure
  "Create the hover instance, modifying the descriptor if necessary.
  Preconditions: instance key must be nil."
  [descriptor]
  {:pre [(not (nil? descriptor)) (nil? (descriptor :instance))]}
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
  therefore we do not select hovers using the first parameter. It is
  there though, as we will probably need it in the future"
  [_content-type state-mask descriptors]
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
        default (some-> (ccw-combined-prefs) (.getDefaultString pref))] 
    (read-and-sanitize-descriptor-string (preference pref (or default "")))))

(defn- merge-descriptors
  "Merges the key-values of the two hover lists in input. The first list
  gets its key-values replaced by the second in case. See merge."
  [first-descriptor-list second-descriptor-list]
  (if (seq first-descriptor-list) 
    (let [second-list-by-id (into {} (map (juxt :id identity) second-descriptor-list))]
      (map #(merge %1 (get second-list-by-id (:id %1) {})) first-descriptor-list))
    second-descriptor-list))

(defn- remove-and-cons
  "Removes the items in coll for which (pred old-item) is true and then conses new-item.
  Returns a lazy seq."
  [pred new-item coll]
  (cons new-item (remove pred coll)))

(defn- merge-from-preference
  "Updates the contributed-hovers atom reading and merging from preferences as well."
  [old-values]
  (merge-descriptors old-values (read-descriptors-from-preference!)))

(defn- persist-java-hover-descriptors!
  "Persists the input hover desrcriptors for later retrieval."
  [java-descriptors]
  (let [descriptors (map #(from-java %1) java-descriptors)]
    (preference! PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS
                 (pr-str (map #(select-keys %1 descriptor-persisted-keys) descriptors)))))

(defn- load-extensions
  "Loads hover extensions."
  []
  (configuration-elements text-hover-extension-point))

(defn- load-descriptors-if-necessary
  "Processing closure on the hover data structure."
  [old-values]
  (if-not (nil? old-values)
    old-values
    (map (comp assoc-create-hover-closure hover-element->descriptor) (load-extensions))))

;;;;;;;;;;;;;;;;;;
;;; Observables ;;
;;;;;;;;;;;;;;;;;;

(defonce ^:private ^{:doc "Var containing the ObservableList of HoverDescriptor(s). The underlaying
 implementation must be thread safe as this instance is injected early (its lifecycle coinciding
 with the plugin's) and will mutate."} 
  observable-hovers (WritableList. (ArrayList.) HoverDescriptor))

(defn- update-observables-atom
  "Updates the observable-hovers mutable atom. Returns the new value."
  [descriptors]
  (.clear ^ObservableList observable-hovers)
  (.addAll ^ObservableList observable-hovers (create-java-descriptor-list descriptors))
  observable-hovers)

;;;;;;;;;;;;;
;;; Atoms ;;;
;;;;;;;;;;;;;

(defonce ^:private ^{:doc "Atom containing the list of hover descriptors."}
  contributed-hovers (atom nil))

(defonce ^:private ^{:doc "Atom containing the hover instances"}
  hover-instances (atom nil))

(defn- reset-atoms
  "Resets the hover atoms."
  []
  (reset! contributed-hovers nil))

(defn- init-atoms
  "Initializes the hover atoms."
  []
  (swap! contributed-hovers (comp merge-from-preference load-descriptors-if-necessary)))

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

(def ^:private
  reset-default-hover-on-active-editor!
  "Function that resets the default hover of the active editor."
  #(reset-default-hover-on-editor! (workbench-editor)))

;;;;;;;;;;;;;;;;;
;;; Listeners ;;;
;;;;;;;;;;;;;;;;;

(defn add-registry-listener
  []
  (trace :support/hover (str "Registering" text-hover-extension-point "extension point registry listener..."))
  (add-extension-listener
   (reify
     IRegistryEventListener
     (^void added [this #^"[Lorg.eclipse.core.runtime.IExtension;" extensions]
       (doseq [^IExtension ex extensions] (trace :support/hover (str "IRegistryEventListener added extension with id: " (.getLabel ex))))
       (trace :support/hover "Resetting hovers...")
       (reset-atoms)
       (reset-default-hover-on-active-editor!))

     (^void removed [this #^"[Lorg.eclipse.core.runtime.IExtension;" extensions]
       (doseq [^IExtension ex extensions] (trace :support/hover (str "IRegistryEventListener removed extension with id: " (.getLabel ex))))
       (trace :support/hover "Resetting hovers...")
       (reset-atoms)
       (reset-default-hover-on-active-editor!))

     (^void added [this #^"[Lorg.eclipse.core.runtime.IExtensionPoint;" extension-points]
       (doseq [^IExtensionPoint ep extension-points] (trace :support/hover (str "IRegistryEventListener added extension point with id: " (.getLabel ep))))
       (trace :support/hover "Resetting hovers...")
       (reset-default-hover-on-active-editor!))

     (^void removed [this #^"[Lorg.eclipse.core.runtime.IExtensionPoint;" extension-points]
       (doseq [^IExtensionPoint ep extension-points] (trace :support/hover (str "IRegistryEventListener removed extension point with id: " (.getLabel ep))))
       (trace :support/hover "Resetting hovers...")
       (reset-atoms)
       (reset-default-hover-on-active-editor!)))

   text-hover-extension-point))

(defn add-preference-listener
  []
  (let [pref-key PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS]
    (trace :support/hover (str "Registering" pref-key " listener..."))
    (ccw.eclipse/add-preference-listener pref-key
                                         #(do
                                            (trace :support/hover (str "Preference " pref-key " has changed. Resetting hovers..."))
                                            (reset-atoms)
                                            (reset-default-hover-on-active-editor!)))))

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

(defn hover-instance
  "Returns an ITextHover (or extensions) instance given editor, content-type and state-mask."
  [editor content-type state-mask]
  ;; TODO The selection could be improved by indexing the descriptors by state-mask
  (init-atoms)
  (let [select-f (partial select-descriptor
                          (partial select-hover-by-state-mask content-type state-mask))]
    (when-let [selected-descriptor (select-f @contributed-hovers)]
      ;; ensure non nil descriptor arrives here
      (let [new-descriptor (ensure-hover-created! selected-descriptor)
            remove-hover #(= (:id new-descriptor) (:id %1))]
        ;; TODO improve
        (if-not (identical? selected-descriptor new-descriptor)
          (swap! contributed-hovers (partial remove-and-cons
                                             remove-hover new-descriptor)))
        (new-descriptor :instance)))))

(defn init-injections
  "Sets a new instance of HoverModel in the input context."
  [^IEclipseContext context]
  (trace :support/hover (str "Initializing Injections..."))
  (.set context StaticStrings/CCW_CONTEXT_VALUE_HOVERMODEL
        (reify
          HoverModel
          (observableHoverDescriptors [this]
            (init-atoms)
            (update-observables-atom @contributed-hovers))

          (persistHoverDescriptors [this list]
            (persist-java-hover-descriptors! observable-hovers)))))

(defn configured-state-masks
  "Returns the configured state masks (as array of int) given source viewer and content type.
  This function is called early, when the contributed-hovers atom is still empty, therefore needs
  to get its returned array from the preferences."
  [_ _]
  (int-array (map :state-mask (merge-from-preference @contributed-hovers)) ))
