package ccw.util;

import org.eclipse.core.runtime.Plugin;

import ccw.CCWPlugin;
import ccw.util.osgi.ClojureOSGi;

public class ClojureInvoker {
	
	private final String namespace;
	
	public ClojureInvoker(String namespace) {
		this.namespace = namespace;
	}

	public Object _(final String varName, final Object arg1) {
		return ClojureUtils.invoke(namespace, varName, arg1);
	}

	public Object _(final String varName, final Object arg1, final Object arg2) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2);
	}

	public Object _(final String varName, final Object arg1, final Object arg2, final Object arg3) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3);
	}

	public Object _(final String varName, final Object arg1, final Object arg2, final Object arg3, final Object arg4) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3, arg4);
	}

	public Object _(final String varName, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5) {
		return ClojureUtils.invoke(namespace, varName, arg1, arg2, arg3, arg4, arg5);
	}
	
	public static ClojureInvoker newInvoker(Plugin plugin, final String namespace) {
		try {
			ClojureOSGi.require(plugin.getBundle(), namespace);
			return new ClojureInvoker(namespace);
		} catch (Exception e) {
			CCWPlugin.logError("Exception while calling newInvoker(" 
					+ plugin.getBundle().getSymbolicName() 
					+ ", " + namespace + ")"
				, e);
			throw new RuntimeException(
					"Exception while calling newInvoker(" 
							+ plugin.getBundle().getSymbolicName() 
							+ ", " + namespace + ")",
			        e);
		}
	}


}
