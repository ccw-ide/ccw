package ccw.editors.antlrbased.formatting;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import ccw.editors.antlrbased.AntlrBasedClojureEditor;
import ccw.editors.antlrbased.ClojureEditorMessages;

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
        try {
            String originalContents = original.get();
            String formatted = new ClojureFormat().formatCode(originalContents);
            if (!formatted.equals(originalContents)) {
                replaceOriginalWithFormatted(original, sourceViewer, formatted);
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceOriginalWithFormatted(IDocument original, ISourceViewer sourceViewer, String formatted) throws BadLocationException {
        IRegion selection = editor.getSignedSelection(sourceViewer);
        int sourceCaretOffset = selection.getOffset();
        int originalCaretLine = original.getLineOfOffset(sourceCaretOffset);
        int lineOffset = original.getLineOffset(originalCaretLine);
        int lineLength = original.getLineLength(originalCaretLine);
        ITokenScanner tokenScanner = editor.getTokenScanner();
        tokenScanner.setRange(original, lineOffset, lineLength);
        int tokenCount = 0;
        while (tokenScanner.getTokenOffset() <= sourceCaretOffset && !tokenScanner.nextToken().isEOF()) {
            tokenCount++;
        }
        int tokenOffset = tokenScanner.getTokenOffset();
        int offsetFromBeginningOfToken=sourceCaretOffset-tokenOffset;
        original.set(formatted);
        Document formattedDocument = new Document(formatted);
        int newLineOffset = formattedDocument.getLineOffset(originalCaretLine);
        int newLineLength = formattedDocument.getLineLength(originalCaretLine);
        tokenScanner.setRange(formattedDocument, newLineOffset, newLineLength);
        while (tokenCount-- > 0) {
            tokenScanner.nextToken();
        }
        int targetOffset = tokenScanner.getTokenOffset() + offsetFromBeginningOfToken;
        sourceViewer.setSelectedRange(targetOffset, 0);
        sourceViewer.revealRange(targetOffset, 0);
    }
}
