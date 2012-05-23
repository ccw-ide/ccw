package ccw.leiningen;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import ccw.util.ClojureInvoker;

public class NewLeiningenProjectWizard extends BasicNewProjectResourceWizard {

	private static final String performFinish = "perform-finish";

	private static final ClojureInvoker wizard = ClojureInvoker.newInvoker(
			                                         Activator.getDefault(),
			                                         "ccw.leiningen.wizard");
	
    public NewLeiningenProjectWizard() {
        super();
    }

    public void addPages() {
        super.addPages();
        doAddPages();
    }

	private void doAddPages() {
		getStartingPage().setDescription("Create a new Leiningen project.");
        getStartingPage().setTitle("Leiningen project");
        setWindowTitle("New Leiningen project");
	}

    public boolean performFinish() {
        if (super.performFinish()) {
            return doPerformFinish();
        } else {
        	return false;
        }
    }

	private boolean doPerformFinish() {
		IProject project = getNewProject();

		try {
			wizard._(performFinish,
					 project
						//,
						//project.getLocation().toFile()
					 );
			return true;
		} catch (Exception e) {
			Activator.logError("Exception while creating new project " + project.getName(), e);
			return false;
		}
	}
    
}
