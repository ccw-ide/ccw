package clojuredev.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class ArgBracketRule implements IRule {

    /** Clojure brackets */
    private final char[] ARG_BRACKETS = { '[', ']', };
    /** Token to return for this rule */
    private final IToken fToken;

    /**
     * Creates a new bracket rule.
     * 
     * @param token
     *            Token to use for this rule
     */
    public ArgBracketRule(IToken token) {
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
    public boolean isArgBracket(char character) {
        for (int index = 0; index < ARG_BRACKETS.length; index++) {
            if (ARG_BRACKETS[index] == character)
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
        if (isArgBracket((char) character)) {
            do {
                character = scanner.read();
            }
            while (isArgBracket((char) character));
            scanner.unread();
            return fToken;
        }
        else {
            scanner.unread();
            return Token.UNDEFINED;
        }
    }

}
