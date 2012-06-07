(ns ccw.leiningen.classpath-container
  (:use [clojure.core.incubator :only [-?> -?>>]])
  (:require [leiningen.core.project :as p]
            [leiningen.core.classpath :as cp]
            [cemerick.pomegranate.aether :as aether]
            [clojure.string :as str]
            [ccw.util.eclipse :as e]
            [clojure.java.io :as io]
            [ccw.leiningen.util :as u])
  (:import [org.eclipse.core.runtime CoreException
                                     IPath
                                     Path]
           [org.eclipse.jdt.core ClasspathContainerInitializer
                                 IClasspathContainer
                                 IClasspathEntry
                                 IJavaProject
                                 IClasspathEntry
                                 JavaCore]
           [org.eclipse.jdt.ui.wizards IClasspathContainerPage]
           [org.eclipse.jface.wizard WizardPage]
           [org.eclipse.swt SWT]
           [org.eclipse.swt.widgets Composite]
           [org.eclipse.core.resources IResource
                                       IProject
                                       IMarker
                                       ResourcesPlugin]
           [org.sonatype.aether.resolution DependencyResolutionException]
           [ccw.leiningen Activator
                          Messages
                          ]
           [ccw.util Logger
                     Logger$Severity]
           [java.io File
                    FilenameFilter]))

(println "ccw.leiningen.classpath-container load starts")

(def CONTAINER-PATH (Path. "ccw.LEININGEN_CONTAINER"))

(def CONTAINER-DESCRIPTION "Leiningen dependencies")

(def LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE
  "ccw.leiningen.problemmarkers.classpathcontainer")

(def logger (Logger. (Activator/PLUGIN_ID)))

