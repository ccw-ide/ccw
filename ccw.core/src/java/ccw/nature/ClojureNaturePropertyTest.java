package ccw.nature;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.FileEditorInput;

import ccw.CCWPlugin;
import ccw.ClojureCore;

public class ClojureNaturePropertyTest extends PropertyTester {
    
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    	assert "hasClojureNature".equals(property);

    	if (receiver instanceof IResource)
    		return hasClojureNature((IResource) receiver);
    	else if (receiver instanceof FileEditorInput) {
    		FileEditorInput fei = (FileEditorInput) receiver;
    		return hasClojureNature(fei.getFile());
    	} else {
    		return false;
    	}
    }

    public static boolean hasClojureNature(IResource resource) {
        try {
        	IProject project = resource.getProject();
        	if (project.isOpen()) {
        		return project.hasNature(ClojureCore.NATURE_ID);
        	} else {
        		return false;
        	}
        } catch (CoreException e) {
            CCWPlugin.logError("error while evaluating if resource " + resource +
                    " belongs to a project which has nature " + ClojureCore.NATURE_ID, e);
            return false;
        }
    }
    
}
