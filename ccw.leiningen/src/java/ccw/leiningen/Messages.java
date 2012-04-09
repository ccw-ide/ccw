package ccw.leiningen;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "ccw.leiningen.messages"; //$NON-NLS-1$

    public static String PageName;

    public static String PageDesc;

    public static String PageTitle;
        

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
