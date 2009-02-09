package clojuredev;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class ClojureNaturePropertyTest extends PropertyTester {
    
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        assert IProject.class.isInstance(receiver);
        assert "hasClojureNature".equals(property);
        
        IProject project = (IProject) receiver;
        
        try {
            return project.hasNature(ClojureProjectNature.NATURE_ID);
        } catch (CoreException e) {
            ClojuredevPlugin.logError("error while evaluating if project " + project +
                    " has nature " + expectedValue, e);
            return false;
        }
    }

    
}
