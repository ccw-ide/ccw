package clojuredev.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;

import clojure.lang.RT;
import clojure.lang.Var;
import clojuredev.ClojuredevPlugin;
import clojuredev.launching.LaunchUtils;

public class ClojureClient {
	private static final Var remoteLoad;
	private static final Var remoteLoadRead;
	private static final Var localLoad;
	private static final Var localLoadRead;
	private static final Var starPort;
	private final int port;
	
	static {
		remoteLoad = RT.var("clojuredev.debug.clientrepl", "remote-load");
		remoteLoadRead = RT.var("clojuredev.debug.clientrepl", "remote-load-read");
		localLoad = RT.var("clojure.core", "local-load");
		localLoadRead = RT.var("clojure.core", "local-load-read");
		starPort = RT.var("clojuredev.debug.clientrepl", "*default-repl-port*");
	}
	
	public ClojureClient(int port) {
		this.port = port;
	}

	public String remoteLoad(String remoteCode) {
		return (String) invokeClojureVarWith(remoteLoad, remoteCode);
	}
	
	public Object remoteLoadRead(String remoteCode) {
		return invokeClojureVarWith(remoteLoadRead, remoteCode);
	}
	
	public String localLoad(String localCode) {
		return (String) invokeClojureVarWith(localLoad, localCode);
	}
	
	public Object localLoadRead(String localCode) {
		return invokeClojureVarWith(localLoadRead, localCode);
	}
	
	private Object invokeClojureVarWith(Var varToInvoke, String code) {
		try {
	        Var.pushThreadBindings(RT.map(starPort, port));
	        return varToInvoke.invoke(code);
		} catch (final Exception e) {
	        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
			        MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "remote call exception!", e.getMessage());
				}
	        });
		 	ClojuredevPlugin.logError("clojure invocation error", e);
		 	return null;
		} finally {
			Var.popThreadBindings();
		}
	}
	
	// TODO move this in a more central place ?
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
							try {
								int port = Integer.valueOf(processConsole.getProcess().getLaunch().getLaunchConfiguration().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, -1));
	    						if (port != -1) {
	    							return new ClojureClient(port);
	    						}
							} catch (CoreException e) {
								ClojuredevPlugin.logError("while searching active console port, unexpected error. Continue with other consoles", e);
							}
            			}
            		}
            	}
            }
        }
        return null;
	}
}
