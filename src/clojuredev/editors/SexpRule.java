package clojuredev.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SexpRule implements IPredicateRule {

    public SexpRule(IToken successToken) {
        this.successToken = successToken;
    }
    
    @Override
    public IToken getSuccessToken() {
        return successToken;
    }

    public void setSuccessToken(IToken successToken) {
        this.successToken = successToken;
    }

    private IToken successToken;
    
    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        if (scanner.read() != '(') {
            scanner.unread();
            return Token.UNDEFINED; 
        }
        int depth = 1;
        for (int c = scanner.read(); c != ICharacterScanner.EOF; c = scanner.read()) {
            if (c == '(') {
                depth++;
            }
            else if (c == ')') {
                depth--;
            }
            if (depth == 0) {
                break;
            }
        }
        
        return depth == 0 ? getSuccessToken() : Token.UNDEFINED;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        return evaluate(scanner);
    }

}
