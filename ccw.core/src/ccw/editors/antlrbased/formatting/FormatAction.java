package ccw.editors.antlrbased.formatting;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import ccw.editors.antlrbased.AntlrBasedClojureEditor;
import ccw.editors.antlrbased.ClojureEditorMessages;
import ccw.editors.antlrbased.Tokens;

public class FormatAction extends Action {
    public final static String ID = "FormatAction"; //$NON-NLS-1$
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
        String originalContents = original.get();
        String formatted = new ClojureFormat().formatCode(originalContents);
        if (!formatted.equals(originalContents)) {
            replaceOriginalWithFormatted(original, sourceViewer, formatted);
        }
    }

    private void replaceOriginalWithFormatted(IDocument original, ISourceViewer sourceViewer, String formatted) {
        IRegion selection = editor.getSignedSelection(sourceViewer);
        Tokens tokens = new Tokens(original, selection);
        Document formattedDocument = new Document(formatted);
        Tokens formattedTokens = new Tokens(formattedDocument);
        int targetOffset = formattedTokens.sameStructuralOffset(tokens);
        original.set(formatted);
        sourceViewer.setSelectedRange(targetOffset, 0);
        sourceViewer.revealRange(targetOffset, 0);
    }
}
