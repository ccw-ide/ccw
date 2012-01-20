package ccw.repl;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "ccw.repl.messages"; //$NON-NLS-1$

    public static String REPLView_autoEval_on_Enter_active;
    public static String REPLView_autoEval_on_Enter_inactive;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
    public static String format(String message, Object... bindings) {
    	return NLS.bind(message, bindings);
    }
}
