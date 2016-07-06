package ccw.util;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import ccw.util.osgi.ClojureOSGi;

public class ClojureInvoker {
	
	private final String namespace;
	
	public ClojureInvoker(String namespace) {
		this.namespace = namespace;
	}

	public Object __(final String varName) {
		return ClojureUtils.invoke(namespace, varName);
	}

	public Object __(final String varName, final Object arg1) {
		return ClojureUtils.invoke(namespace, varName, arg1);
	}

	public Object __(final String varName, final Object arg1, final Object arg2) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2);
	}

	public Object __(final String varName, final Object arg1, final Object arg2, final Object arg3) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3);
	}

	public Object __(final String varName, final Object arg1, final Object arg2, final Object arg3, final Object arg4) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3, arg4);
	}

	public Object __(final String varName, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3, arg4, arg5);
	}
	
	public static ClojureInvoker newInvoker(Plugin plugin, final String namespace) {
		return newInvoker(plugin.getBundle(), namespace);
	}
	public static ClojureInvoker newInvoker(Bundle bundle, final String namespace) {
		try {
			ClojureOSGi.require(bundle, namespace);
			return new ClojureInvoker(namespace);
		} catch (Exception e) {
			CCWPlugin.logError("Exception while calling newInvoker(" 
					+ bundle.getSymbolicName() 
					+ ", " + namespace + ")"
				, e);
			throw new RuntimeException(
					"Exception while calling newInvoker(" 
							+ bundle.getSymbolicName() 
							+ ", " + namespace + ")",
			        e);
		}
	}


}
