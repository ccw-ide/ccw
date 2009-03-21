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
package clojuredev.debug;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;

import clojure.lang.RT;
import clojure.lang.Var;
import clojuredev.ClojuredevPlugin;
import clojuredev.launching.LaunchUtils;

public class ClojureClient {
	private static final Var remoteLoad;
	private static final Var remoteLoadRead;
	private static final Var loadString;
//	private static final Var localLoadRead;
	private static final Var starPort;
	private final int port;
	
	static {
		remoteLoad = RT.var("clojuredev.debug.clientrepl", "remote-load");
		remoteLoadRead = RT.var("clojuredev.debug.clientrepl", "remote-load-read");
		loadString = RT.var("clojure.core", "load-string");
//		localLoadRead = RT.var("clojure.core", "local-load-read");
		starPort = RT.var("clojuredev.debug.clientrepl", "*default-repl-port*");
	}
	
	public ClojureClient(int port) {
		this.port = port;
	}
	
	public int getPort() { return port; }

	public String remoteLoad(String remoteCode) {
		Object result = invokeClojureVarWith(remoteLoad, remoteCode);
		return (result == null) ? null : result.toString();
	}
	
	public Object remoteLoadRead(String remoteCode) {
		return invokeClojureVarWith(remoteLoadRead, remoteCode);
	}
	
	public static Object loadString(String localCode) {
		return invokeLocalClojureVarWith(loadString, localCode);
	}
	
//	public Object localLoadRead(String localCode) {
//		return invokeClojureVarWith(localLoadRead, localCode);
//	}
	
	private Object invokeClojureVarWith(Var varToInvoke, String code) {
		try {
	        Var.pushThreadBindings(RT.map(starPort, port));
	        return varToInvoke.invoke(code);
		} catch (final Exception e) {
			ClojuredevPlugin.logError("clojure remote call exception", e);
		 	return null;
		} finally {
			Var.popThreadBindings();
		}
	}
	
	
	private static Object invokeLocalClojureVarWith(Var varToInvoke, String code) {
		try {
	        return varToInvoke.invoke(code);
		} catch (final Exception e) {
			ClojuredevPlugin.logError("following clojure code thrown an exception:'" + code + "'", e);
		 	return null;
		}
	}
	
	public static ClojureClient newClientForActiveRepl() {
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
            				String portAttr = processConsole.getProcess().getLaunch().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN);
            				if (portAttr != null) {
    							Integer port = Integer.valueOf(portAttr);
        						if (port != -1) {
        							return new ClojureClient(port);
        						}
            				}
            			}
            		}
            	}
            }
        }
        return null;
	}
	
	public static IOConsole findActiveReplConsole() {
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
						    String portAttr = processConsole.getProcess().getLaunch().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN);
						    if (portAttr != null) {
								int port = Integer.valueOf(portAttr);
	    						if (port != -1) {
	    							assert IOConsole.class.isInstance(processConsole);
	    							return (IOConsole) processConsole;
	    						}
						    }
            			}
            		}
            	}
            }
        }
        return null;
	}

}
