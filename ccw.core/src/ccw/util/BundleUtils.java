package ccw.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import ccw.CCWPlugin;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class BundleUtils {
	private static final Var findNs = RT.var("clojure.core", "find-ns");
	
	private BundleUtils() {
		// Not intended to be instanciated
	}
	
	/**
	 * Returns the var corresponding to <code>varName</code>, requiring its
	 * namespace first if not already present in memory.
	 * 
	 * @param bundleSymbolicName the symbolic name of the bundle from which the
	 *        namespace would be loaded, if so needed
	 * @param varName fully qualified var name
	 * @return the var
	 * @throws CoreException
	 */
	public static Var requireAndGetVar(String bundleSymbolicName, String varName) throws CoreException {
		final String[] nsFn = varName.split("/");
		try {
			if (findNs.invoke(Symbol.intern(nsFn[0])) == null) {
				CCWPlugin.getClojureOSGi().require(loadAndGetBundle(bundleSymbolicName), nsFn[0]);
			}
			return RT.var(nsFn[0], nsFn[1]);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, bundleSymbolicName, 
					"Problem requiring namespace/getting var " + varName 
					+ " from bundle " + bundleSymbolicName, e);
			throw new CoreException(status);
		}
	}
	public static Bundle loadAndGetBundle(String bundleSymbolicName) throws CoreException {
		// TODO: not good??, maybe we will not catch the right bundle (the same the OSGi framework would use ...)
		try {
			Bundle b = Platform.getBundle(bundleSymbolicName);
			if ((b.getState() != Bundle.STARTING) && (b.getState() != Bundle.ACTIVE)) {
				b.start();
			}
			return b;
		} catch (BundleException e) {
			IStatus status = new Status(IStatus.ERROR, bundleSymbolicName, 
					"Unable to start bundle", e);
			throw new CoreException(status);
		}
	}

}
