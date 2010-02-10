package ccw.editors.antlrbased;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class SameWordHighlightingCaretListener implements CaretListener {
    private final AntlrBasedClojureEditor editor;

    public SameWordHighlightingCaretListener(AntlrBasedClojureEditor editor) {
        this.editor = editor;
    }

    public void caretMoved(CaretEvent event) {
        IDocument document = editor.getDocument();
        Tokens tokens = new Tokens(document, event.caretOffset);
        tokens.putTokenScannerRangeOnCurrentLine();
        IToken tokenAtCaret = tokens.tokenAtCaret();
        boolean wordIsNotFormatted = tokenAtCaret.getData() == null;
        if (wordIsNotFormatted) {
            StyleRange range = createRange(tokens);
            String wordAtCaret = tokens.tokenContents();
            editor.sourceViewer().invalidateTextPresentation();
            colorOtherMatches(document, tokens, wordAtCaret);
            editor.sourceViewer().getTextWidget().setStyleRange(range);
        }
    }

    private StyleRange createRange(Tokens tokens) {
        return tokens.styleRange(new Color(editor.sourceViewer().getTextWidget().getDisplay(), 225, 225, 225));
    }

    private void colorOtherMatches(IDocument document, Tokens tokens, String original) {
        ITokenScanner tokenScanner = tokens.getTokenScanner();
        tokenScanner.setRange(document, 0, document.getLength());
        IToken token = tokenScanner.nextToken();
        while (!token.isEOF()) {
            if (token.getData() == null) {
                String tokenContents = tokens.tokenContents();
                if (tokenContents.equals(original)) {
                    StyleRange range = tokens.styleRange(new Color(editor.sourceViewer().getTextWidget().getDisplay(), 255, 255, 180));
                    editor.sourceViewer().getTextWidget().setStyleRange(range);
                }
            }
            token = tokenScanner.nextToken();
        }
    }
}