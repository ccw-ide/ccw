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

package clojuredev.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    public static final String CLOJUREDEV_PREFERENCE_PREFIX = "clojuredev.preferences"; //$NON-NLS-1$
    
	public static final String SWITCH_TO_NS_ON_REPL_STARTUP = CLOJUREDEV_PREFERENCE_PREFIX + ".switch_to_ns_on_repl_startup"; //$NON-NLS-1$

	public static final String EDITOR_COLORING_PREFIX = "editor_color"; //$NON-NLS-1$
	
	public static final String EDITOR_FUNCTION_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".function"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_LITERAL_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".literal"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_SPECIAL_FORM_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".special_form"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_COMMENT_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".comment"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_GLOBAL_VAR_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".global_var"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_KEYWORD_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".keyword"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_METADATA_TYPEHINT_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".metadata_typehint"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_MACRO_COLOR = CLOJUREDEV_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".macro"; //$NON-NLS-1$ //$NON-NLS-2$

    /* TODO enable these once text attributes are used in the editor
    public static final String EDITOR_BOLD_SUFFIX = ".bold"; //$NON-NLS-1$
    public static final String EDITOR_ITALIC_SUFFIX = ".italic"; //$NON-NLS-1$
    public static final String EDITOR_UNDERLINE_SUFFIX = ".underline"; //$NON-NLS-1$
    public static final String EDITOR_STRIKETHROUGH_SUFFIX = ".strikethrough"; //$NON-NLS-1$
    */
    
    public static final String EDITOR_COLORING_ENABLED_SUFFIX = ".enabled"; //$NON-NLS-1$
}
