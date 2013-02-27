package ccw.util.osgi;

import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureOSGi {

	static {
		System.out.println("ClojureOSGi: Static initialization, loading clojure.core");
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
		System.out.println("ClojureOSGi: Static initialization, clojure.core loaded");
	}

	public static Object withBundle(Bundle aBundle, RunnableWithException aCode)
			throws RuntimeException {
		
		System.out.println("ClojureOSGi.withBundle(" + aBundle.getSymbolicName() + ")");
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
					System.out.println("ClojureOSGi.require(" + bundle.getSymbolicName() + ", " + namespace + ") - START");
					RT.var("clojure.core", "require").invoke(Symbol.intern(namespace));
					System.out.println("ClojureOSGi.require(" + bundle.getSymbolicName() + ", " + namespace + ") - DONE");
					return null;
				} catch (Exception e) {
					CCWPlugin.logError("ClojureOSGi.require(" + bundle.getSymbolicName() + ", " + namespace + ") - ERROR", e);
					throw e;
				}
			}
		});
	}
}
