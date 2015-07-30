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
 *    Andrea Richiardi - new ClojureCharRule
 *******************************************************************************/
package ccw.editors.clojure.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import ccw.editors.clojure.scanners.rules.ClojureCharRule;

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
}
