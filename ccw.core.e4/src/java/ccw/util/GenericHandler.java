package ccw.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;

import ccw.CCWPlugin;
import ccw.util.osgi.ClojureOSGi;
import ccw.util.osgi.RunnableWithException;
import clojure.lang.Var;

/**
 *  This class has the ability to delegate the execution of code in an Eclipse
 *  4 <code>IEclipseContext</code> to a clojure var.
 *  <p>The namespace of the Clojure var will first be required, then the var
 *  will be invoked with the Eclipse context as the sole argument.
 *  </p>
 *  
 *  @note Future enhancements for this classe may involve some kind of 
 *  Eclipse-like dependency injection based on introspection of the var metadata
 *  for automatically gathering the relevant dependencies from the Eclipse
 *  context.
 *  
 * @author laurentpetit
 */
public class GenericHandler {

	private final String varName;
	private Var var;
	
	public GenericHandler(Var var) {
		this(null, var);
	}
	
	public GenericHandler(String var) {
		this(var, null);
	}
	
	public GenericHandler(String varName, Var var) {
		this.varName = varName;
		this.var = var;
	}
	
	@Execute()
	public void execute(final IEclipseContext context) throws CoreException {
		if (var == null) {
		    var = BundleUtils.requireAndGetVar("ccw.core", varName);
		}
		ClojureOSGi.withBundle(CCWPlugin.getDefault().getBundle(),
				new RunnableWithException() {
					@Override public Object run() throws Exception {
						var.invoke(context);
						return null;
					}
				});
	}
	
}
