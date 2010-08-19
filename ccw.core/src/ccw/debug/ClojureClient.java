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
package ccw.debug;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;

import ccw.CCWPlugin;
import ccw.launching.ClojureLaunchShortcut;
import ccw.launching.LaunchUtils;
import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureClient {
	private static final Var remoteLoad;
	private static final Var remoteLoadRead;
	private static final Var loadString;
//	private static final Var localLoadRead;
	private static final Var starPort;
	private final int port;
	
	static {
		remoteLoad = RT.var("ccw.debug.clientrepl", "remote-load");
		remoteLoadRead = RT.var("ccw.debug.clientrepl", "remote-load-read");
		loadString = RT.var("clojure.core", "load-string");
//		localLoadRead = RT.var("clojure.core", "local-load-read");
		starPort = RT.var("ccw.debug.clientrepl", "*default-repl-port*");
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
			CCWPlugin.logError("clojure remote call exception", e);
		 	return null;
		} finally {
			Var.popThreadBindings();
		}
	}
	
	
	private static Object invokeLocalClojureVarWith(Var varToInvoke, String code) {
		try {
	        return varToInvoke.invoke(code);
		} catch (final Exception e) {
			CCWPlugin.logError("following clojure code thrown an exception:'" + code + "'", e);
		 	return null;
		}
	}
	
	/**
	 * Invoke <code>${ns}/${name}</code> clojure callable, requiring the namespace
	 * first.
	 */
	public static Object invoke(String ns, String name, Object... args) throws Exception {
		loadString("(clojure.core/require '" + ns + ")");
		
		Var var = RT.var(ns, name);
		switch (args.length) {
		case 0:
			return var.invoke();
		case 1:
			return var.invoke(args[0]);
		case 2:
			return var.invoke(args[0], args[1]);
		case 3:
			return var.invoke(args[0], args[1], args[2]);
		case 4:
			return var.invoke(args[0], args[1], args[2], args[3]);
		case 5:
			return var.invoke(args[0], args[1], args[2], args[3], args[4]);
		case 6:
			return var.invoke(args[0], args[1], args[2], args[3], args[4], args[5]);
	    default:
	    	throw new RuntimeException("ClojureClient.invoke() does not yet handle that much arguments");
		}
	}
	
    public static ClojureClient newClientForActiveRepl() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                for (IViewReference r : page.getViewReferences()) {
                    IViewPart v = r.getView(false);
                    if (IConsoleView.class.isInstance(v)) {
                        ClojureClient clojure = getClojureClientAndActivateRepl(page, v);
                        if (clojure != null) {
                            return clojure;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ClojureClient getClojureClientAndActivateRepl(IWorkbenchPage page, IViewPart v) {
        IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (IConsole console : consoles) {
            if (console instanceof org.eclipse.debug.ui.console.IConsole) {
                org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
                int port = LaunchUtils.getLaunchServerReplPort(processConsole.getProcess().getLaunch());
                if (port != -1) {
                    if (!page.isPartVisible(v)) {
                        activateReplAndShowConsole(page, v, console);
                    }
                    return new ClojureClient(port);
                }
            }
        }
        return null;
    }

    private static void activateReplAndShowConsole(IWorkbenchPage page, IViewPart v, IConsole console) {
        IConsoleView cv = (IConsoleView) v;
        page.activate(cv);
        cv.display(console);
    }

	/**
	 * @param createOneIfNoneFound If no active Repl Console is found for the project,
	 *        create one if <code>createOneIfNoneFound</code> is true, else return null.
	 * @param project null if <code>createOneIfNoneFound</code> is false, or the project
	 *        for which to start a new Clojure JVM
	 * @return
	 */
    public static IOConsole findActiveReplConsole(boolean createOneIfNoneFound, IProject project) {
    	IOConsole ioc = findActiveReplConsole();
    	if (ioc != null) {
    		System.out.println("active console found");
    		return ioc;
    	} else {
    		System.out.print("no active console found...");
    		if (!createOneIfNoneFound) {
    			System.out.println("but createOneIfNoneFound=false, thus will not try to create a new one");
    			return null;
    		} else {
	    		// Start a new one
	    		System.out.println("will start new one");
				new ClojureLaunchShortcut().launchProject(project, ILaunchManager.RUN_MODE);
				System.out.println("launchEditorPart called, launch returned");
				IOConsole console = findActiveReplConsole();
				System.out.println("console found after creation of new ILaunch");
				return console;
    		}
    	}
    }

    private static IOConsole findActiveReplConsole() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
            	IConsoleView v;
				try {
					v = (IConsoleView) page.showView("org.eclipse.ui.console.ConsoleView");
				} catch (PartInitException e) {
					e.printStackTrace(); // Improbable, or Eclipse would have been wrongly initialized
					return null;
				}
                IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
                for (IConsole console : consoles) {
                    if (console instanceof org.eclipse.debug.ui.console.IConsole) {
                        org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
                        if (!processConsole.getProcess().isTerminated()) {
	                        int port = LaunchUtils.getLaunchServerReplPort(processConsole.getProcess().getLaunch());
	                        if (port != -1) {
	                            if (!page.isPartVisible(v)) {
	                                activateReplAndShowConsole(page, v, console);
	                            }
	                            assert IOConsole.class.isInstance(processConsole);
	                            return (IOConsole) processConsole;
	                        }
                        }
                    }
                }
            }
        }
        return null;
    }
}
