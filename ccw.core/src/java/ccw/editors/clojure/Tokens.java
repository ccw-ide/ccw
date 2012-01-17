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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

import ccw.CCWPlugin;

public class Tokens {
    private final ITokenScanner tokenScanner;
    private final IDocument document;
    private int caretOffset;
    private IClojureEditor clojureEditor;

    public Tokens(IDocument document, IClojureEditor clojureEditor, IRegion selection) {
        this(document, clojureEditor);
        this.caretOffset = selection.getOffset();
    }

    public Tokens(IDocument document, IClojureEditor clojureEditor) {
        this.tokenScanner = tokenScanner(clojureEditor);
        this.clojureEditor = clojureEditor;
        tokenScanner.setRange(document, 0, document.getLength());
        this.document = document;
    }

    public Tokens(IDocument document, IClojureEditor clojureEditor, int caretOffset) {
        this(document, clojureEditor);
        this.caretOffset = caretOffset;
    }

    public static ITokenScanner tokenScanner(IClojureEditor clojureEditor) {
        return new ClojureTokenScannerFactory().create(CCWPlugin.getDefault().getColorRegistry(), CCWPlugin.getDefault().getDefaultScanContext(), clojureEditor);
    }

    public int offsetOfTokenUnderCaret() {
        putTokenScannerRangeOnCurrentLine();
        while (tokenScanner.getTokenOffset() <= caretOffset && !tokenScanner.nextToken().isEOF()) {
        }
        return tokenScanner.getTokenOffset();
    }

    public int numberOfTokenUnderCaret() {
        putTokenScannerRangeOnCurrentLine();
        int tokenCount = 0;
        while (tokenScanner.getTokenOffset() <= caretOffset && !tokenScanner.nextToken().isEOF()) {
            tokenCount++;
        }
        return tokenCount;
    }

    public void putTokenScannerRangeOnCurrentLine() {
        try {
            int caretLineNumber = document.getLineOfOffset(caretOffset);
            setTokenScannerRangeOnLine(caretLineNumber);
        } catch (BadLocationException e1) {
            throw new RuntimeException(e1);
        }
    }

    public void setTokenScannerRangeOnLine(int lineNumber) {
        try {
            int lineOffset = document.getLineOffset(lineNumber);
            int lineLength = document.getLineLength(lineNumber);
            tokenScanner.setRange(document, lineOffset, lineLength);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public int offsetOfNthTokenOnLine(int line, int tokenCount) {
        setTokenScannerRangeOnLine(line);
        while (tokenCount-- > 0) {
            tokenScanner.nextToken();
        }
        return tokenScanner.getTokenOffset();
    }

    public int caretOffset() {
        return caretOffset;
    }

    public int caretLine() {
        try {
            return document.getLineOfOffset(caretOffset);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public int sameStructuralOffset(Tokens tokens) {
        if (tokens.caretOffset == 0) {
            return 0;
        }
        int tokenOffset = tokens.offsetOfTokenUnderCaret();
        int offsetFromBeginningOfToken = tokens.caretOffset() - tokenOffset;
        int caretLine = tokens.caretLine();
        int numberOfTokenUnderCaret = tokens.numberOfTokenUnderCaret();
        int offsetOfNthTokenOnLine = offsetOfNthTokenOnLine(caretLine, numberOfTokenUnderCaret);
        return offsetOfNthTokenOnLine + offsetFromBeginningOfToken;
    }

    public IToken tokenAtCaret() {
        IToken token = tokenScanner.nextToken();
        while (!token.isEOF()) {
            if (tokenScanner.getTokenOffset() + tokenScanner.getTokenLength() > caretOffset) {
                return token;
            }
            token = tokenScanner.nextToken();
        }
        return token;
    }

    public int getTokenOffset() {
        return tokenScanner.getTokenOffset();
    }

    public int getTokenLength() {
        return tokenScanner.getTokenLength();
    }

    public IRegion tokenRegion() {
        return new Region(tokenScanner.getTokenOffset(), tokenScanner.getTokenLength());
    }

    public ITokenScanner getTokenScanner() {
        return tokenScanner;
    }

    public StyleRange styleRange(Color color) {
        StyleRange range = new StyleRange();
        range.start = getTokenOffset();
        range.length = getTokenLength();
        range.background = color;
        return range;
    }

    public String tokenContents() {
        try {
            return document.get(getTokenOffset(), getTokenLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
