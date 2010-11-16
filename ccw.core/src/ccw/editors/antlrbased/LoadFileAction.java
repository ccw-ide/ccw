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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IOConsole;

import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.osgi.ClojureOSGi;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.ClojureProject;
import ccw.debug.ClojureClient;
import ccw.repl.REPLView;

public class LoadFileAction extends Action {

	public final static String ID = "LoadFileAction"; //$NON-NLS-1$
	
    private static Var loadFileCommand;
    static {
        try {
            ClojureOSGi.require(CCWPlugin.getDefault().getBundle().getBundleContext(), "clojure.tools.nrepl.helpers");
            loadFileCommand = Var.find(Symbol.intern("clojure.tools.nrepl.helpers/load-file-command"));
        } catch (Exception e) {
            CCWPlugin.logError("Could not initialize code loading helpers.", e);
        }
    }


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
		if (editorFile == null) return;
		
		ClojureProject proj = ClojureCore.getClojureProject(editor.getProject());
		String sourcePath = null;
		String filePath = editorFile.getLocation().toOSString();
		if (proj != null) {
		    for (IFolder f : proj.sourceFolders()) {
		        if (f.getProjectRelativePath().isPrefixOf(editorFile.getProjectRelativePath())) {
		            sourcePath = f.getLocation().toOSString();
		            break;
		        }
		    }
		}

        REPLView repl = REPLView.activeREPL.get();
        if (repl != null && !repl.isDisposed()) {
            EvaluateTextUtil.evaluateText(repl, ";; Loading file " + editorFile.getProjectRelativePath().toOSString(), true);
    		try {
                EvaluateTextUtil.evaluateText(repl, (String)loadFileCommand.invoke(new java.io.File(filePath), sourcePath), false);
            } catch (Exception e) {
                CCWPlugin.logError("Could not load file " + filePath, e);
            }
        }
	}

}
