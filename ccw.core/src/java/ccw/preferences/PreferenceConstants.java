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

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    public static final String CCW_PREFERENCE_PREFIX = "ccw.preferences"; //$NON-NLS-1$
    
	public static final String SWITCH_TO_NS_ON_REPL_STARTUP = CCW_PREFERENCE_PREFIX + ".switch_to_ns_on_repl_startup"; //$NON-NLS-1$

	public static final String USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT = CCW_PREFERENCE_PREFIX + ".use_strict_structural_editing_mode_by_default"; //$NON-NLS-1$

	public static final String USE_TAB_FOR_REINDENTING_LINE = CCW_PREFERENCE_PREFIX + ".use_tab_for_reindenting_line";
	
	public static final String EDITOR_COLORING_PREFIX = "editor_color"; //$NON-NLS-1$
	
	public static final String EDITOR_FUNCTION_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".function"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_LITERAL_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".literal"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_SPECIAL_FORM_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".special_form"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_COMMENT_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".comment"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_GLOBAL_VAR_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".global_var"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_KEYWORD_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".keyword"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_METADATA_TYPEHINT_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".metadata_typehint"; //$NON-NLS-1$ //$NON-NLS-2$
    public static final String EDITOR_MACRO_COLOR = CCW_PREFERENCE_PREFIX + "." + EDITOR_COLORING_PREFIX + ".macro"; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String REPL_VIEW_AUTO_EVAL_ON_ENTER_ACTIVE = CCW_PREFERENCE_PREFIX + ".repl_view_autoeval_on_enter_active";
    
    /* TODO enable these once text attributes are used in the editor
    public static final String EDITOR_BOLD_SUFFIX = ".bold"; //$NON-NLS-1$
    public static final String EDITOR_ITALIC_SUFFIX = ".italic"; //$NON-NLS-1$
    public static final String EDITOR_UNDERLINE_SUFFIX = ".underline"; //$NON-NLS-1$
    public static final String EDITOR_STRIKETHROUGH_SUFFIX = ".strikethrough"; //$NON-NLS-1$
    */
    
    public static final String EDITOR_COLORING_ENABLED_SUFFIX = ".enabled"; //$NON-NLS-1$
}
