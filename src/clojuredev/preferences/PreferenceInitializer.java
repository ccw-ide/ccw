/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Stephan Muehlstrasser - moved into preferences package
 *******************************************************************************/
package clojuredev.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import clojuredev.ClojuredevPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
	    IPreferenceStore store = ClojuredevPlugin.getDefault().getPreferenceStore();
	    store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
	    store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, 
                StringConverter.asString(new RGB(150, 150, 150)));
	    store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 2);
	    store.setDefault(clojuredev.preferences.PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP, true);
	}
}
