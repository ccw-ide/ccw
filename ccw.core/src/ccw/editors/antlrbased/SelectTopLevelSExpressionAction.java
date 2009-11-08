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
package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;

public class SelectTopLevelSExpressionAction extends Action {

	public final static String ID = "SelectTopLevelSExpressionAction"; //$NON-NLS-1$

	private final AntlrBasedClojureEditor editor;

	public SelectTopLevelSExpressionAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.SelectTopLevelSExpressionAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		editor.selectTopLevelSExpression();
	}
}
