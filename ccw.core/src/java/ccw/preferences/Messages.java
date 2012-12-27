/*******************************************************************************
 * Copyright (c) 2009 Stephan Muehlstrasser and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - Initial implementation
 *******************************************************************************/
package ccw.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "ccw.preferences.messages"; //$NON-NLS-1$


    public static String ClojurePreferencePage_Description;

    public static String ClojurePreferencePage_description;
    
    public static String ClojureEditorPreferencePage_Description;

    public static String ClojurePreferencePage_displayed_tab_width;

    public static String ClojurePreferencePage_highlight_matching_brackets;
    
    public static String ClojurePreferencePage_switch_to_ns_on_repl_startup;

    public static String ClojurePreferencePage_use_strict_structural_editing_mode_by_default;
    
    public static String ClojurePreferencePage_show_rainbow_parens_by_default;

    public static String ClojurePreferencePage_use_tab_for_reindenting_line;

    public static String ClojurePreferencePage_force_two_spaces_indent;

    public static String ClojurePreferencePage_escape_on_paste;

    public static String ClojurePreferencePage_code_completion_auto_activate;
    
    public static String ClojurePreferencePage_show_more_context_in_tabs;

    public static String SyntaxColoringPreferencePage_function;

    public static String SyntaxColoringPreferencePage_callableFunction;

    // TODO new messages in properties
    public static String SyntaxColoringPreferencePage_link;

    public static String SyntaxColoringPreferencePage_coloring_element;

    public static String SyntaxColoringPreferencePage_enable;

    public static String SyntaxColoringPreferencePage_color;

    public static String SyntaxColoringPreferencePage_bold;

    public static String SyntaxColoringPreferencePage_italic;

    public static String SyntaxColoringPreferencePage_strikethrough;

    public static String SyntaxColoringPreferencePage_underline;

    public static String SyntaxColoringPreferencePage_literal;

    public static String SyntaxColoringPreferencePage_specialForm;

    public static String SyntaxColoringPreferencePage_callableSpecialForm;

    public static String SyntaxColoringPreferencePage_comment;

    public static String SyntaxColoringPreferencePage_globalVar;

    public static String SyntaxColoringPreferencePage_callableGlobalVar;
    
    public static String SyntaxColoringPreferencePage_keyword;

    public static String SyntaxColoringPreferencePage_metadataTypehint;
    
    public static String SyntaxColoringPreferencePage_readerLiteralTag;
    
    public static String SyntaxColoringPreferencePage_macro;
    
    public static String SyntaxColoringPreferencePage_callableMacro;
    
    public static String SyntaxColoringPreferencePage_symbol;
    
    public static String SyntaxColoringPreferencePage_rawSymbol;
    
    public static String SyntaxColoringPreferencePage_callableRawSymbol;
    
    public static String SyntaxColoringPreferencePage_string;    
    
    public static String SyntaxColoringPreferencePage_regex; 
    
    public static String SyntaxColoringPreferencePage_int;
    
    public static String SyntaxColoringPreferencePage_float;
    
    public static String SyntaxColoringPreferencePage_char;
    
    public static String SyntaxColoringPreferencePage_otherLiterals;    

    public static String SyntaxColoringPreferencePage_javaClass;
    
    public static String SyntaxColoringPreferencePage_javaInstanceMethod;
    
    public static String SyntaxColoringPreferencePage_javaStaticMethod;
    
    public static String SyntaxColoringPreferencePage_callableJavaClass;
    
    public static String SyntaxColoringPreferencePage_callableJavaInstanceMethod;
    
    public static String SyntaxColoringPreferencePage_callableJavaStaticMethod;
    
	public static String SyntaxColoringPreferencePage_deactivateRainbowParen;

	public static String SyntaxColoringPreferencePage_rainbowParenLevel1;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel2;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel3;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel4;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel5;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel6;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel7;
	public static String SyntaxColoringPreferencePage_rainbowParenLevel8;

	public static String SyntaxColoringPreferencePage_replLogValue;
	public static String SyntaxColoringPreferencePage_replLogError;

	public static String SyntaxColoringPreferencePage_preview;

	public static String ClojureREPLPreferencePage_Description;
	
    public static String REPLViewPreferencePage_activate_autoEval_on_Enter;

    public static String REPLViewPreferencePage_displayHint;

    public static String REPLViewPreferencePage_quietLoggingMode;
    
    public static String ClojureGeneralPreferencePage_Description;
    public static String ClojureGeneralPreferencePage_automatic_nature_addition;
    public static String ClojureGeneralPreferencePage_auto_reload_on_startup_save;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
