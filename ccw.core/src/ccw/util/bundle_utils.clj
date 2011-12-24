(ns ccw.util.bundle-utils
  (:import org.eclipse.core.runtime.CoreException
           org.eclipse.core.runtime.IStatus
           org.eclipse.core.runtime.Platform
           org.eclipse.core.runtime.Status
           org.osgi.framework.Bundle
           org.osgi.framework.BundleException
           clojure.osgi.BundleClassLoader
           ))

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
 
 (defn load-and-get-bundle [bundle-symbolic-name]
   ;; TODO: not good??, maybe we will not catch the right bundle (the same the OSGi framework would use ...)
   (try 
     (let [b (Platform/getBundle bundle-symbolic-name)]
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
;

;; http://www.ibm.com/developerworks/opensource/library/os-ecl-dynext/
(defn add-contribution [s bundle]
  (let [registry (org.eclipse.core.runtime.RegistryFactory/getRegistry)
        key (.getTemporaryUserToken registry)
        contributor (org.eclipse.core.runtime.ContributorFactoryOSGi/createContributor bundle)
        is (java.io.ByteArrayInputStream. (.getBytes s))]
    (.addContribution registry is contributor false nil nil key)))

;(ccw.util.bundle-utils/add-contribution 
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

;(ccw.util.bundle-utils/add-contribution 
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

;(ccw.util.bundle-utils/add-contribution 
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


      
;(ccw.util.bundle-utils/add-contribution 
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