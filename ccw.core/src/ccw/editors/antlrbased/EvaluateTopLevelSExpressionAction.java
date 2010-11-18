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
import org.eclipse.ui.console.IOConsole;

import ccw.debug.ClojureClient;
import ccw.repl.Actions;
import ccw.repl.REPLView;

public class EvaluateTopLevelSExpressionAction extends Action {

	public final static String ID = "EvaluateTopLevelSExpressionAction"; //$NON-NLS-1$

	private final AntlrBasedClojureEditor editor;

	public EvaluateTopLevelSExpressionAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.EvaluateTopLevelSExpressionAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		String selectedText = editor.getSelectedText();
		
		if (selectedText==null || selectedText.trim().equals("")) {
			selectedText = editor.getCurrentOrNextTopLevelSExpression();
		}

		REPLView repl = REPLView.activeREPL.get();
		if (repl != null && !repl.isDisposed()) {
			EvaluateTextUtil.evaluateText(repl, selectedText, true);
			Actions.ShowActiveREPL.execute(false);
		}
	}
}
