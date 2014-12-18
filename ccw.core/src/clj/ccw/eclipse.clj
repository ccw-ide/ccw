(ns ^{:doc "Eclipse interop utilities"}
     ccw.eclipse
  (:require [clojure.java.io :as io])
  (:use [clojure.core.incubator :only [-?> -?>>]])
  (:import [org.eclipse.core.resources IResource
                                       IProject
                                       IProjectDescription
                                       ResourcesPlugin
                                       IWorkspaceRunnable
                                       IWorkspace
                                       IWorkspaceRoot
                                       WorkspaceJob]
           [org.eclipse.core.runtime IPath 
                                     Path
                                     Platform
                                     Plugin
                                     FileLocator
                                     IAdaptable
                                     IProgressMonitor
                                     IStatus
                                     Status]
           [org.eclipse.core.runtime.preferences IEclipsePreferences
                                                 InstanceScope]
           [org.eclipse.jdt.core IJavaProject
                                 JavaCore]
           [org.eclipse.debug.core ILaunchConfiguration ILaunch]
           [org.eclipse.ui.handlers HandlerUtil]
           [org.eclipse.ui IEditorPart
                           PlatformUI
                           IWorkbench]
           [org.eclipse.jface.viewers IStructuredSelection]
           [org.eclipse.jface.operation IRunnableWithProgress]
           [org.eclipse.core.commands ExecutionEvent]
           [org.eclipse.ui.actions WorkspaceModifyDelegatingOperation]
           [java.io File IOException]
           [ccw CCWPlugin]
           [ccw.util PlatformUtil DisplayUtil]
           [ccw.launching LaunchUtils]))

(defn adapter
  "Invokes Eclipse Platform machinery to try by all means to adapt object to 
   class. Either because object is an instance of class, or when object implements
   IAdapter and when invoked returns a non null instance of class, or via the
   Eclipse Platform Extensions mechanism"
  [object class]
  (PlatformUtil/getAdapter object class))

(extend-protocol io/Coercions
  IResource
  (io/as-file [r] (io/as-file (.getLocation r)))
  (io/as-url [r] (io/as-url (io/as-file r)))
  
  IPath
  (io/as-file [p] (.toFile p))
  (io/as-url [p] (io/as-url (io/as-file p)))
  
  Path
  (io/as-file [p] (.toFile p))
  (io/as-url [p] (io/as-url (io/as-file p))))

(defn workspace 
  "Return the Eclipse Workspace" []
  (ResourcesPlugin/getWorkspace))


(defn workspace-root 
  "Return the Eclipse Workspace root"
  []
  (.getRoot (workspace)))

(defn workbench 
  "Return the Eclipse Workbench" []
  (PlatformUI/getWorkbench))

(defn workbench-window
  "Return the Active workbench window" 
  ([] (workbench-window (workbench)))
  ([workbench] (.getActiveWorkbenchWindow workbench)))

(defn workbench-page
  "Return the Active workbench page" 
  ([] (workbench-page (workbench-window)))
  ([workbench-window] (.getActivePage workbench-window)))


(defprotocol IProjectCoercion
  (project ^org.eclipse.core.resources.IProject [this] "Coerce this into an IProject"))

(defprotocol IResourceCoercion
  (resource ^org.eclipse.core.resources.IResource [this] "Coerce this in a IResource"))

(defprotocol IPathCoercion
  (path ^org.eclipse.core.runtime.IPath [this] "Coerce this to an IPath"))

(extend-protocol IProjectCoercion
  nil
  (project [_] nil)
  
  Object
  (project [o] (adapter o IProject))
  
  IProject
  (project [o] o)
  
  clojure.lang.Symbol
  (project [s] (project (name s)))
  
  clojure.lang.Keyword
  (project [s] (project (name s)))
  
  String
  (project [s] (.getProject (workspace-root) s))
  
  IResource
  (project [r] (.getProject r))
  
  IJavaProject
  (project [this] (.getProject this))
  
  IStructuredSelection
  (project [this] (project (resource this)))
  
  IEditorPart
  (project [this] (project (resource this)))
  
  ExecutionEvent
  (project [this] (project (resource this)))
  
  ILaunchConfiguration
  (project [^ILaunchConfiguration l] (LaunchUtils/getProject l))
  
  ILaunch
  (project [^ILaunch l] (LaunchUtils/getProject l)))

