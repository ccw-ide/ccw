/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *******************************************************************************/
package ccw.editors.rulesbased;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class SexpRule implements IPredicateRule {

    public SexpRule(IToken successToken) {
        this.successToken = successToken;
    }
    
    public IToken getSuccessToken() {
        return successToken;
    }

    public void setSuccessToken(IToken successToken) {
        this.successToken = successToken;
    }

    private IToken successToken;
    
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

    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        return evaluate(scanner);
    }

}
