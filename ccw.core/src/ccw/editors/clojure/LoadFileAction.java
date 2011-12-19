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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.ClojureProject;
import ccw.launching.ClojureLaunchShortcut;
import ccw.repl.Actions;
import ccw.repl.REPLView;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.osgi.ClojureOSGi;

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


	private final ClojureEditor editor;

	public LoadFileAction(ClojureEditor editor) {
		super(ClojureEditorMessages.LoadFileAction_label);
		Assert.isNotNull(editor);
		this.editor= editor;
		setEnabled(true);
	}

	public void run() {
        IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
        if (editorFile == null) {
        	editor.setStatusLineErrorMessage("Unable to create a Clojure Application for this editor's content");
        	return;
        }

        ClojureProject proj = ClojureCore.getClojureProject(editor.getProject());
        String sourcePath = null;
        String filePath = editorFile.getLocation().toOSString();
        if (proj != null) {
            for (IFolder f : proj.sourceFolders()) {
                if (f.getProjectRelativePath().isPrefixOf(editorFile.getProjectRelativePath())) {
                    sourcePath = editorFile.getProjectRelativePath().makeRelativeTo(f.getProjectRelativePath()).toOSString();
                    break;
                }
            }
        }
        if (sourcePath == null) sourcePath = filePath;

        REPLView repl = REPLView.activeREPL.get();
        if (repl != null && !repl.isDisposed()) {
            EvaluateTextUtil.evaluateText(repl, ";; Loading file " + editorFile.getProjectRelativePath().toOSString(), true);
            try {
                EvaluateTextUtil.evaluateText(repl, (String)loadFileCommand.invoke(editor.getDocument().get(), sourcePath, editorFile.getName()), false);
                Actions.ShowActiveREPL.execute(false);
            } catch (Exception e) {
                CCWPlugin.logError("Could not load file " + filePath, e);
            }
        } else {
    		// Start a new one
          	new ClojureLaunchShortcut().launch(new StructuredSelection(editorFile), ILaunchManager.RUN_MODE);
        }
	}
}
