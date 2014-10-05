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
import ccw.editors.clojure.EvaluateTextUtil;
import ccw.repl.Actions;
import ccw.repl.REPLView;

public class EvaluateTopLevelSExpressionAction extends Action {

	public final static String ID = "EvaluateTopLevelSExpressionAction"; //$NON-NLS-1$

	private final ClojureEditor editor;

	public EvaluateTopLevelSExpressionAction(ClojureEditor editor) {
		super(ClojureEditorMessages.EvaluateTopLevelSExpressionAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		String selectedText = editor.getSelectedText();
		
		if (selectedText==null || selectedText.trim().equals("")) {
			selectedText = editor.getCurrentTopLevelSExpression();
		}

		REPLView repl = REPLView.activeREPL.get();
		if (repl != null && !repl.isDisposed()) {
			String textToEvaluate = selectedText;
			String editorNamespace = editor.findDeclaringNamespace();
			String replNamespace = repl.getCurrentNamespace();
			if (editorNamespace != null && !editorNamespace.equals(replNamespace)) {
				textToEvaluate = "(in-ns '" + editorNamespace + ")\n" + textToEvaluate + "\n(in-ns '" + replNamespace + ")";
			}
			
			EvaluateTextUtil.evaluateText(repl, textToEvaluate, true);
			Actions.ShowActiveREPL.execute(false);
		}
	}
}
