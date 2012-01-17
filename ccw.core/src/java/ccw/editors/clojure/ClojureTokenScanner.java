/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Thomas Ettinger
 *******************************************************************************/
package ccw.editors.clojure;

import static ccw.CCWPlugin.getCCWColor;
import static ccw.CCWPlugin.getSystemColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ccw.CCWPlugin;
import ccw.util.ClojureUtils;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.osgi.ClojureOSGi;

abstract public class ClojureTokenScanner implements ITokenScanner {
    private static final String EDITOR_SUPPORT_NS = "ccw.editors.clojure.editor-support";
    private static final String ClojureTopLevelFormsDamager_NS = "ccw.editors.clojure.ClojureTopLevelFormsDamagerImpl";
    static {
    	try {
			ClojureOSGi.require(CCWPlugin.getDefault().getBundle().getBundleContext(), EDITOR_SUPPORT_NS);
			ClojureOSGi.require(CCWPlugin.getDefault().getBundle().getBundleContext(), ClojureTopLevelFormsDamager_NS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    private int currentOffset;
    private final Map<Object, IToken> tokenTypeToJFaceToken;
    private String text;
    private boolean initialized = false;
    private final IScanContext context;
    private final IToken[] parenLevelTokens = new IToken[] { newParenTokenWith(getSystemColor(SWT.COLOR_RED)), newParenTokenWith(getCCWColor(0)), newParenTokenWith(getSystemColor(SWT.COLOR_GRAY)), newParenTokenWith(getSystemColor(SWT.COLOR_MAGENTA)), newParenTokenWith(getCCWColor(1)), newParenTokenWith(getCCWColor(2)), newParenTokenWith(getCCWColor(3)), newParenTokenWith(getSystemColor(SWT.COLOR_DARK_GRAY)), newParenTokenWith(getCCWColor(4)), newParenTokenWith(getSystemColor(SWT.COLOR_DARK_BLUE)), newParenTokenWith(getCCWColor(5)), newParenTokenWith(getSystemColor(SWT.COLOR_DARK_CYAN)) };

    private IClojureEditor clojureEditor;

    private static IToken newParenTokenWith(Color color) {
        return new org.eclipse.jface.text.rules.Token(new TextAttribute(color));
    }

    protected static final IToken errorToken = new org.eclipse.jface.text.rules.Token(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_WHITE), Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED), TextAttribute.UNDERLINE));
    private int currentParenLevel = 0;
	private ISeq tokenSeq;
	private Map<?,?> currentToken;
	private static Keyword symbolKeyword = Keyword.intern("symbol");
	private static Keyword tokenTypeKeyword = Keyword.intern("token-type");
	private static Keyword tokenLengthKeyword = Keyword.intern("token-length");
	private static Keyword nestKeyword = Keyword.intern("nest");
	private static Keyword unnestKeyword = Keyword.intern("unnest");
	private static Keyword openListKeyword = Keyword.intern("open-list");
	private static Keyword openFnKeyword = Keyword.intern("open-fn");
	private static Keyword openChimeraKeyword = Keyword.intern("open-chimera");
	private static Keyword closeListKeyword = Keyword.intern("close-list");
	private static Keyword closeFnKeyword = Keyword.intern("close-fn");
	private static Keyword closeChimeraKeyword = Keyword.intern("close-chimera");
	
    public ClojureTokenScanner(IScanContext context, IClojureEditor clojureEditor) {
        if (clojureEditor == null) {
        	throw new IllegalArgumentException("clojureEditor cannot be null");
        }
        this.context = context;
        this.clojureEditor = clojureEditor;
        tokenTypeToJFaceToken = new HashMap<Object, IToken>();
        initClojureTokenTypeToJFaceTokenMap();
        initialized = true;
    }

    abstract protected void initClojureTokenTypeToJFaceTokenMap();

    public final void addTokenType(Object tokenIndex, org.eclipse.jface.text.rules.IToken token) {
        if (initialized) {
            throw lifeCycleError();
        }
        tokenTypeToJFaceToken.put(tokenIndex, token);
    }

    public final void addTokenType(Object tokenIndex, TextAttribute textAttribute) {
        if (initialized) {
            throw lifeCycleError();
        }
        addTokenType(tokenIndex, new org.eclipse.jface.text.rules.Token(textAttribute));
    }

    public final void addToken(int tokenIndex, String tokenData) {
        if (initialized) {
            throw lifeCycleError();
        }
        addTokenType(tokenIndex, new org.eclipse.jface.text.rules.Token(tokenData));
    }

    private RuntimeException lifeCycleError() {
        return new RuntimeException("Object Lifecycle error: method called at an inappropriate time");
    }

    public final int getTokenLength() {
    	long start = System.currentTimeMillis();
    	Long tokenLength = (Long) currentToken.get(tokenLengthKeyword);
        long localDuration = System.currentTimeMillis() - start;
        getTokenLengthDuration += localDuration;
		duration += localDuration;
		return tokenLength.intValue();
    }

    public final int getTokenOffset() {
    	return currentOffset;
    }

