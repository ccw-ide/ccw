package clojuredev.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import clojuredev.ClojuredevPlugin;
import clojuredev.debug.ClojureClient;
import clojuredev.debug.IClojureClientProvider;
import clojuredev.launching.LaunchUtils;
import clojuredev.outline.ClojureNSOutlinePage;

public class ConsolePageParticipant implements IConsolePageParticipant {
	private ClojureNSOutlinePage contentOutlinePage;
	private TextConsole console;
	private boolean associatedWithClojureVM = false;
	private int clojureVMPort = -1;

	public void activated() {
		if (contentOutlinePage != null) {
			contentOutlinePage.refresh();
		}
	}

	public void deactivated() {
	}

	public void dispose() {
	}

	public void init(IPageBookViewPage page, IConsole console) {
		assert org.eclipse.debug.ui.console.IConsole.class.isInstance(console);
		assert TextConsole.class.isInstance(console);
		
		this.console = (TextConsole) console;
		
		org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
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
			if (adapter == ClojureNSOutlinePage.class) {
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
		console.addPatternMatchListener(new IPatternMatchListener() {
            public int getCompilerFlags() {
                return 0;
            }
            public String getLineQualifier() {
                return null;
            }

            public String getPattern() {
                return ".+\n";
            }

            public void connect(TextConsole console) {
                // Nothing
            }

            public void disconnect() {
                // Nothing
            }

            public void matchFound(PatternMatchEvent event) {
                if (contentOutlinePage != null) {
                    contentOutlinePage.refresh();
                }
            }
		});
	}
}
