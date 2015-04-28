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

import static ccw.preferences.PreferenceConstants.USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import ccw.CCWPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private class SyntaxColoringDefault {
        String preferenceConstant;
        boolean defaultEnabled;
        RGB defaultColor;
        boolean isBold;
        boolean isItalic;
        
        SyntaxColoringDefault(String preferenceConstant, boolean defaultEnabled, RGB defaultColor, boolean isBold, boolean isItalic) {
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
    
    RGB callableRGB = new RGB(127, 0, 85);
//    RGB literalRGB = new RGB(34, 139, 34);
    RGB literalRGB = new RGB(63, 95, 191);
    RGB metaRGB = new RGB(63, 95, 191);
    RGB commentRGB = new RGB(63, 127, 95);
    RGB stringKeywordLiteralRGB = new RGB(42, 0, 255);
    RGB characterLiteralRGB = new RGB(0, 0, 192);
    private SyntaxColoringDefault[] coloringDefaults = new SyntaxColoringDefault[] {
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.FUNCTION_Token), false, new RGB(0,0,0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableFUNCTION_Token), true, callableRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.SPECIAL_FORM_Token), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableSPECIAL_FORM_Token), true, callableRGB, true, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.RAW_SYMBOL_Token), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callable_RAW_SYMBOL_Token), true, callableRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.commentToken), true, commentRGB, false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.GLOBAL_VAR_Token), true, literalRGB, false, false), /* rename in earMuffedVar */
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableGLOBAL_VAR_Token), true, literalRGB, true, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.keywordToken), true, stringKeywordLiteralRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.metaToken), true, metaRGB, false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.readerLiteralTag), true, metaRGB, false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.MACRO_Token), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableMACRO_Token), true, callableRGB, true, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.stringToken), true, stringKeywordLiteralRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.otherLiteralsToken), true, literalRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.regexToken), true, stringKeywordLiteralRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.intToken), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.floatToken), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.charToken), true, characterLiteralRGB, false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.JAVA_CLASS_Token), true, new RGB(0, 0, 0), false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableJAVA_CLASS_Token), true, callableRGB, false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.JAVA_INSTANCE_METHOD_Token), true, new RGB(0, 0, 0), false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableJAVA_INSTANCE_METHOD_Token), true, callableRGB, false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.JAVA_STATIC_METHOD_Token), true, new RGB(0, 0, 0), false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.callableJAVA_STATIC_METHOD_Token), true, callableRGB, false, true),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.deactivatedRainbowParen), true, new RGB(145, 145, 145), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel1), true, new RGB(204, 122, 122), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel2), true, new RGB(204, 176, 122), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel3), true, new RGB(122, 204, 122), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel4), true, new RGB(122, 204, 176), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel5), true, new RGB(122, 176, 204), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel6), true, new RGB(122, 122, 204), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel7), true, new RGB(176, 122, 204), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.rainbowParenLevel8), true, new RGB(204, 122, 176), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.replLogValue), true, new RGB(0, 0x80, 0), false, false),
        new SyntaxColoringDefault(PreferenceConstants.getTokenPreferenceKey(PreferenceConstants.replLogError), true, new RGB(0x80, 0, 0), false, false),
    };
    
	@Override
	public void initializeDefaultPreferences() {
	    IPreferenceStore store = CCWPlugin.getDefault().getPreferenceStore();
	    
	    store.setDefault(PreferenceConstants.CCW_GENERAL_AUTOMATIC_NATURE_ADDITION, true);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE, false);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_LAUNCH_REPLS_IN_DEBUG_MODE, true);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_USE_LEININGEN_LAUNCHER, true);
	    
	    store.setDefault(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
	    store.setDefault(org.eclipse.jdt.ui.PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, 
                StringConverter.asString(new RGB(150, 150, 150)));
	    store.setDefault(PreferenceConstants.EDITOR_ESCAPE_ON_PASTE, false);
	    store.setDefault(PreferenceConstants.EDITOR_CODE_COMPLETION_AUTO_ACTIVATE, true);
	    store.setDefault(PreferenceConstants.EDITOR_DISPLAY_NAMESPACE_IN_TABS, true);
	    store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 2);
	    store.setDefault(PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP, true);
	    store.setDefault(USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT, false);
	    store.setDefault(PreferenceConstants.SHOW_RAINBOW_PARENS_BY_DEFAULT, true);
	    store.setDefault(PreferenceConstants.USE_TAB_FOR_REINDENTING_LINE, true);
	    store.setDefault(PreferenceConstants.FORCE_TWO_SPACES_INDENT, false);
	    
	    store.setDefault(PreferenceConstants.REPL_VIEW_AUTO_EVAL_ON_ENTER_ACTIVE, true);
	    store.setDefault(PreferenceConstants.REPL_VIEW_DISPLAY_HINTS, true);
	    store.setDefault(PreferenceConstants.REPL_VIEW_PPRINT_RESULT, true);

	    store.setDefault(PreferenceConstants.REPL_HISTORY_MAX_SIZE, 1000);
	    store.setDefault(PreferenceConstants.REPL_HISTORY_PERSIST_SCHEDULE, 30000);
	    
	    for (SyntaxColoringDefault d: coloringDefaults) {
	        store.setDefault(d.getPreferenceConstant(),
	                StringConverter.asString(d.getDefaultColor()));
	        store.setDefault(SyntaxColoringHelper.getEnabledPreferenceKey(d.getPreferenceConstant()),
                    d.isDefaultEnabled());
	        store.setDefault(SyntaxColoringHelper.getBoldPreferenceKey(d.getPreferenceConstant()), d.isBold());
	        store.setDefault(SyntaxColoringHelper.getItalicPreferenceKey(d.getPreferenceConstant()), d.isItalic());
	    }
	    
	    // Experimental Features
	    store.setDefault(PreferenceConstants.EXPERIMENTAL_AUTOSHIFT_ENABLED, true);
	}
}
