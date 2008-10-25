package clojuredev.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class ParensRule implements IRule {

    /** Clojure brackets */
    private final char[] PARENS = { '(', ')', };
    /** Token to return for this rule */
    private final IToken fToken;

    /**
     * Creates a new bracket rule.
     * 
     * @param token
     *            Token to use for this rule
     */
    public ParensRule(IToken token) {
        fToken = token;
    }

    /**
     * Is this character a bracket character?
     * 
     * @param character
     *            Character to determine whether it is a bracket character
     * @return <code>true</code> iff the character is a bracket,
     *         <code>false</code> otherwise.
     */
    public boolean isParen(char character) {
        for (int index = 0; index < PARENS.length; index++) {
            if (PARENS[index] == character)
                return true;
        }
        return false;
    }

    /*
     * @see
     * org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules
     * .ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {

        int character = scanner.read();
        if (isParen((char) character)) {
            do {
                character = scanner.read();
            }
            while (isParen((char) character));
            scanner.unread();
            return fToken;
        }
        else {
            scanner.unread();
            return Token.UNDEFINED;
        }
    }

}
