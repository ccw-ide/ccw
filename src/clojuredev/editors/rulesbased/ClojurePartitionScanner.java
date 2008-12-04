package clojuredev.editors.rulesbased;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class ClojurePartitionScanner extends RuleBasedPartitionScanner {
	public static final String CLOJURE_PARTITIONING = "__clojure_partitioning";

	public final static String CLOJURE_COMMENT = "__clojure_comment"; //$NON-NLS-1$
	public final static String CLOJURE_STRING = "__clojure_string"; //$NON-NLS-1$

	public final static String[] CLOJURE_CONTENT_TYPES= 
		new String[] { CLOJURE_COMMENT, CLOJURE_STRING };

    public ClojurePartitionScanner() {
        IToken comment = new Token(CLOJURE_COMMENT);
    
        IToken string = new Token(CLOJURE_STRING);
        
        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
        rules.add(new EndOfLineRule(";", comment));
        rules.add(new MultiLineRule("\"", "\"", string, '\\'));
         
        setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
    }
    
}
