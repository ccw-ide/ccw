package ccw.util.osgi;

import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureOSGi {

	static {
		CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "ClojureOSGi: Static initialization, loading clojure.core");
		ClassLoader loader = new BundleClassLoader(CCWPlugin.getDefault().getBundle());
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(loader);
			RT.var("clojure.core", "require");
		} catch (Exception e) {
			throw new RuntimeException(
					"ClojureOSGi: Static initialization, Exception while loading clojure.core", e);
		} finally {
			Thread.currentThread().setContextClassLoader(saved);
		}
		CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "ClojureOSGi: Static initialization, clojure.core loaded");
	}

	public static Object withBundle(Bundle aBundle, RunnableWithException aCode)
			throws RuntimeException {
		
		CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "ClojureOSGi.withBundle(" + aBundle.getSymbolicName() + ")");
		ClassLoader loader = new BundleClassLoader(aBundle);
		IPersistentMap bindings = RT.map(Compiler.LOADER, loader);

		boolean pushed = true;

		ClassLoader saved = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(loader);

			try {
				Var.pushThreadBindings(bindings);
			} catch (RuntimeException aEx) {
				pushed = false;
				throw aEx;
			}

			return aCode.run();
			
		} catch (Exception e) {
			CCWPlugin.logError(
					"Exception while calling withBundle(" 
							+ aBundle.getSymbolicName() + ", aCode)"
					, e);
			throw new RuntimeException(
					"Exception while calling withBundle(" 
							+ aBundle.getSymbolicName() + ", aCode)",
					e);
		} finally {
			if (pushed)
				Var.popThreadBindings();

			Thread.currentThread().setContextClassLoader(saved);
		}
	}

	public synchronized static void require(final Bundle bundle, final String namespace) {
		ClojureOSGi.withBundle(bundle, new RunnableWithException() {
			@Override
			public Object run() throws Exception {
				try {
					CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "ClojureOSGi.require(" + bundle.getSymbolicName() + ", " + namespace + ") - START");
					RT.var("clojure.core", "require").invoke(Symbol.intern(namespace));
					CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "ClojureOSGi.require(" + bundle.getSymbolicName() + ", " + namespace + ") - DONE");
					return null;
				} catch (Exception e) {
					CCWPlugin.logError("ClojureOSGi.require(" + bundle.getSymbolicName() + ", " + namespace + ") - ERROR", e);
					throw e;
				}
			}
		});
	}
}
