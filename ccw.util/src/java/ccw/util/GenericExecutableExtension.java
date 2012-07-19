package ccw.util;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import ccw.util.internal.Activator;
import clojure.lang.Var;
import clojure.osgi.ClojureOSGi;
import clojure.osgi.RunnableWithException;

public class GenericExecutableExtension implements IExecutableExtensionFactory, IExecutableExtension {

	private String bundleName;
	private Var factory;
	private Map<String, String> factoryParams;
	
	public Object create() throws CoreException {
		try {
			return ClojureOSGi.withBundle(BundleUtils.loadAndGetBundle(bundleName), new RunnableWithException() {
				public Object run() throws Exception {
					return factory.invoke(factoryParams);
				}
			});
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			throw new CoreException(Activator.createErrorStatus(
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
				throw new CoreException(Activator.createErrorStatus(
						  this.getClass().getName() + " was unable to initialize correctly:"
						  + " mandatory param \"factory\" is missing."));
			}
		} else {
			throw new CoreException(Activator.createErrorStatus(
			  this.getClass().getName() + " was unable to initialize correctly:"
			  + " the parameter data passed to it should be of type String "
			  + " or Map<String, String> only."));
		}
	}
	
	private void initFactory(String varName) throws CoreException {
		factory = BundleUtils.requireAndGetVar(bundleName, varName);
	}
}
