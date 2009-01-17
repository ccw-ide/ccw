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

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import util.DisplayUtil;
import util.IOUtils;
import clojuredev.debug.ClojureClient;

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
		final String text = editor.getCurrentOrNextTopLevelSExpression();
		if (text == null)
			return;

		IOConsole console = ClojureClient.findActiveReplConsole();
		if (console == null)
			return;
		
		IOConsoleOutputStream os = null;
		IOConsoleInputStream is = console.getInputStream();
		try {
			os = console.newOutputStream();
			os.setColor(is.getColor());
			os.setFontStyle(SWT.ITALIC);
			os.write("\nCode sent by editor for evaluation:\n" + text + "\n");
			is.appendData(text + "\n");
			return;
		} catch (IOException e) {
			DisplayUtil.syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							"Expression evaluation", 
							"Unable to write to active REPL.\nThe following expression has not been launched:\n\n" + text);
				}
			});
		} finally {
			IOUtils.safeClose(os);
		}
	}
}
