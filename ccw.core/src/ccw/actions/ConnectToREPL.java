package ccw.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ccw.repl.REPLView;

public class ConnectToREPL implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            return REPLView.connect();
        } catch (Exception e) {
            throw new ExecutionException("Could not connect to repl", e);
        }
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isHandled() {
        return true;
    }

    public void removeHandlerListener(IHandlerListener handlerListener) {
    }

    public void addHandlerListener(IHandlerListener handlerListener) {
    }

    public void dispose() {
    }
}
