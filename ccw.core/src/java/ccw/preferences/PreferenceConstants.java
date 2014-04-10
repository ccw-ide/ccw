/*******************************************************************************
 * Copyright (c) 2009 Stephan Muehlstrasser and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - Initial implementation
 *    Stephan Muehlstrasser - Enabling/disabling of syntax coloring
 *******************************************************************************/

package ccw.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;

import ccw.CCWPlugin;
import clojure.lang.Keyword;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
	
	public static final Keyword stringToken = Keyword.intern("string");
	public static final Keyword otherLiteralsToken = Keyword.intern("other-literals");
	public static final Keyword regexToken = Keyword.intern("regex");
	public static final Keyword intToken = Keyword.intern("int");
	public static final Keyword floatToken = Keyword.intern("float");
	public static final Keyword charToken = Keyword.intern("char");
//	public static final Keyword literalSymbolToken = Keyword.intern("literalSymbol");

//	public static final Keyword symbolToken = Keyword.intern("symbol");

	public static final Keyword FUNCTION_Token = Keyword.intern("FUNCTION");
	public static final Keyword callableFUNCTION_Token = Keyword.intern("callableFUNCTION");
	public static final Keyword MACRO_Token = Keyword.intern("MACRO");
	public static final Keyword callableMACRO_Token = Keyword.intern("callableMACRO");
	public static final Keyword SPECIAL_FORM_Token = Keyword.intern("SPECIAL_FORM");
	public static final Keyword callableSPECIAL_FORM_Token = Keyword.intern("callableSPECIAL_FORM");
	public static final Keyword GLOBAL_VAR_Token = Keyword.intern("GLOBAL_VAR");
	public static final Keyword callableGLOBAL_VAR_Token = Keyword.intern("callableGLOBAL_VAR");
	public static final Keyword JAVA_CLASS_Token = Keyword.intern("JAVA_CLASS");
	public static final Keyword callableJAVA_CLASS_Token = Keyword.intern("callableJAVA_CLASS");
	public static final Keyword JAVA_INSTANCE_METHOD_Token = Keyword.intern("JAVA_INSTANCE_METHOD");
	public static final Keyword callableJAVA_INSTANCE_METHOD_Token = Keyword.intern("callableJAVA_INSTANCE_METHOD");
	public static final Keyword JAVA_STATIC_METHOD_Token = Keyword.intern("JAVA_STATIC_METHOD");
	public static final Keyword callableJAVA_STATIC_METHOD_Token = Keyword.intern("callableJAVA_STATIC_METHOD");
	public static final Keyword RAW_SYMBOL_Token = Keyword.intern("RAW_SYMBOL");
	public static final Keyword callable_RAW_SYMBOL_Token = Keyword.intern("callableRAW_SYMBOL");
	
	public static final Keyword deactivatedRainbowParen = Keyword.intern("deactivated-rainbow-paren");
	public static final Keyword rainbowParenLevel1 = Keyword.intern("rainbow-paren-level-1");
	public static final Keyword rainbowParenLevel2 = Keyword.intern("rainbow-paren-level-2");
	public static final Keyword rainbowParenLevel3 = Keyword.intern("rainbow-paren-level-3");
	public static final Keyword rainbowParenLevel4 = Keyword.intern("rainbow-paren-level-4");
	public static final Keyword rainbowParenLevel5 = Keyword.intern("rainbow-paren-level-5");
	public static final Keyword rainbowParenLevel6 = Keyword.intern("rainbow-paren-level-6");
	public static final Keyword rainbowParenLevel7 = Keyword.intern("rainbow-paren-level-7");
	public static final Keyword rainbowParenLevel8 = Keyword.intern("rainbow-paren-level-8");

	public static final Keyword keywordToken = Keyword.intern("keyword");
	public static final Keyword commentToken = Keyword.intern("comment");
	public static final Keyword whitespaceToken = Keyword.intern("whitespace");
	public static final Keyword metaToken = Keyword.intern("meta");
	public static final Keyword readerLiteralTag = Keyword.intern("reader-literal");
	
	public static final Keyword replLogValue = Keyword.intern("repl-log-keyword-value");
	public static final Keyword replLogError = Keyword.intern("repl-log-error");
	
	/** 
	 * Set of tokens keywords for which syntax color information can be retrieved
	 * from preferences via the <code>getColorizableToken()</code> method
	 */
	public static final Set<Keyword> colorizableTokens;
	
	/** List of tokens, ordered to be displayed on the preferences page. */
	public static final List<Keyword> orderedColorizableTokens; // FIXME: use it, or remove it
	
	static {
		orderedColorizableTokens = Collections.unmodifiableList(
				new ArrayList<Keyword>() {
			{
				add(stringToken);
				add(otherLiteralsToken);
				add(regexToken);
				add(intToken);
				add(floatToken);
				add(charToken);
				add(FUNCTION_Token);
				add(callableFUNCTION_Token);
				add(MACRO_Token);
				add(callableMACRO_Token);
				add(SPECIAL_FORM_Token);
				add(callableSPECIAL_FORM_Token);
				add(GLOBAL_VAR_Token);
				add(callableGLOBAL_VAR_Token);
				add(JAVA_CLASS_Token);
				add(callableJAVA_CLASS_Token);
				add(JAVA_INSTANCE_METHOD_Token);
				add(callableJAVA_INSTANCE_METHOD_Token);
				add(JAVA_STATIC_METHOD_Token);
				add(callableJAVA_STATIC_METHOD_Token);
				add(RAW_SYMBOL_Token);
				add(callable_RAW_SYMBOL_Token);
				add(keywordToken);
				add(commentToken);
				add(metaToken);
				add(readerLiteralTag);
				add(deactivatedRainbowParen);
				add(rainbowParenLevel1);
				add(rainbowParenLevel2);
				add(rainbowParenLevel3);
				add(rainbowParenLevel4);
				add(rainbowParenLevel5);
				add(rainbowParenLevel6);
				add(rainbowParenLevel7);
				add(rainbowParenLevel8);
				add(replLogValue);
				add(replLogError);
			}
		});
		
		colorizableTokens = Collections.unmodifiableSet(
				new HashSet<Keyword>(orderedColorizableTokens));
	}
	
	
    public static final String CCW_PREFERENCE_PREFIX = "ccw.preferences"; //$NON-NLS-1$
    
    
    public static final String CCW_GENERAL_AUTOMATIC_NATURE_ADDITION = CCW_PREFERENCE_PREFIX + ".automatic_nature_addition";
    
    public static final String CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE = CCW_PREFERENCE_PREFIX + ".auto_reload_on_startup_save";
    
    public static final String CCW_GENERAL_LAUNCH_REPLS_IN_DEBUG_MODE = CCW_PREFERENCE_PREFIX + ".automatic_launch_repls_in_debug_mode";

    public static final String SWITCH_TO_NS_ON_REPL_STARTUP = CCW_PREFERENCE_PREFIX + ".switch_to_ns_on_repl_startup"; //$NON-NLS-1$

	public static final String USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT = CCW_PREFERENCE_PREFIX + ".use_strict_structural_editing_mode_by_default"; //$NON-NLS-1$
	public static final String SHOW_RAINBOW_PARENS_BY_DEFAULT = CCW_PREFERENCE_PREFIX + ".show_rainbow_parens_by_default"; //$NON-NLS-1$

	public static final String USE_TAB_FOR_REINDENTING_LINE = CCW_PREFERENCE_PREFIX + ".use_tab_for_reindenting_line"; //$NON-NLS-1$
	
	public static final String FORCE_TWO_SPACES_INDENT = CCW_PREFERENCE_PREFIX + ".force_two_spaces_indent"; //$NON-NLS-1$

	public static final String EXPERIMENTAL_AUTOSHIFT_ENABLED = CCW_PREFERENCE_PREFIX + ".experimental.editor.autoshift_enabled";
	
	public static final String EDITOR_COLORING_PREFIX = "editor_color"; //$NON-NLS-1$
	
    public static final String EDITOR_ESCAPE_ON_PASTE = CCW_PREFERENCE_PREFIX + ".escape_on_paste"; //$NON-NLS-1$
    
	public static final String EDITOR_CODE_COMPLETION_AUTO_ACTIVATE = CCW_PREFERENCE_PREFIX + ".code_completion.auto_activate";
	
	public static final String EDITOR_DISPLAY_NAMESPACE_IN_TABS = CCW_PREFERENCE_PREFIX + ".editor.display_namespace_in_tabs";


    public static final String REPL_VIEW_AUTO_EVAL_ON_ENTER_ACTIVE = CCW_PREFERENCE_PREFIX + ".repl_view_autoeval_on_enter_active"; //$NON-NLS-1$
    public static final String REPL_VIEW_DISPLAY_HINTS = CCW_PREFERENCE_PREFIX + ".repl_view_display_hints"; //$NON-NLS-1$
    
    public static final String EDITOR_BOLD_SUFFIX = ".bold"; //$NON-NLS-1$
    public static final String EDITOR_ITALIC_SUFFIX = ".italic"; //$NON-NLS-1$
    /* TODO enable these once text attributes are used in the editor
    public static final String EDITOR_UNDERLINE_SUFFIX = ".underline"; //$NON-NLS-1$
    public static final String EDITOR_STRIKETHROUGH_SUFFIX = ".strikethrough"; //$NON-NLS-1$
    */
    
    public static final String EDITOR_COLORING_ENABLED_SUFFIX = ".enabled"; //$NON-NLS-1$

    public static class ColorizableToken {
    	public final RGB rgb;
    	public final Boolean isBold;
    	public final Boolean isItalic;
    	public ColorizableToken(RGB rgb, Boolean isBold, Boolean isItalic) {
    		this.rgb = rgb;
    		this.isBold = isBold;
    		this.isItalic = isItalic;
    	}
    }
    
    public static String getTokenPreferenceKey(Keyword token) {
    	return CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + "." + token.getName(); //$NON-NLS-1$
    }
    public static ColorizableToken getColorizableToken(IPreferenceStore store, Keyword token, RGB defaultColor) {
    	String tokenKey = getTokenPreferenceKey(token);
    	return new ColorizableToken(
    			CCWPlugin.getPreferenceRGB(store, tokenKey, defaultColor),
    			store.getBoolean(SyntaxColoringHelper.getEnabledPreferenceKey(tokenKey)) 
    				? store.getBoolean(SyntaxColoringHelper.getBoldPreferenceKey(tokenKey)) 
    				: null,
    			store.getBoolean(SyntaxColoringHelper.getEnabledPreferenceKey(tokenKey))
    				? store.getBoolean(SyntaxColoringHelper.getItalicPreferenceKey(tokenKey)) 
    				: null);
    }
    
}
