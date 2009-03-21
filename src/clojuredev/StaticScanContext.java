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

package clojuredev;

import java.util.HashMap;
import java.util.Map;

import clojuredev.debug.ClojureClient;
import clojuredev.utils.editors.antlrbased.IScanContext;

public class StaticScanContext implements IScanContext {
	private Map<String,SymbolType> clojureSymbolTypesCache = new HashMap<String, SymbolType>();
	
	private synchronized Map<?,?> getClojureSymbolTypes() {
		return new HashMap<Object, Object>();
//		if (clojureSymbolTypes == null) {
//			clojureSymbolTypes = (Map<?,?>) ClojureClient.loadString("(clojure-symbol-types)");
//		}
//		return clojureSymbolTypes;
	}
	
	public SymbolType getSymbolType(String symbol) {
		if (symbol.startsWith("*") && symbol.endsWith("*")) {
			// Even if it is not true that it is a global var,
			// force a global var look and feel to warn the user that
			// something is wrong (convention not respected)
			return SymbolType.GLOBAL_VAR;
		}
		
		if (clojureSymbolTypesCache.containsKey(symbol)) {
			return clojureSymbolTypesCache.get(symbol);
		} else {
			Object symbolType = ClojureClient.loadString("(clojuredev.debug.clientrepl/core-symbol-type \"" + symbol + "\")");;		
	
			if (symbolType == null) {
				clojureSymbolTypesCache.put(symbol, null);
				return null;
			} else {
				try {
					SymbolType st = SymbolType.valueOf(symbolType.toString());
					clojureSymbolTypesCache.put(symbol, st);
					return st;
				} catch (IllegalArgumentException e) {
					ClojuredevPlugin.logError("The clojure code returned an invalid symbolType value:'" + symbolType + "' for enumeration SymbolType", e);
					return null;
				}
			}
		}
	}
			
}
