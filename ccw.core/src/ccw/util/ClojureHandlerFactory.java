package ccw.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class ClojureHandlerFactory implements IExecutableExtensionFactory, IExecutableExtension {
	private String bundleName;
	private String handlerFn;
	
	public Object create() throws CoreException {
		return new ClojureHandlerProxy(bundleName, handlerFn);
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		assert data instanceof String;
		bundleName = config.getDeclaringExtension().getContributor().getName();
		handlerFn = (String) data;
	}

}
