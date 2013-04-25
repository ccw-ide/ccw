package ccw.repl;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.handlers.HandlerUtil;

import ccw.CCWPlugin;

public class Actions {
    private Actions () {}
    
    public static class Connect extends AbstractHandler {
        public Object execute (ExecutionEvent event) throws ExecutionException {
            try {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                ConnectDialog dlg = new ConnectDialog(window.getShell(), CCWPlugin.getDefault().getDialogSettings());
                
                REPLView repl = null;
                if (dlg.open() == ConnectDialog.OK) {
                    repl = REPLView.connect(dlg.getURL(), true);
                }
                
                return repl;
            } catch (Exception e) {
                throw new ExecutionException("Could not connect to repl", e);
            }
        }
    }
    
    public static class ConnectToEclipseNREPL extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
            try {
                return REPLView.connect("nrepl://127.0.0.1:" + CCWPlugin.getDefault().getREPLServerPort(), true);
            } catch (Exception e) {
                throw new ExecutionException("Could not connect to Eclipse's internal nrepl server", e);
            }
		}
    }
    
    public static class ShowActiveREPL extends AbstractHandler {
        public static boolean execute (boolean activate) {
            REPLView active = REPLView.activeREPL.get();
            if (active != null) {
                for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                    for (IWorkbenchPage p : window.getPages()) {
                        for (IViewReference ref : p.getViewReferences()) {
                            if (ref.getPart(false) == active) {
                                if (activate) {
                                    p.activate(active);
                                } else {
                                    p.bringToTop(active);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        
        public Object execute (ExecutionEvent event) throws ExecutionException {
            if (execute(true)) return null;
            
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                    "No Active REPL",
                    "No REPL is active. Click in an existing REPL to make it the active target, or open a new REPL.");
            return null;
        }
    }
    
    public static class ShowConsoleHandler extends AbstractREPLViewHandler {
    	public void doExecute(ExecutionEvent event, REPLView repl) throws ExecutionException {
            ConsolePlugin.getDefault().getConsoleManager().showConsoleView(repl.getConsole());
        }
    	// TODO: see if this ever worked ?
        @Override
        public void setEnabled(Object evaluationContext) {
        	if (evaluationContext != null && (evaluationContext instanceof IEvaluationContext)) {
        		Object part = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_PART_NAME);
        		if (part != null && (part instanceof REPLView)) {
        			REPLView repl = (REPLView) part;
        			setBaseEnabled(repl.getConsole() != null);
        		}
        	}
        }
    }

    public static class ClearLogHandler extends AbstractREPLViewHandler {        
    	public void doExecute(ExecutionEvent event, REPLView repl) throws ExecutionException {
            repl.logPanel.setText("");
        }
    }
    
    public static class ReconnectHandler extends AbstractREPLViewHandler {
    	public void doExecute(ExecutionEvent event, REPLView repl) throws ExecutionException {
            try {
                repl.reconnect();
            } catch (Exception e) {
            	final String MSG = "Unexpected exception occured while trying to reconnect REPL view to clojure server"; 
            	ErrorDialog.openError(
            			HandlerUtil.getActiveShell(event),
            			"Reconnection Error",
            			MSG,
            			CCWPlugin.createErrorStatus(MSG, e));
            }
        }
    }
    
    public static class NewSessionHandler extends AbstractREPLViewHandler {
        public void doExecute(ExecutionEvent event, REPLView repl) throws ExecutionException {
            try {
                REPLView.connect(repl.getConnection().url, true);
            } catch (Exception e) {
                final String msg = "Unexpected exception occured while trying to connect REPL view to clojure server"; 
                ErrorDialog.openError(
                        HandlerUtil.getActiveShell(event),
                        "Connection Error",
                        msg,
                        CCWPlugin.createErrorStatus(msg, e));
            }
        }
    }
    
    public static class PrintErrorHandler extends AbstractREPLViewHandler {
		public void doExecute(ExecutionEvent event, REPLView repl) throws ExecutionException {
			repl.printErrorDetail();
		}
    }
    
    public static class InterruptHandler extends AbstractREPLViewHandler {
        public void doExecute(ExecutionEvent event, REPLView repl) throws ExecutionException {
            repl.sendInterrupt();
        }
    }
    
    private static abstract class AbstractREPLViewHandler extends AbstractHandler {
		public final Object execute(ExecutionEvent event) throws ExecutionException {
			IWorkbenchPart part = HandlerUtil.getActivePart(event);
			if (! (part instanceof REPLView)) {
				return null;
			}
			doExecute(event, (REPLView) part);
			return null;
		}
		protected abstract void doExecute(ExecutionEvent event, REPLView part) throws ExecutionException;
    }
    
}
