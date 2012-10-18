package ccw;

import java.util.HashMap;
import java.util.Map;

/**
 * @author laurentpetit
 */
public class TraceOptions {

	public static final String LOG_INFO = "log/info";
	public static final String LOG_WARNING = "log/warning";
	public static final String LOG_ERROR = "log/error";

	public static final String REPL = "repl";
	public static final String REPL_FOCUS = "repl/focus";
	public static final String BUILDER = "builder";
	public static final String LAUNCHER = "launcher";
	public static final String AUTOCOMPLETION = "autocompletion";

	@SuppressWarnings("serial")
	public static final Map<String, Boolean> getTraceOptions() {
		return new HashMap<String, Boolean>() {
			{
				put(LOG_INFO, false);
				put(LOG_WARNING, false);
				put(LOG_ERROR, false);

				put(REPL, false);
				put(REPL_FOCUS, false);
				put(BUILDER, false);
				put(LAUNCHER, false);
				put(AUTOCOMPLETION, false);
			}
		};
	}

}
