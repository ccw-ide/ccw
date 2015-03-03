(ns
  ^{:author "Andrea Richiardi"
    :doc "Implements the pluggable hover framework for ClojureEditor." }
  ccw.editors.clojure.hover-support
  (:import java.util.ArrayList
           [org.eclipse.core.databinding.observable.list ObservableList
                                                         WritableList]
           [org.eclipse.core.runtime CoreException
                                     IRegistryEventListener
                                     IExtensionRegistry]
           [org.eclipse.e4.core.contexts IEclipseContext
                                         ContextInjectionFactory]
           org.eclipse.jface.resource.JFaceResources
           org.eclipse.jface.internal.text.html.HTMLPrinter
           [org.eclipse.jface.text Region
                                   ITextHover
                                   ITextHoverExtension
                                   ITextHoverExtension2
                                   ITextViewerExtension2
                                   IDocument]
           [org.eclipse.jface.text.information IInformationProvider
                                               IInformationProviderExtension
                                               IInformationProviderExtension2]
           org.eclipse.swt.SWT
           ccw.CCWPlugin
           ccw.core.StaticStrings
           [ccw.editors.clojure ClojureEditorMessages
                                IClojureAwarePart]
           [ccw.editors.clojure.hovers HoverModel
                                       HoverDescriptor
                                       IClojureHover]
           ccw.util.UiUtils
           ccw.preferences.PreferenceConstants
           ccw.TraceOptions)
  (:use clojure.java.data)
  (:require [clojure.string :refer [blank? join]]
            [clojure.test :refer [deftest is run-tests]]
            [ccw.bundle :refer [available? bundle]]
            [ccw.eclipse :refer [preference
                                 preference!
                                 workbench-active-editor]]
            [paredit.loc-utils :refer [start-offset
                                       loc-count]]
            [ccw.core.doc-utils :refer [var-doc-info-html]]
            [ccw.editors.clojure.editor-common :refer [offset-loc
                                                       find-var-metadata
                                                       parse-symbol?]]
            [ccw.editors.clojure.editor-support :refer [source-viewer set-status-line-error-msg-async]]
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
            ;; remove these
            [clojure.pprint :refer [pprint]]))

#_(set! *warn-on-reflection* true)

