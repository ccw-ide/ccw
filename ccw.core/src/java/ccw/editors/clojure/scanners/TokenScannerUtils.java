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
package ccw.editors.clojure.scanners;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import ccw.CCWPlugin;
import clojure.lang.Keyword;


public class TokenScannerUtils {
	private ClojureTokenScanner scanner;
//	private ColorRegistry colorCache;

	public TokenScannerUtils(ClojureTokenScanner scanner) {
		this.scanner = scanner;
	}
	
	/**
     * Creates the data to feed an IToken.
     * @param rgb A color (RGB).
     * @param isBold If it is in bold style.
     * @param isItalic If it is in italic style.
     * @return An Object (tipically a TextAttribute)
     */
	public static TextAttribute createTokenData(RGB rgb, Boolean isBold, Boolean isItalic) {
	    return createTokenData(StringConverter.asString(rgb), isBold, isItalic);
	}
	
	/**
	 * Creates the data to feed an IToken.
	 * @param rgb A color string (RGB).
	 * @param isBold If it is in bold style.
	 * @param isItalic If it is in italic style.
	 * @return An Object (tipically a TextAttribute)
	 */
	public static TextAttribute createTokenData(String rgb, Boolean isBold, Boolean isItalic) {
	    TextAttribute textAttribute;
        
        if (isBold==null && isItalic==null) {
            // means let's use the defaults
            textAttribute = new TextAttribute((rgb==null)? null : CCWPlugin.getColor(rgb));
        } else {
            int style = SWT.NONE;
            if (isBold) style |= SWT.BOLD;
            if (isItalic) style |= SWT.ITALIC;
            textAttribute = new TextAttribute((rgb==null)? null : CCWPlugin.getColor(rgb), null, style);
        }
        return textAttribute;
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
		scanner.addTokenType(tokenIndex, createTokenData(rgb, isBold, isItalic));
	}

	public void addTokenType(Keyword tokenIndex, IToken token) {
		scanner.addTokenType(tokenIndex, token);
	}
}
