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
package clojuredev.editors.antlrbased;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import clojuredev.console.ClojureConsole;

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
		String text = editor.getCurrentOrNextTopLevelSExpression();
		if (text == null)
			return;

        ClojureConsole clojureCons = null;
        for (IConsole console : ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles()) {
            if (console instanceof ClojureConsole) {
                clojureCons = (ClojureConsole) console;
            }
        }

        if (clojureCons != null)
        	clojureCons.evalString(text);
	}
}
