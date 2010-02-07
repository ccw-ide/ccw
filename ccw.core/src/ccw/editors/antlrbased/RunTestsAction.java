package ccw.editors.antlrbased;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;

import ccw.debug.ClojureClient;

public class RunTestsAction extends Action {
    public static final String RUN_TESTS_ID = "RunTestsAction";
    private final AntlrBasedClojureEditor editor;

    public RunTestsAction(AntlrBasedClojureEditor editor) {
        this.editor = editor;
    }

    @Override
    public void run() {
        super.run();
        String lib = editor.getDeclaringNamespace();
        ClojureClient clojure = ClojureClient.newClientForActiveRepl();
        String compilationResult = clojure.remoteLoad(CompileLibAction.compileLibCommand(lib));
        refreshCompilationResults();
        if (compilationResult.contains("\"response-type\" 0,")) {
            runTests(lib, clojure);
        } else {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Compilation_failed);
            setReplBackgroundColor(new Color(Display.getDefault(), 0xff, 0xff, 0x7f));
        }
    }

    private void runTests(String lib, ClojureClient clojure) {
        String results = clojure.remoteLoad(runTestsCommand(lib));
        if (results.contains(":fail 0, :error 0")) {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Tests_passed);
            setReplBackgroundColor(new Color(Display.getDefault(), 0x7f, 0xff, 0x7f));
        } else {
            editor.setStatusLineErrorMessage(ClojureEditorMessages.Tests_failed);
            setReplBackgroundColor(new Color(Display.getDefault(), 0xff, 0x7f, 0x7f));
        }
    }

    private void setReplBackgroundColor(Color background) {
        IOConsole replConsole = ClojureClient.findActiveReplConsole();
        replConsole.setBackground(background);
    }

    private void refreshCompilationResults() {
        try {
            IFile editorFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
            editorFile.getProject().getFolder("classes").refreshLocal(IFolder.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static String runTestsCommand(String libName) {
        return "(clojure.test/run-tests'" + libName + ")";
    }
}
