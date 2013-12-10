package ccw.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactorySpi;
import org.osgi.framework.Bundle;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import ccw.util.osgi.ClojureOSGi;
import ccw.util.osgi.RunnableWithException;
import clojure.lang.Var;

// TODO add Traces instead of sysout.printlns
/**
 * Implementation of <code>IContributionFactorySpi</code> interface for
 * enabling custom <code>bundleclass://...</code> URLs inside the Eclipse
 * Application Model.
 * <p>
 * The URL must be of the form 
 * <code>bundleclass://bundleSymbolicName/clojure/var-ns/var-name/var-arg1/var-arg2</code>
 * <br/>
 * For instance:
 * <code>bundleclass://ccw.core/clojure/ccw.e4.dsl/generic-handler/....</code> for
 * invoking var ccw.e4.dsl/generic-handler with the following parameters,
 * and using the result as the instance the bundleclass was meant to create.
 * </p>
 * 
 * @author laurentpetit
 */
public class ClojureContributionFactorySpi implements IContributionFactorySpi {

	public ClojureContributionFactorySpi() { }
	
	@Override
	public Object create(final Bundle bundle, final String varAndParams,
			final IEclipseContext context) {
		CCWPlugin.getTracer().trace(TraceOptions.LOG_INFO, "create object for bundleclass://" + bundle.getSymbolicName() + "/" + "clojure" + "/" + varAndParams);
		try {
			final String[] parts = varAndParams.split("\\/");
			String var = parts[0] + "/" + parts[1];
			final Var v = BundleUtils.requireAndGetVar(bundle, var);
			return ClojureOSGi.withBundle(bundle, new RunnableWithException() {
				@Override
				public Object run() throws Exception {
					switch (parts.length) {
					case 2: return v.invoke(context);
					case 3: return v.invoke(context, parts[2]);
					case 4: return v.invoke(context, parts[2], parts[3]);
					case 5: return v.invoke(context, parts[2], parts[3], parts[4]);
					case 6: return v.invoke(context, parts[2], parts[3], parts[4], parts[5]);
					case 7: return v.invoke(context, parts[2], parts[3], parts[4], parts[5], parts[6]);
					case 8: return v.invoke(context, parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
					default: throw new UnsupportedOperationException("Cannot handle more than 6 arguments");
					}
				}
			});
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Object call(Object object, String methodName,
			IEclipseContext context, Object defaultValue) {
		throw new UnsupportedOperationException("call for object " + object + ", methodName " + methodName + ", context " + context + ", defaultValue " + defaultValue);
	}
}
