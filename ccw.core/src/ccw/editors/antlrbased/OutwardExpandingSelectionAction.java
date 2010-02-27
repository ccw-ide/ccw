/*******************************************************************************
 * Copyright (c) 2010 Tuomas KARKKAINEN.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Tuomas KARKKAINEN - initial API and implementation
 *******************************************************************************/
package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;

public class OutwardExpandingSelectionAction extends Action {
    public final static String ID = "ClojureOutwardExpandingSelection"; //$NON-NLS-1$
    private final AntlrBasedClojureEditor editor;

    public OutwardExpandingSelectionAction(AntlrBasedClojureEditor editor) {
        super(ClojureEditorMessages.OutwardExpandingSelection_label);
        Assert.isNotNull(editor);
        this.editor = editor;
        setEnabled(true);
    }

    @Override
    public void run() {
        select();
    }

    private void select() {
        ISourceViewer sourceViewer = editor.sourceViewer();
        IRegion selection = editor.getUnSignedSelection(sourceViewer);
        boolean previousSelectionExists = Math.abs(selection.getLength()) > 0;
        int caretOffset = selection.getOffset();
        if (previousSelectionExists) {
            caretOffset = selection.getOffset() - 1;
        }
        Tokens tokens = new Tokens(editor.getDocument(), caretOffset);
        if (tokens.tokenAtCaret().getData() == null) {
            showSelection(sourceViewer, tokens.getTokenOffset(), tokens.getTokenLength());
            return;
        }
        int originalSelectionEnd = selection.getOffset() + selection.getLength();
        IRegion region = null;
        while (region == null && caretOffset >= 0) {
            region = editor.getPairsMatcher().match(editor.getDocument(), caretOffset);
            if (region != null) {
                int newSelectionEnd = region.getOffset() + region.getLength();
                if (newSelectionEnd < originalSelectionEnd) {
                    region = null;
                }
            }
            if (region == null) {
                caretOffset--;
            }
        }
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
                    actualSelection(editor.getDocument(), sourceViewer, caretOffset, offset, length, anchor, targetOffset);
                } else {
                    showError(sourceViewer, ClojureEditorMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
                }
            }
        }
    }

    public void showError(ISourceViewer sourceViewer, String error) {
        editor.setStatusLineErrorMessage(error);
        sourceViewer.getTextWidget().getDisplay().beep();
    }

    public void actualSelection(IDocument document, ISourceViewer sourceViewer, int sourceCaretOffset, int offset, int length, int anchor, int targetOffset) {
        int distanceBetweenBrackets = sourceCaretOffset - targetOffset + offsetAdjustment(sourceCaretOffset, offset, length, anchor);
        int adjustedTargetOffset = targetOffset + targetOffsetAdjustment(anchor);
        if (distanceBetweenBrackets < 0) {
            adjustedTargetOffset = adjustedTargetOffset + distanceBetweenBrackets;
            distanceBetweenBrackets = Math.abs(distanceBetweenBrackets);
        }
        if (previousCharacterIsPound(document, adjustedTargetOffset)) {
            adjustedTargetOffset--;
            distanceBetweenBrackets++;
        }
        showSelection(sourceViewer, adjustedTargetOffset, distanceBetweenBrackets);
    }

    public void showSelection(ISourceViewer sourceViewer, int offset, int length) {
        sourceViewer.setSelectedRange(offset, length);
        sourceViewer.revealRange(offset, length);
    }

    public boolean previousCharacterIsPound(IDocument document, int adjustedTargetOffset) {
        try {
            String previousCharacter = document.get(adjustedTargetOffset - 1, 1);
            return "#".equals(previousCharacter);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
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
