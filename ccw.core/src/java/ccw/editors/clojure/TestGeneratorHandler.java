package ccw.editors.clojure;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swtbot.generator.ui.StartupRecorder;

public class TestGeneratorHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        StartupRecorder.openRecorder(null);
        return null;
    }
}
