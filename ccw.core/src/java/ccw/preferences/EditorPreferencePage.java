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

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import ccw.CCWPlugin;

public class EditorPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public EditorPreferencePage() {
        super(GRID);
        setPreferenceStore(CCWPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.ClojureEditorPreferencePage_Description); 
    }
    
    public void createFieldEditors() {
        addField(
            new BooleanFieldEditor(
                PreferenceConstants.EDITOR_MATCHING_BRACKETS,
                Messages.ClojurePreferencePage_highlight_matching_brackets,
                getFieldEditorParent()));

        addField(
            new IntegerFieldEditor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, Messages.ClojurePreferencePage_displayed_tab_width, getFieldEditorParent()));
        
        addField(
    		new BooleanFieldEditor(ccw.preferences.PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP, Messages.ClojurePreferencePage_switch_to_ns_on_repl_startup, getFieldEditorParent()));

        addField(
        	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.USE_STRICT_STRUCTURAL_EDITING_MODE_BY_DEFAULT, Messages.ClojurePreferencePage_use_strict_structural_editing_mode_by_default, getFieldEditorParent()));

        addField(
            	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.SHOW_RAINBOW_PARENS_BY_DEFAULT, Messages.ClojurePreferencePage_show_rainbow_parens_by_default, getFieldEditorParent()));
        
        addField(
            	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.EDITOR_ESCAPE_ON_PASTE, Messages.ClojurePreferencePage_escape_on_paste, getFieldEditorParent()));
        
        addField(
            	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.USE_TAB_FOR_REINDENTING_LINE, Messages.ClojurePreferencePage_use_tab_for_reindenting_line, getFieldEditorParent()));
        
        addField(
            	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.FORCE_TWO_SPACES_INDENT, Messages.ClojurePreferencePage_force_two_spaces_indent, getFieldEditorParent()));
        
        addField(
            	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.EDITOR_CODE_COMPLETION_AUTO_ACTIVATE, Messages.ClojurePreferencePage_code_completion_auto_activate, getFieldEditorParent()));
        
        addField(
            	new BooleanFieldEditor(ccw.preferences.PreferenceConstants.EDITOR_DISPLAY_NAMESPACE_IN_TABS, Messages.ClojurePreferencePage_show_more_context_in_tabs, getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }

}
