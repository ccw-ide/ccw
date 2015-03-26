package ccw.preferences;

import org.eclipse.swt.graphics.RGB;

/**
 * A preference for colorable keywords (identified by a string).
 * @author Andrea Richiardi (refactored)
 *
 */
public class SyntaxColoringPreference {
    
    public String preferenceConstant;
    public boolean defaultEnabled;
    public RGB defaultColor;
    public boolean isBold;
    public boolean isItalic;

    SyntaxColoringPreference(String preferenceConstant, boolean defaultEnabled, RGB defaultColor, boolean isBold, boolean isItalic) {
        this.preferenceConstant = preferenceConstant;
        this.defaultEnabled = defaultEnabled;
        this.defaultColor = defaultColor;
        this.isBold = isBold;
        this.isItalic = isItalic;
    }

    public String getPreferenceConstant() {
        return preferenceConstant;
    }

    public RGB getDefaultColor() {
        return defaultColor;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
    
    public boolean isBold() {
        return isBold;
    }
    
    public boolean isItalic() {
        return isItalic;
    }
}