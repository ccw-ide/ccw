package ccw.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.commands.ToggleNatureCommand;

final class ClojureNatureAdderWorkspaceJob extends
		WorkspaceJob {
	private final IProject project;

	ClojureNatureAdderWorkspaceJob(IProject project) {
		super("Adding Clojure Nature for project " + project.getName());
		this.project = project;
		// We cannot set project as the rule, since some project operations
		// try to lock the whole workspace
		this.setRule(project.getWorkspace().getRoot()); 
		this.setUser(true);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {

		try {
			if (!project.exists() || !project.isOpen())
				return Status.CANCEL_STATUS;

			if (project.hasNature(ClojureCore.NATURE_ID)) {
				return Status.CANCEL_STATUS;
			}
			
			boolean hasClojurePackage = JavaCore.create(project).findElement(ClojurePackageElementChangeListener.CLOJURE_PACKAGE_PATH) != null;
			
			if (hasClojurePackage) {
				ToggleNatureCommand.toggleNature(project, true);
			}
			
		} catch (CoreException e) {
			e.printStackTrace();
			return CCWPlugin.createErrorStatus(
					"Exception occured while trying to automatically " 
			        + "add Clojure nature for project " 
					+ project.getName(), 
					e);
		}
		return Status.OK_STATUS;
	}
}