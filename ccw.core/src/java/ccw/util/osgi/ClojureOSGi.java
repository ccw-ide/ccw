package ccw.util.osgi;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import ccw.util.DisplayUtil;
import clojure.java.api.Clojure;
import clojure.lang.Compiler;
import clojure.lang.DynamicClassLoader;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureOSGi {
	private static volatile boolean initialized;
	private static void initialize() {
		if (initialized) return;
		synchronizedInitialize();
	}
	private synchronized static void synchronizedInitialize() {
		if (initialized) return;

		CCWPlugin plugin = CCWPlugin.getDefault();
		if (plugin == null) {
			CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI,
					"ClojureOSGi.initialize will fail because ccw.core plugin not activated yet");
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
	public static Object withBundle(Bundle aBundle, RunnableWithException aCode)
			throws RuntimeException {
		return withBundle(aBundle, aCode, null);
	}

	private static final ConcurrentHashMap<Bundle,DynamicClassLoader> bundleClassLoaders = new ConcurrentHashMap<Bundle,DynamicClassLoader>();
	
	/**
	 * Note: if method called with a different list of additionalURLs for same bundle which is already
	 * cached, the list will not be used...
	 */
	private static DynamicClassLoader getDynamicClassLoader(Bundle bundle, List<URL> additionalURLs) {
		DynamicClassLoader l = bundleClassLoaders.get(bundle);
		if (l == null) {
			synchronized (bundleClassLoaders) {
				l = bundleClassLoaders.get(bundle);
				if (l == null) {
					ClassLoader bundleLoader = new BundleClassLoader(bundle);
					l = new DynamicClassLoader(bundleLoader);
					if (additionalURLs != null) {
						for (URL url: additionalURLs) {
							l.addURL(url);
						}
					}
					bundleClassLoaders.put(bundle, l);
				}
			}
		}
		return l;
	}
	
	public static Object withBundle(Bundle aBundle, RunnableWithException aCode, List<URL> additionalURLs)
			throws RuntimeException {

		initialize();

		DynamicClassLoader loader = getDynamicClassLoader(aBundle, additionalURLs);
		
		IPersistentMap bindings = RT.map(Compiler.LOADER, loader);
		bindings = bindings.assoc(RT.USE_CONTEXT_CLASSLOADER, true);

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
			CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, e, msg);
			throw new RuntimeException(msg, e);
		} finally {
			if (pushed) {
				Var.popThreadBindings();
			}
			Thread.currentThread().setContextClassLoader(saved);
		}
	}

	private static final Set<String> synchronizedAlreadyRequiredNamespaces = new HashSet<String>();
	
	public synchronized static void require(final Bundle bundle, final String namespace) {
		if (DisplayUtil.isUIThread()) {
			CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI_UI_THREAD, "should not be called from UI Tread");
			CCWPlugin.getTracer().traceDumpStack(TraceOptions.CLOJURE_OSGI_UI_THREAD);
		}
		
		if (synchronizedAlreadyRequiredNamespaces.contains(namespace)) {
			return;
		}
		
		ClojureOSGi.withBundle(bundle, new RunnableWithException() {
			@Override
			public Object run() throws Exception {
				try {
					Clojure.var("clojure.core", "require").invoke(Clojure.read(namespace));
					String msg = "Namespace " + namespace + " loaded from bundle " + bundle.getSymbolicName();
					CCWPlugin.getTracer().trace(TraceOptions.CLOJURE_OSGI, msg);
					synchronizedAlreadyRequiredNamespaces.add(namespace);
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
