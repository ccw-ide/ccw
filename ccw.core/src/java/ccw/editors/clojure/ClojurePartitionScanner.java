/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *    Laurent Petit  - evolution and maintenance
 *******************************************************************************/
package ccw.editors.clojure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class ClojurePartitionScanner extends RuleBasedPartitionScanner {
	public static final String CLOJURE_PARTITIONING = "__clojure_partitioning";

	public final static String CLOJURE_COMMENT = "__clojure_comment"; //$NON-NLS-1$
	public final static String CLOJURE_STRING = "__clojure_string"; //$NON-NLS-1$
	public final static String CLOJURE_CHAR = "__clojure_char"; //$NON-NLS-1$

	public final static String[] CLOJURE_CONTENT_TYPES= 
		new String[] { CLOJURE_COMMENT, CLOJURE_STRING, CLOJURE_CHAR };

    public ClojurePartitionScanner() {
        IToken comment = new Token(CLOJURE_COMMENT);
    
        IToken string = new Token(CLOJURE_STRING);
        
        IToken cljChar = new Token(CLOJURE_CHAR);
        
        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
        rules.add(new EndOfLineRule(";", comment));
        rules.add(new MultiLineRule("\"", "\"", string, '\\'));
        rules.add(new ClojureCharRule(cljChar)); 
         
        setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
    }
    
    public static class ClojureCharRule implements IPredicateRule {
    	private final IToken tokenToReturn;
    	public ClojureCharRule(IToken tokenToReturn) {
    		this.tokenToReturn = tokenToReturn; 
    	}

		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			if (resume == true) {
				throw new IllegalArgumentException("unhandled case when resume = true");
			} else {
				return evaluate(scanner);
			}
		}

		public IToken getSuccessToken() {
			return tokenToReturn;
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int firstChar = scanner.read();
			if ((char)firstChar != '\\') {
				scanner.unread();
				return Token.UNDEFINED;
			} else {
				int next = scanner.read();
				if (Character.isWhitespace(next)) {
					scanner.unread();
					return Token.UNDEFINED;
				}
//				if ((char)next == 'n') {
//					int second = scanner.read();
//					if ((char)second != 'e') {
//						scanner.unread();
//						return getSuccessToken();
//					} else {
//						
//					}
//				}
//				if ((char)next == 's') {
//					int second = scanner.read();
//					if ((char)second != 'p') {
//						scanner.unread();
//						return getSuccessToken();
//					} else {
//						
//					}
//				}
//				if ((char)next == 't') {
//					int second = scanner.read();
//					if ((char)second != 'a') {
//						scanner.unread();
//						return getSuccessToken();
//					} else {
//						
//					}
//				}
				return getSuccessToken();
			}
		}
    	
    }
    
}
