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

    RGB callableRGB = new RGB(127, 0, 85);
//    RGB literalRGB = new RGB(34, 139, 34);
    RGB literalRGB = new RGB(63, 95, 191);
    RGB metaRGB = new RGB(63, 95, 191);
    RGB commentRGB = new RGB(63, 127, 95);
    RGB stringKeywordLiteralRGB = new RGB(42, 0, 255);
    RGB characterLiteralRGB = new RGB(0, 0, 192);
    private SyntaxColoringPreference[] coloringDefaults = new SyntaxColoringPreference[] {
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.FUNCTION_Token), false, new RGB(0,0,0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableFUNCTION_Token), true, callableRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.SPECIAL_FORM_Token), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableSPECIAL_FORM_Token), true, callableRGB, true, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.RAW_SYMBOL_Token), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callable_RAW_SYMBOL_Token), true, callableRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.commentToken), true, commentRGB, false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.GLOBAL_VAR_Token), true, literalRGB, false, false), /* rename in earMuffedVar */
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableGLOBAL_VAR_Token), true, literalRGB, true, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.keywordToken), true, stringKeywordLiteralRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.metaToken), true, metaRGB, false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.readerLiteralTag), true, metaRGB, false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.MACRO_Token), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableMACRO_Token), true, callableRGB, true, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.stringToken), true, stringKeywordLiteralRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.otherLiteralsToken), true, literalRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.regexToken), true, stringKeywordLiteralRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.intToken), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.floatToken), false, new RGB(0, 0, 0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.charToken), true, characterLiteralRGB, false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.JAVA_CLASS_Token), true, new RGB(0, 0, 0), false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableJAVA_CLASS_Token), true, callableRGB, false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.JAVA_INSTANCE_METHOD_Token), true, new RGB(0, 0, 0), false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableJAVA_INSTANCE_METHOD_Token), true, callableRGB, false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.JAVA_STATIC_METHOD_Token), true, new RGB(0, 0, 0), false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.callableJAVA_STATIC_METHOD_Token), true, callableRGB, false, true),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.deactivatedRainbowParen), true, new RGB(145, 145, 145), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel1), true, new RGB(204, 122, 122), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel2), true, new RGB(204, 176, 122), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel3), true, new RGB(122, 204, 122), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel4), true, new RGB(122, 204, 176), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel5), true, new RGB(122, 176, 204), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel6), true, new RGB(122, 122, 204), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel7), true, new RGB(176, 122, 204), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.rainbowParenLevel8), true, new RGB(204, 122, 176), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.replLogValue), true, new RGB(0, 0x80, 0), false, false),
        new SyntaxColoringPreference(PreferenceConstants.getTokenColorPreferenceKey(PreferenceConstants.replLogError), true, new RGB(0x80, 0, 0), false, false)
    };
    
    public static final String DEFAULT_EDITOR_TEXT_HOVER_DESCRIPTORS = "()";
    public static final String DEFAULT_FOLDING_DESCRIPTORS =
              "({:id :fold-parens"
            + " :enabled true"
            + " :loc-tags #{:list}"
            + " :label \"" + Messages.FoldingPreferencePage_fold_parens_label + "\""
            + " :description \"" + Messages.FoldingPreferencePage_fold_parens_description + "\"}"
            + "{:id :fold-double-apices "
            + " :enabled true"
            + " :loc-tags #{:string}"
            + " :label \"" + Messages.FoldingPreferencePage_fold_double_apex_label + "\""
            + " :description \"" + Messages.FoldingPreferencePage_fold_double_apex_description + "\"})";
    public static final Boolean DEFAULT_PROJECTION_ENABLED = Boolean.TRUE;
    
	@Override
	public void initializeDefaultPreferences() {
	    IPreferenceStore store = CCWPlugin.getDefault().getPreferenceStore();
	    
	    store.setDefault(PreferenceConstants.CCW_GENERAL_AUTOMATIC_NATURE_ADDITION, true);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE, false);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_LAUNCH_REPLS_IN_DEBUG_MODE, true);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_USE_LEININGEN_LAUNCHER, true);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_USE_CIDER_NREPL, true);
	    store.setDefault(PreferenceConstants.CCW_GENERAL_USE_CLOJURE_1_6_PRINT_OBJECT_HACK, true);
	    
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
	    store.setDefault(PreferenceConstants.REPL_VIEW_PPRINT_RIGHT_MARGIN, 40);

	    store.setDefault(PreferenceConstants.REPL_HISTORY_MAX_SIZE, 1000);
	    store.setDefault(PreferenceConstants.REPL_HISTORY_PERSIST_SCHEDULE, 30000);
	    
	    for (SyntaxColoringPreference d: coloringDefaults) {
	        store.setDefault(d.getPreferenceConstant(),
	                StringConverter.asString(d.getDefaultColor()));
	        store.setDefault(PreferenceConstants.getEnabledPreferenceKey(d.getPreferenceConstant()),
                    d.isDefaultEnabled());
	        store.setDefault(PreferenceConstants.getBoldPreferenceKey(d.getPreferenceConstant()), d.isBold());
	        store.setDefault(PreferenceConstants.getItalicPreferenceKey(d.getPreferenceConstant()), d.isItalic());
	    }
	    
	    // Experimental Features
	    store.setDefault(PreferenceConstants.EXPERIMENTAL_AUTOSHIFT_ENABLED, true);
	    
	    // Necessary to get the correct coloring after preference initialization
	    CCWPlugin.registerEditorColors(store);

	    // Hover pref
	    store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_DESCRIPTORS, DEFAULT_EDITOR_TEXT_HOVER_DESCRIPTORS);

	    // Folding pref
        store.setDefault(PreferenceConstants.EDITOR_FOLDING_DESCRIPTORS, DEFAULT_FOLDING_DESCRIPTORS);
        store.setDefault(PreferenceConstants.EDITOR_FOLDING_PROJECTION_ENABLED, DEFAULT_PROJECTION_ENABLED);
	}
}
