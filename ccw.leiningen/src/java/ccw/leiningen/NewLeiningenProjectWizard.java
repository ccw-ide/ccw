package ccw.leiningen;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import ccw.util.ClojureUtils;
import clojure.osgi.ClojureOSGi;

public class NewLeiningenProjectWizard extends BasicNewProjectResourceWizard {

	private static final String WizardNamespace = "ccw.leiningen.wizard";
	private static final String performFinish = "perform-finish";

	static {
		try {
			ClojureOSGi.require(Activator.getDefault().getBundle().getBundleContext(), WizardNamespace);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
    public NewLeiningenProjectWizard() {
        super();
    }

    public void addPages() {
        super.addPages();
        getStartingPage().setDescription("Create a new Leiningen project.");
        getStartingPage().setTitle("Leiningen project");
        setWindowTitle("New Leiningen project");
    }

    public boolean performFinish() {
        if (super.performFinish()) {
            IProject project = getNewProject();

            try {
            	ClojureUtils.invoke(
            			WizardNamespace, 
            			performFinish, 
            			project
            			//,
            			//project.getLocation().toFile()
            			);
            	return true;
            } catch (Exception e) {
            	Activator.logError("Exception while creating new project " + project.getName(), e);
            	return false;
            }
        } else {
        	return false;
        }
    }
    
}
