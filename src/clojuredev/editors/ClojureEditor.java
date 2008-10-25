package clojuredev.editors;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ClojureEditor extends AbstractTextEditor {

    public ClojureEditor() {
        setDocumentProvider(new ClojureDocumentProvider());
        setSourceViewerConfiguration(new ClojureSourceViewerConfiguration((ClojureEditor) this));
    }

    public IProject iproject() {
        IEditorInput input0 = getEditorInput();
        if (input0 instanceof FileEditorInput) return ((FileEditorInput) input0).getFile().getProject();
        return null;
    }
    
}
