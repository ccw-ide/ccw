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

package ccw.util;

import clojure.lang.Keyword;

/**
 * @author Laurent
 *
 */
public final class ClojureDocUtils {
	private ClojureDocUtils() {
		// Not intended to be subclassed
	}
	
	public static final Keyword KEYWORD_ARGLISTS = Keyword.intern(null, "arglists");
	public static final Keyword KEYWORD_DOC = Keyword.intern(null, "doc");

}
