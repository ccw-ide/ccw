package clojuredev.debug;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import clojure.lang.RT;
import clojure.lang.Var;
import clojuredev.ClojuredevPlugin;

public class ClojureClient {
	private final Var invokeStr;
	private final Var starPort;
	private final int port;
	
	public ClojureClient(int port) {
		this.port = port;
		invokeStr = RT.var("clojuredev.debug.clientrepl", "invoke-str");
		starPort = RT.var("clojuredev.debug.clientrepl", "*default-repl-port*");	
	}

	public Object invoke(String string) {
		try {
	        Var.pushThreadBindings(RT.map(starPort, port));
	 
	        final Object result = invokeStr.invoke(string + "\n");
//	        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//				public void run() {
//			        MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "remote call!", result.toString());
//				}
//	        });
			return result;
		} catch (final Exception e) {
	        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
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
}
