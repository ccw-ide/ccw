/*******************************************************************************
 * Copyright (c) 2009 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Laurent PETIT - initial API and implementation
 *******************************************************************************/

package ccw.editors.clojure.scanners;

import clojure.lang.Keyword;

/**
 * Interface for providing context (static or dynamic) 
 * to a text scanning operation
 * 
 * @author Laurent Petit <laurent.petit@gmail.com>
 */
public interface IScanContext {
	
	/**
	 * Tries to guess the type of the symbol passed to String.
	 * 
	 * @param symbol
	 * @return an IScanContext.SymbolType enum instance, or null if it 
	 * cannot guess or the symbol does not exist
	 */
	Keyword getSymbolType(String symbol, boolean isCallableSymbol);

}