(extend-protocol IResourceCoercion
  nil
  (resource [this] nil)
  
  Object
  (resource [o] (adapter o IResource))

  IResource
  (resource [this] this)
  
  IJavaProject
  (resource [this] (project this))
  
  String
  (resource [filesystem-path] (resource (path filesystem-path)))
  
  File
  (resource [file] (resource (.getCanonicalPath file)))
  
  IPath
  (resource [filesystem-path] 
    (.getFileForLocation (workspace-root) filesystem-path))
  
  IStructuredSelection
  (resource [selection] 
    (if (= (.size selection) 1)
      (resource (.getFirstElement selection))
      (throw (RuntimeException. 
               (str "IResourceCoercion/resource: Can only coerce"
                    " a selection of size 1. Selection size: " (.size selection)
                    " Selection: " selection)))))
  
  IEditorPart
  (resource [editor] (adapter (.getEditorInput editor) IResource))
  
  ExecutionEvent
  (resource [execution] (resource (HandlerUtil/getCurrentSelection execution))))

(extend-protocol IPathCoercion
  nil
  (path [_] nil)

  Object
  (path [o] (adapter o IPath))
  
  IPath
  (path [this] this)
  
  IResource
  (path [r] (.getFullPath r))
  
  String
  (path [s] (Path. s))
  
  File
  (path [f] (Path. (.getAbsolutePath f))))


 (def ^:private resource-refresh-depth
   {IResource/DEPTH_ZERO     IResource/DEPTH_ZERO
    IResource/DEPTH_ONE      IResource/DEPTH_ONE
    IResource/DEPTH_INFINITE IResource/DEPTH_INFINITE
    :zero IResource/DEPTH_ZERO
    :one  IResource/DEPTH_ONE
    :infinite IResource/DEPTH_INFINITE})
 
 (defn refresh-resource
   "Synchronize the resource with the filesystem.
    r: the resource to refresh (an IResourceCoercion)
    keyword-based options:
    :depth :zero (refresh only the resource), 
           :one  (refresh resource and immediate children), 
        or :infinite (refresh resource and recursively)
           defaults to :infinite
    :monitor an IProgressMonitor, defaults to nil"
   [r & {:keys [depth monitor] :or {depth :infinite} :as options}]
   (.refreshLocal r (resource-refresh-depth depth) monitor))


(defn plugin-state-location 
  "Return the plugin's state location as a path representing 
   an absolute filesystem path."
  ^IPath [^Plugin plugin]
  (.getStateLocation plugin))

