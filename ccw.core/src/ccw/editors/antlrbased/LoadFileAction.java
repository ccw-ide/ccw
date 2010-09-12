/*******************************************************************************
 * Copyright (c) 2009 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package ccw.editors.antlrbased;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IOConsole;

import ccw.debug.ClojureClient;

public class LoadFileAction extends Action {

	public final static String ID = "LoadFileAction"; //$NON-NLS-1$

	private final AntlrBasedClojureEditor editor;

	public LoadFileAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.LoadFileAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		String title = "File Loader";
		String message = "The editor has pending changes. Clicking OK will save the changes and load the file.";
		if (!EvaluateTextUtil.canProceed(editor, title, message))
			return;

		loadFile();
	}
	
	protected final void loadFile() {
		IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editorFile == null)
			return;
		
		String absoluteFilePath = editorFile.getLocation().toOSString();
		String text = "(clojure.core/load-file \"" + absoluteFilePath.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\\\"") + "\")";

		IOConsole console = ClojureClient.findActiveReplConsole(false, editor.getProject(), false);
		boolean mustStartNew = (console == null);
		if (mustStartNew) {
			console = ClojureClient.findActiveReplConsole(true, editor.getProject(), false);
		}
		EvaluateTextUtil.evaluateText(console, text, true);
		if (mustStartNew && editor.getDeclaringNamespace() != null) {
			EvaluateTextUtil.evaluateText(console, "(clojure.core/in-ns '" + editor.getDeclaringNamespace() + ")", true);
		}
	}

}
