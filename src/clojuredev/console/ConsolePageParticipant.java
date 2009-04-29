/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package clojuredev.console;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import clojuredev.ClojureCore;
import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.editors.antlrbased.EvaluateTextAction;
import clojuredev.launching.LaunchUtils;
import clojuredev.outline.NamespaceBrowser;
import clojuredev.preferences.PreferenceConstants;

public class ConsolePageParticipant implements IConsolePageParticipant {
	private IOConsole console;
	private ClojureClient clojureClient;

	public void init(IPageBookViewPage page, IConsole console) {
		assert org.eclipse.debug.ui.console.IConsole.class.isInstance(console);
		assert TextConsole.class.isInstance(console);

		this.console = (IOConsole) console;
		
	}

	public void activated() {
		if (clojureClient == null) {
			bindConsoleToClojureEnvironment();
			if (clojureClient != null) {
				System.out.println("activated");
			}
		}
		if (clojureClient != null) {
			NamespaceBrowser.setClojureClient(clojureClient);
		}
	}
	
	private synchronized void bindConsoleToClojureEnvironment() {
		if (clojureClient == null) {
			org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
			int clojureVMPort = LaunchUtils.getLaunchServerReplPort(processConsole.getProcess().getLaunch());
			if (clojureVMPort != -1) {
				clojureClient = new ClojureClient(clojureVMPort);
				addPatternMatchListener(this.console);
				if (ClojuredevPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP)) {
					try {
						List<IFile> files = LaunchUtils.getFilesToLaunchList(processConsole.getProcess().getLaunch().getLaunchConfiguration());
						if (files.size() > 0) {
							String namespace = ClojureCore.getDeclaredNamespace(files.get(0));
							if (namespace != null) {
								EvaluateTextAction.evaluateText(this.console, "(in-ns '" + namespace + ")", false); 
							}
						}
					} catch (CoreException e) {
						ClojuredevPlugin.logError("error while trying to guess the ns to which make the REPL console switch", e);
					}
				}
			}
		}
	}
	
	public void deactivated() {
		// Nothing
	}

	public void dispose() {
		// Nothing
	}

	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
	
	private void addPatternMatchListener(TextConsole console) {
		console.addPatternMatchListener(new IPatternMatchListener() {
            public int getCompilerFlags() {
                return 0;
            }
            public String getLineQualifier() {
                return null;
            }

            public String getPattern() {
                return ".*\n";
            }

            public void connect(TextConsole console) {
                // Nothing
            }

            public void disconnect() {
                // Nothing
            }

            public void matchFound(PatternMatchEvent event) {
            	if (clojureClient != null) {
            		NamespaceBrowser.setClojureClient(clojureClient);
            	}
            }
		});
	}

}
