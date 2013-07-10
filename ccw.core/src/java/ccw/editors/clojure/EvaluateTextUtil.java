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
package ccw.editors.clojure;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import ccw.repl.REPLView;
import ccw.util.DisplayUtil;

final public class EvaluateTextUtil {
	private EvaluateTextUtil() {
		// Not intended to be subclassed
	}
	
	public static final void evaluateText(REPLView console, final String text, boolean repeatLastREPLEvalIfActive) {
	    if (console == null || console.isDisposed()) {
            DisplayUtil.syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
                            "Expression evaluation", 
                            "Please activate a running REPL session before attempting to evaluate code.");
                }
            });
        } else {
            console.evalExpression(text, false, false, repeatLastREPLEvalIfActive);
        }
	}
	
	/**
	 * Verifies if all is OK. Currently, that just means that if the file is not
	 * saved, we ask the user for the permission to save the file and continue
	 * @return true if it is ok to do the action, false if the action is cancelled
	 */
	public static boolean canProceed(IEditorPart editor, String title, String message) {
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