    private void advanceToken() {
    	long start = System.currentTimeMillis();
    	boolean firstToken;
    	if (currentToken == null) {
    		firstToken = true;
    	} else {
    		firstToken = false;
    	}
        if (!firstToken) {
        	long count = (Long) currentToken.get(tokenLengthKeyword);
        	currentOffset += count;
        	tokenSeq = tokenSeq.next();
        }
        //System.out.println(tokenSeq.first());
        currentToken = (Map<?,?>)tokenSeq.first();
        advanceTokenDuration += System.currentTimeMillis() - start;
    }
    public final IToken nextToken() {
    	long start = System.currentTimeMillis();
    	advanceToken();   	
    	IToken result;
    	
		if (currentToken.get(tokenTypeKeyword).equals(nestKeyword)) {
            currentParenLevel += 1;
            long localDuration = System.currentTimeMillis() - start;
            duration += localDuration;
            nextTokenDuration += localDuration;
            return nextToken();
        }
        if (currentToken.get(tokenTypeKeyword).equals(unnestKeyword)) {
        	currentParenLevel -= 1;
            long localDuration = System.currentTimeMillis() - start;
            duration += localDuration;
            nextTokenDuration += localDuration;
            return nextToken();
        }
        
        if (    currentToken.get(tokenTypeKeyword).equals(openListKeyword)
        		||
        		currentToken.get(tokenTypeKeyword).equals(openFnKeyword)
        		||
        		currentToken.get(tokenTypeKeyword).equals(openChimeraKeyword)
        		||
        		currentToken.get(tokenTypeKeyword).equals(closeListKeyword)
     		    ||
     		    currentToken.get(tokenTypeKeyword).equals(closeFnKeyword)
     		    ||
     		    currentToken.get(tokenTypeKeyword).equals(closeChimeraKeyword)) {
        	if (currentParenLevel < 0) {
        		result = errorToken;
        	} else {
        		result = parenLevelTokens[currentParenLevel % parenLevelTokens.length];
        	}
        } else {
            result = toJFaceToken();
        }
        if (result.equals(Token.EOF)) {
            long localDuration = System.currentTimeMillis() - start;
            duration += localDuration;
            nextTokenDuration += localDuration;
//    		System.out.println("total time spent in the scanner (secs):" + (duration / 1000));
//    		System.out.println("total time spent in nextToken (secs):" + (nextTokenDuration / 1000));
//    		System.out.println("total time spent in getTokenLength (secs):" + (getTokenLengthDuration / 1000));
//    		System.out.println("total time spent in advanceToken (secs):" + (advanceTokenDuration / 1000));
//    		System.out.println("total time spent in toJFaceToken(secs)" + (toJFaceTokenDuration / 1000));
//    		System.out.println("total time spent in guessEclipseTokenTypeForSymbol(secs)" + (guessEclipseTokenTypeForSymbolDuration / 1000));
//    		System.out.println("total time spent in getSymbolTypeDuration(secs):" + (getSymbolTypeDuration / 1000));
    		return result;
        }
        long localDuration = System.currentTimeMillis() - start;
        nextTokenDuration += localDuration;
        duration += localDuration;
        return result;
    }
    long duration;
	private long getTokenLengthDuration;
	private long nextTokenDuration;
	private long advanceTokenDuration;
	
	private void printSetRange(String name, IDocument document, int offset, int length) {
		System.out.println("setRange() called on " + name);
		System.out.println("offset:" + offset);
		System.out.println("length:" + length);
		System.out.println("document:" + document);
		System.out.println("---------------------------");
	};

    public final void setRange(IDocument document, int offset, int length) {
		//printSetRange("ClojureTokenScanner", document, offset, length);
    	long start = System.currentTimeMillis();
    	getTokenLengthDuration = 0;
    	nextTokenDuration = 0;
    	duration = 0;
    	guessEclipseTokenTypeForSymbolDuration = 0;
    	toJFaceTokenDuration = 0;
    	advanceTokenDuration = 0;
    	getSymbolTypeDuration = 0;
    	text = document.get();
        tokenSeq = (ISeq) ClojureUtils.invoke(ClojureTopLevelFormsDamager_NS, "getTokensSeq",
        		ClojureUtils.invoke(EDITOR_SUPPORT_NS, "getParseTree", clojureEditor.getParseState())
        		, offset, length);
        currentParenLevel = -1; // STRONG HYPOTHESIS HERE (related to the Damager used: offset always corresponds to the start of a top level form
        currentOffset = offset;
        currentToken = null;
        //System.out.println("setRange(offset:" + offset + ", length:" + length + ")");
        duration += System.currentTimeMillis() - start;
    }

    private long toJFaceTokenDuration;
    private IToken toJFaceToken() {
    	long start = System.currentTimeMillis();
		Object type = currentToken.get(tokenTypeKeyword);
		if (type.equals(symbolKeyword)) {
            type = guessEclipseTokenTypeForSymbol(text.substring(currentOffset, currentOffset + ((Long) currentToken.get(tokenLengthKeyword)).intValue()));
        }
		IToken retToken = tokenTypeToJFaceToken.get(type);
        if (retToken == null) {
            retToken = Token.UNDEFINED;
        }
        toJFaceTokenDuration += System.currentTimeMillis() - start; 
        return retToken;
//    	return Token.UNDEFINED;
    }

    // hack, waiting for "nil", "true", "false" to be handled as symbol literals by the grammar itself
    @SuppressWarnings("serial")
	private static final Set<String> symbolLiterals = new HashSet<String>() { { add("nil"); add("true"); add("false"); } };

	private long guessEclipseTokenTypeForSymbolDuration;
	private long getSymbolTypeDuration;
    private Object guessEclipseTokenTypeForSymbol(String symbol) {
    	long start = System.currentTimeMillis();
    	Object res;
    	if (symbolLiterals.contains(symbol)) {
    		res = Keyword.intern("literalSymbol");
    	} else {
    		long sttart = System.currentTimeMillis();
    		res = context.getSymbolType(symbol);
    		getSymbolTypeDuration += System.currentTimeMillis() - sttart;
    	}
    	guessEclipseTokenTypeForSymbolDuration += System.currentTimeMillis() - start;
    	return res;
    }
}
