/*******************************************************************************
 * Copyright (c) 2008 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/
package clojuredev.editors.antlrbased;


import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;


import clojuredev.lexers.ClojureLexer;
import clojuredev.utils.editors.antlrbased.AntlrBasedTokenScanner;
import clojuredev.utils.editors.antlrbased.TokenScannerUtils;


public final class ClojureTokenScannerFactory {

	public AntlrBasedTokenScanner create(final IColorManager colorProvider) {
		return new AntlrBasedTokenScanner(new ClojureLexer()) {
			@Override
			protected void initAntlrTokenTypeToJFaceTokenMap() {
				TokenScannerUtils u = new TokenScannerUtils(this, colorProvider);
				u.addTokenType(ClojureLexer.STRING, IJavaColorConstants.JAVA_STRING); 
				u.addTokenType(ClojureLexer.NUMBER, IJavaColorConstants.JAVA_STRING);
				u.addTokenType(ClojureLexer.CHARACTER, IJavaColorConstants.JAVA_STRING);
				u.addTokenType(ClojureLexer.NIL, IJavaColorConstants.JAVA_DEFAULT);
				u.addTokenType(ClojureLexer.BOOLEAN, IJavaColorConstants.JAVA_STRING);
				u.addTokenType(ClojureLexer.SYMBOL, IJavaColorConstants.JAVA_DEFAULT);
				u.addBoldToken(ClojureLexer.SPECIAL_FORM, IJavaColorConstants.JAVA_KEYWORD);
				u.addTokenType(ClojureLexer.T21, IJavaColorConstants.JAVA_DEFAULT);//'&'=20
				u.addTokenType(ClojureLexer.T22, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);//'('=21
				u.addTokenType(ClojureLexer.T23, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);//')'=22
				u.addTokenType(ClojureLexer.T24, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);//'['=23
				u.addTokenType(ClojureLexer.T25, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);//']'=24
				u.addTokenType(ClojureLexer.T26, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);//'{'=25
				u.addTokenType(ClojureLexer.T27, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);//'}'=26
				u.addTokenType(ClojureLexer.T28, IJavaColorConstants.JAVA_KEYWORD);//'\''=27
				u.addTokenType(ClojureLexer.T29, IJavaColorConstants.JAVA_DEFAULT);//'^'=28
				u.addTokenType(ClojureLexer.T30, IJavaColorConstants.JAVA_DEFAULT);//'@'=29
				u.addTokenType(ClojureLexer.T31, IJavaColorConstants.JAVA_DEFAULT);//'#'=30
				u.addTokenType(ClojureLexer.KEYWORD, IJavaColorConstants.JAVA_STRING);
				u.addTokenType(ClojureLexer.SYNTAX_QUOTE, IJavaColorConstants.JAVA_DEFAULT);
				u.addTokenType(ClojureLexer.UNQUOTE_SPLICING, IJavaColorConstants.JAVA_DEFAULT);
				u.addTokenType(ClojureLexer.UNQUOTE, IJavaColorConstants.JAVA_DEFAULT);
				u.addItalicToken(ClojureLexer.COMMENT, IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);
				u.addTokenType(ClojureLexer.SPACE, IJavaColorConstants.JAVA_DEFAULT);
				u.addTokenType(ClojureLexer.LAMBDA_ARG, IJavaColorConstants.JAVA_STRING);			
			}
		};
	}
}
