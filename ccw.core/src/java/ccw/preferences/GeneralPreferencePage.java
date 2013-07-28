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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ccw.CCWPlugin;

public class GeneralPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public GeneralPreferencePage() {
        super(GRID);
        setPreferenceStore(CCWPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.ClojureGeneralPreferencePage_Description); 
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors() {
    	
        addField(
            new BooleanFieldEditor(
            	ccw.preferences.PreferenceConstants.CCW_GENERAL_AUTOMATIC_NATURE_ADDITION,
                Messages.ClojureGeneralPreferencePage_automatic_nature_addition,
                getFieldEditorParent()));

        addField(
                new BooleanFieldEditor(
                	ccw.preferences.PreferenceConstants.CCW_GENERAL_AUTO_RELOAD_ON_STARTUP_SAVE,
                    Messages.ClojureGeneralPreferencePage_auto_reload_on_startup_save,
                    getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }

}
