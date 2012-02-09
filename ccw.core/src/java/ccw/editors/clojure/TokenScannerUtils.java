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

import clojure.lang.Keyword;


public class TokenScannerUtils {
	private ClojureTokenScanner scanner;
	private ColorRegistry colorCache;

	public TokenScannerUtils(ClojureTokenScanner scanner, ColorRegistry colorProvider) {
		this.scanner = scanner;
		this.colorCache = colorProvider;
	}
	
	/**
	 * To use default font rgb, call with null
	 * To use default font styles, call with null in both isBold & isItalic
	 * @param tokenIndex
	 * @param rgb
	 * @param isBold
	 * @param isItalic
	 */
	public void addTokenType(Keyword tokenIndex, String rgb, Boolean isBold, Boolean isItalic) {
		TextAttribute textAttribute;
		
		if (isBold==null && isItalic==null) {
			// means let's use the defaults
			textAttribute = new TextAttribute((rgb==null)? null : colorCache.get(rgb));
		} else {
			int style = SWT.NONE;
			if (isBold) style |= SWT.BOLD;
			if (isItalic) style |= SWT.ITALIC;
			textAttribute = new TextAttribute((rgb==null)? null : colorCache.get(rgb), null, style);
		}
		scanner.addTokenType(tokenIndex, textAttribute);
	}

	public void addTokenType(Keyword tokenIndex, IToken token) {
		scanner.addTokenType(tokenIndex, token);
	}
	
}
