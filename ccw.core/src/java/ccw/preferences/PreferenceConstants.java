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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
	public static final Map<String, Keyword> colorKeyKeywordMap; // FIXME: use it, or remove it // AR - now we use it
	
	static {
		colorKeyKeywordMap = Collections.unmodifiableMap(
				new HashMap<String, Keyword>() {
			{
				put(getTokenColorPreferenceKey(stringToken), stringToken);
				put(getTokenColorPreferenceKey(otherLiteralsToken), otherLiteralsToken);
				put(getTokenColorPreferenceKey(regexToken), regexToken);
				put(getTokenColorPreferenceKey(intToken), intToken);
				put(getTokenColorPreferenceKey(floatToken), floatToken);
				put(getTokenColorPreferenceKey(charToken), charToken);
				put(getTokenColorPreferenceKey(FUNCTION_Token), FUNCTION_Token);
				put(getTokenColorPreferenceKey(callableFUNCTION_Token), callableFUNCTION_Token);
				put(getTokenColorPreferenceKey(MACRO_Token), MACRO_Token);
				put(getTokenColorPreferenceKey(callableMACRO_Token), callableMACRO_Token);
				put(getTokenColorPreferenceKey(SPECIAL_FORM_Token), SPECIAL_FORM_Token);
				put(getTokenColorPreferenceKey(callableSPECIAL_FORM_Token), callableSPECIAL_FORM_Token);
				put(getTokenColorPreferenceKey(GLOBAL_VAR_Token), GLOBAL_VAR_Token);
				put(getTokenColorPreferenceKey(callableGLOBAL_VAR_Token), callableGLOBAL_VAR_Token);
				put(getTokenColorPreferenceKey(JAVA_CLASS_Token), JAVA_CLASS_Token);
				put(getTokenColorPreferenceKey(callableJAVA_CLASS_Token), callableJAVA_CLASS_Token);
				put(getTokenColorPreferenceKey(JAVA_INSTANCE_METHOD_Token), JAVA_INSTANCE_METHOD_Token);
				put(getTokenColorPreferenceKey(callableJAVA_INSTANCE_METHOD_Token), callableJAVA_INSTANCE_METHOD_Token);
				put(getTokenColorPreferenceKey(JAVA_STATIC_METHOD_Token), JAVA_STATIC_METHOD_Token);
				put(getTokenColorPreferenceKey(callableJAVA_STATIC_METHOD_Token), callableJAVA_STATIC_METHOD_Token);
				put(getTokenColorPreferenceKey(RAW_SYMBOL_Token), RAW_SYMBOL_Token);
				put(getTokenColorPreferenceKey(callable_RAW_SYMBOL_Token), callable_RAW_SYMBOL_Token);
				put(getTokenColorPreferenceKey(keywordToken), keywordToken);
				put(getTokenColorPreferenceKey(commentToken), commentToken);
				put(getTokenColorPreferenceKey(metaToken), metaToken);
				put(getTokenColorPreferenceKey(readerLiteralTag), readerLiteralTag);
				put(getTokenColorPreferenceKey(deactivatedRainbowParen), deactivatedRainbowParen);
				put(getTokenColorPreferenceKey(rainbowParenLevel1), rainbowParenLevel1);
				put(getTokenColorPreferenceKey(rainbowParenLevel2), rainbowParenLevel2);
				put(getTokenColorPreferenceKey(rainbowParenLevel3), rainbowParenLevel3);
				put(getTokenColorPreferenceKey(rainbowParenLevel4), rainbowParenLevel4);
				put(getTokenColorPreferenceKey(rainbowParenLevel5), rainbowParenLevel5);
				put(getTokenColorPreferenceKey(rainbowParenLevel6), rainbowParenLevel6);
				put(getTokenColorPreferenceKey(rainbowParenLevel7), rainbowParenLevel7);
				put(getTokenColorPreferenceKey(rainbowParenLevel8), rainbowParenLevel8);
				put(getTokenColorPreferenceKey(replLogValue), replLogValue);
				put(getTokenColorPreferenceKey(replLogError), replLogError);
			}
		});
		
		colorizableTokens = Collections.unmodifiableSet(
				new HashSet<Keyword>(colorKeyKeywordMap.values()));
	}
	
	
    public static final String CCW_PREFERENCE_PREFIX = "ccw.preferences"; //$NON-NLS-1$
    
    public static final String CCW_GENERAL_AUTOMATIC_NATURE_ADDITION = CCW_PREFERENCE_PREFIX + ".automatic_nature_addition";
    
    public static final String CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE = CCW_PREFERENCE_PREFIX + ".auto_reload_on_startup_save";
    
    public static final String CCW_GENERAL_LAUNCH_REPLS_IN_DEBUG_MODE = CCW_PREFERENCE_PREFIX + ".automatic_launch_repls_in_debug_mode";

    /** Will leiningen projects be launched via the leiningen launcher? (or the default java launcher?) */
    public static final String CCW_GENERAL_USE_LEININGEN_LAUNCHER = CCW_PREFERENCE_PREFIX + ".use_leiningen_launcher";

    /** Will ccw try to install cider-nrepl middlewares or ccw.server functions in launched repls ? */
    public static final String CCW_GENERAL_USE_CIDER_NREPL = CCW_PREFERENCE_PREFIX + ".user_cider_nrepl";
    
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
    public static final String REPL_VIEW_PPRINT_RESULT = CCW_PREFERENCE_PREFIX + " .repl_view_pprint_result";
	public static final String REPL_VIEW_PPRINT_RIGHT_MARGIN = CCW_PREFERENCE_PREFIX + " .repl_view_pprint_right_margin";

    public static final String REPL_HISTORY_MAX_SIZE = CCW_PREFERENCE_PREFIX + ".repl_history_max_size"; //$NON-NLS-1$
    public static final String REPL_HISTORY_PERSIST_SCHEDULE = CCW_PREFERENCE_PREFIX + ".repl_history_persist_schedule"; //$NON-NLS-1$
    
	public static final String EDITOR_BOLD_SUFFIX = ".bold"; //$NON-NLS-1$
    public static final String EDITOR_ITALIC_SUFFIX = ".italic"; //$NON-NLS-1$
    /* TODO enable these once text attributes are used in the editor
    public static final String EDITOR_UNDERLINE_SUFFIX = ".underline"; //$NON-NLS-1$
    public static final String EDITOR_STRIKETHROUGH_SUFFIX = ".strikethrough"; //$NON-NLS-1$
    */
    
    public static final String EDITOR_COLORING_ENABLED_SUFFIX = ".enabled"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifier state masks.
	 * The value is only used if the value of <code>EDITOR_TEXT_HOVER_MODIFIERS</code>
	 * cannot be resolved to valid SWT modifier bits (taken from org.eclipse.jdt.ui).
	 */
	public static final String EDITOR_TEXT_HOVER_DESCRIPTORS = CCW_PREFERENCE_PREFIX + ".hover_descriptors"; //$NON-NLS-1$

	/**
     * A Boolean preference that defines whether to show the hint to make sticky hover.
     */
    public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE= CCW_PREFERENCE_PREFIX + ".show_hover_affordance"; //$NON-NLS-1$
    
    public static class ColorizableToken {
    	public final @NonNull RGB rgb;
    	public final @Nullable Boolean isBold;
    	public final @Nullable Boolean isItalic;
    	public ColorizableToken(@NonNull RGB rgb, Boolean isBold, Boolean isItalic) {
    		this.rgb = rgb;
    		this.isBold = isBold;
    		this.isItalic = isItalic;
    	}
    }
    
    /**
     * Strips the suffix from a previous suffixed preference key.
     * @param suffixToStrip The suffix to strip off.
     * @param suffixedTokenkey The string to process.
     * @return A non-null stripped string.
     */
    public static @NonNull String stripPreferenceSuffix(String suffixToStrip, String suffixedTokenkey) {
        int categoryIdx = suffixedTokenkey.indexOf(suffixToStrip);
        if (categoryIdx == -1) {
            return suffixedTokenkey;
        }
        return suffixedTokenkey.substring(0, categoryIdx);
    }
    
    /**
     * Preference Keyword->String
     * @param keyword A keyword.
     * @return A token key in String format, never null.
     */
    public static @NonNull String getTokenPreferenceKey(String categoryPrefix, Keyword keyword) {
    	return CCW_PREFERENCE_PREFIX + "." + categoryPrefix + "." + keyword.getName(); //$NON-NLS-1$
    }
    
    public static @Nullable Keyword guessPreferenceKeyword(String key) {
        // AR - If more keyword->string are introduced,
        // more attempts need to be added to this guessing method.
        
        // Strip bold suffix
        String stripped = stripPreferenceSuffix(PreferenceConstants.EDITOR_BOLD_SUFFIX, key);
        Keyword keyword = getTokenColorPreferenceKeyword(stripped);
        if (keyword != null) {
            return keyword;
        }
        
        // Strip bold suffix
        stripped = stripPreferenceSuffix(PreferenceConstants.EDITOR_ITALIC_SUFFIX, key);
        keyword = getTokenColorPreferenceKeyword(stripped);
        if (keyword != null) {
            return keyword;
        }
        
        keyword = getTokenColorPreferenceKeyword(key);
        return keyword;
    }
    
    /**
     * Preference String->Keyword
     * @param tokenKey A key generated with getTokenPreferenceKey.
     * @return A keyword object, can be null if it has never been interned.
     */
    public static @Nullable Keyword getTokenPreferenceKeyword(String categoryPrefix, String tokenKey) {
        int categoryIdx = tokenKey.indexOf(categoryPrefix);
        if (categoryIdx == -1) {
            return null;
        }
        String keywordString = tokenKey.substring(categoryIdx + categoryPrefix.length() + 1);
        Keyword keyword = colorKeyKeywordMap.get(keywordString);
        return keyword;
    }
    
    /**
     * Convenience method for getting Color preferences.
     * @see PreferenceConstants#getTokenColorPreferenceKey(Keyword)
     * @param keyword A keyword.
     * @return A token key in String format, never null.
     */
    public static @NonNull String getTokenColorPreferenceKey(Keyword keyword) {
        return getTokenPreferenceKey(EDITOR_COLORING_PREFIX, keyword);
    }
    
    /**
     * Convenience method for getting Color preferences.
     * @see PreferenceConstants#getTokenPreferenceKeyword(Keyword)
     * @param tokenKey A key generated with getTokenPreferenceKey.
     * @return A keyword object, can be null if it has never been interned.
     */
    public static @Nullable Keyword getTokenColorPreferenceKeyword(String tokenKey) {
        return colorKeyKeywordMap.get(tokenKey);
    }
    
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
    
    public static Boolean isBoldPreferenceKey(String key) {
        return key.endsWith(PreferenceConstants.EDITOR_BOLD_SUFFIX);
    }
    
    public static Boolean isItalicPreferenceKey(String key) {
        return key.endsWith(PreferenceConstants.EDITOR_ITALIC_SUFFIX);
    }
    
    /**
     * Get the ColorizableToken for the given keyword key in the preference store.
     * It does not accepts default value but tries to recover itself from missing colors.
     * @param store A store
     * @param token A keyword key (String)
     * @return A ColorizableToken, never null.
     */
    public static @NonNull ColorizableToken getColorizableToken(IPreferenceStore store, String tokenKey) {
        return new ColorizableToken(
                CCWPlugin.getPreferenceRGB(store, tokenKey),
                store.getBoolean(getEnabledPreferenceKey(tokenKey)) 
                    ? store.getBoolean(getBoldPreferenceKey(tokenKey)) 
                    : null,
                store.getBoolean(getEnabledPreferenceKey(tokenKey))
                    ? store.getBoolean(getItalicPreferenceKey(tokenKey)) 
                    : null);
    }
    
    /**
     * Get the Colorizable token for the given keyword in the preference store.
     * It does not accepts default value but tries to recover itself from missing colors.
     * @param store A store
     * @param token A keyword
     * @return A ColorizableToken, never null.
     */
    public static @NonNull ColorizableToken getColorizableToken(IPreferenceStore store, Keyword token) {
        return getColorizableToken(store, getTokenColorPreferenceKey(token));
    }
}
