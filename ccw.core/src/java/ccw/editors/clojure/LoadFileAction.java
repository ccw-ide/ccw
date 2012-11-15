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

import static ccw.preferences.PreferenceConstants.isReplExplicitLoggingMode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.ClojureProject;
import ccw.TraceOptions;
import ccw.launching.ClojureLaunchShortcut;
import ccw.nature.ClojureNaturePropertyTest;
import ccw.repl.Actions;
import ccw.repl.REPLView;
import ccw.util.DisplayUtil;
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
        final IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
        if (editorFile == null) {
        	editor.setStatusLineErrorMessage("Unable to create a Clojure Application for this editor's content");
        	return;
        }
        
        if (!ClojureNaturePropertyTest.hasClojureNature(editorFile)) {
        	editor.setStatusLineErrorMessage(
        			"Cannot invoke command " 
        			+ ClojureEditorMessages.LoadFileAction_label
        			+ " because the current file does not belong to a project "
        			+ " for which Clojure support has been enabled.");
        	return;
        }
        
        ClojureProject proj = ClojureCore.getClojureProject(editor.getProject());
        String tempSourcePath = null;
        final String filePath = editorFile.getLocation().toOSString();
        if (proj != null) {
            for (IFolder f : proj.sourceFolders()) {
                if (f.getProjectRelativePath().isPrefixOf(editorFile.getProjectRelativePath())) {
                	tempSourcePath = editorFile.getProjectRelativePath().makeRelativeTo(f.getProjectRelativePath()).toOSString();
                    break;
                }
            }
        }
        if (tempSourcePath == null) tempSourcePath = filePath;
        final String sourcePath = tempSourcePath;

        final REPLView repl = REPLView.activeREPL.get();
        if (repl != null && !repl.isDisposed())  {
    		evaluateFileText(repl, editor.getDocument().get(), filePath, sourcePath, editorFile.getName());
        } else {
    		CCWPlugin.getTracer().trace(TraceOptions.LAUNCHER, "No active REPL found (",
    				(repl == null) ? "active repl is null" : "active repl is disposed", 
    				"), so launching a new one");
        	new Thread(new Runnable() {
				public void run() {
		        	final IProject project = editorFile.getProject();
		        	new ClojureLaunchShortcut().launchProject(project, ILaunchManager.RUN_MODE, false);
		        	DisplayUtil.asyncExec(new Runnable() {
		        		public void run() {
				        	REPLView repl = CCWPlugin.getDefault().getProjectREPL(project);
				        	if (repl != null && !repl.isDisposed()) {
				        		evaluateFileText(repl, editor.getDocument().get(), filePath, sourcePath, editorFile.getName());
				        		SwitchNamespaceAction.run(repl, editor, false);
				        	} else {
				        		CCWPlugin.logError("Could not start a REPL for loading file " + filePath);
				        	}
		        		}
		        	});
				}
			}).start();
        }
	}
	
	private void evaluateFileText(REPLView repl, String text, String filePath, String sourcePath, String fileName) {
        try {
            if (repl.getAvailableOperations().contains("load-file")) {
                repl.getConnection().sendSession(repl.getSessionId(),
                        "op", "load-file", "file", text,
                        "file-path", sourcePath, "file-name", fileName);
            } else {
                String loadFileText = (String)loadFileCommand.invoke(text, sourcePath, fileName);
                //if (isReplExplicitLoggingMode()) {
                EvaluateTextUtil.evaluateText(repl, ";; Loading file " + filePath, isReplExplicitLoggingMode());
                //}
                EvaluateTextUtil.evaluateText(repl, loadFileText, false);
                Actions.ShowActiveREPL.execute(false);
            }
            Actions.ShowActiveREPL.execute(false);
        } catch (Exception e) {
            CCWPlugin.logError("Could not load file " + filePath, e);
        }
	}
}
