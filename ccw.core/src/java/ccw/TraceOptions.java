package ccw;

/**
 * These constants are used by Java code invoking Eclipse Tracing features.
 * <p>
 * Eclipse tracing features are exposed via <code>CCWPlugin.getTracer()</code>
 * <p>
 * Every trace option here must correspond to a trace option in the
 * .options file of bundle ccw.core.
 *
 * @author laurentpetit
 */
public class TraceOptions {

	/** Global trace options for enabling/disabling traces */
	public static final String DEBUG = "/debug";

	/**
	 * When true CCWPlugin/log() calls will be traced in trace.log
	 * in addition of being logged in .log
	 */
	public static final String LOG_INFO = "/log/info";

	/**
	 * When true CCWPlugin/logWarning() calls will be traced in trace.log
	 * in addition of being logged in .log
	 */
	public static final String LOG_WARNING = "/log/warning";

	/**
	 * When true CCWPlugin/logError() calls will be traced in trace.log
	 * in addition of being logged in .log
	 */
	public static final String LOG_ERROR = "/log/error";

	/** REPLView related traces */
	public static final String REPL = "/repl";

	/** REPLView related traces concerning View focus issues */
	public static final String REPL_FOCUS = "/repl/focus";

	/** Project Builder related traces */
	public static final String BUILDER = "/builder";

	/** Launch Configurations related traces */
	public static final String LAUNCHER = "/launcher";

	/** Autocompletion (content assist) related traces */
	public static final String AUTOCOMPLETION = "/autocompletion";

	/** Paredit related traces */
	public static final String PAREDIT = "/paredit";

	/** Clojure OSGi integration related traces */
	public static final String CLOJURE_OSGI = "/clojure.osgi";

	/** Code Outline related traces */
	public static final String OUTLINE = "/outline";

	/** User plugins loading related traces */
	public static final String USER_PLUGINS = "/user-plugins";

	/** Syntax color - Damager related traces */
	public static final String SYNTAX_COLOR__DAMAGER = "/syntax-color/damager";

	/** Leiningen support related traces */
	public static final String LEININGEN = "/leiningen";

	/** Eclipse 4 Model/Workbench related traces */
	public static final String E4 = "/e4";

	/** CCW API for User Plugins related traces */
	public static final String API = "/api";

	/** Eclipse Event Broker Clojure wrapper related traces */
	public static final String EVENTS = "/events";

	/** Eclipse clojure utilities related traces */
	public static final String ECLIPSE = "/eclipse";

	/** Clojure Editor related traces */
	public static final String EDITOR = "/editor";

	/** Eclipse project management related traces */
	public static final String PROJECT = "/project";

	/** Repl client related traces */
	public static final String REPL_CLIENT = "/repl-client";

}
