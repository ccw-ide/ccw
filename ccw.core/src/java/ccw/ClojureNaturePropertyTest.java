package ccw;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class ClojureNaturePropertyTest extends PropertyTester {
    
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        assert IProject.class.isInstance(receiver);
        assert "hasClojureNature".equals(property);
        
        IProject project = (IProject) receiver;
        
        try {
            return project.hasNature(ClojureCore.NATURE_ID);
        } catch (CoreException e) {
            CCWPlugin.logError("error while evaluating if project " + project +
                    " has nature " + expectedValue, e);
            return false;
        }
    }

    
}