;; TODO refactor this with less interop
(defn get-file-inside-plugin
  [plugin-name file-name]
  (try
    (let [bundle (Platform/getBundle plugin-name)
          clojure-bundle-path (FileLocator/getBundleFile bundle)]
      (if (.isFile clojure-bundle-path)
        (do
          (CCWPlugin/logError (str plugin-name " plugin should be deployed as a directory. This is a regression. Cannot locate file " file-name))
          nil)
        (let [clojure-lib-entry (File. clojure-bundle-path file-name)]
          (if (.exists clojure-lib-entry)
            clojure-lib-entry
            (do
              (CCWPlugin/logError (str "Unable to locate file " + file-name " in " plugin-name " plugin. This is a regression."))
              nil)))))
    (catch IOException e
      (do
        (CCWPlugin/logError (str "Unable to find " plugin-name " plugin. This is probably a regression."))
        nil))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event Handler utilities

(defn current-selection [execution-event]
  (HandlerUtil/getCurrentSelection execution-event))

(defn active-editor [execution-event]
  (HandlerUtil/getActiveEditor execution-event))

(defn active-part [execution-event]
  (HandlerUtil/getActivePart execution-event))

(defn null-progress-monitor []
  (org.eclipse.core.runtime.NullProgressMonitor.))



(def ^:private
  resource-type {IResource/FOLDER  IResource/FOLDER
                 IResource/PROJECT IResource/PROJECT
                 IResource/FILE    IResource/FILE
                 :folder  IResource/FOLDER
                 :project IResource/PROJECT
                 :file    IResource/FILE})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IPaths

(defn path-is-prefix-of 
  "Return truthy (p1) if p1 is a prefix of p2"
  [p1 p2]
  (when (.isPrefixOf (path p1) (path p2)) 
    p1))

(defn path-subtract
  "Return the portion of path 2 which is relative to path p1, aka \"p1 - p2\""
  [p1 p2]
  (.makeRelativeTo (path p2) (path p1)))

(defn path-add-trailing-separator
  "Add a trailing separator to the path represented by p. Return a IPath"
  [p]
  (.addTrailingSeparator (path p)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IProjects
(defn project-name [p] 
  (when-let [p (project p)] (.getName p)))

(defn project-exists [p] 
  (when-let [p (project p)] (and (.exists p) p)))

(defn project-close [p] 
  (when-let [p (project p)] (.close p nil) p))

(defn project-open [p] 
  (when-let [p (project p)] (.open p nil) p))

(defn project-open? [p] 
  (when-let [p (project p)] (and (.isOpen p) p)))

(defn projects-referencing [p] 
  (when-let [p (project p)]
    (into [] (.getReferencingProjects p))))

(defn projects-referenced-by [p] 
  (when-let [p (project p)]
    (into [] (.getReferencedProjects p))))

(defn project-folder [p child-folder-name]
  (when-let [p (project p)]
    (.getFolder p child-folder-name)))

(defn project-file [p child-file-name] 
  (when-let [p (project p)]
    (.getFile p child-file-name)))

(defn projects [] 
  (into [] (.getProjects (workspace-root))))

(defn project-members [p] 
  (when-let [p (project p)] (.members p)))

(defn resource-of-type? [r type] 
  (let [r (resource r)
        type (resource-type type)]
    (and r (= type (.getType r)) r)))


(defn worskpace-file? [r] (resource-of-type? r :file))
(defn workspace-folder? [r] (resource-of-type? r :folder))
(defn workspace-project? [r] (resource-of-type? r :project))

(defn workspace-resource
  "Takes an IPathCoercible representing a workspace-related resource
   (e.g. a String like \"/myproject/src/aPackage/aResource.whatever\")
   and returns a resource handler associated to it"
  [workspace-path]
  (.findMember (workspace-root) (path workspace-path)))

(defn file-extension [r] (and r (.getFileExtension r)))

(defn new-project-desc
  "Initialize a new project description with proj-name for the project name.
   Use as an argument to project-create"
  ([proj-name] (new-project-desc (workspace) proj-name))
  ([w proj-name] (.newProjectDescription w proj-name)))



(defn validate-name-as-resource-type
  "Delagates to Eclipse the validation of n as a valid resource name
   of type type (type being :folder, :project or :file).
   Return nil if ok, an error message otherwise."
  [proj-name type]
  (let [status (.validateName (workspace) proj-name (resource-type type))]
    (when-not (.isOK status) (.getMessage status))))

(defn validate-project-location
  "proj-location must be an absolute path on the filesystem.
   Return nil if ok, an error message otherwise."
  [proj-location]
  (let [status (.validateProjectLocation (workspace)
              	   nil (path proj-location))]
    (when-not (.isOK status) (.getMessage status))))

(defn desc-location! [desc loc]
  (doto desc 
    (.setLocation (path loc))))

(defn -project-create
  "Create project with name proj-name and optional description proj-desc"
  ([proj-name] (-project-create proj-name nil))
  ([proj-name proj-desc]
    (doto (project proj-name)
      (.create proj-desc nil))))

(defn project-create
  "Create project with name proj-name and location proj-location.
   If proj-location is not set, create project in default workspace
   location."
  ([proj-name] (-project-create proj-name))
  ([proj-name proj-location]
    (let [desc (new-project-desc proj-name)
          desc (desc-location! desc proj-location)]
      (-project-create proj-name desc))))

(defn project-desc
  "Return the project description"
  [^IProject proj]
  (.getDescription proj))

(defn project-desc!
  ([^IProject proj desc] (project-desc! proj desc nil))
  ([^IProject proj desc progress-monitor]
    (.setDescription proj desc progress-monitor)))

(defn- update-desc-builders!
  [^IProjectDescription desc f & args]
  (let [spec     (.getBuildSpec desc)
        new-spec (apply f spec args)]
    (doto desc
      (.setBuildSpec (into-array new-spec)))))

(defn desc-builder [^IProjectDescription desc builder-name]
  (doto (.newCommand desc)
    (.setBuilderName builder-name)))

(defn add-desc-builder!
  [desc builder-name]
  (update-desc-builders! desc #(cons (desc-builder desc builder-name) %)))

(defn remove-desc-builder!
  [desc builder-name]
  (update-desc-builders! desc (partial remove #(= builder-name (.getBuilderName %)))))

(defn desc-has-builder? [^IProjectDescription desc builder-name]
  (let [spec (.getBuildSpec desc)]
    (some #(= builder-name (.getBuilderName %)) spec)))

(defn project-has-nature? 
  "Returns the fact that project has nature-id declared. Not the fact that the
   nature is currently activated (which may not be the case if there's a consistency
   problem).
   Pre-requisite: project exists and is open"
  [^IProject proj nature-id]
  {:pre [(.isOpen proj)]}
  (.hasNature proj nature-id))

(defn desc-natures! [^IProjectDescription desc natures]
  (doto desc (.setNatureIds (into-array String natures))))

(defn add-desc-natures! [^IProjectDescription desc & nature-ids]
  (let [natures         (.getNatureIds desc)
        missing-natures (remove (set natures) nature-ids)
        new-natures     (concat natures missing-natures)]
    (desc-natures! desc new-natures)))

(defn remove-desc-natures! [^IProjectDescription desc & nature-ids]
  (let [natures (.getNatureIds desc)]
    (println "(some (set natures) nature-ids)" (some (set natures) nature-ids))
    (if (some (set natures) nature-ids)
      (let [new-natures (remove (set nature-ids) natures)]
        (desc-natures! desc new-natures))
      desc)))

(defn event->project
  "Extract (if possible) a project from an execution event"
  [event]
  (let [sel (current-selection event)
        part (active-part event)]
    (cond
      (project part) (project part)
      (and (instance? IStructuredSelection sel)
           (-> ^IStructuredSelection sel .getFirstElement))
        ;; TODO consider giving the user a hint for why the expected command did not work
        (some-> ^IStructuredSelection sel .getFirstElement resource project)
      :else (when-let [editor (active-editor event)]
              (project editor)))))

(defn event->java-project
  "Extract (if possible) a java project from an execution event"
  [event]
  (when-let [p (event->project event)]
    (JavaCore/create p)))

(defn context-map->project
  "Extract (if possible) a project from a context-map"
  [{:keys [active-selection active-part active-editor]}]
  (println "active-selection:" active-selection)
  (println "active-part:" active-part)
  (println "active-editor:" active-editor)
  (cond
    (project active-part) (project active-part)
    (and
      (instance? IStructuredSelection active-selection)
      (-> ^IStructuredSelection active-selection .getFirstElement))
    ;; TODO consider giving the user a hint for why the expected command did not work
      (some-> ^IStructuredSelection active-selection .getFirstElement resource project)
    :else (when active-editor (project active-editor))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Editor helper functions
;; 

(defn open-workspace-file
  "Eclipse 4 - only.
   Opens a workspace file with the default editor.
   workbench-page is the workbench page to open.
   f is an IFile, or something that can be coerced to an IFile.
   Return the Editor object (IEditorPart)"
  ([f] (open-workspace-file (workbench-page) f))
  ([workbench-page f]
    (org.eclipse.ui.ide.IDE/openEditor
      workbench-page
      (resource f)
      true ; activate the editor
      true ; attempt to resolve the content type for this file
      )))

;; TODO open-filesystem-file

(defn goto-editor-line
  "Given an Editor object, goto the specified line.
   If line is -1, goto last line"
  [editor line]
  (ccw.ClojureCore/gotoEditorLine editor line))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Long-running tasks, background tasks, Workspace Resources related tasks

; Jobs, introduced in Eclipse 3.0, are operations that run in the background. 
; The user is typically prevented from doing anything while an operation is running 
; but is free to continue working when a job is running. Operations and jobs belong 
; together, but jobs needed to live at a lower level in the plug-in architecture 
; to make them usable by non-UI components. 

; job : operation that run in the background. Jobs are usable by non-UI components
; Job: see also http://wiki.eclipse.org/FAQ_Does_the_platform_have_support_for_concurrency%3F
; jobs can report progress via instances of the UI-agnostic IProgressMonitor interface

; Article on jobs treating each and every aspect in depth: http://www.eclipse.org/articles/Article-Concurrency/jobs-api.html

(defprotocol RunnableWithProgress
  (runnable-with-progress [this] "Returns an instance of IRunnableWithProgress"))

(extend-protocol RunnableWithProgress
  nil
  (runnable-with-progress [this] nil)
  
  IRunnableWithProgress
  (runnable-with-progress [this] this)
  
  clojure.lang.IFn
  (runnable-with-progress [f]
    (reify IRunnableWithProgress
      (run [this progress-monitor] 
        (try 
          (f progress-monitor)
          (catch RuntimeException e 
            (throw e))
          (catch Exception e
            (throw (java.lang.reflect.InvocationTargetException. e))))))))


(defn workspace-runnable 
  "operation is a function which takes a progress monitor and will be executed inside
   an IWorkspaceRunnable"
  [operation]
  (reify IWorkspaceRunnable (run [this progress-monitor] (operation progress-monitor))))


(defn run-in-workspace
  "runnable is a function which takes an IProgressMonitor as its argument.
   rule allows to restrain the scope of locked workspace resources.
   avoid-update? enables grouping of resource modification events.
   progress-monitor optional monitor for reporting."
  [runnable rule avoid-update? progress-monitor]
  (let [avoid-update (if avoid-update? IWorkspace/AVOID_UPDATE 0)]
    (-> (ResourcesPlugin/getWorkspace)
      (.run runnable rule avoid-update progress-monitor))))

(defn workspace-job
  "Create a WorkspaceJob with name, and delegate runInWorkspace to (f monitor).
    Takes care of returning (Status/OK_STATUS) in case of success"
  [name f]
  (proxy [WorkspaceJob] [name]
    (runInWorkspace [^IProgressMonitor monitor]
      (let [s (f monitor)]
        (if (instance? IStatus s)
          s
          (Status/OK_STATUS))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Preferences management utilities

(defn preferences
  "Return the preferences for node-name. node-name is generally a bundle name."
  [node-name]
  (.getNode InstanceScope/INSTANCE node-name))

(defn- as-preferences
  "Return a preferences given either an argument of type either String or preferences"
  [preferences-or-node-name]
  (if (instance? String preferences-or-node-name)
    (preferences preferences-or-node-name)
    preferences-or-node-name))

(defn preference
  "Get the value for key, or return the default value.
   preferences-or-node-name is generally either a bundle name, or the result of
   calling 'preferences.
   The 2-arity version is a shortcut for selecting the ccw.core bundle as the node-name
   Example: (preference \"org.eclipse.jdt.ui\" \"org.eclipse.jdt.ui.wizards.srcBinFoldersBinName\")"
  ([key default-value] (preference "ccw.core" key default-value))
  ([preferences-or-node-name key default-value]
   (.get (as-preferences preferences-or-node-name) key default-value)))

(defn preference!
  "Set the value for key.
   preferences-or-node-name is generally either a bundlename, or the result of
   calling 'preferences.
   The 2-arity version is a shortcut for selecting the ccw.core bundle as the node-name.
   Example:"
  ([key value] (preference! "ccw.core" key value))
  ([preferences-or-node-name key value] (.put (as-preferences preferences-or-node-name) key value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SWT utilities

;;; TODO fusionner avec swt.clj
(defn ui*
  "Calls f with (optionally) args on the UI Thread, using
   Display/asyncExec.
   Return a promise which can be used to get back the
   eventual result of the execution of f.
   If calling f throws an Exception, the exception itself is delivered
   to the promise."
  [f & args] 
  (let [a (promise)]
    (-> 
      (org.eclipse.swt.widgets.Display/getDefault)
      (.asyncExec 
        #(deliver a (try
                      (apply f args)
                      (catch Exception e e)))))
    a))

(defmacro async-ui [& args]
  `(ui* (fn [] ~@args)))

(defmacro ui [& args]
  `(if (org.eclipse.swt.widgets.Display/getCurrent)
     (atom (do ~@args))
     (ui* (fn [] ~@args))))

(defn active-shell []
  (-> (org.eclipse.swt.widgets.Display/getDefault)
    .getActiveShell))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Message dialogs

(defn info-dialog 
  ([title message] (info-dialog nil title message))
  ([shell title message]
    (ui
      (let [shell (or shell (active-shell))]
        (org.eclipse.jface.dialogs.MessageDialog/openInformation
          shell title message)))))

(defn confirm-dialog
  ([title message] (info-dialog nil title message))
  ([shell title message]
    (ui
      (let [shell (or shell (active-shell))]
        (org.eclipse.jface.dialogs.MessageDialog/openConfirm
          shell title message)))))

(defn error-dialog 
  ([title message] (info-dialog nil title message))
  ([shell title message]
    (ui
      (let [shell (or shell (active-shell))]
        (org.eclipse.jface.dialogs.MessageDialog/openError
          shell title message)))))

(defn warning-dialog 
  ([title message] (info-dialog nil title message))
  ([shell title message]
    (ui
      (let [shell (or shell (active-shell))]
        (org.eclipse.jface.dialogs.MessageDialog/openWarning
          shell title message)))))

(defn question-dialog 
  ([title message] (info-dialog nil title message))
  ([shell title message]
    (ui
      (let [shell (or shell (active-shell))]
        (org.eclipse.jface.dialogs.MessageDialog/openQuestion
          shell title message)))))

(defn input-dialog 
  "validator is a fn taking the String to validate and returning
   either nil if no error, or the error message if error"
  ([title message initial-value] (input-dialog title message initial-value nil))
  ([title message initial-value validator] (input-dialog nil title message initial-value validator))
  ([shell title message initial-value validator]
    (ui
      (let [shell (or shell (active-shell))
            d     (org.eclipse.jface.dialogs.InputDialog.
                    shell
                    title
                    message
                    initial-value 
                    (when validator
                      (reify org.eclipse.jface.dialogs.IInputValidator
                        (isValid [this new-text] (validator new-text)))))]
        (.open d)
        (.getValue d)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Platform utilities

(def services 
  {:commands org.eclipse.ui.commands.ICommandService
   :handlers org.eclipse.ui.handlers.IHandlerService
   :bindings org.eclipse.ui.keys.IBindingService
   :contexts org.eclipse.ui.contexts.IContextService
   })

(defn service [service-locator s]
  (.getService service-locator (services s s)))

;; command spec
;{:name "command name"
; :description "command description"
; :id "the.qualified.unique.command.id"
; :category {:id "kkk", :name "kkkk", :description "kkkk"}
; }

(defn- handler-proxy [ f ]
  (proxy [org.eclipse.core.commands.AbstractHandler] []
    (execute [event] 
      (f event)
      nil)))

(defn define-handler!*
  ([hdl-spec] (define-handler!* (workbench) hdl-spec))
  ([service-locator hdl-spec]
    (let [hdl-service (service service-locator :handlers)
          handler (handler-proxy (:fn hdl-spec (constantly nil)))]
      (.activateHandler hdl-service 
        (name (:command-id hdl-spec))
        handler
        nil
        true))))

(def global-handlers (atom {}))

(defn define-handler! [hspec]
  (let [hdl-service (service (workbench) :handlers)
        id (name (:command-id hspec))
        prev-handler-activation (@global-handlers id)]
    (when prev-handler-activation
      (println "deactivating previous handler")
      (.deactivateHandler hdl-service prev-handler-activation))
    (let [handler-activation (define-handler!* hspec)]
      (println "registering new handler")
      (swap! global-handlers assoc id handler-activation))))

(def default-category
  {:id :ccw.eclipse.default-category,
   :name "Default Category (CCW)",
   :description "Default Category (CCW)"})

(defn category
  ([cmd-service spec]
    (let [c (.getCategory cmd-service (name (:id spec)))]
      (doto c 
        (.define 
          (:name spec "<no name>") 
          (:description spec "<no description>"))))))

(defn category-ids
  ([] (category-ids (workbench)))
  ([service-locator]
    (let [cmd-service (service service-locator :commands)]
      (.getDefinedCategoryIds cmd-service))))

(defn category-by-id 
  ([id] (category-by-id (workbench) id))
  ([service-locator id]
    (let [cmd-service (service service-locator :commands)]
      (.getCategory cmd-service (name id)))))

(defn command-ids 
  ([] (command-ids (workbench)))
  ([service-locator]
    (let [cmd-service (service service-locator :commands)]
      (into [] (.getDefinedCommandIds cmd-service)))))

(defn command-by-id
  ([id] (command-by-id (workbench) id))
  ([service-locator id]
    (let [cmd-service (service service-locator :commands)]
      (.getCommand cmd-service (name id)))))

(defn define-command! 
  ([cmd-spec] (define-command! (workbench) cmd-spec))
  ([service-locator cmd-spec]
    (let [cmd-service (service service-locator :commands)
          c (command-by-id service-locator (name (:id cmd-spec)))]
      (doto c
        (.define 
          (:name cmd-spec (name (:id cmd-spec)))
          (:description cmd-spec "<no description>")
          (category cmd-service (or (:category cmd-spec) default-category))))
      (when-let [hdl (:handler cmd-spec)]
        (define-handler! {:command-id (:id cmd-spec)
                          :fn hdl}))
      c)))

(defn defined-commands
  ([] (defined-commands (workbench)))
  ([service-locator]
    (let [cmd-service (service service-locator :commands)]
      (into [] (.getDefinedCommands cmd-service)))))

(defmethod print-method 
  org.eclipse.core.commands.Command 
  [o, ^java.io.Writer w]
  (print-method
    (with-meta
      {:id (.getId o)
       :name (.getName o)
       :description (.getDescription o)
       :category {:id (.getId (.getCategory o))}}
      {:tag org.eclipse.core.commands.Command})
    w))

;; handler spec
;{:command-id "...."
; :fn #()}

(defn execute-command 
  ([command-id] (execute-command (workbench) (name command-id)))
  ([service-locator command-id]
    (let [hdl-service (service service-locator :handlers)]
      (.executeCommand hdl-service (name command-id) nil))))

(defn active-bindings-for 
  ([command-id] (active-bindings-for (workbench) command-id))
  ([service-locator command-id]
    (let [binding-service (service service-locator :bindings)]
      (into [] (.getActiveBindingsFor binding-service (name command-id))))))

(defn key-sequence [s] 
  (cond
    (instance? org.eclipse.jface.bindings.keys.KeySequence s) 
      s
    :else
      (org.eclipse.jface.bindings.keys.KeySequence/getInstance s)))

(defn key-sequence-format [key-seq] (.format key-seq))

(defn- parameterized-command [command]
  (cond
    (instance? org.eclipse.core.commands.ParameterizedCommand command) 
      command
    (instance? org.eclipse.core.commands.Command command)
      (org.eclipse.core.commands.ParameterizedCommand. command nil)
    :else 
      (org.eclipse.core.commands.ParameterizedCommand. 
        (command-by-id
          (name command))
        nil)))

(import 'org.eclipse.core.commands.contexts.Context)

(defn context 
  ([id] (context (workbench) id))
  ([service-locator id]
    (if (instance? Context id)
      id
      (let [ctx-service (service service-locator :contexts)]
        (.getContext ctx-service (name id))))))

(defn active-context-ids 
  ([] (active-context-ids (workbench)))
  ([service-locator]
    (let [ctx-service (service service-locator :contexts)]
      (into [] (.getActiveContextIds ctx-service)))))

(defn context-ids 
  ([] (context-ids (workbench)))
  ([service-locator]
    (let [ctx-service (service service-locator :contexts)]
      (into [] (.getDefinedContextIds ctx-service)))))
;; key-binding desc

;{:key-sequence (key-sequence "COMMAND+P O")
; :command  ????
; :scheme-id
; :context-id
; :locale
; :platform
; :window-manager
; }
(defn key-binding 
  [{:keys [key-sequence command scheme-id context-id
           locale platform window-manager type] 
    :or {scheme-id "org.eclipse.ui.defaultAcceleratorConfiguration"
         context-id "org.eclipse.ui.contexts.dialogAndWindow"
         locale nil
         platform nil
         window-manager nil
         type org.eclipse.jface.bindings.Binding/USER}
    :as k}]
  (cond
    (instance? org.eclipse.jface.bindings.keys.KeyBinding k)
      k
    :else 
      (org.eclipse.jface.bindings.keys.KeyBinding.
        (ccw.eclipse/key-sequence key-sequence)
        (parameterized-command command)
        (when scheme-id (name scheme-id))
        (name context-id)
        (when locale (name locale))
        (when platform (name platform))
        (when window-manager (name window-manager))
        type)))

(defn bindings 
  ([] (bindings (workbench))) 
  ([service-locator] 
    (let [binding-service (service service-locator :bindings)]
      (into [] (.getBindings binding-service)))))

(defn save-key-binding! 
  ([desc] (save-key-binding! (workbench) desc))
  ([service-locator desc]
    (let [binding-service (service service-locator :bindings)
          kb (key-binding desc)
          kb-id (-?> kb .getParameterizedCommand .getId)
          id-binding-map (-?>> (bindings service-locator) 
                           (group-by #(-?> % .getParameterizedCommand .getId)))
          id-binding-map (update-in id-binding-map [kb-id] (fnil conj []) kb)]
      (.savePreferences binding-service 
        (.getActiveScheme binding-service) 
        (into-array org.eclipse.jface.bindings.Binding 
                    (mapcat identity (vals id-binding-map)))))))
