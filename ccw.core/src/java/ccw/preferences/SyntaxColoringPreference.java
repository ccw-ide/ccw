/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - refactored out for clarity
 *******************************************************************************/
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