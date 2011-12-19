package ccw.util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import clojure.lang.RT;
import clojure.lang.Var;
import clojure.osgi.ClojureOSGi;

public class ClojureHandlerProxy extends AbstractHandler {
	private final Var execute;
	public ClojureHandlerProxy(final String bundleSymbolicName, final String handlerFn) throws CoreException {
		final String[] nsFn = handlerFn.split("/");
		try {
			ClojureOSGi.require(getBundle(bundleSymbolicName).getBundleContext(), nsFn[0]);
			execute = RT.var(nsFn[0], nsFn[1]);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, bundleSymbolicName, "Problem creating Handler proxy for handlerFn: " + handlerFn + " from bundle " + bundleSymbolicName, e);
			throw new CoreException(status);
		}
	}

	private Bundle getBundle(String bundleSymbolicName) throws CoreException {
		// TODO: not good??, maybe we will not catch the right bundle (the same the OSGi framework would use ...)
		try {
			Bundle b = Platform.getBundle(bundleSymbolicName);
			if ((b.getState() != Bundle.STARTING) && (b.getState() != Bundle.ACTIVE)) {
				b.start();
			}
			return b;
		} catch (BundleException e) {
			IStatus status = new Status(IStatus.ERROR, bundleSymbolicName, "Unable to start bundle", e);
			throw new CoreException(status);
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			execute.invoke(this, event);
		} catch (Exception e) {
			throw new ExecutionException("clojure handler fn " + execute.ns + "/" + execute.sym + " threw an exception", e);
		}
		return null;
	}

}
