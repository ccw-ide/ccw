package ccw.repl;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ReconnectReplAction implements IViewActionDelegate, IHandler {

    private REPLView repl;
    
    public void run (IAction action) {
        try {
            repl.reconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {}

    public void init(IViewPart view) {
        repl = (REPLView)view;
    }

    public void addHandlerListener(IHandlerListener handlerListener) {
    }

    public void dispose() {
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        return null;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isHandled() {
        return true;
    }

    public void removeHandlerListener(IHandlerListener handlerListener) {
        
    }

}
