/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *    Andrea RICHIARDI - removed state and pasted useful keywords
 *                       added readToken for ClojureCharRule
 *******************************************************************************/
package ccw.editors.clojure.scanners;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import ccw.CCWPlugin;
import clojure.lang.Keyword;

/**
 * Common utils for scanning tokens using Paredit's parse tree
 */
public class TokenScannerUtils {
    
    public static Keyword symbolKeyword = Keyword.intern("symbol");
    public static Keyword tokenTypeKeyword = Keyword.intern("token-type");
    public static Keyword tokenLengthKeyword = Keyword.intern("token-length");
    public static Keyword nestKeyword = Keyword.intern("nest");
    public static Keyword unnestKeyword = Keyword.intern("unnest");
    public static Keyword openListKeyword = Keyword.intern("open-list");
    public static Keyword openFnKeyword = Keyword.intern("open-fn");
    public static Keyword openChimeraKeyword = Keyword.intern("open-chimera");
    public static Keyword closeListKeyword = Keyword.intern("close-list");
    public static Keyword closeFnKeyword = Keyword.intern("close-fn");
    public static Keyword closeChimeraKeyword = Keyword.intern("close-chimera");
    public static Keyword metaKeyword = Keyword.intern("meta");
    public static Keyword readerLiteralTagKeyword = Keyword.intern("reader-literal");
    public static Keyword whitespaceKeyword = Keyword.intern("whitespace");

	private TokenScannerUtils() {}

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
	public static void addTokenType(ClojureTokenScanner scanner, Keyword tokenIndex, String rgb, Boolean isBold, Boolean isItalic) {
		scanner.addTokenType(tokenIndex, createTokenData(rgb, isBold, isItalic));
	}

	public static void addTokenType(ClojureTokenScanner scanner, Keyword tokenIndex, IToken token) {
		scanner.addTokenType(tokenIndex, token);
	}

    /**
     * Reads a token, implementation adapted from
     * <a href="https://github.com/clojure/clojure/commits/clojure-1.7.0">Clojure</a>.
     * @param scanner A ICharacterScanner
     * @param sb An already built StringBuilder
     * @return A token string
     */
    private static String readToken(ICharacterScanner scanner, StringBuilder sb) {
        for(; ;)
        {
            int ch = scanner.read();
            if(ch == -1 || isWhitespace(ch) || isTerminatingMacro(ch))
            {
                scanner.unread();
                return sb.toString();
            }
            sb.append((char) ch);
        }
    }

    /**
     * Reads a token, implementation adapted from
     * <a href="https://github.com/clojure/clojure/commits/clojure-1.7.0">Clojure</a>.
     * @param scanner A ICharacterScanner
     * @return A token string
     */
    public static String readToken(ICharacterScanner scanner) {
        StringBuilder sb = new StringBuilder();
        return readToken(scanner, sb);
    }

    /**
     * Reads a token, implementation adapted from
     * <a href="https://github.com/clojure/clojure/commits/clojure-1.7.0">Clojure</a>.
     * @param scanner A ICharacterScanner
     * @param initch Init char
     * @return A token string
     */
    public static String readToken(ICharacterScanner scanner, char initch) {
        StringBuilder sb = new StringBuilder();
        sb.append(initch);
        return readToken(scanner, sb);
    }

    /**
     * Reads a Unicode char from a token, implementation adapted from
     * <a href="https://github.com/clojure/clojure/commits/clojure-1.7.0">Clojure</a>.
     * @param token The token
     * @param offset An offset in the token
     * @param length The length of the token
     * @param base the base used for the decoding
     * @return
     */
    public static int readUnicodeChar(String token, int offset, int length, int base) {
        if(token.length() != offset + length)
            throw new IllegalArgumentException("Invalid unicode character: \\" + token);
        int uc = 0;
        for(int i = offset; i < offset + length; ++i)
            {
            int d = Character.digit(token.charAt(i), base);
            if(d == -1)
                throw new IllegalArgumentException("Invalid digit: " + token.charAt(i));
            uc = uc * base + d;
            }
        return (char) uc;
    }

    /**
     * AR - Could have used paredit.clj/loc_utils definition of whitespace here but I guess this is faster.
     * @param ch
     * @return
     */
    public static boolean isWhitespace(int ch){
        return Character.isWhitespace(ch) || ch == ',';
    }

    // AR - I am leaving the structure of clojure's source as it might be still useful
    public static Character[] macroChars = new Character[256];
    static {
        macroChars['"'] = new Character('"');
        macroChars[';'] = new Character(';');
        macroChars['\''] = new Character('\'');
        macroChars['@'] = new Character('@');
        macroChars['^'] = new Character('^');
        macroChars['`'] = new Character('`');
        macroChars['~'] = new Character('~');
        macroChars['('] = new Character('(');
        macroChars[')'] = new Character(')');
        macroChars['['] = new Character('[');
        macroChars[']'] = new Character(']');
        macroChars['{'] = new Character('{');
        macroChars['}'] = new Character('}');
        //  macroChars['|'] = new Character('|');
        macroChars['\\'] = new Character('\\');
        macroChars['%'] = new Character('%');
        macroChars['#'] = new Character('#');
    }

    static private boolean isMacro(int ch){
        return (ch < macroChars.length && macroChars[ch] != null);
    }

    static private boolean isTerminatingMacro(int ch){
        return (ch != '#' && ch != '\'' && ch != '%' && isMacro(ch));
    }
}
