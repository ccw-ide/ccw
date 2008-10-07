package clojuredev;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewClojureProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();
		if (result) {
			IProject project = getNewProject();
			try {
				IProjectDescription description = project.getDescription();
				String[] natures = description.getNatureIds();
				String[] newNatures = new String[natures.length + 2];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = "clojuredev.nature";
				newNatures[natures.length+1] = "org.eclipse.jdt.core.javanature";
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(new Status(
						IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}

		}
		return result;
	}

}
