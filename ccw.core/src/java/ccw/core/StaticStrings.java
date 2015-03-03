package ccw.core;

import ccw.CCWPlugin;

/**
 * Container of static strings, used for various tasks in Counterclockwise.
 * Examples of string included here can be, command-line properties names, context/injection keys...
 * @author Andrea Richiardi
 *
 */
public class StaticStrings {

	// Properties
	public static final String CCW_PROPERTY_NREPL_PORT = "ccw.nrepl.port";
	public static final String CCW_PROPERTY_NREPL_AUTOSTART = "ccw.nrepl.autostart";
	
	// Context
	public static final String CCW_CONTEXT_NAME = "ccw.context";
	public static final String CCW_CONTEXT_VALUE_HOVERMODEL = "ccw.context.value.hover-model";
	
	// Loggers
	public static final String CCW_STATIC_LOGGER = "ccw.logger";
	
	// Files
	public final static String CCW_HOVER_CSS = "platform:/plugin/" + CCWPlugin.PLUGIN_ID + "/assets/hover-control-style.css";

	// Symbolic Names
	public final static String CCW_HOVER_FONT = org.eclipse.jdt.ui.PreferenceConstants.APPEARANCE_JAVADOC_FONT;
}