(defn- create-hover
  "When the bundle is available and the element has activate=true, adds
  a closure that, when called, will create the hover.  Underneath, this
  will call createExecutableExtension on the value of the class
  attribute. The value, common in the Eclipse platform, needs to be a
  class that extends either IExecutableExtension or
  IExecutableExtensionFactory."
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
  (assoc descriptor :create-hover #(create-hover (descriptor :element))))

(defn- descriptor->types
  [descriptor]
  (cond-> []
    (instance? ITextHoverExtension2 (:instance descriptor)) (conj 'ITextHoverExtension2)
    (instance? ITextHoverExtension (:instance descriptor)) (conj 'ITextHoverExtension)
    (instance? ITextHover (:instance descriptor)) (conj 'ITextHover)))

(def ^{ :const true :private true :doc "The list of persisted properties."}
  descriptor-persisted-keys [:id :enabled :modifier-string])

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

(def ^{:const true :private true} debug-hover-id "ccw.editors.clojure.hovers.DebugHover")

(defn- select-debug-descriptor
  "Always selects and returns the Debug Hover."
  [descriptors]
  (first (filter #(= (%1 :id) debug-hover-id) descriptors)))

(defn- select-default-descriptor
  "Always selects and returns the enabled Default descriptor, that is the one with
  with blank string modifier/state mask. Returns null instead."
  [descriptors]
  (first (filter
          #(let [state-mask (:state-mask %1)]
             (and (%1 :enabled) (or (= SWT/DEFAULT state-mask) (= SWT/NONE state-mask))))
          descriptors)))

(defn- select-hover-by-state-mask
  "Selects the first enabled descriptor with the input state mask. No
  check on the uniqueness of the descriptor is performed.  The return
  value is a hover descriptor, or nil if nothing matching the state mask
  can be found."
  [_ state-mask descriptors]
  (first (filter #(and (%1 :enabled) (= state-mask (%1 :state-mask))) descriptors)))

(defn- select-hover-or-default
  "This function wraps the input selecting function so that if the
  selected hover is nil the default hover (the one with blank string
  modifier/state mask), if enabled, is returned instead. If even the
  default is not enabled, this function returns nil.  The selecting-fn
  must accept three parameters in this order: the content type, the
  state mask and the list of descriptors."
  [selecting-fn content-type state-mask descriptors]
  (if-let [selected-descriptor (selecting-fn content-type state-mask descriptors)]
    selected-descriptor
    (select-default-descriptor descriptors)))

;;;;;;;;;;;;;;;;
;;; Private  ;;;
;;;;;;;;;;;;;;;;

(def ^{:const true :doc "The hover extension point unique identifier."}
  text-hover-extension-point "ccw.core.cljEditorTextHovers")

(defn- sanitize-descriptor
  "Sanitize function on a hover descriptor."
  [descriptor]
  (let [state-mask (UiUtils/computeStateMask (:modifier-string descriptor))
        modifier-string (UiUtils/getModifierString state-mask)]
    (assoc descriptor :modifier-string modifier-string :state-mask state-mask)))

(defn- read-and-sanitize-descriptor-string
  "Reads a descriptor list from the input string (in edn format, will use read-string."
  [edn-string]
  (binding [*read-eval* false]
    (map sanitize-descriptor (read-string edn-string))))

(defn- read-descriptors-from-preference!
  "Loading what it is in the preferences, returns the hover descriptors accordingly."
  []
  (read-and-sanitize-descriptor-string (preference PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS "")))

(defn- merge-descriptors
  "Merges the key-values of the two hover lists in input. The first list
  gets its key-values replaced by the second in case. See merge."
  [first-descriptor-list second-descriptor-list]
  (let [second-list-by-id (into {} (map (juxt :id identity) second-descriptor-list))]
    (map #(merge %1 (get second-list-by-id (:id %1) {})) first-descriptor-list)))

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

(defn- reset-hover-on-editor!
  "Sets the hover with given content-type and state-mask to nil on the input editor."
  [content-type state-mask editor]
  {:pre [(not (nil? editor))]}
  (when-let [sv (source-viewer editor)]
    (condp instance? sv
      ITextViewerExtension2 (.setTextHover sv nil content-type state-mask)
      true (.setTextHover sv nil content-type))))

(def ^{:private true :doc "Function that resets the default hover of the input editor."}
  reset-default-hover-on-editor!
  (partial reset-hover-on-editor! IDocument/DEFAULT_CONTENT_TYPE ITextViewerExtension2/DEFAULT_HOVER_STATE_MASK))

(def ^{:private true :doc "Function that resets the default hover of the active editor."}
  reset-default-hover-on-active-editor!
  #(reset-default-hover-on-editor! (workbench-active-editor)))

(defn- select-descriptor
  "Selects an hover descriptor given content-type, status-mask and a
  list of descriptors."
  [content-type state-mask descriptors]
  (select-hover-or-default select-hover-by-state-mask
                           content-type
                           state-mask
                           descriptors))

(defmacro ^{:private true} do-if-no-value
  "Expands to the lookup of accessor in coll and the conditional execution of the side-effect form.
  The side-effect is executed iff no value can be found in coll. The
  form can access to a local named value# (the anaphora, from The Joy of
  Clojure - 8.5.1) which will be bound either to not-found-value or to
  nil. The macro returns the found key or not-found-value."
  [coll accessor side-effect not-found-value]
  `(let [~'value# (get ~coll ~accessor ~not-found-value)]
     (if ~'value#
       (do ~side-effect ~not-found-value)
       ~'value#)))

(defn hover-result-pair
  "This function is strictly tied with the hover-instances
  representation but tries to hide what it can. If previous-hover is not
  nil, it just executes f-on-hover on it.  If nil, it maps and filters
  over hover-instances in order to find the first hover
  where (result-pred (f-on-hover ith-hover)) is true. It also executes
  (else-side-effects [hover result]) when previous-hover is nil. It
  always returns a pair [hover result] or nil."
  [f-on-hover result-pred hover-instances previous-hover else-side-effect]
  (if previous-hover
    (let [result (f-on-hover previous-hover)]
      [previous-hover result])
    (let [[hover result] (first (take-while (fn [[hover result]] (result-pred result))
                                            (map (juxt val #(f-on-hover (val %1))) hover-instances)))]
      (else-side-effect [hover result])
      [hover result])))

;;;;;;;;;;;;;;;;
;; Hover info ;;
;;;;;;;;;;;;;;;;

(defonce ^{:private true :doc "The common hover css."}
  hover-css (do (trace :support/hover (str "Processing " StaticStrings/CCW_HOVER_CSS "..."))
                (slurp StaticStrings/CCW_HOVER_CSS)))

(defn- hover-prepend-prolog
  "Prepend text to hover information."
  [hover-info]
  (let [buffer (java.lang.StringBuffer. hover-info)
        prepend-text (HTMLPrinter/convertTopLevelFont hover-css
                                                      (aget (-> (JFaceResources/getFontRegistry)
                                                                (.getFontData StaticStrings/CCW_HOVER_FONT)) 0))]
    (HTMLPrinter/insertPageProlog buffer 0 prepend-text)
    (HTMLPrinter/addPageEpilog buffer)
    (.toString buffer)))

(defn hover-info
  "Return the documentation hover text to be displayed at offset offset
  for editor. The text can be composed of a subset of html (e.g. <pre>,
  <i>, etc.). If no info is available or the REPL is nil, it returns
  nil."
  [^IClojureAwarePart part offset]
  (let [loc (offset-loc part offset)
        parse-symbol (parse-symbol? loc)]
    (trace :support/hover (str "hover-info:\n" "offset -> " offset "\n" "parse-symbol -> " parse-symbol "\n"))
    (when parse-symbol
      (let [m (find-var-metadata (.findDeclaringNamespace part)
                                 part
                                 parse-symbol)]
        (var-doc-info-html m)))))

(defn hover-region
  "For editor, given the character offset, return a vector of [offset
  length] representing a region of the editor (containing offset).  The
  idea is that for every offset in that region, the same documentation
  hover as the one computed for offset will be used.  This is a function
  for optimizing the number of times the hover-info function is called."
  [^IClojureAwarePart part offset]
  (let [loc (offset-loc part offset)]
    [(start-offset loc) (loc-count loc)]))

(defn hover-html
  "Given a IClojureAwarePart and an offset, builds the html file to be displayed.
  Simlarly to hover-info, if no info is available or the REPL is nil, it
  returns nil."
  [part offset]
  (let [info (hover-info part offset)]
    (if-not (blank? info)
      (hover-prepend-prolog info))))

;;;;;;;;;;;;;;;;;;
;;; Observables ;;
;;;;;;;;;;;;;;;;;;

(defonce ^{:doc "Var containing the ObservableList of HoverDescriptor(s). The underlaying
 implementation must be thread safe as this instance is injected early (its lifecycle coinciding
 with the plugin's) and will mutate." }
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

(defonce ^{:doc "It contains the hover descriptors."}
  contributed-hovers (atom nil))

(defonce ^{:doc "It contains the hover instances."}
  hover-instances (atom nil))

(defn- reset-locals
  "Resets the hover local (possibly shared) instances."
  []
  (reset! contributed-hovers nil)
  (reset! hover-instances nil))

(defn- init-locals
  "Initializes the hover local (possibly shared) instances."
  []
  (swap! contributed-hovers (comp merge-from-preference load-descriptors-if-necessary)))

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
           (UiUtils/computeStateMask ms)
           ms))))

(defmethod from-java HoverDescriptor [^HoverDescriptor instance]
  "The conversion function from the Java world, modifier string and state mask are not checked here."
  {:id (.getId instance)
   :label (.getLabel instance)
   :enabled (.isEnabled instance)
   :description (.getDescription instance)
   :modifier-string (.getModifierString instance)
   :state-mask (.getStateMask instance)})

;;;;;;;;;;;;;;
;;; Public ;;;
;;;;;;;;;;;;;;

(defn add-registry-listener
  []
  (println "Registering" text-hover-extension-point "extension point registry listener...")
  (add-extension-listener
   (reify
     IRegistryEventListener
     (^void added [this #^"[Lorg.eclipse.core.runtime.IExtension;" extensions]
       (doall (map #(trace :support/hover (str "IRegistryEventListener added extension with id: "
                                               (.getLabel ^org.eclipse.core.runtime.IExtension %1))) extensions))
       (trace :support/hover "Resetting hovers...")
       (reset-locals)
       (reset-default-hover-on-active-editor!))

     (^void removed [this #^"[Lorg.eclipse.core.runtime.IExtension;" extensions]
       (doall (map #(trace :support/hover (str "IRegistryEventListener removed extension with id: "
                                               (.getLabel ^org.eclipse.core.runtime.IExtension %1))) extensions))
       (trace :support/hover "Resetting hovers...")
       (reset-locals)
       (reset-default-hover-on-active-editor!))

     (^void added [this #^"[Lorg.eclipse.core.runtime.IExtensionPoint;" extensionpoints]
       (doall (map #(trace :support/hover (str "IRegistryEventListener removed extension point with id: "
                                               (.getLabel ^org.eclipse.core.runtime.IExtension %1))) extensionpoints))
       (trace :support/hover "Resetting hovers...")
       (reset-locals)
       (reset-default-hover-on-active-editor!))

     (^void removed [this #^"[Lorg.eclipse.core.runtime.IExtensionPoint;" extensionpoints]
       (doall (map #(trace :support/hover (str "IRegistryEventListener removed extension point with id: "
                                               (.getLabel ^org.eclipse.core.runtime.IExtension %1))) extensionpoints))
       (trace :support/hover "Resetting hovers...")
       (reset-locals)
       (reset-default-hover-on-active-editor!)))

   text-hover-extension-point))

(defn add-preference-listener
  []
  (let [pref-key PreferenceConstants/EDITOR_TEXT_HOVER_DESCRIPTORS]
    (trace :support/hover (str "Registering" pref-key " listener..."))
    (ccw.eclipse/add-preference-listener pref-key
                                         #(do
                                            (trace :support/hover (str "Preference " pref-key " has changed. Resetting hovers..."))
                                            (reset-locals)
                                            (reset-default-hover-on-active-editor!)))))

(defn- create-hover-instance
  "Returns an ITextHover (or extensions) instance given, content-type and state-mask."
  [descriptors content-type state-mask]
  ;; TODO The selection could be improved by indexing the descriptors by state-mask
  (init-locals)
  (when-let [selected-descriptor (select-descriptor content-type state-mask descriptors)]
    ;; ensure non nil descriptor arrives here
    (let [state-mask (selected-descriptor :state-mask)]
      (do-if-no-value descriptors state-mask
                      (swap! hover-instances #(assoc %1 state-mask value#))
                      ((selected-descriptor :create-hover))))))

(defn hover-instance
  "Public API which is called from java."
  [content-type state-mask]
  (create-hover-instance @contributed-hovers content-type state-mask))

(defn init-injections
  "Sets the a new instance of HoverModel in the input context."
  [^IEclipseContext context]
  (trace :support/hover (str "Initializing Injections..."))
  (.set context StaticStrings/CCW_CONTEXT_VALUE_HOVERMODEL
        (reify
          HoverModel
          (observableHoverDescriptors [this]
            (init-locals)
            (update-observables-atom @contributed-hovers))

          (persistHoverDescriptors [this list]
            (persist-java-hover-descriptors! observable-hovers)))))

(defn configured-state-masks
  "Returns the configured state masks (as array of int) given source viewer and content type.
  This function is called early, when the contributed-hovers atom is still empty, therefore needs
  to get its returned array from the preferences."
  [_ _]
  (int-array (map #(%1 :state-mask) (read-descriptors-from-preference!))))

(defn hover-information-provider
  "Creates the InformationProvider that displays hovers."
  []
  (let [previous-hover (atom nil)]
    (reify
      IInformationProvider
      (getSubject [this text-viewer offset]
        (trace :support/hover (str (simple-name this) ": offset-> " offset))
        (let [[_ region] (hover-result-pair #(.getHoverRegion %1 text-viewer offset)
                                            (complement nil?)
                                            @hover-instances
                                            @previous-hover
                                            (fn [[hover _]] (reset! previous-hover hover)))]
          region))

      (getInformation [this text-viewer region]
        ;; AR - deprecated
        nil)

      IInformationProviderExtension
      (getInformation2 [this text-viewer region]
        (trace :support/hover (str (simple-name this) ": region " region))
        (let [[_ info] (hover-result-pair #(.getHoverInfo2 %1 text-viewer region)
                                          blank?
                                          @hover-instances
                                          @previous-hover
                                          (fn [[hover _]] (reset! previous-hover hover)))]
          (let [[i msg] (if info [info nil] [nil ClojureEditorMessages/You_need_a_running_repl])]
            (do (set-status-line-error-msg-async text-viewer msg) i))))

      IInformationProviderExtension2
      (getInformationPresenterControlCreator [this]
        (trace :support/hover (str (simple-name this) "getInformationPresenterControlCreator called"))
        (.getInformationPresenterControlCreator @previous-hover)))))
