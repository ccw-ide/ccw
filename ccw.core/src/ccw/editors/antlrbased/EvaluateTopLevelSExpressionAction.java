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

public class EvaluateTopLevelSExpressionAction extends EvaluateTextAction {

	public final static String ID = "EvaluateTopLevelSExpressionAction"; //$NON-NLS-1$

	private final AntlrBasedClojureEditor editor;

	public EvaluateTopLevelSExpressionAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.EvaluateTopLevelSExpressionAction_label, editor.getProject());
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		String selectedText = editor.getSelectedText();
		
		if (selectedText==null || selectedText.trim().equals("")) {
			selectedText = editor.getCurrentOrNextTopLevelSExpression();
		}

		evaluateText(selectedText);
	}
}
