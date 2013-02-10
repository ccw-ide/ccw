package ccw.util.osgi;

import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureOSGi {
	final static private Var REQUIRE;
//	final static private Var OSGI_REQUIRE;

	static {
		try {
			BundleClassLoader loader = new BundleClassLoader(CCWPlugin.getDefault().getBundle());
			IPersistentMap bindings = RT.map(Compiler.LOADER, loader);

			boolean pushed = true;

			ClassLoader saved = Thread.currentThread().getContextClassLoader();

			try {
				Thread.currentThread().setContextClassLoader(loader);

				try {
					Var.pushThreadBindings(bindings);
				} catch (Exception aEx) {
					pushed = false;
					throw aEx;
				}

				REQUIRE = RT.var("clojure.core", "require");
//				OSGI_REQUIRE = RT.var("clojure.osgi.core",
//						"osgi-require");
//				REQUIRE.invoke(Symbol.intern("clojure.main"));
//				REQUIRE.invoke(Symbol.intern("clojure.osgi.core"));
			} finally {
				if (pushed)
					Var.popThreadBindings();

				Thread.currentThread().setContextClassLoader(saved);
			}
		} catch (Exception aEx) {
			throw new RuntimeException(aEx);
		}
	}

//	public static void require(BundleContext aContext, final String aName)
//			throws Exception {
//
//		try {
//
//			withBundle(aContext.getBundle(), new RunnableWithException() {
//				public Object run() throws Exception {
//					OSGI_REQUIRE.invoke(Symbol.intern(aName));
//					return null;
//				}
//			});
//		} catch (Exception aEx) {
//			throw aEx;
//		}
//	}
	
//	public static void loadAOTClass(final BundleContext aContext,
//			final String fullyQualifiedAOTClassName) throws Exception {
//		
//		withBundle(aContext.getBundle(), new RunnableWithException() {
//			public Object run() throws Exception {
//				Class.forName(fullyQualifiedAOTClassName, true,
//						new BundleClassLoader(aContext.getBundle()));
//				return null;
//			}
//		});
//	}

//	public static void requireAndStart(final BundleContext aContext,
//			final String aNamespace) throws Exception {
//
//		try {
//			withBundle(aContext.getBundle(), new RunnableWithException() {
//				public Object run() throws Exception {
//
//					String name = "bundle-start";
//					final Var var = RT.var(aNamespace, name);
//					if (var.isBound())
//						var.invoke(aContext);
//					else
//						throw new Exception(String.format(
//								"'%s' is not bound in '%s'", name, aNamespace));
//					return null;
//				}
//			});
//		} catch (Exception aEx) {
//			throw aEx;
//		}
//	}

	public static Object withBundle(Bundle aBundle, RunnableWithException aCode)
			throws Exception {
		ClassLoader loader = new BundleClassLoader(aBundle);
		IPersistentMap bindings = RT.map(Compiler.LOADER,
				loader);

		boolean pushed = true;

		ClassLoader saved = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(loader);

			try {
				Var.pushThreadBindings(bindings);
			} catch (Exception aEx) {
				pushed = false;
				throw aEx;
			}

			return aCode.run();
		} finally {
			if (pushed)
				Var.popThreadBindings();

			Thread.currentThread().setContextClassLoader(saved);
		}
	}
}
