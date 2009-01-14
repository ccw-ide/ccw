package clojuredev;

import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import clojure.lang.Compiler;
import clojure.lang.RT;
import clojure.lang.Var;
import clojuredev.launching.LaunchUtils;

public class TestNamespacesHandler implements IHandler {

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			URL clientReplBundleUrl = ClojuredevPlugin.getDefault().getBundle().getResource("clojuredev/clientrepl.clj");
			URL clientReplFileUrl = FileLocator.toFileURL(clientReplBundleUrl);
			String serverRepl = clientReplFileUrl.getFile(); 

			Compiler.loadFile(serverRepl);
			 
	        Var invokeStr = RT.var("clojuredev.clientrepl", "invoke-str");
	        Var starPort = RT.var("clojuredev.clientrepl", "*default-repl-port*");
			try {
		        Var.pushThreadBindings(
					RT.map(starPort, LaunchUtils.DEFAULT_SERVER_PORT)); // FIXME MAKE DYNAMIC
		        // Get a reference to the foo function.
		 
		        final Object result = invokeStr.invoke("(+ 1 2)\n");
		        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
				        MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "remote call!", result.toString());
					}
		        });
			} finally {
				Var.popThreadBindings();
			}
			return null;
		} catch (final Exception e) {
	        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
			        MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "remote call exception!", e.getMessage());
				}
	        });
			throw new ExecutionException("clojure invocation error", e);
		}
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
