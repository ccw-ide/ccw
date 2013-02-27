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

import ccw.editors.clojure.IScanContext;
import ccw.preferences.PreferenceConstants;
import ccw.util.ClojureInvoker;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public class StaticScanContext implements IScanContext {
	private Map<String,Keyword> clojureSymbolTypesCache = new HashMap<String, Keyword>();
	
	// TODO this is to ensure that ccw.debug.clientrepl is launched ...
	private ClojureInvoker clientrepl = ClojureInvoker.newInvoker(CCWPlugin.getDefault(), "ccw.debug.clientrepl");
	
	private final Var coreSymbolType = RT.var("ccw.debug.clientrepl", "core-symbol-type");
	
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
	
	private boolean isEarmuffedVar(String symbol) {
		return (!symbol.equals("*")
			   &&
				symbol.startsWith("*") || symbol.contains("/*"))
			   &&
			   symbol.endsWith("*");
	}
	
	public Keyword getSymbolType(String symbol, boolean isCallableSymbol) {
		assert symbol != null && symbol.length() > 0;
		
		if (isEarmuffedVar(symbol)) {
			return isCallableSymbol ? PreferenceConstants.callableGLOBAL_VAR_Token : PreferenceConstants.GLOBAL_VAR_Token;
		}
		
		if (symbol.startsWith(".")  &&  symbol.length() > 1) {
			if (isJavaIdentifier(symbol.substring(1))) {
				return isCallableSymbol ? PreferenceConstants.callableJAVA_INSTANCE_METHOD_Token : PreferenceConstants.JAVA_INSTANCE_METHOD_Token;
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
					return isCallableSymbol ? PreferenceConstants.callableJAVA_STATIC_METHOD_Token : PreferenceConstants.JAVA_STATIC_METHOD_Token;
				} else {
					return isCallableSymbol ? PreferenceConstants.callableJAVA_CLASS_Token : PreferenceConstants.JAVA_CLASS_Token;
				}
			} else {
				from = letterAfterPointIndex;
			}
		} while ((from < symbol.length()) && (foundIndex = symbol.indexOf('.', from)) > 0);
		/*
		 TODO activate this possibility via a preference to get a visible bootstrap gain
		if (isCallableSymbol) // 2.7 sec.
			return PreferenceConstants.RAW_SYMBOL_Token;
		else 
			return PreferenceConstants.callable_RAW_SYMBOL_Token;
		*/
		if (clojureSymbolTypesCache.containsKey(symbol)) {
			return getCallableOrNonCallable(clojureSymbolTypesCache.get(symbol), isCallableSymbol);
		} else {
			Keyword symbolType =  (Keyword) coreSymbolType.invoke(symbol);	
	
			if (symbolType == null) {
				clojureSymbolTypesCache.put(symbol, PreferenceConstants.RAW_SYMBOL_Token);
				return getCallableOrNonCallable(PreferenceConstants.RAW_SYMBOL_Token, isCallableSymbol);
			} else {
				try {
					clojureSymbolTypesCache.put(symbol, symbolType);
					return getCallableOrNonCallable(symbolType, isCallableSymbol);
				} catch (IllegalArgumentException e) {
					CCWPlugin.logError("The clojure code returned an invalid symbolType value:'" + symbolType + "' for enumeration SymbolType", e);
					return null;
				}
			}
		}
		
	}
	
	private static final Map<Keyword, Keyword> symbolToCallable = new HashMap<Keyword, Keyword>() {
		{
			put(PreferenceConstants.RAW_SYMBOL_Token, PreferenceConstants.callable_RAW_SYMBOL_Token);
			put(PreferenceConstants.FUNCTION_Token, PreferenceConstants.callableFUNCTION_Token);
			put(PreferenceConstants.MACRO_Token, PreferenceConstants.callableMACRO_Token);
			put(PreferenceConstants.SPECIAL_FORM_Token, PreferenceConstants.callableSPECIAL_FORM_Token);
		}
	};
	
	private Keyword getCallableOrNonCallable(Keyword symbolKeyword, boolean isCallableSymbol) {
		if (isCallableSymbol) {
			Keyword callableFlavor = symbolToCallable.get(symbolKeyword);
			if (callableFlavor != null) {
				return callableFlavor;
			} else {
				return symbolKeyword;
			}
		} else {
			return symbolKeyword;
		}
	}
			
}
