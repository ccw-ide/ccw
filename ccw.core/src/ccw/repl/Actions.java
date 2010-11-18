package ccw.repl;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.console.ConsolePlugin;

import ccw.editors.antlrbased.EvaluateTextUtil;

public class Actions {
    private Actions () {}
    
    public static class Connect extends AbstractHandler {
        public Object execute (ExecutionEvent event) throws ExecutionException {
            try {
                return REPLView.connect();
            } catch (Exception e) {
                throw new ExecutionException("Could not connect to repl", e);
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
    
    public static class ClearLog extends REPLViewAction {        
        public void run (IAction action) {
            repl.logPanel.setText("");
        }
    }
    
    public static class Reconnect extends REPLViewAction {
        public void run (IAction action) {
            try {
                repl.reconnect();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static class ShowConsole extends REPLViewAction {
        public void run (IAction action) {
            ConsolePlugin.getDefault().getConsoleManager().showConsoleView(repl.getConsole());
        }
        public boolean isEnabled () {
            return repl.getConsole() != null;
        }
    }
    
    public static class PrintErrorDetail extends REPLViewAction {
        public void run (IAction action) {
            repl.printErrorDetail();
        }
    }
    
    private static abstract class REPLViewAction extends AbstractHandler implements IViewActionDelegate {
        protected REPLView repl;

        public void selectionChanged (IAction action, ISelection selection) {}

        public void init (IViewPart view) {
            repl = (REPLView)view;
        }

        public Object execute(ExecutionEvent event) throws ExecutionException {
            return null;
        }
    }
}
