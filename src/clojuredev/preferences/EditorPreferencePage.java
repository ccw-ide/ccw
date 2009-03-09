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
package clojuredev.preferences;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import clojuredev.ClojuredevPlugin;

public class EditorPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public EditorPreferencePage() {
        super(GRID);
        setPreferenceStore(ClojuredevPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.ClojurePreferencePage_Description); 
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors() {
        /*addField(new DirectoryFieldEditor(clojuredev.preferences.PreferenceConstants.P_PATH, 
                Messages.ClojurePreferencePage_4, getFieldEditorParent()));*/
        addField(
            new BooleanFieldEditor(
                PreferenceConstants.EDITOR_MATCHING_BRACKETS,
                Messages.ClojurePreferencePage_highlight_matching_brackets,
                getFieldEditorParent()));

/*      addField(new RadioGroupFieldEditor(
                clojuredev.preferences.PreferenceConstants.P_CHOICE,
            Messages.ClojurePreferencePage_2,
            1,
            new String[][] { { Messages.ClojurePreferencePage_1, Messages.ClojurePreferencePage_5 }, {
                Messages.ClojurePreferencePage_6, Messages.ClojurePreferencePage_7 }
        }, getFieldEditorParent()));*/
/*      addField(
            new StringFieldEditor(clojuredev.preferences.PreferenceConstants.P_STRING, Messages.ClojurePreferencePage_8, getFieldEditorParent()));*/
        addField(
                new IntegerFieldEditor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, Messages.ClojurePreferencePage_displayed_tab_width, getFieldEditorParent()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

}
