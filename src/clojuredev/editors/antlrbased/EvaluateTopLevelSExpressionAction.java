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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.launching.LaunchUtils;

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

	    IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null) {
	        IWorkbenchPage page= window.getActivePage();
	        if (page != null) {
	        	for (IViewReference r: page.getViewReferences()) {
	        		IViewPart v = r.getView(false);
	        		if (IConsoleView.class.isInstance(v) && page.isPartVisible(v)) {
	        			IConsoleView cv = (IConsoleView) v;
	        			if (org.eclipse.debug.ui.console.IConsole.class.isInstance(cv.getConsole())) {
	        				org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) cv.getConsole();
							try {
								int port = Integer.valueOf(processConsole.getProcess().getLaunch().getLaunchConfiguration().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, -1));
	    						if (port != -1) {
	    							ClojureClient clojureClient = ClojureClient.newClientForActiveRepl();
	    							if (clojureClient != null) {
	    							 	 IOConsoleOutputStream os = null;
	    							 	 IOConsoleInputStream is = ((IOConsole) processConsole).getInputStream();
		    							try {
		    								os = ((IOConsole) processConsole).newOutputStream();
		    								os.setColor(is.getColor());
		    								os.setFontStyle(SWT.ITALIC);
		    								os.write("\nCode sent by editor for evaluation:\n" + text + "\n");
//		    								is.setFontStyle(SWT.BOLD);
		    								is.appendData(text + "\n");
//		    								is.setFontStyle(SWT.BOLD);
		    								return;
										} catch (IOException e) {
											Display display = PlatformUI.getWorkbench().getDisplay();
											display.asyncExec(new Runnable() {
												public void run() {
													MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
															"Expression evaluation", 
															"Unable to write to active REPL.\nThe following expression has not been launched:\n\n" + text);
												}
											});
										} finally {
											if (os != null)
												try {
													os.close();
												} catch (IOException e) {
													// Nothing to do ?
												}
										}
	    							} else {
	    								Display display = PlatformUI.getWorkbench().getDisplay();
	    								display.asyncExec(new Runnable() {
	    									public void run() {
	    										MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
	    												"Expression evaluation", 
	    												"Unable to locate an active REPL.\nThe following expression has not been launched:\n\n" + text);
	    									}
	    								});
	    							}
	    							return;
	    						}
							} catch (CoreException e) {
								ClojuredevPlugin.logError("while searching active console port, unexpected error. Continue with other consoles", e);
							}
	        			}
	        		}
	        	}
	        }
	    }
	}
}
