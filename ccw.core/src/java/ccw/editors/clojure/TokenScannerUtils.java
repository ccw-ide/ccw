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
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.SWT;


public class TokenScannerUtils {
	private ClojureTokenScanner scanner;
	private ColorRegistry colorProvider;

	public TokenScannerUtils(ClojureTokenScanner scanner, ColorRegistry colorProvider) {
		this.scanner = scanner;
		this.colorProvider = colorProvider;
	}
	
	public void addTokenType(Object tokenIndex, String colorKey) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.get(colorKey)));
	}
	public void addTokenType(Object tokenIndex) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.get(ClojureEditor.ID + "_" +  tokenIndex)));
	}
	
	public void addTokenType(Object tokenIndex, IToken token) {
		scanner.addTokenType(tokenIndex, token);
	}
	
	public void addBoldToken(Object tokenIndex, String colorKey) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.get(colorKey), null, SWT.BOLD));
	}
	public void addBoldToken(Object tokenIndex) {
		scanner.addTokenType(tokenIndex, new TextAttribute(
				colorProvider.get(ClojureEditor.ID + "_" +  tokenIndex), null, SWT.BOLD));
	}
	
	public void addItalicToken(Object tokenIndex, String colorKey) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.get(colorKey), null, SWT.ITALIC));
	}
	public void addItalicToken(Object tokenIndex) {
		scanner.addTokenType(tokenIndex, new TextAttribute(
				colorProvider.get(ClojureEditor.ID + "_" +  tokenIndex), null, SWT.ITALIC));
	}
}
