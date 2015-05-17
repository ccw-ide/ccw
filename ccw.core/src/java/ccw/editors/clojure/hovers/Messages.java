package ccw.editors.clojure.hovers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "ccw.editors.clojure.hovers.messages";
    
    public static String You_need_a_running_repl_docstring;
    public static String You_need_a_running_repl_macro;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
