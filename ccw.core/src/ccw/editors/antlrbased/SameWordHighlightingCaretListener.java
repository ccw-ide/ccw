package ccw.editors.antlrbased;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;

public class SameWordHighlightingCaretListener implements CaretListener {
    private final AntlrBasedClojureEditor editor;

    public SameWordHighlightingCaretListener(AntlrBasedClojureEditor editor) {
        this.editor = editor;
    }

    public void caretMoved(CaretEvent event) {
        try {
            IDocument document = editor.getDocument();
            ITokenScanner tokenScanner = editor.getTokenScanner();
            startTokenScannerAtTheBeginningOfCurrentLine(event, document, tokenScanner);
            IToken tokenAtCaret = tokenAtCaret(tokenScanner, event.caretOffset);
            boolean wordIsNotFormatted = tokenAtCaret.getData() == null;
            if (wordIsNotFormatted) {
                StyleRange range = createRange(tokenScanner);
                String wordAtCaret = document.get(range.start, range.length);
                editor.sourceViewer().invalidateTextPresentation();
                colorOtherMatches(document, tokenScanner, wordAtCaret);
                editor.sourceViewer().getTextWidget().setStyleRange(range);
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private StyleRange createRange(ITokenScanner tokenScanner) {
        StyleRange range = new StyleRange();
        range.background = editor.sourceViewer().getTextWidget().getDisplay().getSystemColor(15);
        range.start = tokenScanner.getTokenOffset();
        range.length = tokenScanner.getTokenLength();
        return range;
    }

    private void colorOtherMatches(IDocument document, ITokenScanner tokenScanner, String original) throws BadLocationException {
        tokenScanner.setRange(document, 0, document.getLength());
        IToken token = tokenScanner.nextToken();
        while (!token.isEOF()) {
            if (token.getData() == null) {
                String tokenContents = document.get(tokenScanner.getTokenOffset(), tokenScanner.getTokenLength());
                if (tokenContents.equals(original)) {
                    StyleRange range = new StyleRange();
                    range.start = tokenScanner.getTokenOffset();
                    range.length = tokenScanner.getTokenLength();
                    range.background = editor.sourceViewer().getTextWidget().getDisplay().getSystemColor(5);
                    editor.sourceViewer().getTextWidget().setStyleRange(range);
                }
            }
            token = tokenScanner.nextToken();
        }
    }

    private IToken tokenAtCaret(ITokenScanner tokenScanner, int caretOffset) {
        IToken token = tokenScanner.nextToken();
        while (!token.isEOF()) {
            if (tokenScanner.getTokenOffset() + tokenScanner.getTokenLength() > caretOffset)
                return token;
            token = tokenScanner.nextToken();
        }
        return token;
    }

    private void startTokenScannerAtTheBeginningOfCurrentLine(CaretEvent event, IDocument document, ITokenScanner tokenScanner) throws BadLocationException {
        int lineOfOffset = document.getLineOfOffset(event.caretOffset);
        int lineOffset = document.getLineOffset(lineOfOffset);
        int lineLength = document.getLineLength(lineOfOffset);
        tokenScanner.setRange(document, lineOffset, lineLength);
    }
}