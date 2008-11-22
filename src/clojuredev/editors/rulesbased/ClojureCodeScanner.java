package clojuredev.editors.rulesbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ClojureCodeScanner extends RuleBasedScanner {
	
    private final String[] specialForms = { 
            "def", "if", "do", "let", "quote", "var", "fn", "loop",
            "recur", "throw", "try", "monitor-enter", "monitor-exit",
            "new", "set!"
    };

	protected final HashMap<String,Token> tokens = new HashMap<String,Token>();

	public ClojureCodeScanner() {
        Color defaultColor = new Color(Display.getCurrent(), 0, 0, 0);
	    TextAttribute defaultAttribute = new TextAttribute(defaultColor);
	    IToken defaultToken = new Token(defaultAttribute);
	    
        Color stringColor = new Color(Display.getCurrent(), 0, 0, 255);
        TextAttribute stringAttribute = new TextAttribute(stringColor);
        IToken stringToken = new Token(stringAttribute);
        
        Color keywordColor = new Color(Display.getCurrent(), 127, 0, 127);
        TextAttribute keywordAttribute = new TextAttribute(keywordColor, null, SWT.BOLD);
        IToken keywordToken = new Token(keywordAttribute);
        
        List<IRule> rules = new ArrayList<IRule>();
        
        // String color
        rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
        
        // Whitespace rule
        rules.add(new WhitespaceRule(new IWhitespaceDetector(){

            @Override
            public boolean isWhitespace(char c) {
                return Character.isWhitespace(c);
            }
            
        }));
        
        // Clojure special forms
        WordRule keywordRule = new WordRule(new ClojureWordDetector(), defaultToken);
        for (String keyword : specialForms) {
            keywordRule.addWord(keyword, keywordToken);
        }
        rules.add(keywordRule);
        
        setRules(rules.toArray(new IRule[0]));
    }
	
}
