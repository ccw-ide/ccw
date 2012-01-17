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
package ccw.editors.clojure;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;

public class SameWordHighlightingCaretListener implements CaretListener {
	private static final String COLOR_KEY = "ccw.editors.SameWordHighlightingCaretListener.COLOR_KEY";
	private static final String OTHER_MATCHES_COLOR_KEY = "ccw.editors.SameWordHighlightingCaretListener.OTHER_MATCHES_COLOR_KEY";
	
    private final ClojureEditor editor;
    private final ColorRegistry colorRegistry;

    public SameWordHighlightingCaretListener(ClojureEditor editor, ColorRegistry colorRegistry) {
        this.editor = editor;
        this.colorRegistry = colorRegistry;
        initColorRegistry();
    }
    
    private void initColorRegistry() {
    	if (!colorRegistry.hasValueFor(COLOR_KEY)) {
    		colorRegistry.put(COLOR_KEY, new RGB(225, 225, 225));
    	}
    	if (!colorRegistry.hasValueFor(OTHER_MATCHES_COLOR_KEY)) {
    		colorRegistry.put(OTHER_MATCHES_COLOR_KEY, new RGB(255, 255, 180));
    	}
    }

    public void caretMoved(CaretEvent event) {
        IDocument document = editor.getDocument();
        Tokens tokens = new Tokens(document, editor, event.caretOffset);
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
        return tokens.styleRange(colorRegistry.get(COLOR_KEY));
    }

    private void colorOtherMatches(IDocument document, Tokens tokens, String original) {
        ITokenScanner tokenScanner = tokens.getTokenScanner();
        tokenScanner.setRange(document, 0, document.getLength());
        IToken token = tokenScanner.nextToken();
        while (!token.isEOF()) {
            if (token.getData() == null) {
                String tokenContents = tokens.tokenContents();
                if (tokenContents.equals(original)) {
                    StyleRange range = tokens.styleRange(colorRegistry.get(OTHER_MATCHES_COLOR_KEY));
                    editor.sourceViewer().getTextWidget().setStyleRange(range);
                }
            }
            token = tokenScanner.nextToken();
        }
    }
}