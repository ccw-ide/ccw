package ccw.editors.clojure.scanners.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import ccw.editors.clojure.scanners.TokenScannerUtils;

/**
 * The Character rule, implementation adapted from
 * <a href="https://github.com/clojure/clojure/commits/clojure-1.7.0">Clojure</a>.
 * @author Andrea Richiardi
 *
 */
public class ClojureCharRule implements IPredicateRule {
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

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        int readCharCount = 0;
        
        int ch = scanner.read();
        readCharCount += 1;
        
        if(ch == ICharacterScanner.EOF) return undefined(scanner, readCharCount);
        if ((char)ch != '\\') return undefined(scanner, readCharCount);
        
        String token = TokenScannerUtils.readToken(scanner);
        readCharCount += token.length();
        
        if(token.length() == 1) {
            char next = token.charAt(0);
            if (Character.isDefined(next)) return getSuccessToken();
            else return undefined(scanner, readCharCount);
        }
        else if(token.equals("newline"))
            return getSuccessToken();
        else if(token.equals("space"))
            return getSuccessToken();
        else if(token.equals("tab"))
            return getSuccessToken();
        else if(token.equals("backspace"))
            return getSuccessToken();
        else if(token.equals("formfeed"))
            return getSuccessToken();
        else if(token.equals("return"))
            return getSuccessToken();
        else if(token.startsWith("u")) {
            char c = (char) TokenScannerUtils.readUnicodeChar(token, 1, 4, 16);
            if(c >= '\uD800' && c <= '\uDFFF') // surrogate code unit?
                return undefined(scanner, readCharCount);
            return getSuccessToken();
        } else if(token.startsWith("o")) {
            int len = token.length() - 1;
            if(len > 3)
                return undefined(scanner, readCharCount);
            int uc = TokenScannerUtils.readUnicodeChar(token, 1, len, 8);
            if(uc > 0377)
                return undefined(scanner, readCharCount);
            return getSuccessToken();
        }
        return undefined(scanner, readCharCount);
    }
    
    /**
     * Returns Token.UNDEFINED and unreads a char.
     * @param scanner
     * @return Token.UNDEFINED
     */
    private IToken undefined(ICharacterScanner scanner, int charCountToUnread) {
        for (int i = 0; i < charCountToUnread; i++) {
            scanner.unread();
        }
        return Token.UNDEFINED;
    }
}
