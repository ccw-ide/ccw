package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class FormatAction extends Action {
    private AntlrBasedClojureEditor editor;

    public FormatAction(AntlrBasedClojureEditor editor) {
        super(ClojureEditorMessages.FormatAction_label);
        Assert.isNotNull(editor);
        this.editor = editor;
        setEnabled(true);
    }

    @Override
    public void run() {
        IDocument original = editor.getDocument();
        String originalContents = original.get();
        String formatted = new ClojureFormat().formatCode(originalContents);
        if (!formatted.equals(originalContents)) {
            replaceWithIndented(original, new Document(formatted));
        }
    }

    private void replaceWithIndented(IDocument original, Document formatted) {
        MultiTextEdit combinedEdit = new MultiTextEdit();
        try {
            for (int i = 0; i < original.getNumberOfLines(); i++) {
                String originalLine = getLine(original, i);
                String formattedLine = getLine(formatted, i);
                if (!originalLine.equals(formattedLine)) {
                    combinedEdit.addChild(replacementLine(original, i, formattedLine));
                }
            }
            combinedEdit.apply(original);
        } catch (MalformedTreeException e) {
            throw new RuntimeException(e);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private ReplaceEdit replacementLine(IDocument original, int i, String formattedLine) throws BadLocationException {
        return new ReplaceEdit(original.getLineOffset(i), original.getLineLength(i), formattedLine);
    }

    private String getLine(IDocument document, int lineNumber) throws BadLocationException {
        return document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber));
    }
}
