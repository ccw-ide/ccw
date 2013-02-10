package ccw.util;

import org.eclipse.core.runtime.Plugin;

import ccw.util.osgi.ClojureOSGi;
import ccw.util.osgi.RunnableWithException;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureInvoker {
	private static final Var require = RT.var("clojure.core", "require");
	
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
	
//	public static ClojureInvoker newInvoker(Plugin plugin, String namespace) {
//		try {
//			ClojureOSGi.require(plugin.getBundle().getBundleContext(), namespace);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		
//		return new ClojureInvoker(namespace);
//	}
	
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
			throw new RuntimeException(e);
		}
		return new ClojureInvoker(namespace);
	}


}
