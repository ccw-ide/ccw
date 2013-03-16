package ccw.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import ccw.util.osgi.ClojureOSGi;
import ccw.util.osgi.RunnableWithException;
import clojure.lang.RT;
import clojure.lang.Var;

public final class BundleUtils {
	
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
	public static Var requireAndGetVar(final Bundle bundle, final String varName) throws CoreException {
		final String[] nsFn = varName.split("/");
		try {
			final String nsName = nsFn[0];
			ClojureOSGi.require(bundle, nsName);
			return (Var) ClojureOSGi.withBundle(bundle, new RunnableWithException() {
				@Override
				public Object run() throws Exception {
					try {
						return RT.var(nsName, nsFn[1]);
					} catch( Exception e) {
						System.out.println("Error while getting var " + varName);
						throw e;
					}
				}
			});
		} catch (Exception e) {
			System.out.println( 
					"Problem requiring namespace/getting var " + varName 
					+ " from bundle " + bundle.getSymbolicName());
			IStatus status = new Status(IStatus.ERROR, bundle.getSymbolicName(), 
					"Problem requiring namespace/getting var " + varName 
					+ " from bundle " + bundle.getSymbolicName(), e);
			throw new CoreException(status);
		}
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
	public static Var requireAndGetVar(final String bundleSymbolicName, final String varName) throws CoreException {
		return requireAndGetVar(loadAndGetBundle(bundleSymbolicName), varName);
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
