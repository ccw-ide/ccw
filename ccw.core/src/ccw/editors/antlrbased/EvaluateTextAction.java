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

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import ccw.debug.ClojureClient;
import ccw.util.DisplayUtil;
import ccw.util.IOUtils;

abstract public class EvaluateTextAction extends Action {
	protected final IProject project;
	
	public EvaluateTextAction(String name, IProject project) {
		super(name);
		
		assert project != null;
		
		this.project = project;
	}
	
	public final void evaluateText(final String text) {
		EvaluateTextAction.evaluateText(text, project);
	}
	public static final void evaluateText(final String text, IProject project) {
		System.out.println("before findActiveReplConsole()");
		IOConsole console = ClojureClient.findActiveReplConsole(true, project, false);
		System.out.println("after findActiveReplConsole()");
		evaluateText(console, text);
	}
	
	public static final void evaluateText(IOConsole console, final String text) {
		evaluateText(console, text, true);
	}
	
	public static final void evaluateText(IOConsole console, final String text, boolean verboseMode) {
		if (text == null)
			return;

		if (console == null)
			return;
		
		IOConsoleOutputStream os = null;
		IOConsoleInputStream is = console.getInputStream();
		try {
			if (verboseMode) {
				os = console.newOutputStream();
				os.setColor(is.getColor());
				os.setFontStyle(SWT.ITALIC);
				os.write("\nCode sent for evaluation:\n" + text + "\n");
			}
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
	
	/**
	 * Verifies if all is OK. Currently, that just means that if the file is not
	 * saved, we ask the user for the permission to save the file and continue
	 * @return true if it is ok to do the action, false if the action is cancelled
	 */
	protected boolean canProceed(IEditorPart editor, String title, String message) {
		if (editor.isDirty()) {
			boolean saveAndCompileLoad = MessageDialog.openConfirm(editor.getSite().getShell(), title, message);
			if (saveAndCompileLoad) {
				editor.getSite().getWorkbenchWindow().getActivePage().saveEditor(editor, false);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

}
