/*******************************************************************************
 * Copyright (c) 2009 Laurent PETIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Gorsal - patch for correcting namespace browser initialisation issue
 *******************************************************************************/
package ccw.console;

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

import ccw.ClojureCore;
import ccw.CCWPlugin;
import ccw.debug.ClojureClient;
import ccw.editors.antlrbased.EvaluateTextAction;
import ccw.launching.LaunchUtils;
import ccw.outline.NamespaceBrowser;
import ccw.preferences.PreferenceConstants;

public class ConsolePageParticipant implements IConsolePageParticipant {
	private IOConsole console;
	private ClojureClient clojureClient;

	private Thread initializationThread;
	public void init(IPageBookViewPage page, IConsole console) {
		assert org.eclipse.debug.ui.console.IConsole.class.isInstance(console);
		assert TextConsole.class.isInstance(console);

		this.console = (IOConsole) console;
		this.initializationThread = new Thread(new Runnable () {
			public void run() {
				initNamespaceBrowser();
			}
		});
		initializationThread.start();
	}

	public void activated() {
		// Nothing to do
	}
	
	private synchronized void initNamespaceBrowser() {
		if (clojureClient == null) {
			bindConsoleToClojureEnvironment();
		}
		if (clojureClient != null) {
			System.out.println("activated");
			addPatternMatchListener(this.console);
			if (CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SWITCH_TO_NS_ON_REPL_STARTUP)) {
				try {
					org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
					List<IFile> files = LaunchUtils.getFilesToLaunchList(processConsole.getProcess().getLaunch().getLaunchConfiguration());
					if (files.size() > 0) {
						String namespace = ClojureCore.getDeclaredNamespace(files.get(0));
						if (namespace != null) {
							EvaluateTextAction.evaluateText(this.console, "(in-ns '" + namespace + ")", false); 
						}
					}
				} catch (CoreException e) {
					CCWPlugin.logError("error while trying to guess the ns to which make the REPL console switch", e);
				}
			}
			NamespaceBrowser.setClojureClient(clojureClient);
		}
	}
	
	private void bindConsoleToClojureEnvironment() {
		org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
		boolean stop = false;
		int selfTimeout = 60000; // 60 seconds
		while (!stop && selfTimeout > 0) {
			if (Thread.interrupted()) {
				stop = true;
			} else {
				int clojureVMPort = LaunchUtils.getLaunchServerReplPort(processConsole.getProcess().getLaunch());
				if (clojureVMPort != -1) {
					clojureClient = new ClojureClient(clojureVMPort);
					stop = true;
				} else {
					try {
						Thread.sleep(100);
						selfTimeout -= 100;
					} catch (InterruptedException e) {
						e.printStackTrace();
						stop = true;
					}
				}
			}
		}
	}
	
	public void deactivated() {
		// Nothing
	}

	public void dispose() {
		if (initializationThread.isAlive()) {
			initializationThread.interrupt();
		}
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
