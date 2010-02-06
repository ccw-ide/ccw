package ccw.editors.antlrbased.formatting;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;

import ccw.editors.antlrbased.AntlrBasedClojureEditor;
import ccw.editors.antlrbased.ClojureEditorMessages;

public class FormatAction extends Action {
    private final AntlrBasedClojureEditor editor;

    public FormatAction(AntlrBasedClojureEditor editor) {
        super(ClojureEditorMessages.FormatAction_label);
        Assert.isNotNull(editor);
        this.editor = editor;
        setEnabled(true);
    }

    @Override
    public void run() {
        IDocument original = editor.getDocument();
        ISourceViewer sourceViewer = editor.sourceViewer();
        IRegion selection = editor.getSignedSelection(sourceViewer);
        int sourceCaretOffset = selection.getOffset() + selection.getLength();
        String originalContents = original.get();
        String formatted = new ClojureFormat().formatCode(originalContents);
        if (!formatted.equals(originalContents)) {
            original.set(formatted);
            sourceViewer.setSelectedRange(sourceCaretOffset, 0);
            sourceViewer.revealRange(sourceCaretOffset, 0);
        }
    }
}
