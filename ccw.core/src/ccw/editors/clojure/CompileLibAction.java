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
package ccw.editors.clojure;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

public class CompileLibAction extends Action {

	public final static String ID = "CompileLibAction"; //$NON-NLS-1$

	private final ClojureEditor editor;

	public CompileLibAction(ClojureEditor editor) {
		super(ClojureEditorMessages.CompileLibAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		String title = "File Compiler and loader";
		String message = "The editor has pending changes. Clicking OK will save the changes and compile+load the file.";
		if (!EvaluateTextUtil.canProceed(editor, title, message))
			return;

		compileLoadFile();
	}

	protected final void compileLoadFile() {
		IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editorFile == null)
			return;

		String lib = editor.findDeclaringNamespace();

		if (lib == null || lib.trim().equals("")) {
			String title = "Compilation impossible";
			String message = "The file " + editorFile.getName() + " cannot be compiled because it does not declare a namespace.";
			MessageDialog.openError(editor.getSite().getShell(), title, message);
			return;
		}


		EvaluateTextUtil.evaluateText(editor.getCorrespondingREPL(), compileLibCommand(lib), true);
		// TODO: send the compile via the server to synchronize with the compilation end before refreshing the project
		try {
			editorFile.getProject().getFolder("classes").refreshLocal(IFolder.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static String compileLibCommand (String libName) {
		return "(ccw.debug.serverrepl/with-exception-serialization (clojure.core/binding [clojure.core/*compile-path* \"classes\"] (clojure.core/compile '" + libName + ")))";
	}
}
