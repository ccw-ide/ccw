/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Stephan Muehlstrasser - moved into preferences package
 *    Stephan Muehlstrasser - preference support for syntax coloring
 *******************************************************************************/
package ccw.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import ccw.CCWPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private class SyntaxColoringDefault {
        String preferenceConstant;
        RGB defaultColor;
        boolean defaultEnabled;
        
        SyntaxColoringDefault(String preferenceConstant, RGB defaultColor, boolean defaultEnabled) {
            this.preferenceConstant = preferenceConstant;
            this.defaultColor = defaultColor;
            this.defaultEnabled = defaultEnabled;
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
    }
    
    private SyntaxColoringDefault[] coloringDefaults = new SyntaxColoringDefault[] {
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_FUNCTION_COLOR, new RGB(218, 112, 214), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_LITERAL_COLOR, new RGB(188, 143, 143), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_SPECIAL_FORM_COLOR, new RGB(160, 32, 240), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_COMMENT_COLOR, new RGB(178, 34, 34), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_GLOBAL_VAR_COLOR, new RGB(34, 139, 34), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_KEYWORD_COLOR, new RGB(218, 112, 214), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_METADATA_TYPEHINT_COLOR, new RGB(34, 139, 34), true),
        new SyntaxColoringDefault(PreferenceConstants.EDITOR_MACRO_COLOR, new RGB(9, 107, 243), true),
    };
    
	@Override
	public void initializeDefaultPreferences() {
	    IPreferenceStore store = CCWPlugin.getDefault().getPreferenceStore();
	    store.setDefault(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
	    store.setDefault(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, 
                StringConverter.asString(new RGB(150, 150, 150)));
	    store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 2);
	    store.setDefault(ccw.preferences.PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP, true);
	    
	    for (SyntaxColoringDefault d: coloringDefaults) {
	        store.setDefault(d.getPreferenceConstant(),
	                StringConverter.asString(d.getDefaultColor()));
	        store.setDefault(SyntaxColoringPreferencePage.getEnabledPreferenceKey(d.getPreferenceConstant()),
                    d.isDefaultEnabled());
	    }
	}
}
