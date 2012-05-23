package ccw.util;

import org.eclipse.core.runtime.Plugin;

import clojure.osgi.ClojureOSGi;

public class ClojureInvoker {
	private final String namespace;
	
	public ClojureInvoker(String namespace) {
		this.namespace = namespace;
	}

	public Object _(String varName, Object arg1) {
		return ClojureUtils.invoke(namespace, varName, arg1);
	}

	public Object _(String varName, Object arg1, Object arg2) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2);
	}

	public Object _(String varName, Object arg1, Object arg2, Object arg3) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3);
	}

	public Object _(String varName, Object arg1, Object arg2, Object arg3, Object arg4) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3, arg4);
	}

	public Object _(String varName, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3, arg4, arg5);
	}
	
	public static ClojureInvoker newInvoker(Plugin plugin, String namespace) {
		try {
			ClojureOSGi.require(plugin.getBundle().getBundleContext(), namespace);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return new ClojureInvoker(namespace);
	}


}
