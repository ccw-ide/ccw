package clojuredev.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.FileEditorInput;

import clojuredev.console.ClojureConsole;

public class ClojureLaunchShortcut implements ILaunchShortcut {

    public void launch(ISelection selection, String mode) {
        ClojureConsole clojureCons = null;
        for (IConsole console : ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles()) {
            if (console instanceof ClojureConsole) {
                clojureCons = (ClojureConsole) console;
            }
        }

        if (clojureCons == null)
        	return;
        
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSel = (IStructuredSelection) selection;
            for (Object selObj : structuredSel.toList()) {
                if (selObj instanceof IFile) {
                    clojureCons.evalFile((IFile) selObj);
                }
            }
        }
    }

    public void launch(IEditorPart editor, String mode) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            IFile file = ((FileEditorInput) input).getFile();

            try {
                for (IConsole console : ConsolePlugin.getDefault()
                        .getConsoleManager().getConsoles()) {
                    if (console instanceof ClojureConsole) {
                        ((ClojureConsole) console).evalFile(file);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
