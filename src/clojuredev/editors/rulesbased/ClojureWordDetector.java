package clojuredev.editors.rulesbased;

import org.eclipse.jface.text.rules.IWordDetector;

public class ClojureWordDetector implements IWordDetector {

    @Override
    public boolean isWordPart(char character) {
        return Character.isJavaIdentifierPart(character);
    }
    
    @Override
    public boolean isWordStart(char character) {
        return Character.isJavaIdentifierStart(character);
    }

}
