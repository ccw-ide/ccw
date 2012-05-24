package ccw;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class ClojureNaturePropertyTest extends PropertyTester {
    
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        assert IResource.class.isInstance(receiver);
        assert "hasClojureNature".equals(property);
        
        return hasClojureNature((IResource) receiver);
    }

    public static boolean hasClojureNature(IResource resource) {
        try {
        	IProject project = resource.getProject();
            return project.hasNature(ClojureCore.NATURE_ID);
        } catch (CoreException e) {
            CCWPlugin.logError("error while evaluating if resource " + resource +
                    " belongs to a project which has nature " + ClojureCore.NATURE_ID, e);
            return false;
        }
    }
    
}
