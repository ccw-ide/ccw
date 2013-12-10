package ccw.util.osgi;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureOSGi {
	private static volatile boolean initialized;
	private synchronized static void initialize() {
		if (initialized) return;
		
		CCWPlugin plugin = CCWPlugin.getDefault();
		if (plugin == null) {
			System.out.println("======= ClojureOSGi.initialize will fail because ccw.core plugin not activated yet");
			System.out.flush();
		}
		ClassLoader loader = new BundleClassLoader(plugin.getBundle());
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(loader);
			Class.forName("clojure.lang.RT", true, loader); // very important, uses the right classloader
			CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "namespace clojure.core loaded");
			initialized = true;
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception while loading namespace clojure.core", e);
		} finally {
			Thread.currentThread().setContextClassLoader(saved);
		}
		
	}
	public synchronized static Object withBundle(Bundle aBundle, RunnableWithException aCode)
			throws RuntimeException {
		return withBundle(aBundle, aCode, null);
	}
	
	public synchronized static Object withBundle(Bundle aBundle, RunnableWithException aCode, List<URL> additionalURLs)
			throws RuntimeException {
		
		initialize();
		
		ClassLoader bundleLoader = new BundleClassLoader(aBundle);
		final URL[] urls = (additionalURLs == null) ? new URL[] {} : additionalURLs.toArray(new URL[additionalURLs.size()]);
		URLClassLoader loader = new URLClassLoader(urls, bundleLoader);
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
			String msg = "Exception while executing code from bundle " 
					+ aBundle.getSymbolicName();
			CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, e,
					msg);
			throw new RuntimeException(msg, e);
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
					RT.var("clojure.core", "require").invoke(Symbol.intern(namespace));
					CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, "Namespace " + namespace + " loaded from bundle " + bundle.getSymbolicName());
					return null;
				} catch (Exception e) {
					String msg = "Exception loading namespace " + namespace + " from bundle " + bundle.getSymbolicName();
					CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, e, msg);
					throw new RuntimeException(msg, e);
				}
			}
		});
	}
}
