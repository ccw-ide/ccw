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

import clojuredev.lexers.ClojureLexer;
import clojuredev.lexers.ClojurePartitionLexer;
import clojuredev.utils.editors.antlrbased.AntlrBasedPartitionScanner;

public final class ClojurePartitionScannerFactory {
	public final static String CLOJURE_COMMENT = "__clojure_comment"; //$NON-NLS-1$
	public final static String CLOJURE_STRING = "__clojure_string"; //$NON-NLS-1$
	public final static String CLOJURE_CODE = "__clojure_code"; //$NON-NLS-1$
	public final static String[] CLOJURE_PARTITION_TYPES= new String[] { CLOJURE_COMMENT, CLOJURE_STRING, CLOJURE_CODE };

	public ClojurePartitionScannerFactory() {
	}
	
	public AntlrBasedPartitionScanner create() {
		return new AntlrBasedPartitionScanner(new ClojureLexer()) {
			@Override
			protected void initAntlrTokenTypeToJFaceTokenMap() {
				addToken(ClojurePartitionLexer.PARTITION_STRING, CLOJURE_STRING);
				addToken(ClojurePartitionLexer.PARTITION_COMMENT, CLOJURE_COMMENT);
				addToken(ClojurePartitionLexer.PARTITION_CODE, CLOJURE_CODE);
			}
		};
	}
}
