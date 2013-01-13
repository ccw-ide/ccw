/*******************************************************************************
 * Copyright (c) 2012 Gunnar Völkel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Gunnar Völkel - initial implementation
 *******************************************************************************/

package ccw.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ccw.CCWPlugin;

public class REPLHistoryPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public REPLHistoryPreferencePage() {
        super(GRID);
        setPreferenceStore(CCWPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.ClojureREPLHistoryPreferencePage_Description); 
    }

    public void createFieldEditors() {

    	addField(new IntegerFieldEditor(
    			ccw.preferences.PreferenceConstants.REPL_HISTORY_MAX_SIZE, 
    			Messages.REPLHistoryPreferencePage_max_size, 
    			getFieldEditorParent()));
    	addField(new IntegerFieldEditor(
    			ccw.preferences.PreferenceConstants.REPL_HISTORY_PERSIST_SCHEDULE, 
    			Messages.REPLHistoryPreferencePage_persist_schedule, 
    			getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }

}
