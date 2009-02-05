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

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import clojuredev.debug.ClojureClient;
import clojuredev.launching.LaunchUtils;
import clojuredev.outline.NamespaceBrowser;

public class ConsolePageParticipant implements IConsolePageParticipant {
	private TextConsole console;
	private ClojureClient clojureClient;

	public void init(IPageBookViewPage page, IConsole console) {
		assert org.eclipse.debug.ui.console.IConsole.class.isInstance(console);
		assert TextConsole.class.isInstance(console);

		this.console = (TextConsole) console;
		
		org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
		if (processConsole.getProcess().getLaunch().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN) == null) {
		} else {
			int clojureVMPort = Integer.valueOf(processConsole.getProcess().getLaunch().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN));
			clojureClient = new ClojureClient(clojureVMPort);
			addPatternMatchListener(this.console);
		}
	}

	public void activated() {
		if (clojureClient != null) {
			NamespaceBrowser.setClojureClient(clojureClient);
		}
		System.out.println("activated");
	}
	
	public void deactivated() {
//		ContentOutline.setClojureClient(null);
//		System.out.println("deactivated");
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
