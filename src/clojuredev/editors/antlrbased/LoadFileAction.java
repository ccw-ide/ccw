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
package clojuredev.editors.antlrbased;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;

public class LoadFileAction extends EvaluateTextAction {

	public final static String ID = "LoadFileAction"; //$NON-NLS-1$

	private final AntlrBasedClojureEditor editor;

	public LoadFileAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.LoadFileAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		if (editor.isDirty()) {
			String title = "File Loader";
			String message = "The editor has pending changes. Clicking OK will save the changes and load the file.";
			boolean saveAndLoad = MessageDialog.openConfirm(editor.getSite().getShell(), title, message);
			if (saveAndLoad) {
				editor.getSite().getWorkbenchWindow().getActivePage().saveEditor(editor, false);
				loadFile();
			} else {
				return;
			}
		} else {
			loadFile();
		}
	}
	
	protected final void loadFile() {
		IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editorFile == null)
			return;
		
		String absoluteFilePath = editorFile.getLocation().toOSString();
		String text = "(load-file \"" + absoluteFilePath.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\\\"") + "\")";

		evaluateText(text);
	}

}
