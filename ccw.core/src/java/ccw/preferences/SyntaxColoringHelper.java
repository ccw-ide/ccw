package ccw.preferences;

public class SyntaxColoringHelper {

    /**
     * A named preference that controls if the given Clojure syntax highlighting is enabled.
     *
     * @param the preference key
     * @return the enabled preference key
     */
    public static String getEnabledPreferenceKey(String preferenceKey) {
    	return preferenceKey + PreferenceConstants.EDITOR_COLORING_ENABLED_SUFFIX;
    }
    
    /**
     * A named preference that controls if the given semantic highlighting has the text attribute bold.
     *
     * @param semanticHighlighting the semantic highlighting
     * @return the bold preference key
     */
    public static String getBoldPreferenceKey(String keyPrefix) {
        return keyPrefix + PreferenceConstants.EDITOR_BOLD_SUFFIX;
    }

    /**
     * A named preference that controls if the given semantic highlighting has the text attribute italic.
     *
     * @param semanticHighlighting the semantic highlighting
     * @return the italic preference key
     */
    public static String getItalicPreferenceKey(String keyPrefix) {
        return keyPrefix + PreferenceConstants.EDITOR_ITALIC_SUFFIX;
    }

}
