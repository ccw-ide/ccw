package ccw.editors.clojure;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;

import ccw.CCWPlugin;
import ccw.repl.Actions;
import ccw.repl.REPLView;

public class SwitchNamespaceAction extends Action {

    public final static String ID = "SwitchNamespaceAction"; //$NON-NLS-1$

    private final ClojureEditor editor;

    public SwitchNamespaceAction(ClojureEditor editor) {
        super(ClojureEditorMessages.SwitchNamespaceAction_label);
        Assert.isNotNull(editor);
        this.editor = editor;
        setEnabled(true);
    }

    public void run() {
        REPLView repl = REPLView.activeREPL.get();
        if (repl == null || repl.isDisposed()) {

            return;
        }

        String ns = editor.findDeclaringNamespace();
        if (ns == null) {
            // put error msg in footer instead
            CCWPlugin.logError("Could not switch ns to: " + ns);
        } else {
            //EvaluateTextUtil.evaluateText(repl, String.format(";; Switching to %s namespace", ns), false);
            EvaluateTextUtil.evaluateText(repl, String.format("(clojure.core/in-ns '%s)", ns), false);
            Actions.ShowActiveREPL.execute(true);
        }
    }
}