(defmacro with-exc-logged [& body]
  `(try ~@body
     (catch Exception e#
       (println (.getMessage e#))
       (.printStackTrace e#)
       nil)))

(defn plugin 
  "Return the plugin instance"
  []
  (ccw.leiningen.Activator/getDefault))

(defn make-leiningen-classpath-container 
  "Create an instance of an IClasspathContainer given the arguments.
   classpath-entries: list of IClasspathEntry instances"
  [classpath-entries]
  (reify 
    IClasspathContainer
    (getClasspathEntries [this] 
       ; We explicitly create a new array everytime
       (into-array IClasspathEntry classpath-entries))
    (getDescription [this] CONTAINER-DESCRIPTION)
    (getKind [this] (IClasspathContainer/K_APPLICATION))
    (getPath [this] CONTAINER-PATH)))

(defn- library-entry
  "Wrapper for the ccw.leiningen.util/library-entry function"
  [{:keys [path native-path]}]
  (let [params {:path path}
        params (if native-path
                 (update-in params [:extra-attributes] 
                            assoc u/native-library (str (or (-?> native-path e/resource e/path .makeRelative) 
                                                            (e/path native-path))))
                 params)]
    (u/library-entry params)))

(defn leiningen-classpath-container
  "Given a project, grab its dependencies, and create an Eclipse Classpath Container
   for them. Lets any thrown exception pass through (e.g. Aether exception)"
  [project-dependencies]
  (let [entry-list (map library-entry project-dependencies)]
    (make-leiningen-classpath-container entry-list)))

(defn resolve-dependencies
  "ADAPTED FROM LEININGEN-CORE resolve-dependencies.
  Simply delegate regular dependencies to pomegranate. This will
  ensure they are downloaded into ~/.m2/repositories and that native
  deps have been extracted to :native-path.  If :add-classpath? is
  logically true, will add the resolved dependencies to Leiningen's
  classpath.

   Returns a set of the dependencies' files."
  [project-name dependencies-key {:keys [repositories native-path] :as project} & rest]
  (let [deps-paths 
          ; We use eval-in-project or else we may not benefit from the right SSL
          ; certificates declared for the project
          (u/eval-in-project
            project-name
            `(do
               (require 'cemerick.pomegranate.aether)
               (require 'leiningen.core.classpath)
               (let [~'dependencies (->> (apply #'leiningen.core.classpath/get-dependencies '~dependencies-key '~project '~rest)
                                      (cemerick.pomegranate.aether/dependency-files)
                                      (filter #(re-find #"\.(jar|zip)$" (.getName %))))]
                 (leiningen.core.classpath/extract-native-deps ~'dependencies '~native-path)
                 ; we serialize paths to Strings so that clojure datastructures can be passed back
                 (map #(.getAbsolutePath %) ~'dependencies))))]
    (map #(File. %) deps-paths)))

(defn ser-dep [path native-path]
  {:path (.getAbsolutePath (io/as-file path))
   :native-path (.getAbsolutePath (io/as-file native-path))})

(defn get-project-dependencies
  "Return the dependencies sorted alphabetically via their file name.
   Throws Aether exceptions if a problem occured"
  [project-name lein-project]
  (let [dependencies (resolve-dependencies project-name :dependencies lein-project)]
    (map 
      ser-dep
      (->> dependencies
        (filter #(-> % .getName (.endsWith ".jar")))
        (sort-by #(.getName %)))
      (repeat (u/lein-native-platform-path lein-project)))))

(defn- delete-container-markers [?project]
  (.deleteMarkers (e/resource ?project) 
    LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE,
    true,
    IResource/DEPTH_ONE ; DEPTH_ONE so that we also remove markers from project.clj
    ))

; TODO generalize on the plugin, and then extract into ccw.util.eclipse
(defn- state-file 
  "Return the state file, if it exists, or nil"
  [project state-name]
  (io/file (.toFile (e/plugin-state-location (plugin)))
           (str (.getName (e/project project)) state-name)))


(defn save-project-state
  "Save on disk (in the Plugin state directory), with the specified state-name,
   the serialized clojure datastructure data.
   Writes log and returns nil if save failed, or return the file"
  [project state-name data]
  (with-exc-logged
    (let [f (state-file project state-name)]
      (spit f (pr-str data))
      f)))

(defn save-project-dependencies 
  "Save on disk (in the Plugin state directory), the project deps for the 
   corresponding project.
   project deps is a list of jar files (or coercible to jar files)
   Writes log and returns nil if save failed, or return the file"
  [project project-deps]
  (save-project-state project ".container" project-deps))

(defn load-project-state
  "Retrieve from disk (from the Plugin state directory), the state for state-name, for the project.
   Read the file content, and call read-string on it    
   Writes log and returns nil if loading failed."
  [project state-name]
  (with-exc-logged
    (when-let [state-file (-> project (state-file state-name) u/file-exists?)]
      (let [state (read-string (slurp state-file))]
        (println state)
        (when (or (empty? state) (map? (first state))) (do (println "yo!") state))))))

(defn deser-dep [dep-map]
  (-> dep-map 
    (update-in [:path] #(File. %))
    (update-in [:native-path] #(File. %))))

(defn load-project-dependencies
  "Retrieve from disk (from the Plugin state directory), the deps for the project.
   Return a list of Files
   Writes log and returns nil if loading failed."
  [project]
  (map deser-dep (load-project-state project ".container")))

(defn- add-container-marker 
  "Delete previous container markers, add new one"
  [?project message]
  (e/run-in-workspace
    (e/workspace-runnable 
      (fn [_] 
        (delete-container-markers ?project)
        (let [marker (.createMarker (e/resource ?project) LEININGEN_CLASSPATH_CONTAINER_PROBLEM_MARKER_TYPE)]
          (.setAttributes marker
            (let [m (java.util.HashMap.)]
              (doto m
                (.put IMarker/MESSAGE message)
                (.put IMarker/LINE_NUMBER (Integer/valueOf "1"))
                (.put IMarker/PRIORITY (Integer. IMarker/PRIORITY_HIGH))
                (.put IMarker/SEVERITY (Integer. IMarker/SEVERITY_ERROR))))))))
    nil
    true
    nil))

(defn- report-container-error [?project message e]
  (add-container-marker (e/resource ?project) message)
  (println message)
  (.printStackTrace e))

(defn- unresolved-artifacts [artifact-results]
  (remove #(.isResolved %) artifact-results))

(defn- requested-artifact [artifact-result]
  (-> artifact-result .getRequest .getArtifact))

(defn- artifact-string 
  ([artifact] 
    (artifact-string (.getGroupId artifact) 
                     (.getArtifactId artifact)
                     (.getBaseVersion artifact)))
  ([group-id artifact-id base-version]
    (str 
      "["
      (when group-id (str group-id "/"))
      artifact-id
      " "
      "\"" base-version "\""
      "]")))

(defn flat-dependencies [dependency-node]
  (concat 
    (when-let [d (.getDependency dependency-node)] [d])
    (mapcat flat-dependencies (.getChildren dependency-node))))

(defn- unresolved-dependencies-string [exc]
  (let [unresolved (-> exc .getResult .getArtifactResults unresolved-artifacts)
        artifacts (map (comp artifact-string requested-artifact) unresolved)]
    (when-not artifacts (println "missing artifacts, printing the stacktrace instead:" (.printStackTrace exc)))      
    (str/join ", " artifacts)))

;; TODO fixme: if the network is down, and we write a non existent dependency, the message does not manage to list the dependencies
;; we were not able to fetch. the following Exception chaining must be deciphered:
;java.lang.RuntimeException: org.sonatype.aether.resolution.DependencyResolutionException: Failed to collect dependencies for clojure.lang.LazySeq@47d9ca99
;	at clojure.lang.Util.runtimeException(Util.java:165)
;	at clojure.lang.Reflector.invokeInstanceMethod(Reflector.java:35)
;	at cemerick.pomegranate.aether$resolve_dependencies.doInvoke(aether.clj:269)
;	at clojure.lang.RestFn.invoke(RestFn.java:457)
;	at leiningen.core.classpath$resolve_dependencies.invoke(classpath.clj:51)
;	at ccw.leiningen.classpath_container$get_project_dependencies.invoke(classpath_container.clj:91)
;	at ccw.leiningen.classpath_container$update_project_dependencies.invoke(NO_SOURCE_FILE:9)
;	at ccw.leiningen.handlers$update_dependencies.invoke(handlers.clj:51)
;	at clojure.lang.Var.invoke(Var.java:405)
;	at ccw.util.factories$handler_factory$fn__2017.invoke(factories.clj:14)
;	at ccw.util.factories.proxy$org.eclipse.core.commands.AbstractHandler$0.execute(Unknown Source)
;	at org.eclipse.ui.internal.handlers.HandlerProxy.execute(HandlerProxy.java:293)
;	at org.eclipse.core.commands.Command.executeWithChecks(Command.java:476)
;	at org.eclipse.core.commands.ParameterizedCommand.executeWithChecks(ParameterizedCommand.java:508)
;	at org.eclipse.ui.internal.handlers.HandlerService.executeCommand(HandlerService.java:169)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.executeCommand(WorkbenchKeyboard.java:468)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.press(WorkbenchKeyboard.java:786)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.processKeyEvent(WorkbenchKeyboard.java:885)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.filterKeySequenceBindings(WorkbenchKeyboard.java:567)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.access$3(WorkbenchKeyboard.java:508)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard$KeyDownFilter.handleEvent(WorkbenchKeyboard.java:123)
;	at org.eclipse.swt.widgets.EventTable.sendEvent(EventTable.java:84)
;	at org.eclipse.swt.widgets.Display.filterEvent(Display.java:1069)
;	at org.eclipse.swt.widgets.Display.sendEvent(Display.java:4127)
;	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1457)
;	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1480)
;	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1465)
;	at org.eclipse.swt.widgets.Widget.sendKeyEvent(Widget.java:1494)
;	at org.eclipse.swt.widgets.Control.insertText(Control.java:2056)
;	at org.eclipse.swt.widgets.Canvas.insertText(Canvas.java:256)
;	at org.eclipse.swt.widgets.Display.windowProc(Display.java:5561)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSend(Native Method)
;	at org.eclipse.swt.internal.cocoa.NSResponder.interpretKeyEvents(NSResponder.java:68)
;	at org.eclipse.swt.widgets.Composite.keyDown(Composite.java:587)
;	at org.eclipse.swt.widgets.Display.windowProc(Display.java:5473)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSendSuper(Native Method)
;	at org.eclipse.swt.widgets.Widget.callSuper(Widget.java:220)
;	at org.eclipse.swt.widgets.Widget.windowSendEvent(Widget.java:2092)
;	at org.eclipse.swt.widgets.Shell.windowSendEvent(Shell.java:2252)
;	at org.eclipse.swt.widgets.Display.windowProc(Display.java:5535)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSendSuper(Native Method)
;	at org.eclipse.swt.widgets.Display.applicationSendEvent(Display.java:4989)
;	at org.eclipse.swt.widgets.Display.applicationProc(Display.java:5138)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSend(Native Method)
;	at org.eclipse.swt.internal.cocoa.NSApplication.sendEvent(NSApplication.java:128)
;	at org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3610)
;	at org.eclipse.ui.internal.Workbench.runEventLoop(Workbench.java:2696)
;	at org.eclipse.ui.internal.Workbench.runUI(Workbench.java:2660)
;	at org.eclipse.ui.internal.Workbench.access$4(Workbench.java:2494)
;	at org.eclipse.ui.internal.Workbench$7.run(Workbench.java:674)
;	at org.eclipse.core.databinding.observable.Realm.runWithDefault(Realm.java:332)
;	at org.eclipse.ui.internal.Workbench.createAndRunWorkbench(Workbench.java:667)
;	at org.eclipse.ui.PlatformUI.createAndRunWorkbench(PlatformUI.java:149)
;	at org.eclipse.ui.internal.ide.application.IDEApplication.start(IDEApplication.java:123)
;	at org.eclipse.equinox.internal.app.EclipseAppHandle.run(EclipseAppHandle.java:196)
;	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.runApplication(EclipseAppLauncher.java:110)
;	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.start(EclipseAppLauncher.java:79)
;	at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:344)
;	at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:179)
;	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
;	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
;	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
;	at java.lang.reflect.Method.invoke(Method.java:597)
;	at org.eclipse.equinox.launcher.Main.invokeFramework(Main.java:622)
;	at org.eclipse.equinox.launcher.Main.basicRun(Main.java:577)
;	at org.eclipse.equinox.launcher.Main.run(Main.java:1410)
;	at org.eclipse.equinox.launcher.Main.main(Main.java:1386)
;Caused by: org.sonatype.aether.resolution.DependencyResolutionException: Failed to collect dependencies for clojure.lang.LazySeq@47d9ca99
;	at org.sonatype.aether.impl.internal.DefaultRepositorySystem.resolveDependencies(DefaultRepositorySystem.java:371)
;	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
;	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
;	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
;	at java.lang.reflect.Method.invoke(Method.java:597)
;	at clojure.lang.Reflector.invokeMatchingMethod(Reflector.java:92)
;	at clojure.lang.Reflector.invokeInstanceMethod(Reflector.java:30)
;	... 65 more
;Caused by: org.sonatype.aether.collection.DependencyCollectionException: Failed to collect dependencies for clojure.lang.LazySeq@47d9ca99
;	at org.sonatype.aether.impl.internal.DefaultDependencyCollector.collectDependencies(DefaultDependencyCollector.java:258)
;	at org.sonatype.aether.impl.internal.DefaultRepositorySystem.resolveDependencies(DefaultRepositorySystem.java:333)
;	... 71 more
;Caused by: org.sonatype.aether.resolution.ArtifactDescriptorException: Failed to read artifact descriptor for foobar:foobar:jar:1.24.1
;	at org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.loadPom(DefaultArtifactDescriptorReader.java:282)
;	at org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.readArtifactDescriptor(DefaultArtifactDescriptorReader.java:172)
;	at org.sonatype.aether.impl.internal.DefaultDependencyCollector.process(DefaultDependencyCollector.java:412)
;	at org.sonatype.aether.impl.internal.DefaultDependencyCollector.collectDependencies(DefaultDependencyCollector.java:240)
;	... 72 more
;Caused by: org.sonatype.aether.resolution.ArtifactResolutionException: Could not transfer artifact foobar:foobar:pom:1.24.1 from/to clojars (http://clojars.org/repo): Error transferring file: clojars.org
;	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:538)
;	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolveArtifacts(DefaultArtifactResolver.java:216)
;	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolveArtifact(DefaultArtifactResolver.java:193)
;	at org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.loadPom(DefaultArtifactDescriptorReader.java:267)
;	... 75 more
;Caused by: org.sonatype.aether.transfer.ArtifactTransferException: Could not transfer artifact foobar:foobar:pom:1.24.1 from/to clojars (http://clojars.org/repo): Error transferring file: clojars.org
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$4.wrap(WagonRepositoryConnector.java:950)
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$4.wrap(WagonRepositoryConnector.java:940)
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$GetTask.run(WagonRepositoryConnector.java:669)
;	at org.sonatype.aether.util.concurrency.RunnableErrorForwarder$1.run(RunnableErrorForwarder.java:60)
;	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
;	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
;	at java.lang.Thread.run(Thread.java:680)
;Caused by: org.apache.maven.wagon.TransferFailedException: Error transferring file: clojars.org
;	at org.apache.maven.wagon.providers.http.LightweightHttpWagon.fillInputData(LightweightHttpWagon.java:143)
;	at org.apache.maven.wagon.StreamWagon.getInputStream(StreamWagon.java:116)
;	at org.apache.maven.wagon.StreamWagon.getIfNewer(StreamWagon.java:88)
;	at org.apache.maven.wagon.StreamWagon.get(StreamWagon.java:61)
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$GetTask.run(WagonRepositoryConnector.java:601)
;	... 4 more
;Caused by: java.net.UnknownHostException: clojars.org
;	at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:195)
;	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:432)
;	at java.net.Socket.connect(Socket.java:529)
;	at java.net.Socket.connect(Socket.java:478)
;	at sun.net.NetworkClient.doConnect(NetworkClient.java:163)
;	at sun.net.www.http.HttpClient.openServer(HttpClient.java:395)
;	at sun.net.www.http.HttpClient.openServer(HttpClient.java:530)
;	at sun.net.www.http.HttpClient.<init>(HttpClient.java:234)
;	at sun.net.www.http.HttpClient.New(HttpClient.java:307)
;	at sun.net.www.http.HttpClient.New(HttpClient.java:324)
;	at sun.net.www.protocol.http.HttpURLConnection.getNewHttpClient(HttpURLConnection.java:970)
;	at sun.net.www.protocol.http.HttpURLConnection.plainConnect(HttpURLConnection.java:911)
;	at sun.net.www.protocol.http.HttpURLConnection.connect(HttpURLConnection.java:836)
;	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1172)
;	at java.net.HttpURLConnection.getResponseCode(HttpURLConnection.java:379)
;	at org.apache.maven.wagon.providers.http.LightweightHttpWagon.fillInputData(LightweightHttpWagon.java:115)
;	... 8 more
;Leiningen Managed Dependencies issue: problem resolving following dependencies: 
;java.lang.RuntimeException: org.sonatype.aether.resolution.DependencyResolutionException: Failed to collect dependencies for clojure.lang.LazySeq@47d9ca99
;	at clojure.lang.Util.runtimeException(Util.java:165)
;	at clojure.lang.Reflector.invokeInstanceMethod(Reflector.java:35)
;	at cemerick.pomegranate.aether$resolve_dependencies.doInvoke(aether.clj:269)
;	at clojure.lang.RestFn.invoke(RestFn.java:457)
;	at leiningen.core.classpath$resolve_dependencies.invoke(classpath.clj:51)
;	at ccw.leiningen.classpath_container$get_project_dependencies.invoke(classpath_container.clj:91)
;	at ccw.leiningen.classpath_container$update_project_dependencies.invoke(NO_SOURCE_FILE:9)
;	at ccw.leiningen.handlers$update_dependencies.invoke(handlers.clj:51)
;	at clojure.lang.Var.invoke(Var.java:405)
;	at ccw.util.factories$handler_factory$fn__2017.invoke(factories.clj:14)
;	at ccw.util.factories.proxy$org.eclipse.core.commands.AbstractHandler$0.execute(Unknown Source)
;	at org.eclipse.ui.internal.handlers.HandlerProxy.execute(HandlerProxy.java:293)
;	at org.eclipse.core.commands.Command.executeWithChecks(Command.java:476)
;	at org.eclipse.core.commands.ParameterizedCommand.executeWithChecks(ParameterizedCommand.java:508)
;	at org.eclipse.ui.internal.handlers.HandlerService.executeCommand(HandlerService.java:169)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.executeCommand(WorkbenchKeyboard.java:468)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.press(WorkbenchKeyboard.java:786)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.processKeyEvent(WorkbenchKeyboard.java:885)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.filterKeySequenceBindings(WorkbenchKeyboard.java:567)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard.access$3(WorkbenchKeyboard.java:508)
;	at org.eclipse.ui.internal.keys.WorkbenchKeyboard$KeyDownFilter.handleEvent(WorkbenchKeyboard.java:123)
;	at org.eclipse.swt.widgets.EventTable.sendEvent(EventTable.java:84)
;	at org.eclipse.swt.widgets.Display.filterEvent(Display.java:1069)
;	at org.eclipse.swt.widgets.Display.sendEvent(Display.java:4127)
;	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1457)
;	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1480)
;	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1465)
;	at org.eclipse.swt.widgets.Widget.sendKeyEvent(Widget.java:1494)
;	at org.eclipse.swt.widgets.Control.insertText(Control.java:2056)
;	at org.eclipse.swt.widgets.Canvas.insertText(Canvas.java:256)
;	at org.eclipse.swt.widgets.Display.windowProc(Display.java:5561)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSend(Native Method)
;	at org.eclipse.swt.internal.cocoa.NSResponder.interpretKeyEvents(NSResponder.java:68)
;	at org.eclipse.swt.widgets.Composite.keyDown(Composite.java:587)
;	at org.eclipse.swt.widgets.Display.windowProc(Display.java:5473)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSendSuper(Native Method)
;	at org.eclipse.swt.widgets.Widget.callSuper(Widget.java:220)
;	at org.eclipse.swt.widgets.Widget.windowSendEvent(Widget.java:2092)
;	at org.eclipse.swt.widgets.Shell.windowSendEvent(Shell.java:2252)
;	at org.eclipse.swt.widgets.Display.windowProc(Display.java:5535)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSendSuper(Native Method)
;	at org.eclipse.swt.widgets.Display.applicationSendEvent(Display.java:4989)
;	at org.eclipse.swt.widgets.Display.applicationProc(Display.java:5138)
;	at org.eclipse.swt.internal.cocoa.OS.objc_msgSend(Native Method)
;	at org.eclipse.swt.internal.cocoa.NSApplication.sendEvent(NSApplication.java:128)
;	at org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3610)
;	at org.eclipse.ui.internal.Workbench.runEventLoop(Workbench.java:2696)
;	at org.eclipse.ui.internal.Workbench.runUI(Workbench.java:2660)
;	at org.eclipse.ui.internal.Workbench.access$4(Workbench.java:2494)
;	at org.eclipse.ui.internal.Workbench$7.run(Workbench.java:674)
;	at org.eclipse.core.databinding.observable.Realm.runWithDefault(Realm.java:332)
;	at org.eclipse.ui.internal.Workbench.createAndRunWorkbench(Workbench.java:667)
;	at org.eclipse.ui.PlatformUI.createAndRunWorkbench(PlatformUI.java:149)
;	at org.eclipse.ui.internal.ide.application.IDEApplication.start(IDEApplication.java:123)
;	at org.eclipse.equinox.internal.app.EclipseAppHandle.run(EclipseAppHandle.java:196)
;	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.runApplication(EclipseAppLauncher.java:110)
;	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.start(EclipseAppLauncher.java:79)
;	at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:344)
;	at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:179)
;	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
;	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
;	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
;	at java.lang.reflect.Method.invoke(Method.java:597)
;	at org.eclipse.equinox.launcher.Main.invokeFramework(Main.java:622)
;	at org.eclipse.equinox.launcher.Main.basicRun(Main.java:577)
;	at org.eclipse.equinox.launcher.Main.run(Main.java:1410)
;	at org.eclipse.equinox.launcher.Main.main(Main.java:1386)
;Caused by: org.sonatype.aether.resolution.DependencyResolutionException: Failed to collect dependencies for clojure.lang.LazySeq@47d9ca99
;	at org.sonatype.aether.impl.internal.DefaultRepositorySystem.resolveDependencies(DefaultRepositorySystem.java:371)
;	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
;	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
;	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
;	at java.lang.reflect.Method.invoke(Method.java:597)
;	at clojure.lang.Reflector.invokeMatchingMethod(Reflector.java:92)
;	at clojure.lang.Reflector.invokeInstanceMethod(Reflector.java:30)
;	... 65 more
;Caused by: org.sonatype.aether.collection.DependencyCollectionException: Failed to collect dependencies for clojure.lang.LazySeq@47d9ca99
;	at org.sonatype.aether.impl.internal.DefaultDependencyCollector.collectDependencies(DefaultDependencyCollector.java:258)
;	at org.sonatype.aether.impl.internal.DefaultRepositorySystem.resolveDependencies(DefaultRepositorySystem.java:333)
;	... 71 more
;Caused by: org.sonatype.aether.resolution.ArtifactDescriptorException: Failed to read artifact descriptor for foobar:foobar:jar:1.24.1
;	at org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.loadPom(DefaultArtifactDescriptorReader.java:282)
;	at org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.readArtifactDescriptor(DefaultArtifactDescriptorReader.java:172)
;	at org.sonatype.aether.impl.internal.DefaultDependencyCollector.process(DefaultDependencyCollector.java:412)
;	at org.sonatype.aether.impl.internal.DefaultDependencyCollector.collectDependencies(DefaultDependencyCollector.java:240)
;	... 72 more
;Caused by: org.sonatype.aether.resolution.ArtifactResolutionException: Could not transfer artifact foobar:foobar:pom:1.24.1 from/to clojars (http://clojars.org/repo): Error transferring file: clojars.org
;	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolve(DefaultArtifactResolver.java:538)
;	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolveArtifacts(DefaultArtifactResolver.java:216)
;	at org.sonatype.aether.impl.internal.DefaultArtifactResolver.resolveArtifact(DefaultArtifactResolver.java:193)
;	at org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.loadPom(DefaultArtifactDescriptorReader.java:267)
;	... 75 more
;Caused by: org.sonatype.aether.transfer.ArtifactTransferException: Could not transfer artifact foobar:foobar:pom:1.24.1 from/to clojars (http://clojars.org/repo): Error transferring file: clojars.org
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$4.wrap(WagonRepositoryConnector.java:950)
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$4.wrap(WagonRepositoryConnector.java:940)
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$GetTask.run(WagonRepositoryConnector.java:669)
;	at org.sonatype.aether.util.concurrency.RunnableErrorForwarder$1.run(RunnableErrorForwarder.java:60)
;	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
;	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
;	at java.lang.Thread.run(Thread.java:680)
;Caused by: org.apache.maven.wagon.TransferFailedException: Error transferring file: clojars.org
;	at org.apache.maven.wagon.providers.http.LightweightHttpWagon.fillInputData(LightweightHttpWagon.java:143)
;	at org.apache.maven.wagon.StreamWagon.getInputStream(StreamWagon.java:116)
;	at org.apache.maven.wagon.StreamWagon.getIfNewer(StreamWagon.java:88)
;	at org.apache.maven.wagon.StreamWagon.get(StreamWagon.java:61)
;	at org.sonatype.aether.connector.wagon.WagonRepositoryConnector$GetTask.run(WagonRepositoryConnector.java:601)
;	... 4 more
;Caused by: java.net.UnknownHostException: clojars.org
;	at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:195)
;	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:432)
;	at java.net.Socket.connect(Socket.java:529)
;	at java.net.Socket.connect(Socket.java:478)
;	at sun.net.NetworkClient.doConnect(NetworkClient.java:163)
;	at sun.net.www.http.HttpClient.openServer(HttpClient.java:395)
;	at sun.net.www.http.HttpClient.openServer(HttpClient.java:530)
;	at sun.net.www.http.HttpClient.<init>(HttpClient.java:234)
;	at sun.net.www.http.HttpClient.New(HttpClient.java:307)
;	at sun.net.www.http.HttpClient.New(HttpClient.java:324)
;	at sun.net.www.protocol.http.HttpURLConnection.getNewHttpClient(HttpURLConnection.java:970)
;	at sun.net.www.protocol.http.HttpURLConnection.plainConnect(HttpURLConnection.java:911)
;	at sun.net.www.protocol.http.HttpURLConnection.connect(HttpURLConnection.java:836)
;	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1172)
;	at java.net.HttpURLConnection.getResponseCode(HttpURLConnection.java:379)
;	at org.apache.maven.wagon.providers.http.LightweightHttpWagon.fillInputData(LightweightHttpWagon.java:115)
;	... 8 more




(defn- dependency-resolution-message [exc java-project]
  [(-> java-project e/resource (.getFile "project.clj"))
   (str "problem resolving following dependencies: "
        (unresolved-dependencies-string exc))])

(defn- resource-message
  "Return [resource message] where resource is the Eclipse IResource to be used
   as the target of the problem marker, and message is personalized given exc
   and java-project values"
  [exc java-project]
  (cond
    (nil? exc) [(e/resource java-project) "unknown problem (missing exception)"]
    (instance? DependencyResolutionException exc)
      (dependency-resolution-message exc java-project)
    (instance? DependencyResolutionException (.getCause exc))
      (dependency-resolution-message (.getCause exc) java-project)
    (nil? (.getMessage exc)) [(e/resource java-project) "unknown problem (missing exception message)"]
    (.contains (.getMessage exc) "project.clj")
      (if (.contains (.getMessage exc) "FileNotFoundException")
        [(e/resource java-project) "project.clj file is missing"]
        [(-> java-project e/resource (.getFile "project.clj")) "problem with project.clj file"])
    (.contains (.getMessage exc) "DependencyResolutionException")
      [(e/resource java-project) (str "problem when grabbing dependencies from repositories:" (.getMessage exc))]
    :else
      [(e/resource java-project) (str "unknown problem: " (.getMessage exc))]))

(defn set-classpath-container
  "Sets classpath-container for java-project under the key container-path"
  [java-project container-path classpath-container]
  (JavaCore/setClasspathContainer
    container-path
    (into-array IJavaProject [java-project])
    (into-array IClasspathContainer [classpath-container])
    nil))

(defn set-lein-container
  "Sets container as the leiningen container for the java-project."
  [java-project deps]
  (let [container (leiningen-classpath-container deps)]
    (set-classpath-container java-project CONTAINER-PATH container)))

(defn update-project-dependencies
  "Get the dependencies.
   If deps fetched ok: sets lein container, save the dependencies list on disk.
   If an exception is throw while fetching deps: report problem markers, 
   do not touch the current lein container."
  [java-project] ;; TODO checks
  (try
    (let [lein-project (u/lein-project java-project)
          deps (get-project-dependencies (.getName (e/project java-project)) lein-project)]
      (set-lein-container java-project deps)
      (delete-container-markers java-project)
      (save-project-dependencies java-project deps))
    (catch Exception e
      ;; TODO enhance this in the future ... (more accurate problem markers)
      (let [[jresource message] (resource-message e java-project)
            project-name (-> java-project e/resource .getName)]
        (report-container-error 
          jresource
          ;(str "Project '" project-name "', Leiningen classpath container problem: " message)
          (str "Leiningen Managed Dependencies issue: " message)
          e)))))

(defn has-container? [java-project container-path]
  (let [entries (.getRawClasspath java-project)]
    (some #(and
             (= IClasspathEntry/CPE_CONTAINER  (.getEntryKind %))
             (= container-path (.getPath %)))
          entries)))

(defn has-lein-container? [java-project] 
  (has-container? java-project CONTAINER-PATH))

(defn get-lein-container [java-project]
  (JavaCore/getClasspathContainer CONTAINER-PATH java-project))

(defn initializer-factory 
  "Creates a ClasspathContainerInitializer instance for Leiningen projects"
  [_]
  (proxy [ClasspathContainerInitializer]
         []
    (initialize [container-path, java-project]
      (if-let [deps (load-project-dependencies java-project)]
        (set-lein-container java-project deps)
        (update-project-dependencies java-project)))
    
    (canUpdateClasspathContainer [container-path, java-project]
      false)
    
    (getDescription [container-path, java-project]
      CONTAINER-DESCRIPTION)))

(defn page-factory 
  "Creates a IClasspathContainerPage instance for Leiningen Managed Dependencies"
  [_]
  (doto 
    (proxy [WizardPage, IClasspathContainerPage]
           [(Messages/PageName), (Messages/PageTitle), nil]
      (createControl [parent]
        (let [composite (Composite. parent SWT/NULL)]
          (proxy-super setControl composite)))
      
      (finish [] true)
      
      (getSelection []
        (JavaCore/newContainerEntry CONTAINER-PATH))
      
      (setSelection [_]))
    (.setDescription (Messages/PageDesc))
    (.setPageComplete true)))

(println "classpath-container namespace loaded")