package clojuredev.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class ClojurePartitionScanner extends RuleBasedPartitionScanner {

    public static final String SEXP = "__clojuredev_sexp";
    public static final String FUNARGS = "__clojuredev_funargs";
    public static final String SINGLE_LINE_COMMENT = "__clojuredev_sline_cmt";
    
    public ClojurePartitionScanner() {
        IToken sexp = new Token(SEXP);
        IToken funargs = new Token(FUNARGS);
        IToken slineCmt = new Token(SINGLE_LINE_COMMENT);
        
        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
        rules.add(new SexpRule(sexp));
        rules.add(new MultiLineRule("[", "]", funargs));
        rules.add(new SingleLineRule(";", "", slineCmt));
        
        setPredicateRules(rules.toArray(new IPredicateRule[0]));
    }
    
}
