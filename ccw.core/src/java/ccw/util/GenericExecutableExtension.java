package ccw.util;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import ccw.CCWPlugin;
import ccw.util.osgi.ClojureOSGi;
import ccw.util.osgi.RunnableWithException;
import clojure.lang.Var;

public class GenericExecutableExtension implements IExecutableExtensionFactory, IExecutableExtension {

	private String bundleName;
	private Var factory;
	private Map<String, String> factoryParams;
	
	public Object create() throws CoreException {
		try {
			return ClojureOSGi.withBundle(CCWPlugin.getDefault().getBundle(), new RunnableWithException() {
				public Object run() throws Exception {
					System.out.println("GenericExecutableExtension.create() - with factory: " + factory);
					Object ret = factory.invoke(factoryParams);
					System.out.println("GenericExecutableExtension.create() - factory: " + factory + " returned");
					return ret;
				}
			});
		} catch (Exception e) {
			CCWPlugin.logError(
					this.getClass().getName() + " was unable"
							+ " to instanciate Clojure factory "
							+ factory.ns.name.getName() + "/" 
							+ factory.sym.getName() 
							+ " from bundle " + bundleName 
							+ " with following factory params:" + factoryParams, e);
			throw new CoreException(CCWPlugin.createErrorStatus(
					this.getClass().getName() + " was unable"
						+ " to instanciate Clojure factory "
						+ factory.ns.name.getName() + "/" 
						+ factory.sym.getName() 
						+ " from bundle " + bundleName 
						+ " with following factory params:" + factoryParams, e));
		}
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		bundleName = config.getDeclaringExtension().getContributor().getName();
		if (data instanceof String) {
			String name = (String) data;
			initFactory(name);
		} else if (data instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, String> factoryParams = (Map<String, String>) data;
			if (factoryParams.containsKey("factory")) {
				String name = factoryParams.get("factory");
				initFactory(name);
				this.factoryParams = Collections.unmodifiableMap(factoryParams);
			} else {
				throw new CoreException(CCWPlugin.createErrorStatus(
						  this.getClass().getName() + " was unable to initialize correctly:"
						  + " mandatory param \"factory\" is missing."));
			}
		} else {
			throw new CoreException(CCWPlugin.createErrorStatus(
			  this.getClass().getName() + " was unable to initialize correctly:"
			  + " the parameter data passed to it should be of type String "
			  + " or Map<String, String> only."));
		}
	}
	
	private void initFactory(String varName) throws CoreException {
		System.out.println("initFactory(" + varName + ") - START");
		factory = BundleUtils.requireAndGetVar(bundleName, varName);
		System.out.println("initFactory(" + varName + ") - STOP");
	}
}
