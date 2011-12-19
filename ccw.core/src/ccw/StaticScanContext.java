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

package ccw;

import java.util.HashMap;
import java.util.Map;

import ccw.debug.ClojureClient;
import ccw.editors.clojure.IScanContext;

public class StaticScanContext implements IScanContext {
	private Map<String,SymbolType> clojureSymbolTypesCache = new HashMap<String, SymbolType>();

	private boolean isJavaIdentifier(String s) {
		assert s != null && s.length() > 0;

		if (!Character.isJavaIdentifierStart(s.charAt(0))) {
			return false;
		}
		for (int i = 1; i < s.length(); i++) {
			if (!Character.isJavaIdentifierPart(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public SymbolType getSymbolType(String symbol) {
		assert symbol != null && symbol.length() > 0;

		if (symbol.startsWith("*") && symbol.endsWith("*")) {
			// Even if it is not true that it is a global var,
			// force a global var look and feel to warn the user that
			// something is wrong (convention not respected)
			return SymbolType.GLOBAL_VAR;
		}

		if (symbol.startsWith(".")  &&  symbol.length() > 1) {
			if (isJavaIdentifier(symbol.substring(1))) {
				return SymbolType.JAVA_INSTANCE_METHOD;
			}
		}

		// This code must appear after the test for JAVA_INSTANCE_METHOD, or a bug in it would mask
		// any attempt to guess a java instance method
		int from;
		int foundIndex = -1;
		int letterAfterPointIndex = 0;
		do {
			letterAfterPointIndex = foundIndex + 1;
			if ( (letterAfterPointIndex < symbol.length()) && Character.isUpperCase(symbol.charAt(letterAfterPointIndex)) ) {
				if (symbol.substring(letterAfterPointIndex + 1).contains("/")) {
					return SymbolType.JAVA_STATIC_METHOD;
				} else {
					return SymbolType.JAVA_CLASS;
				}
			} else {
				from = letterAfterPointIndex;
			}
		} while ((from < symbol.length()) && (foundIndex = symbol.indexOf('.', from)) > 0);

		if (clojureSymbolTypesCache.containsKey(symbol)) {
			return clojureSymbolTypesCache.get(symbol);
		} else {
			Object symbolType = ClojureClient.loadString("(ccw.debug.clientrepl/core-symbol-type \"" + symbol + "\")");

			if (symbolType == null) {
				clojureSymbolTypesCache.put(symbol, SymbolType.RAW_SYMBOL);
				return SymbolType.RAW_SYMBOL;
			} else {
				try {
					SymbolType st = SymbolType.valueOf(symbolType.toString());
					clojureSymbolTypesCache.put(symbol, st);
					return st;
				} catch (IllegalArgumentException e) {
					CCWPlugin.logError("The clojure code returned an invalid symbolType value:'" + symbolType + "' for enumeration SymbolType", e);
					return null;
				}
			}
		}
	}

}
