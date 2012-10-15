package ccw;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.service.debug.DebugOptions;

/**
 * Helper class for issuing trace options.
 * 
 *  User code must only use the constants to refer to trace options, the rest
 *  of the methods is internal to the tracing mechanism support.
 *  
 * @author laurentpetit
 */
public class TraceOptions {
	
	public static final String REPL = "repl";
	public static final String REPL_FOCUS = "repl/focus";
	public static final String BUILDER = "builder";
	public static final String LAUNCHER = "launcher";
	public static final String AUTOCOMPLETION = "autocompletion";
	
	private final String bundleSymbolicName;
	
	private Map<String, Boolean> options = new HashMap<String, Boolean>() {
		{
			put(REPL, false);
			put(REPL_FOCUS, false);
			put(BUILDER, false);
			put(LAUNCHER, false);
			put(AUTOCOMPLETION, false);
		}
	};
	
	/**
	 * Should only be instanciated by tracing mechanism, not directly by user
	 * code.
	 * 
	 * @param bundleSymbolicName
	 */
	public TraceOptions(String bundleSymbolicName) {
		this.bundleSymbolicName = bundleSymbolicName;
	}
	
	/**
	 * Should only be called by tracing mechanism, not directly by user code 
	 * @param options
	 */
	public void updateOptions(DebugOptions options) {
		for (String option: this.options.keySet()) {
			this.options.put(option, options.getBooleanOption(bundleSymbolicName + "/" + option, false));
		}
	}
	
	/**
	 * Should only be called by tracing mechanism, not directly by user code 
	 * @param options
	 */
	public boolean isOptionEnabled(String option) {
		Boolean res = options.get(option);
		if (res != null) {
			return res;
		} else {
			return false;
		}
	}
}
