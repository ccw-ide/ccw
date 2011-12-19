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
package ccw.editors.clojure;


import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.rules.Token;

import ccw.editors.clojure.IScanContext.SymbolType;
import clojure.lang.Keyword;


public final class ClojureTokenScannerFactory {

	public ClojureTokenScanner create(final ColorRegistry colorProvider, IScanContext scanContext, IClojureEditor clojureEditor) {
		return new ClojureTokenScanner(scanContext, clojureEditor) {
			@Override
			protected void initClojureTokenTypeToJFaceTokenMap() {
				TokenScannerUtils u = new TokenScannerUtils(this, colorProvider);

				u.addTokenType(Keyword.intern("string"));
				u.addTokenType(Keyword.intern("regex"));
				u.addTokenType(Keyword.intern("int"));
				u.addTokenType(Keyword.intern("float"));
				u.addTokenType(Keyword.intern("char"));
				u.addTokenType(Keyword.intern("literalSymbol"));
				u.addTokenType(Keyword.intern("symbol"));
				u.addTokenType(Keyword.intern("unexpected"), ClojureTokenScanner.errorToken);
				u.addTokenType(IScanContext.SymbolType.FUNCTION);
				u.addTokenType(IScanContext.SymbolType.GLOBAL_VAR);
				u.addTokenType(IScanContext.SymbolType.MACRO);
				u.addTokenType(IScanContext.SymbolType.SPECIAL_FORM);

				u.addItalicToken(IScanContext.SymbolType.JAVA_CLASS);
				u.addItalicToken(IScanContext.SymbolType.JAVA_INSTANCE_METHOD);
				u.addItalicToken(IScanContext.SymbolType.JAVA_STATIC_METHOD);

				u.addTokenType(SymbolType.RAW_SYMBOL);

				u.addTokenType(Keyword.intern("meta"));
				u.addTokenType(Keyword.intern("keyword"));
				u.addTokenType(Keyword.intern("comment"));
				u.addTokenType(Keyword.intern("whitespace"));

				u.addTokenType(Keyword.intern("eof"), Token.EOF);
				u.addTokenType(Keyword.intern("whitespace"), Token.WHITESPACE);
			}
		};
	}
}
