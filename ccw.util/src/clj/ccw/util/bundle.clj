(ns ccw.util.bundle
  (:refer-clojure :exclude [< > =])
  (:import org.eclipse.core.runtime.CoreException
           org.eclipse.core.runtime.IStatus
           org.eclipse.core.runtime.Platform
           org.eclipse.core.runtime.Status
           org.osgi.framework.Bundle
           org.osgi.framework.Version
           org.osgi.framework.BundleException
           clojure.osgi.ClojureOSGi)
  (:require [clojure.xml :as xml]))

;(defn require-and-get-var)
;	public static Var requireAndGetVar(String bundleSymbolicName, String varName) throws CoreException {
;		final String[] nsFn = varName.split("/");
;		try {
;			ClojureOSGi.require(loadAndGetBundle(bundleSymbolicName).getBundleContext(), nsFn[0]);
;			return RT.var(nsFn[0], nsFn[1]);
;		} catch (Exception e) {
;			IStatus status = new Status(IStatus.ERROR, bundleSymbolicName, 
;					"Problem requiring namespace/getting var " + varName 
;					+ " from bundle " + bundleSymbolicName, e);
;			throw new CoreException(status);
;		}
;	}
;	public static Bundle loadAndGetBundle(String bundleSymbolicName) throws CoreException {
;
;}

 (def ^:private start-states #{(Bundle/STARTING) (Bundle/ACTIVE)})
 
 (defn bundle 
   "Return the bundle object associated with the bundle-symbolic-name (a String)"
   [bundle-symbolic-name]
   (Platform/getBundle bundle-symbolic-name))
 
 (defn version 
   "Create a version object from a map with keys
    :major
    :minor
    :micro
    :qualifier
    "
   [& {:keys [major minor micro qualifier]}]
   (Version. major minor micro qualifier))
 
 (defn compareTo [v1 v2] (.compareTo v1 v2))
 
 (defn <
   "Return truethiness if version1 is < than version2"
   [v1 v2]
   (neg? (compareTo v1 v2)))
 
 (defn =
   "Return truethiness if version1 = version2"
   [v1 v2]
   (zero? (compareTo v1 v2)))
 
 (defn >
   "Return truethiness if version1 is > than version2"
   [v1 v2]
   (pos? (compareTo v1 v2)))
 
 (defn load-and-get-bundle [bundle-symbolic-name]
   ;; TODO: not good??, maybe we will not catch the right bundle (the same the OSGi framework would use ...)
   (try 
     (let [b (bundle bundle-symbolic-name)]
       (when-not (start-states (.getState b))
         (.start b))
       b)
     (catch BundleException e
       (let [status (Status. IStatus/ERROR, bundle-symbolic-name, 
                             "Unable to start bundle", e)]
         (throw (CoreException. status))))))


(defn set-context-classloader! [l]
  (-> (Thread/currentThread) (.setContextClassLoader l)))

(defn bundle-classloader [bundle]
  (clojure.osgi.BundleClassLoader. bundle))

(defn set-bundle-classloader! [bundle-symbolic-name]
  (-> bundle-symbolic-name 
    load-and-get-bundle
    bundle-classloader
    set-context-classloader!))

;(defn with-bundle* [bundle f]
;  (let [new-loader (clojure.osgi.BundleClassLoader. bundle)
;        old-loader (.getContextClassLoader (Thread/currentThread))]
;    (try
;      (set-context-classloader! new-loader)
;      (binding [clojure.osgi.core/*bundle* bundle
;                (clojure.lang.Compiler/LOADER) new-loader]
;        (f))
;      (finally (set-context-classloader! old-loader)))))

;(defn with-bundle* [bundle f]
; (binding [*bundle* bundle load (clojure.osgi.core/osgi-load bundle)]
;    (clojure.osgi.internal.ClojureOSGi/withLoader 
;      (bundle-class-loader bundle) function)
; )   
;)

(defn with-bundle* [bundle f]
  (clojure.osgi.ClojureOSGi/withBundle 
    bundle
    (reify clojure.osgi.RunnableWithException
      (run [this] (f)))))

;; http://www.ibm.com/developerworks/opensource/library/os-ecl-dynext/
(defn add-contribution! [s bundle]
  (let [registry (org.eclipse.core.runtime.RegistryFactory/getRegistry)
        key (.getTemporaryUserToken registry)
        contributor (org.eclipse.core.runtime.ContributorFactoryOSGi/createContributor bundle)
        is (java.io.ByteArrayInputStream. (.getBytes s))]
    (.addContribution registry is contributor false nil nil key)))

(defn xml-map->extension [ext-map]
  (let [m {:tag "plugin" :content [ext-map]}]
    (with-out-str (xml/emit-element m))))

(defn add-command! [bundle command-attrs]
  (let [ext-str (xml-map->extension 
                  {:tag "extension"
                   :attrs {:point "org.eclipse.ui.commands"}
                   :content [{:tag "command"
                              :attrs command-attrs}]})]
    (add-contribution! ext-str bundle)))

(defn add-menu! [bundle menu-contribution]
  (let [ext-str (xml-map->extension 
                  {:tag "extension"
                   :attrs {:point "org.eclipse.ui.menus"}
                   :content [menu-contribution]})]
    (add-contribution! ext-str bundle)))

;(ccw.util.bundle/add-contribution! 
;     "
;   <plugin>
;      <extension
;            point='org.eclipse.ui.menus'>
;         <menuContribution
;               locationURI='toolbar:ccw.view.repl'>
;           <command
;               commandId='ccw.ui.edit.text.clojure.evaluate.toplevel.s.expression'
;               icon='icons/repl/stock_mail-send-receive_16x16.png'
;               style='push'>
;         </command>
;          </menuContribution>
;      </extension>
;   </plugin>
;   "
;     b)

;(ccw.util.bundle/add-contribution! 
;     "
;   <plugin>
;      <extension
;            point='org.eclipse.ui.commands'>
;      <command
;            categoryId='org.eclipse.ui.category.navigate'
;            description='brand new exciting command'
;            id='lolo'
;            name='lolo command'>
;      </command>
;      </extension>
;   </plugin>
;   "
;     b)



;(ccw.util.bundle/add-contribution! 
;     "
;   <plugin>
;      <extension
;            point='org.eclipse.ui.handlers'>
;         <handler
;           commandId='lolo'>
;        <class
;              class='ccw.util.GenericExecutableExtension'>
;           <parameter
;                 name='factory'
;                 value='ccw.util.handler-factory/factory'>
;           </parameter>
;           <parameter
;                 name='handler'
;                 value='ccw.editors.clojure.handlers/toggle-line-comment'>
;           </parameter>
;        </class>
;        <enabledWhen>
;           <with
;                 variable='activeContexts'>
;              <iterate
;                    ifEmpty='false'
;                    operator='or'>
;                 <equals
;                       value='ccw.ui.clojureEditorScope'>
;                 </equals>
;              </iterate>
;           </with>
;        </enabledWhen>
;     </handler>
;      </extension>
;   </plugin>
;   "
;     b)


      
;(ccw.util.bundle/add-contribution! 
;     "
;<plugin>
;<extension
;       point='org.eclipse.ui.views'>
;    <view
;          category='ccw.category.views'
;          icon='icons/clojure16x16.png'
;          id='ccw.view.testview'
;          name='Test View'
;          restorable='true'>
;        <class
;              class='ccw.util.GenericExecutableExtension'>
;           <parameter
;                 name='factory'
;                 value='lolo/test-view'>
;           </parameter>
;        </class>
;          
;    </view>
; </extension>
;</plugin>
;" 
;     b)