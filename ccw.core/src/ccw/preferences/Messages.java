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

    public static String ClojurePreferencePage_displayed_tab_width;

    public static String ClojurePreferencePage_highlight_matching_brackets;
    
    public static String ClojurePreferencePage_switch_to_ns_on_repl_startup;

    public static String ClojurePreferencePage_activate_paredit;

    public static String SyntaxColoringPreferencePage_function;

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

    public static String SyntaxColoringPreferencePage_comment;

    public static String SyntaxColoringPreferencePage_globalVar;

    public static String SyntaxColoringPreferencePage_keyword;

    public static String SyntaxColoringPreferencePage_metadataTypehint;
    
    public static String SyntaxColoringPreferencePage_macro;

    public static String SyntaxColoringPreferencePage_preview;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
