(ns ^{:doc "Eclipse 4 Core Model manipulation namespace"}
     ccw.e4.model
  (:require [clojure.java.io :as io]
            [ccw.eclipse     :as e])
  (:import [org.eclipse.e4.core.contexts IEclipseContext]
           [org.eclipse.ui IWorkbenchPart]
           [org.eclipse.ui.services IServiceLocator]
           [org.eclipse.e4.ui.services IServiceConstants]
           [org.eclipse.e4.ui.bindings EBindingService]
           [org.eclipse.e4.core.commands ECommandService
                                         EHandlerService]
           [org.eclipse.e4.ui.workbench.modeling EPartService EModelService]
           [org.eclipse.e4.ui.model.application MApplication]
           [org.eclipse.e4.ui.model.application.commands 
            MCommand MCategory MHandler
            MCommandsFactory]
           [org.eclipse.jface.bindings.keys KeySequence]
           [org.eclipse.swt.widgets Composite]
           [org.eclipse.e4.ui.model.application.ui 
            MContext]
           [org.eclipse.e4.ui.model.application.ui.basic 
            MBasicFactory
            MWindow
            MPart]))

;; TEMPORARY (or not): singleton MApplication set via Eclipse4ModelProcessor
(defonce app (atom nil))
(defn application! [a] (reset! app a))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IEclipseContext: adapters, getter functions

(defprotocol HasContext
  "Protocol for retrieving the associated IEclipseContext for objects
   for which this makes sense."
  (-context [o] "Return the context for o, or nil"))

(extend-protocol HasContext
  IEclipseContext
  (-context [this] this)
  
  IServiceLocator
  (-context [this] (.getService this IEclipseContext))
  
  IWorkbenchPart
  (-context [this] (-context (.getSite this)))
  
  MContext
  (-context [this] (.getContext this)))

(def service-constants
  "Pre-defined service constants for retrieving context information"
  {:active-selection IServiceConstants/ACTIVE_SELECTION
   :active-contexts  IServiceConstants/ACTIVE_CONTEXTS
   :active-part      IServiceConstants/ACTIVE_PART
   :active-shell     IServiceConstants/ACTIVE_SHELL
   :part-service     EPartService
   :command-service  ECommandService
   :handler-service  EHandlerService
   :binding-service  EBindingService
   :model-service    EModelService
   :application      MApplication
   :composite        Composite
   :mwindow          MWindow
   :window           MWindow
   :mpart            MPart
   :part             MPart
   })

(defn context-key 
  "Get value of key k for has-context which must be extended from protocol HasContext"
  [has-context k]
  (.get (-context has-context)
    (if (keyword? k)
      (service-constants k)
      k)))

;; Dynamic creation of Model Elements: MParts, MWindow, etc.
(defn mbasic-factory [] MBasicFactory/INSTANCE)
(defn mcommands-factory [] MCommandsFactory/INSTANCE)

