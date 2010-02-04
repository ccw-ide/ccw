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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;

public class CompileLibAction extends EvaluateTextAction {

	public final static String ID = "CompileLibAction"; //$NON-NLS-1$

	private final AntlrBasedClojureEditor editor;

	public CompileLibAction(AntlrBasedClojureEditor editor) {
		super(ClojureEditorMessages.CompileLibAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
		String title = "File Compiler and loader";
		String message = "The editor has pending changes. Clicking OK will save the changes and compile+load the file.";
		if (!canProceed(editor, title, message))
			return;
		
		compileLoadFile();
	}
	
	protected final void compileLoadFile() {
		IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editorFile == null)
			return;
		
		String lib = editor.getDeclaringNamespace();
		
		if (lib == null || lib.trim().equals("")) {
			String title = "Compilation impossible";
			String message = "The file " + editorFile.getName() + " cannot be compiled because it does not declare a namespace.";
			MessageDialog.openError(editor.getSite().getShell(), title, message);
			return;
		}
		
		evaluateText(compileLibCommand(lib));
		// TODO: send the compile via the server to synchronize with the compilation end before refreshing the project
		try {
			editorFile.getProject().getFolder("classes").refreshLocal(IFolder.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String compileLibCommand(String libName) {
		return "(clojure.core/binding [clojure.core/*compile-path* \"classes\"] (clojure.core/compile '" + libName + "))";
	}
}
