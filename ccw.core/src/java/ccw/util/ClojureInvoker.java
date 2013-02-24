package ccw.util;

import org.eclipse.core.runtime.Plugin;

import ccw.CCWPlugin;
import ccw.util.osgi.ClojureOSGi;
import ccw.util.osgi.RunnableWithException;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureInvoker {
	private static final Var require = (Var) ClojureOSGi.withBundle(
			CCWPlugin.getDefault().getBundle(), 
			new RunnableWithException() {
				@Override
				public Object run() throws Exception {
					return RT.var("clojure.core", "require");
				}
			});
	
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
			ClojureOSGi.withBundle(plugin.getBundle(), new RunnableWithException() {
				
				@Override
				public Object run() throws Exception {
					require.invoke(Symbol.intern(namespace));
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception while calling newInvoker(" 
							+ plugin.getBundle().getSymbolicName() 
							+ ", " + namespace + ")",
			        e);
		}
		return new ClojureInvoker(namespace);
	}


}
