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
package ccw.editors.antlrbased;

import org.antlr.runtime.CommonToken;
import org.eclipse.jface.text.rules.IToken;

public class TokenData {
	public final String text;
	public final int offset;
	public final int length;
	public final IToken iToken;
	
	public TokenData(CommonToken token, IToken iToken){
		text = token.getText();
		length = text != null ? text.length() : 0;
		offset = token.getStartIndex();
		this.iToken = iToken;
	}

	public TokenData(String text, int length, int offset, IToken iToken){
		this.text = text;
		this.length = text != null ? text.length() : 0;
		this.offset = offset;
		this.iToken = iToken;
	}

	@Override
	public String toString() {
		return super.toString() + getClass().getSimpleName() + " [" + text + "] "+ offset + " " + length + " " + iToken;
	}
}