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

import org.antlr.runtime.Lexer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

abstract public class AntlrBasedPartitionScanner extends AntlrBasedTokenScanner implements IPartitionTokenScanner {

	public AntlrBasedPartitionScanner(Lexer lexer) {
		super(lexer);
	}
	
	public final void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
	}

}
