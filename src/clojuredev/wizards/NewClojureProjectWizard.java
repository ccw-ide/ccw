package clojuredev.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.wizards.NewSourceFolderCreationWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import clojuredev.ClojureCore;

public class NewClojureProjectWizard extends BasicNewProjectResourceWizard {

    public NewClojureProjectWizard() {
        super();
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        getStartingPage().setDescription("Create a new Clojure project.");
        getStartingPage().setTitle("Clojure project");
        setWindowTitle("New Clojure project");
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        if (super.performFinish()) {
            IProject project = getNewProject();
            // ScalaCore.getScalaProject(project);
            // ScalaCore.getJavaProject(project);
            boolean ret = ClojureCore.addJavaNature(project)
                    && ClojureCore.addClojureNature(project);
            return ret;
        }
        new NewSourceFolderCreationWizard();

        return false;
    }

}
