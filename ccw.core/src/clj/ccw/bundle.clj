(ns ccw.bundle
  (:refer-clojure :exclude [< > =])
  (:import org.eclipse.core.runtime.CoreException
           org.eclipse.core.runtime.IStatus
           org.eclipse.core.runtime.Platform
           org.eclipse.core.runtime.Status
           org.eclipse.core.internal.registry.ExtensionRegistry
           org.osgi.framework.Bundle
           org.osgi.framework.Version
           org.osgi.framework.BundleException)
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
 (defn available? [bundle]
  (and bundle (start-states (.getState bundle))))

 (defn bundle
   "Return the bundle object associated with the bundle-symbolic-name (a String)"
   [bundle-symbolic-name]
   (Platform/getBundle bundle-symbolic-name))
 
 (defn bundle-version
   "Return the Version object for the bundle"
   [bundle]
   (.getVersion bundle))
 
 (defn string->version "Create a version from a String"
   [s] (Version. s))
 
 (defn version 
   "Create a version object from a map with keys
    :major
    :minor
    :micro
    :qualifier
    "
   [& {:keys [major minor micro qualifier]}]
   (Version. major minor micro qualifier))
 
 (defn compareTo [^Comparable v1 v2] (.compareTo v1 v2))
 
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

(defn expect-bundle
  "Expect that bundle represented by `bundle-symbolic-name` exists and its version is at minimum equal to `bundle-version-string`"
  [bundle-symbolic-name bundle-version-string]
  (let [expected-version (string->version bundle-version-string)
        actual-version   (.getVersion (bundle bundle-symbolic-name))]
    (when (< actual-version expected-version)
      (throw (ex-info (format "user plugin %s can only work with a Counterclockwise version at least equal to %s (%s found)."
                        *ns* expected-version actual-version) {})))))


 (defn load-and-get-bundle [bundle-symbolic-name]
   ;; TODO: not good??, maybe we will not catch the right bundle (the same the OSGi framework would use ...)
   (try 
     (let [b (bundle bundle-symbolic-name)]
       (when-not (available? b)
         (.start b))
       b)
     (catch BundleException e
       (let [status (Status. IStatus/ERROR, bundle-symbolic-name, 
                             "Unable to start bundle", e)]
         (throw (CoreException. status))))))


(defn set-context-classloader! [l]
  (-> (Thread/currentThread) (.setContextClassLoader l)))

(defn bundle-classloader [bundle]
  (ccw.util.osgi.BundleClassLoader. bundle))

(defn set-bundle-classloader! [bundle-symbolic-name]
  (-> bundle-symbolic-name 
    load-and-get-bundle
    bundle-classloader
    set-context-classloader!))

(defn with-bundle* [bundle f]
  (ccw.util.osgi.ClojureOSGi/withBundle 
    bundle
    (reify ccw.util.osgi.RunnableWithException
      (run [this] (f)))))

(defmacro with-bundle [bundle-name & body]
  `(with-bundle* (load-and-get-bundle ~(name bundle-name))
     (fn [] ~@body)))

(defn field
  "Uses reflection to get the value of field field-name of object.
   Works even for private/protected fields. Does not use any cache, beware
   using it in tight loops."
  [object field-name]
  (let [field (.getDeclaredField (.getClass object) field-name)]
    (.setAccessible field true)
    (.get field object)))

(defn force-field!
  "Uses reflection to set the value of field field-name of object to new-value.
   Works even for private/protected fields. Does not use any cache, beware
   using it in tight loops."
  [object field-name new-value]
  (let [field (.getDeclaredField (.getClass object) field-name)]
    (.setAccessible field true)
    (.set object field new-value)))

(defn registry
  "Return the IExtensionRegistry instance"
  [] (org.eclipse.core.runtime.RegistryFactory/getRegistry))

(defn master-token
  "Steals the master token from the Extension Registry so we can do evil things"
  []
  (field (registry) "masterToken"))

;; http://www.ibm.com/developerworks/opensource/library/os-ecl-dynext/
(defn add-contribution!
  "s: the contribution, as a String, in the form
      '<plugin>
          <extension
             id=\"a-dynamic-marker-type-id\"
             name=\"A dynamic marker type\"
             point=\"org.eclipse.core.resources.markers\">
               <persistent value=\"true\" />
               <super type=\"org.eclipse.core.resources.problemmarker\" />
          </extension>
        </plugin>'
   bundle: which bundle declares to be the contributor? defaults to \"ccw.core\"
   persist: if true, the contribution is stored in registry cache, and thus not lost on Eclipse restart
            true by default"
  ([name s] (add-contribution! name s (bundle "ccw.core")))
  ([name s bundle] (add-contribution! name s bundle true))
  ([name s bundle persist]
    (let [token (.getTemporaryUserToken (registry))]
      (add-contribution! name s bundle persist token)))
  ([name s bundle persist token]
    (let [contributor (org.eclipse.core.runtime.ContributorFactoryOSGi/createContributor bundle)
          is (java.io.ByteArrayInputStream. (.getBytes s))]
      (.addContribution (registry) is contributor persist name nil token))))

(defn remove-extension!
  "Remove registry Extension by id.
   Return true if successfully removed, false if not removed or no such extension."
  ([id] (remove-extension! (bundle "ccw.core") id))
  ([bundle id]
    (when-let [extension (.getExtension (registry) id)]
      (.removeExtension (registry) extension (master-token)))))

(defn xml-map->extension [ext-map]
  (let [m {:tag "plugin" :content [ext-map]}]
    (with-out-str (xml/emit-element m))))

;(ccw.bundle/add-contribution! 
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

;(ccw.bundle/add-contribution! 
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



;(ccw.bundle/add-contribution! 
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


      
;(ccw.bundle/add-contribution! 
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
