package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;

public class SelectToMatchingBracketAction extends Action {
    public final static String ID = "ClojureSelectToMatchingBracket"; //$NON-NLS-1$
    private final AntlrBasedClojureEditor editor;

    public SelectToMatchingBracketAction(AntlrBasedClojureEditor editor) {
        super(ClojureEditorMessages.SelectToMatchingBracket_label);
        Assert.isNotNull(editor);
        this.editor = editor;
        setEnabled(true);
    }

    @Override
    public void run() {
        selectToMatchingBracket();
    }

    private void selectToMatchingBracket() {
        ISourceViewer sourceViewer = editor.sourceViewer();
        IRegion selection = editor.getSignedSelection(sourceViewer);
        boolean previousSelectionExists = Math.abs(selection.getLength()) > 1;
        if (previousSelectionExists) {
            String error = ClojureEditorMessages.GotoMatchingBracket_error_invalidSelection;
            showError(sourceViewer, error);
        } else {
            int sourceCaretOffset = selection.getOffset() + selection.getLength();
            IRegion region = editor.getPairsMatcher().match(editor.getDocument(), sourceCaretOffset);
            if (region == null) {
                String error = ClojureEditorMessages.GotoMatchingBracket_error_noMatchingBracket;
                showError(sourceViewer, error);
            } else {
                int offset = region.getOffset();
                int length = region.getLength();
                if (length >= 1) {
                    int anchor = editor.getPairsMatcher().getAnchor();
                    int targetOffset = ICharacterPairMatcher.RIGHT == anchor ? offset + 1 : offset + length;
                    if (visible(sourceViewer, targetOffset)) {
                        actualSelection(sourceViewer, selection, sourceCaretOffset, offset, length, anchor, targetOffset);
                    } else {
                        showError(sourceViewer, ClojureEditorMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
                    }
                }
            }
        }
    }

    public void showError(ISourceViewer sourceViewer, String error) {
        editor.setStatusLineErrorMessage(error);
        sourceViewer.getTextWidget().getDisplay().beep();
    }

    public void actualSelection(ISourceViewer sourceViewer, IRegion selection, int sourceCaretOffset, int offset, int length, int anchor, int targetOffset) {
        if (selection.getLength() < 0) {
            targetOffset -= selection.getLength();
        }
        int distanceBetweenBrackets = sourceCaretOffset - targetOffset + offsetAdjustment(sourceCaretOffset, offset, length, anchor);
        sourceViewer.setSelectedRange(targetOffset + targetOffsetAdjustment(anchor), distanceBetweenBrackets);
        sourceViewer.revealRange(targetOffset + targetOffsetAdjustment(anchor), distanceBetweenBrackets);
    }

    public int targetOffsetAdjustment(int anchor) {
        return ICharacterPairMatcher.RIGHT == anchor ? -1 : 0;
    }

    public int offsetAdjustment(int sourceCaretOffset, int offset, int length, int anchor) {
        switch (anchor) {
        case ICharacterPairMatcher.LEFT:
            if (offset != sourceCaretOffset) {
                return -1;
            }
            return 0;
        case ICharacterPairMatcher.RIGHT:
            if (offset + length != sourceCaretOffset) {
                return 2;
            }
            return 1;
        default:
            throw new IllegalArgumentException("anchor is not a valid value! " + anchor);
        }
    }

    public boolean visible(ISourceViewer sourceViewer, int targetOffset) {
        if (sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
            return extension.modelOffset2WidgetOffset(targetOffset) > -1;
        } else {
            IRegion visibleRegion = sourceViewer.getVisibleRegion();
            return targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength();
        }
    }
}
