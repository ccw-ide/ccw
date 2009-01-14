package clojuredev.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.debug.IClojureClientProvider;
import clojuredev.launching.LaunchUtils;
import clojuredev.outline.ClojureNSOutlinePage;

public class ConsolePageParticipant implements IConsolePageParticipant {
	private IContentOutlinePage contentOutlinePage;
	private boolean associatedWithClojureVM = false;
	private int clojureVMPort = -1;

	public void activated() {
	}

	public void deactivated() {
	}

	public void dispose() {
		if (contentOutlinePage != null) {
			contentOutlinePage.dispose();
			contentOutlinePage = null;
		}
	}

	public void init(IPageBookViewPage page, IConsole console) {
		assert ProcessConsole.class.isInstance(console);
		ProcessConsole processConsole = (ProcessConsole) console;
		try {
			if (processConsole.getProcess().getLaunch().getLaunchConfiguration().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, -1) == -1) {
				this.associatedWithClojureVM = false;
			} else {
				this.associatedWithClojureVM = true;
				clojureVMPort = Integer.valueOf(processConsole.getProcess().getLaunch().getLaunchConfiguration().getAttribute(LaunchUtils.ATTR_CLOJURE_SERVER_LISTEN, -1)); 
			}
		} catch (CoreException e) {
			ClojuredevPlugin.logError(e);
			this.associatedWithClojureVM = false;
		}

	}

	public Object getAdapter(Class adapter) {
		if (associatedWithClojureVM) {
			if (adapter == IContentOutlinePage.class) {
				if (contentOutlinePage == null) {
					createContentOutlinePage();
				}
				return contentOutlinePage;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void createContentOutlinePage() {
		contentOutlinePage = new ClojureNSOutlinePage(new IClojureClientProvider() {
			private final ClojureClient cc;
			{
				cc = new ClojureClient(clojureVMPort);
			}
			public ClojureClient getClojureClient() {
				return cc;
			}
		});
	}

}
