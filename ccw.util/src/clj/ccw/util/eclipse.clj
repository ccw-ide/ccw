(ns ^{:doc "Eclipse interop utilities"}
     ccw.util.eclipse
  (:require [clojure.java.io :as io])
  (:import [org.eclipse.core.resources IResource
                                       IProject
                                       ResourcesPlugin
                                       IWorkspaceRunnable
                                       IWorkspace
                                       IWorkspaceRoot]
           [org.eclipse.core.runtime IPath 
                                     Path
                                     Platform
                                     Plugin]
           [org.eclipse.jdt.core IJavaProject]
           [org.eclipse.ui.handlers HandlerUtil]
           [org.eclipse.ui IEditorPart
                           PlatformUI
                           IWorkbench]
           [org.eclipse.jface.viewers IStructuredSelection]
           [org.eclipse.jface.operation IRunnableWithProgress]
           [org.eclipse.core.commands ExecutionEvent]
           [org.eclipse.ui.actions WorkspaceModifyDelegatingOperation]
           [java.io File]
           [ccw.util PlatformUtil DisplayUtil]))

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
  "Return the Eclipse Workspace" ^IWorkspace []
  (ResourcesPlugin/getWorkspace))

(defn workbench 
  "Return the Eclipse Workbench" ^IWorkbench []
  (PlatformUI/getWorkbench))

(defn workspace-root 
  "Return the Eclipse Workspace root"
  ^IWorkspaceRoot []
  (.getRoot (workspace)))

(defprotocol IProjectCoercion
  (project ^IProject [this] "Coerce this into an IProject"))

(defprotocol IResourceCoercion
  (resource ^IResource [this] "Coerce this in a IResource"))

(defprotocol IPathCoercion
  (path ^IPath [this] "Coerce this to an IPath"))

(extend-protocol IProjectCoercion
  nil
  (project [_] nil)
  
  Object
  (project [s] nil)
  
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
  (project [this] (project (resource this))))

(extend-protocol IResourceCoercion
  nil
  (resource [this] nil)
  
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
  
  IPath
  (path [this] this)
  
  IResource
  (path [r] (.getFullPath r))
  
  String
  (path [s] (Path. s))
  
  File
  (path [f] (Path. (.getAbsolutePath f))))

(defn plugin-state-location 
  "Return the plugin's state location as a path representing 
   an absolute filesystem path."
  ^IPath [^Plugin plugin]
  (.getStateLocation plugin))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Long-running tasks, background tasks, Workspace Resources related tasks

; From http://wiki.eclipse.org/FAQ_What_are_IWorkspaceRunnable%2C_IRunnableWithProgress%2C_and_WorkspaceModifyOperation%3F
; FAQ What are IWorkspaceRunnable, IRunnableWithProgress, and WorkspaceModifyOperation?
; IWorkspaceRunnable is a mechanism for batching a set of changes to the workspace 
; so that change notification and autobuild are deferred until the entire batch completes. 
; IRunnableWithProgress is a mechanism for batching a set of changes to be run outside the UI thread.
; You often need to do both of these at once: Make multiple changes to the workspace outside the UI thread. 
; Wrapping one of these mechanisms inside the other would do the trick, but the resulting code is cumbersome,
; and it is awkward to communicate arguments, results, and exceptions between the caller and the operation to be run.
;
; The solution is to use WorkspaceModifyOperation. This class rolls the two mechanisms together 
; by implementing IRunnableWithProgress and performing the work within a nested IWorkspaceRunnable.
; To use it, simply create a subclass that implements the abstract method execute, 
; and pass an instance of this subclass to IRunnableContext.run to perform the work. 
; If you already have an instance of IRunnableWithProgress on hand, it can be passed 
; to the constructor of the special subclass WorkspaceModifyDelegatingOperation 
; to create a new IRunnableWithProgress that performs workspace batching for you. 

; See also http://wiki.eclipse.org/FAQ_How_do_I_prevent_builds_between_multiple_changes_to_the_workspace%3F

; From http://wiki.eclipse.org/FAQ_Actions%2C_commands%2C_operations%2C_jobs:_What_does_it_all_mean%3F 
; Operations
; Operations aren’t an official part of the workbench API, but the term tends to 
; be used for a long-running unit of behavior. Any work that might take a second
; or more should really be inside an operation. The official designation for operations
; in the API is IRunnableWithProgress, but the term operation tends to be used in its
; place because it is easier to say and remember. Operations are executed within an IRunnableContext.
; The context manages the execution of the operation in a non-UI thread so that the UI stays alive and painting.
; The context provides progress feedback to the user and support for cancellation.
; 
; Jobs
; Jobs, introduced in Eclipse 3.0, are operations that run in the background. 
; The user is typically prevented from doing anything while an operation is running 
; but is free to continue working when a job is running. Operations and jobs belong 
; together, but jobs needed to live at a lower level in the plug-in architecture 
; to make them usable by non-UI components. 

; operation : long-running action which will be executed outside the UI thread to not make it hang
; job : operation that run in the background. Jobs are usable by non-UI components
; Job: see also http://wiki.eclipse.org/FAQ_Does_the_platform_have_support_for_concurrency%3F
; jobs can report progress via instances of the UI-agnostic IProgressMonitor interface

; Article on jobs treating each and every aspect in depth: http://www.eclipse.org/articles/Article-Concurrency/jobs-api.html

; CCW modelisation:
;
; Operation
; =========
; An operation is a function reporting progress via an IProgressMonitor
; (fn [progress-monitor] ....)
; The concept of operation is independent from the thread it may be running on
; (UI thread, background thread), is independent from the fact that the user actions
; may be blocked (modal, busy indicator then modal) or not (not modal, many flavors from unware (system), aware (closeable popup), informed (status line progress bar))
; and is independent from the fact that concurrency goodies (IRules, resource rules)
; have been declared or not to the Eclipse concurrency framework

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
      (run [this progress-monitor] (f progress-monitor)))))

(defn runnable-with-progress-in-workspace
  "Takes an operation, a Scheduling rule, and returns an instance of WorkspaceModifyDelegationOperation"
  [operation rule]
  (WorkspaceModifyDelegatingOperation. 
    (runnable-with-progress operation)
    rule))

(defn run-in-background
  "Uses Eclipse's IProgressService API to run tasks in the background and have
   them play nicely with other background taks, jobs, etc.
   If is the function to be executed, taking a progress-monitor as argument.
   Must switch to the UI Thread if not already within it before calling the
   operation."
  [operation]
  (DisplayUtil/asyncExec
    #(let [ps (.getProgressService (workbench))]
       (.busyCursorWhile ps 
         (runnable-with-progress operation)))))

#_(defn run-with-progress-in-workspace
  "Will run with IProgressService API. Currently busyCursorWhile is hardcoded"
  [operation rule]
  
  )

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SWT utilities

(defn ui*
  "Calls f with (optionnaly) args on the UI Thread, using
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

(defmacro ui [& args]
  `(ui* (fn [] ~@args)))
