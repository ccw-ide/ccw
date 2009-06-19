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
package clojuredev.utils.editors.antlrbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import clojuredev.ClojuredevPlugin;
import clojuredev.editors.antlrbased.TokenData;
import clojuredev.lexers.ClojureLexer;

import static clojuredev.ClojuredevPlugin.getSystemColor;
import static clojuredev.ClojuredevPlugin.getClojuredevColor;

abstract public class AntlrBasedTokenScanner implements ITokenScanner {
	private static int ANTLR_EOF = -1;
	private Lexer lexer;
	private final List<TokenData> tokensData;
	private int currentTokenIndex;
	private final Map<Object, IToken> antlrTokenTypeToJFaceToken;
	private String text;
	private boolean initialized = false;
	private final IScanContext context;
	
	private IToken[] parenLevelTokens = new IToken[] {
			newParenTokenWith(getSystemColor(SWT.COLOR_RED)),
			newParenTokenWith(getClojuredevColor(0)),
			newParenTokenWith(getSystemColor(SWT.COLOR_GRAY)),
			newParenTokenWith(getSystemColor(SWT.COLOR_MAGENTA)),
			newParenTokenWith(getClojuredevColor(1)),
			newParenTokenWith(getClojuredevColor(2)),
			newParenTokenWith(getClojuredevColor(3)),
			newParenTokenWith(getSystemColor(SWT.COLOR_DARK_GRAY)),
			newParenTokenWith(getClojuredevColor(4)),
			newParenTokenWith(getSystemColor(SWT.COLOR_DARK_BLUE)),
			newParenTokenWith(getClojuredevColor(5)),
			newParenTokenWith(getSystemColor(SWT.COLOR_DARK_CYAN)) 
	};
	private static IToken newParenTokenWith(Color color) {
		return new org.eclipse.jface.text.rules.Token(new TextAttribute(color));
	}
	private IToken parenErrorToken = new org.eclipse.jface.text.rules.Token(
			new TextAttribute(
					Display.getDefault().getSystemColor(SWT.COLOR_WHITE),
					Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED),
					TextAttribute.UNDERLINE));
	private int currentParenLevel = 0;

	public AntlrBasedTokenScanner(Lexer lexer, IScanContext context) {
		this.lexer = lexer;
		this.context = context;
		
		tokensData = new ArrayList<TokenData>();
		
		antlrTokenTypeToJFaceToken = new HashMap<Object, IToken>();
		initAntlrTokenTypeToJFaceTokenMap();
		antlrTokenTypeToJFaceToken.put(ANTLR_EOF, org.eclipse.jface.text.rules.Token.EOF);
		initialized = true;
	}
	
	abstract protected void initAntlrTokenTypeToJFaceTokenMap();
	
	public final void addTokenType(Object tokenIndex, org.eclipse.jface.text.rules.Token token) {
		if (initialized) throw lifeCycleError();
		antlrTokenTypeToJFaceToken.put(tokenIndex, token);
	}
	
	public final void addTokenType(Object tokenIndex, TextAttribute textAttribute) {
		if (initialized) throw lifeCycleError();
		addTokenType(tokenIndex, new org.eclipse.jface.text.rules.Token(textAttribute));
	}

	public final void addToken(int tokenIndex, String tokenData) {
		if (initialized) throw lifeCycleError();
		addTokenType(tokenIndex, new org.eclipse.jface.text.rules.Token(tokenData));
	}
	
	private RuntimeException lifeCycleError() {
		return new RuntimeException("Object Lifecycle error: method called at an inappropriate time");
	}

	public final int getTokenLength() {
		return tokensData.get(currentTokenIndex).length;
	}

	public final int getTokenOffset() {
		return tokensData.get(currentTokenIndex).offset;
	}

	public final IToken nextToken() {
		int nextIndex = currentTokenIndex + 1;
		if ( nextIndex >= tokensData.size() ) {
			return org.eclipse.jface.text.rules.Token.EOF;
		}
		currentTokenIndex = nextIndex;
		TokenData token = tokensData.get(currentTokenIndex);
		if( token != null ){
			IToken result;
		    if (token.text.equals("(")) {
		    	if (currentParenLevel < 0) {
		    		currentParenLevel = 0;
		    	}
		    		result = parenLevelTokens[currentParenLevel % parenLevelTokens.length];
		    	currentParenLevel += 1;
		    } else if (token.text.equals(")")) {
		    	currentParenLevel -= 1;
		    	if (currentParenLevel < 0) {
		    		result = parenErrorToken;
		    	} else {
		    		result = parenLevelTokens[currentParenLevel % parenLevelTokens.length];
		    	}
		    } else {
		        result = token.iToken;
		    }
		    return result;
		} else {
			ClojuredevPlugin.logError("nextToken called but null token retrieved ? ! Returning UNDEFINED");
			return org.eclipse.jface.text.rules.Token.UNDEFINED;
		}
	}

	public final void setRange(IDocument document, int offset, int length) {
//		System.out.println("++++++++++++++++++++++++++++++++++++++");
		if (!document.get().equals(text)) {
			tokensData.clear();
			text = document.get();
			
			lexer.setCharStream(new ANTLRStringStream(text));

			while (true) {
				Token token = lexer.nextToken();
				if( token.getType() == ANTLR_EOF ){
					break;
				}
				addTokenInfo((CommonToken) token);
			}
		}
		repositionCurrentTokenAtOffset(offset);
	}

	private void repositionCurrentTokenAtOffset(int offset) {
		currentParenLevel = 0;
		int size = tokensData.size();
		for (int i = 0; i < size; i++) {
			TokenData tokenInfo = tokensData.get(i);
			if (tokenInfo.offset >= offset) {
				currentTokenIndex = i - 1;
				break;
			}
			nextToken(); // called to initialize side effect on variable currentParenLevel FIXME do better ?
		}
	}

	private void addTokenInfo(CommonToken token){
		assert token != null;
		IToken retToken;
		if (token.getType() != ClojureLexer.SYMBOL) {
			retToken = antlrTokenTypeToJFaceToken.get(token.getType());
		} else {
			retToken = guessEclipseTokenForSymbol(token);
		}
		if( retToken == null ) {
			retToken = org.eclipse.jface.text.rules.Token.UNDEFINED; 
		}
		tokensData.add(new TokenData(token, retToken));
	}
	private IToken guessEclipseTokenForSymbol(CommonToken symbolToken) {
		String symbol = symbolToken.getText();
		IScanContext.SymbolType symbolType = context.getSymbolType(symbol);
		if (symbolType == null) {
			return null;
		} else {
			return antlrTokenTypeToJFaceToken.get(symbolType);
		}
	}
}
