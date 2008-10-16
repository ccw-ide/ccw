package clojuredev.editors;

import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISharedTextColors;

public class ClojureScanner extends RuleBasedScanner {

    static public final String[] KEYWORDS = { "def", "if", "then", "do", "let",
            "quote", "var", "fn", "loop", "recur", "throw", "try",
            "monitor-enter", "monitor-exit", "new", "set!", "and", "or",
            "when", "when-not", "cond", "locking", "defn", "defmacro",
            "macroexpand-1", "macroexpand", "->", "doto", "memfn", "bean",
            "gen-class", };

    public ClojureScanner() {
        WordRule rule = new WordRule(new IWordDetector() {
            public boolean isWordStart(char c) {
                return Character.isJavaIdentifierStart(c);
            }

            public boolean isWordPart(char c) {
                return Character.isJavaIdentifierPart(c);
            }
        });

        Token keyword = new Token(IJavaColorConstants.JAVA_KEYWORD);
        Token comment = new Token(IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);
        Token string = new Token(IJavaColorConstants.JAVA_STRING);

        // add tokens for each reserved word
        for (int n = 0; n < KEYWORDS.length; n++) {
            rule.addWord(KEYWORDS[n], keyword);
        }

        setRules(new IRule[] { rule, new SingleLineRule(";", null, comment),
                new SingleLineRule("\"", "\"", string, '\\'),
                new SingleLineRule("'", "'", string, '\\'),
                new WhitespaceRule(new IWhitespaceDetector() {
                    public boolean isWhitespace(char c) {
                        return Character.isWhitespace(c);
                    }
                }), });
    }
}