(defn create-part [] (.createPart (mbasic-factory)))
(defn create-input-part [] (.createInputPart (mbasic-factory)))
(defn create-part-stack [] (.createPartStack (mbasic-factory)))
(defn create-part-sash-container [] (.createPartSashContainer (mbasic-factory)))
(defn create-window [] (.createWindow (mbasic-factory)))
(defn create-trimmed-window [] (.createTrimmedWindow (mbasic-factory)))
(defn create-trim-bar [] (.createTrimBar (mbasic-factory)))
(defn create-binding-context [] (.createBindingContext (mcommands-factory)))
(defn create-binding-table [] (.createBindingTable (mcommands-factory)))
(defn create-command [] (.createCommand (mcommands-factory)))
(defn create-command-parameter [] (.createCommandParameter (mcommands-factory)))
(defn create-handler [] (.createHandler (mcommands-factory)))
(defn create-key-binding [] (.createKeyBinding (mcommands-factory)))
(defn create-parameter [] (.createParameter (mcommands-factory)))
(defn create-category [] (.createCategory (mcommands-factory)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Macros for easily creating clojure->java field functions

(require '[clojure.string :as s])

(defn camelize [s]
  (let [parts (s/split s #"-")
        capitalize #(str (.toUpperCase (subs % 0 1)) (subs % 1))]
    (apply str (map capitalize parts))))

(defn clj->setter [s] (str "set" (camelize s)))

(defn clj->getter [s & options]
  (str (if (some #{:boolean} options) "is" "get")
       (camelize s)))

(defn field* [f]
  (let [d `(defn ~(:name f) ~(or (:doc f) ""))
        getter (when (:getter f)
                 `[([~'o] (. ~'o ~(:getter f)))])
        setter (when (:setter f)
                 `[([~'o ~'value] (. ~'o ~(:setter f) ~'value) ~'o)])]
    (concat d getter setter)))
 
(defmacro deffield [n doc & [getter setter]]
  (let [options (set (keys (meta n)))
        options (if-not (some #{:getter :setter} options)
                  (conj options :getter :setter)
                  options)
        field {:name n
               :doc doc}
        field (if (or getter (:getter options))
                (assoc 
                  field 
                  :getter
                  (or getter
                      (symbol (apply clj->getter (name n) options))))
                field)
        field (if (or setter (:setter options))
                (assoc 
                  field
                  :setter
                  (or setter
                      (symbol (clj->setter (name n)))))
                field)]
    (field* field)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MApplicationElement helper functions.
;; This is the root element for all UI Model elements, defining 
;; attributes common to every element; the element's id as well as 
;; three general storage elements:
;; - tags: This is a set of strings which can be used to stereotype a 
;;   particular element. Tags may be specified in element searches
;;   and can also be referred to in the CSS styling definition.
;; - persiste-state: A string to string map used to store information
;;   that needs to be persisted between sessions.
;; - transient-data: A string to object map which can be used to store 
;;   runtime data relevant to a particular model element.
;;

(deffield element-id "")

(deffield ^:getter persisted-state
  "This is a String to String map that can be used to persist
   information about model elements across program sessions.
   The format of the 'value' string is defined by the code setting 
   the value into the map. Information stored in this map is part of
	 the model and will be persisted and restored as such.")

(deffield ^:getter tags
  "Tags are a list of Strings that are persistent parts of the UI Model.
   They can be used to 'refine' a particular model element, supplying 
   extra 'meta' information. These tags interact with the CSS engine so
   that it's possible to write CSS specific to a particular tag. The 
   platform currently uses this mechanism to cause the color change
   in the stack containing the currently active part")

(deffield contributor-URI
  "This field is used to track the bundle (if any) from which the UI 
   element was derived in order to faciliate its removal should the
   bundle go away or be updated.")

(deffield ^:getter transient-data
  "This is a String to Object map into which any desired runtime 
   information related to a particular element may be stored.
   It is *not* persisted across sessions so it is not necessary that
   the 'values' be serializable.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MUIElement helper functions.
;; This is the base mix-in shared by all model elements that can be
;; rendered into the UI presentation of the application. Its main job
;; is to manage the bindings between the concrete element and 
;; the UI 'widget' representing it in the UI.

(deffield widget
  "This field represents the platform specific UI 'widget' that is
   representing this UIElement on the screen. It will only be
   non-null when the element has been rendered.")

(deffield renderer
  "This field tracks the specific renderer used to create the 'widget'.")

(deffield ^:boolean to-be-rendered
  "This field controls whether the given UIElement should be displayed
   within the application. Note that due to lazy loading it is possible
   to have this field set to true but to not have actually rendered the
   element itself (it does show up as a tab on the appropiate stack but
   will only be rendered when that tab is selected.")

(deffield ^:boolean on-top "")

(deffield ^:boolean visible
  "This field determines whether or not the given UIElement appears in
   the presentation or whether it should be 'cached' for specialized use.
   Under normal circumstances this flag should always be 'true'.
	 The MinMaxAddon uses this flag for example when a stack becomes
   minimized. By setting the flag to false the stack's widget is
   cleanly removed from the UI but is still 'rendered'. Once the widget
   has been cached the minimized stack can then display the widget
   using its own techinques.")

(deffield parent
  "This field is a reference to this element's container.
   Note that while this field is valid for most UIElements there are
   a few (such as TrimBars and the Windows associated with top level
   windows and perspectives) where this will return 'null'.")

(deffield container-data
  "This is a persistent field that may be used by the parent element's
   renderer to maintain any data that it needs to control the container.
   For example this is where the SashRenderer stores the 'weight' of a
   particular element.
	 NOTE: This field is effectively deprecated in favor of the parent
   renderer simply adding a new keyed value to the UIElement's 
   'persistentData' map.")

(deffield cur-shared-ref
  "This is a transient (i.e. non-persisted) field which is used in
   conjunction with MPlaceholders which are used to share elements
   across multiple perspectives. This field will point back to the
   MPlaceholder (if any) currently hosting this one.")

(deffield visible-when "")

(deffield accessibility-phrase
  "This field is provided as a way to inform accessibility screen
   readers with extra information. The intent is that the reader
   should 'say' this phrase as well as what it would normally emit
   given the widget hierarchy.")

(deffield ^:getter localized-accessibility-phrase
  "This field is intended to allow enhanced support for accessibility
   by providing the ability to have a screen reader 'say' this phrase
   along with its normal output.
	 This is currently unused in the base SWT renderer but is available
   for use by other rendering platforms...")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MElementContainer helper functions.
;; This is the base for the two different types of containment used in
;; the model: 'Stacks' (where only one element would be visible at a
;; time) and 'Tiles' (where all the elements are visible at the same
;; time.
;; All containers define the type of element that they are to contain.
;; By design this is always a single type. Where different concrete
;; types are to be contained within the same container they all both
;; mix in a container-specific type. For example both MParts and 
;; MPlaceholders are valid children for an MPartStack so they both mix
;; in 'StackElement' (which is an empty stub used only to constrain
;; the stack's types.

(deffield ^:getter element-container-children
  "This is the list of contained elements in this container. All
   elements must be of the same type.")

(deffield selected-element
  "This field contains the reference to the currently 'selected'
   element within a container.
	 Note that the element must not only be in the container's children
   list but must also be visible in the presentation
   (\"toBeRendered' == true)")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MApplication helper functions.
;; The MApplication acts as the root of the UI Model. It's children
;; are the MWindows representing the UI for this application. It also
;; owns the application's context (which is hooked to the OSGI context,
;; allowing access not only to its own runtime information but also to
;; any registered OSGI service.
;; 
;; It also owns a number of caches which, while independent of the UI
;; itself are used by the application to populate new windows or to
;; define state that is expected to be the same for all windows:
;; - Keybindings, Handlers, Commands
;; - Part Descriptors (to support a 'Show View' dialog...)
;; - Snippets of model (such as saved perspectives...)
;;

(deffield ^:getter commands
  "This is the list of MCommand elements available in the application.
   Commands represent some logical operation. The actual implementation
   of the operation is determined by the MHandler chosen by the system
   based on the current execution context.")

(deffield ^:getter addons
  "This is the ordered list of MAddons for this model. The individual
   addons will be created through injection after the model loads but
   before it is rendered.")

(deffield ^:getter categories "")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MCommand helper functions.
;; A Command represents a logical operation within the application.
;; The implementation is provided by an MHandler chosen by examining
;; all the candidate's enablement.
;;

(deffield command-name
  "This field holds the command's name, used in the UI by default when
   there are menu or toolbar items representing this command.")

(deffield command-description
  "This field holds the command's description, used in the UI when the
   commands being shown in dialogs...."
  getDescription setDescription)

(deffield ^:getter parameters
  "Returns the value of the 'Parameters' containment reference list.
	 The list contents are of type {org.eclipse.e4.ui.model.application.commands.MCommandParameter}.
	 This list defines the set of parameters that this command expects to
   have defined during execution.")

(deffield category "")

(deffield ^:getter localized-command-name
  "This is a method that will return the translated name.")

(deffield ^:getter localized-command-description
  "This is a method that will return the translated description."
  getLocalizedDescription setLocalizedDescription)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MCommandParameter helper functions.
;; This represents the format of a parameter to be used in a Command.
;;

(deffield command-parameter-name
  "The name of the parameter")

(deffield type-id
  "The type of the parameter")

(deffield ^:boolean optional
  "Determines whether or not this parameter is optional")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MCategory helper functions.
;; This defines a logical grouping of Commands in order to facilitate
;; showing the current set of Commands in dialogs, lists etc
;; 

(deffield category-name
  "The name to be displayed for this category"
  getName setName)

(deffield category-description
  "The description to display for this category"
  getDescription setDescription)

(deffield ^:getter localized-category-name
  "This is a method that will return the translated name of the
   Category"
  getLocalizedName setLocalizedName)

(deffield ^:getter localized-category-description
  "This is a method that will return the translated description
   of the Category."
  getLocalizedDescription setLocalizedDescription)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MContribution helper functions.
;; MContribution is a mix-in class used by concrete elements such as 
;; Parts to define the location of the client supplied class 
;; implementing the specific logic needed.
;; 

(deffield contribution-URI
  "The ContributionURI defines the complete path to a class implementing
   the logic for elements that require external code to handle the UI
   such as MParts and MHandlers.")

(deffield object
  "This is the DI created instance of the class implementing the logic
   for the element. It will only be non-null if the element has been
   rendered into the presentation.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MHandler helper functions.
;; Handlers provide the execution logic that provides the
;; implementation of a particular command.
;; 

(deffield command
  "This is a reference to the Command for which this is an execution
   candidate.")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MBindingTableContainer helper functions.
;; This type contains the list of binding 'tables', representing the various
;; sets of bindings based on the application's current running 'context'.
;; Here the 'context' represents the application's UI state
;; (i.e. whether a Dialog is open...).
;; 

(deffield ^:getter binding-tables "list of MBindingTable")

(deffield ^:getter root-context "list of MBindingContext")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MBindingTable helper functions.
;; A set of Bindings that will be active if the matching MBindingContext
;; is active.
;; 

(deffield ^:getter bindings "list of MKeybinding")

(deffield binding-context "MBindingContext")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MKeySequence helper functions.
;; This represents the sequence of characters in a KeyBinding whose 
;; detection will fire the associated Command.
;; 

(deffield key-sequence 
  "This is a formatted string used by the key binding infrastructure to
   determine the exact key sequence for a KeyBinding.")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MKeyBinding helper functions.
;; Keybindings map a particular keyboard sequence (i.e. Ctrl + C for Copy...)
;; onto some command.
;; 

(deffield key-binding-command
  "A reference to the Command to (attempt to) execute if the given key
   sequence is detected."
  getCommand setCommand)

(deffield ^:getter key-binding-parameters
  "This allows a KeyBinding to provide a particular set of parameters to be
   used when the Command is to be executed. This allows generic commands
   like 'Open Part' to have bindings that will open a _specific_ Part..."
  getParameters setParameters)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MWindow helper functions.
;; This is the concrete class representing a bare bones window in the
;; UI Model. Unless specifically desired it's likely better to use the
;; TrimmedWindow instead.
;; 

(deffield main-menu "The main menu (if any) for this window.")

(deffield x "The 'X' position of this window")

(deffield y "The 'Y' position of this window")

(deffield width "The width of this window")

(deffield height "The height of this window")

(deffield ^:getter windows
  "The collection of 'Detached' windows associated with this window")

(deffield ^:getter shared-elements
  "This is the collection of UI Elements that are referenced by
   Placeholders, allowing the re-use of these elements in different
   Perspectives.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MTrimmedWindow helper functions.
;; A subclass of Window that also supports TrimBars on its edges.

(deffield ^:getter trim-bars
  "The collection of TrimBars associated with this window.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MUILabel helper functions.
;; This is a mix in that will be used for UI Elements that are capable
;; of showing label information in the GUI (e.g. Parts, Menus / Toolbars,
;; Persepectives...)
;;

(deffield label
  "The label to display for this element. If the label is expected to
   be internationalized then the label may be set to a 'key' value 
   to be used by the translation service.")

(deffield icon-URI
  "This field contains a fully qualified URL defining the path to an
   Image to display for this element.")

(deffield tooltip
  "The tooltip to display for this element. If the tooltip is expected
   to be internationalized then the tooltip may be set to a 'key' value
   to be used by the translation service.")

(deffield ^:getter localized-label
  "This is a method that will retrieve the internationalized label by
   using the current value of the label itself and some translation
   service.")

(deffield ^:getter localized-tooltip
  "This is a method that will retrieve the internationalized tooltip
   by using the current")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MContext helper functions.
;; This class is mixed into a UI element when that element is expected
;; to participate in the Dependency Injection context hierarchy.
;; The context life-cycle matches that of the rendered element it
;; belongs to. It's automatically created when the element is rendered
;; and disposed when the element is unrendered.
;;
(deffield context
  "This attribute is a reference to the IEclipseContext for this UI
   element. It will be non-null only when the element is rendered.")

(deffield ^:getter variables "")

(deffield ^:getter properties "")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MHandlerContainer helper functions.
;; This provides a container in which to store lists of Handlers
;;

(deffield ^:getter handlers "")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MBindings helper functions.
;; Mixin interface that lists MBindingContexts that should be active
;; when this object is active.
;;Example values: org.eclipse.ui.contexts.dialog, 
;;                org.eclipse.ui.contexts.window

(deffield binding-contexts "")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MBindingContext helper functions.
;; This class describes the hierarchy of contexts that are used by the
;; EBindingService to determine which Bindings are currently available
;; to the user.

(deffield binding-context-name "" getName setName)

(deffield binding-context-description "" getDescription setDescription)

(deffield ^:getter binding-context-children "" getChildren setChildren)

;; TODO MSnippetContainer
;; 

;; TODO Use Prismatic Schema to manage specs

(defn find-command 
  "Find Model Command of id cmd-id in the Application Model application"
  [application cmd-id]
  (first (filter #(= cmd-id (element-id %))
                 (commands application))))

(defn find-handler 
  "Find Model Handler of id hdl-id in the Application Model application"
  [application hdl-id]
  (first (filter #(= hdl-id (element-id %))
                 (handlers application))))

(defn find-binding-context
  [c name]
  (if (= name (element-id c))
    c
    (some #(find-binding-context % name)
            (binding-context-children c))))

(defn find-app-binding-context 
  [app name]
  (let [contexts (root-context app)]
    (some #(find-binding-context % name) contexts)))

(defn find-binding-table 
  [app context]
  (let [context (if (string? context)
                  (find-app-binding-context app context)
                  context)]
    (first (filter #(= context (binding-context %)) (binding-tables app)))))

(defn key-binding=spec? 
  [key-binding spec]
  (and
    (= (:command spec) (key-binding-command key-binding))
    (= (:key-sequence spec) (key-sequence key-binding))
    ;; TODO :locale :platform :scheme
    ))
(defn find-key-binding
  "Find Model Key binding based on its fields"
  [app spec]
  (when-let [b-table (find-binding-table app (:context spec))]
    (first (filter #(key-binding=spec? % spec) (bindings b-table)))))

(defn find-category
  "Find Model Category of id category-id in the Application Model application"
  [application category-id]
  (first (filter #(= category-id (element-id %))
                 (categories application))))

(defn assoc-tags! [x x-spec]
  (let [tgs (tags x)]
    (doseq [t (:tags x-spec)] (.add tgs t))))

(defn add-or-replace-xxx!
  "Add or Replace the command to the Application Model of the HasContext'able
  has-context. Return the application."
  [application find-xxx xxxs xxx]
  (let [app-xxx (find-xxx application (element-id xxx))]
    (when app-xxx (.remove (xxxs application) app-xxx))
    (.add (xxxs application) 0 xxx))
  application)

(defn add-command!
  "Add the command to the Application Model. Return the application."
  [application command]
  (.add (commands application) 0 command)
  application)

(defn add-handler!
  "Add the handler to the Application Model. Return the application"
  [application handler]
  (let [hdls (handlers application)]
    (.add hdls 0 handler)
    application))

(defn execute-command!
  "command can be a :command-id string or a command object
   in both cases, the command must already be existent in
   the Application Model. Returns nil." 
  [has-context command]
  (let [handler-service (context-key has-context :handler-service)
        command-service (context-key has-context :command-service)
        command-id (if (string? command) command (element-id command))
        pcommand (.createCommand command-service command-id nil)]
    (.executeHandler handler-service pcommand)))

(defn find-xxx-by-tags [application xxx search-tags]
  (into [] (filter 
             #(every? (set (tags %)) search-tags)
             (xxx application))))

(defn find-commands-by-tags [application search-tags]
  (find-xxx-by-tags application commands search-tags))

(defn find-handlers-by-tags [application search-tags]
  (find-xxx-by-tags application handlers search-tags))

(defn find-xxx-by-id [application xxx id]
  (into [] (filter #(= id (element-id %)) (xxx application))))

(defn find-commands-by-id [application id]
  (find-xxx-by-id application commands id))

(defn find-handlers-by-id [application id]
  (find-xxx-by-id application handlers id))

(defn find-elements-by-tags [model-service search-root tags]
  (into [] (.findElements model-service search-root nil nil tags)))

(defn find-elements-by-id [model-service search-root id]
  (into [] (.findElements model-service search-root id nil nil)))

(defn find-elements-by-class [model-service search-root class]
  (into [] (.findElements model-service search-root nil class nil)))

(defn remove-xxx-by-tags! [application xxx search-tags]
  (let [app-xxxs (xxx application)
        xxxs (find-xxx-by-tags application xxx search-tags)]
    (doseq [x xxxs] (.remove app-xxxs x))
    application))

(defn remove-commands-by-tags! [application search-tags]
  (remove-xxx-by-tags! application commands search-tags))

(defn remove-handlers-by-tags! [application search-tags]
  (remove-xxx-by-tags! application handlers search-tags))

(defn remove-xxx-by-id! [application xxx id]
  (let [app-xxxs (xxx application)
        xxxs (find-xxx-by-id application xxx id)]
    (doseq [x xxxs]
      (.remove app-xxxs x))
    application))

(defn remove-commands-by-id! [application id]
  (remove-xxx-by-id! application commands id))

(defn remove-handlers-by-id! [application id]
  (remove-xxx-by-id! application handlers id))

(defn remove-elements-by-tags! [model-service application search-tags]
  (let [elts (find-elements-by-tags (or model-service (context-key application :model-service)) application search-tags)]
    (doseq [elt elts]
      (let [parent (.getParent elt)]
        (.remove elt (element-container-children parent))))))

(defn remove-all-by-tags! [model-service application search-tags]
  (remove-commands-by-tags! application search-tags)
  (remove-elements-by-tags! model-service application search-tags))

(defn update-transient-data!
  "Find transient data in (:transient-data spec) and add to the transient
   data of e"
  [e spec]
  ;(println "update-transient-data!" (type e) e spec)
  (let [td (transient-data e)]
    (doseq [[k v] (:transient-data spec)]
      ;(println "transient data:" "k:" k "v:" v)
      (.put td k v))))

(def default-cmd-spec
  ;; :element-id and :command-name are mandatory => no default value
  ;; :category is optional TODO default value = "ccw.user.extension" => something like that
  { :description "" })

;; TODO use :id instead of :element-id
;; TODO use :description instead of :command-description
;; TODO use :name instead of :command-name
;; TODO check spec
;; TODO element-id! => since it changes
(defn update-command! 
  [cmd spec]
  (let [spec (merge default-cmd-spec spec)]
    (doto cmd
      (element-id (:id spec))
      (command-name (:name spec))
      (command-description (:description spec))
      (category (if (string? (:category spec))
                  (find-category app (:category spec))
                  (:category spec)))
      (assoc-tags! spec)
      (update-transient-data! spec))))

(defn merge-command! 
  [app spec]
  (if-let [cmd (find-command app (:id spec))]
    (update-command! cmd spec)
    (let [cmd (create-command)]
      (update-command! cmd spec)
      (add-command! app cmd)
      cmd)))

(def default-hdl-spec {})

(defn update-handler!
  [hdl spec]
  (let [spec (merge default-hdl-spec spec)]
    (doto hdl
      (element-id (:id spec))
      (command (:command spec))
      (contribution-URI (:contribution-URI spec))
      (assoc-tags! spec)
      (update-transient-data! spec))))

(defn merge-handler!
  "TODO REWRITE :command and :contribution-URI mandatory
   command can be either a command-id string, or a command object
   in both cases, the command must already be existent in the
   Application Model"
  [app spec]
  (let [cmd (if (string? (:command spec))
              (find-command app (:command spec))
              (:command spec))
        spec (assoc spec :command cmd)]
    (if-let [hdl (find-handler app (:id spec))]
      (update-handler! hdl spec)
      (let [hdl (create-handler)]
        (update-handler! hdl spec)
        (add-handler! app hdl)
        hdl))))

(defn assoc-value-as-keys
  [m]
  (merge (zipmap (vals m) (vals m)) m))

(def key-binding-scheme
  (assoc-value-as-keys
    {:default "org.eclipse.ui.defaultAcceleratorConfiguration"
     :emacs   "org.eclipse.ui.emacsAcceleratorConfiguration"}))

(def key-binding-context
  (assoc-value-as-keys
    {:clojure-editor "ccw.ui.clojureEditorScope"
     :text-editor "org.eclipse.ui.textEditorScope"
     :repl "ccw.ui.context.repl"
     :dialog+window "org.eclipse.ui.contexts.dialogAndWindow"
     :dialog "org.eclipse.ui.contexts.dialog"
     :window "org.eclipse.ui.contexts.window"
     :views "org.eclipse.ui.contexts.views"
     :console "org.eclipse.ui.console.ConsoleView"}))

(defn update-key-binding!
  [kb spec]
  (doto kb
    (key-binding-command (:command spec))
    (key-sequence (:key-sequence spec))
    (assoc-tags! spec)
    (update-transient-data! spec)))

(defn find-or-create-binding-table 
  [app context]
  (if-let [bt (find-binding-table app context)]
    bt
    (do 
      (println "find-or-create-binding-table bt not found")
      (let [bt (create-binding-table)]
       (doto bt
         (binding-context context))
       (.add (binding-tables app) 0 bt)
       bt))))

(defn add-key-binding!
  "Add the key binding to the Application Model. Return the application"
  [app context kb]
  (let [bt (find-or-create-binding-table app context)]
    (.add (bindings bt) 0 kb)
    app))

(defn normalize-key-sequence
  "Takes a key sequence such as \"M1+A B\" and transforms it into a platform
   specific key sequence such as \"COMMAND+A B\" on OS X or \"CTRL+A B\" on
   Windows/Linux."
  [ks]
  (.toString (KeySequence/getInstance ks)))

(defn desugarize-key-sequence
  "Considers ks is not already normalized, and applies the following transformations
   to it. The result can be passed to 'normalize-key-sequence.
   - transforms \"Cmd+A B\" into \"M1+A B\". Cmd feels more natural than M1, and
     since it only exists on Mac Keyboards, can be used to express either Cmd on
     OS X or Ctrl on Windows/Linux."
  [ks]
  (-> ks
    (.toUpperCase)
    (.replaceAll "CMD" "M1")))

;; how/where to use :scheme ?
(defn merge-key-binding!
  [app spec]
  (let [cmd (if (string? (:command spec))
              (find-command app (:command spec))
              (:command spec))
        spec (assoc spec :command cmd)
        spec (assoc spec :context (or
                                    (find-app-binding-context app
                                      (key-binding-context (:context spec) (:context spec)))
                                    (do
                                      (println "merge-key-binding! error no binding context found for" (:context spec)
                                        ". Falling back to :window context")
                                      (find-app-binding-context app
                                        (key-binding-context :window)))))
        spec (assoc spec :scheme (key-binding-scheme (:scheme spec) (:scheme spec)))
        spec (update-in spec [:key-sequence] (comp normalize-key-sequence desugarize-key-sequence))]
    (println "merge-key-binding! spec" spec)
    (if-let [kb (find-key-binding app spec)]
      (do 
        (println "key binding found:" kb)
        kb)
      (do
        (println "key binding not found, let's create one")
        (let [kb (-> (create-key-binding) (update-key-binding! spec))]
          (println "created key binding:" kb)
          (add-key-binding! app (:context spec) kb)
          (println "added key binding:" kb)
          kb)))))
