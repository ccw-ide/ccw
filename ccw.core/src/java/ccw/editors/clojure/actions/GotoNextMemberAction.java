/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.editors.clojure.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;

import ccw.editors.clojure.ClojureEditor;
import ccw.editors.clojure.ClojureEditorMessages;

public class GotoNextMemberAction extends Action {

	public final static String ID = "ClojureGotoNextMember"; //$NON-NLS-1$

	private final ClojureEditor editor;

	public GotoNextMemberAction(ClojureEditor editor) {
		super(ClojureEditorMessages.GotoNextMemberAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		editor.gotoEndOfMember();
	}
}
