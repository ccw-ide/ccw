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
package clojuredev.utils.editors.antlrbased;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;

public class TokenScannerUtils {
	private AntlrBasedTokenScanner scanner;
	private IColorManager colorProvider;

	public TokenScannerUtils(AntlrBasedTokenScanner scanner, IColorManager colorProvider) {
		this.scanner = scanner;
		this.colorProvider = colorProvider;
	}
	
	public void addTokenType(int tokenIndex, String colorKey) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.getColor(colorKey)));
	}
	
	public void addBoldToken(int tokenIndex, String colorKey) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.getColor(colorKey), null, SWT.BOLD));
	}
	
	public void addItalicToken(int tokenIndex, String colorKey) {
		scanner.addTokenType(tokenIndex, new TextAttribute(colorProvider.getColor(colorKey), null, SWT.ITALIC));
	}
}
