package ccw.nature;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import ccw.CCWPlugin;
import ccw.ClojureCore;
import ccw.commands.ToggleClojureNatureCommand;

final class ClojureNatureAdderWorkspaceJob extends
		WorkspaceJob {
	private final List<IProject> projects;

	ClojureNatureAdderWorkspaceJob(IProject[] projects) {
		super("Checking/Adding Clojure Nature for projects " + projects);

		assert projects.length != 0;
		this.projects = Arrays.asList(projects);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		for (IProject project: projects) {
			try {
				if (isCandidateClojureProject(project)) {
					ToggleClojureNatureCommand.toggleNature(project, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return CCWPlugin.createErrorStatus(
						"Exception occured while trying to automatically "
								+ "add Clojure nature for project "
								+ project.getName(),
								e);
			}
		}
		return Status.OK_STATUS;
	}

	private boolean isCandidateClojureProject(IProject project) {
		try {
			boolean maybeCandidate = project.exists()
					&&
					project.isOpen()
					&&
					!project.hasNature(ClojureCore.NATURE_ID)
					&&
					project.hasNature(JavaCore.NATURE_ID); // Check needed when doing a full scan
			if (!maybeCandidate) {
				return false;
			} else {
				IJavaProject jProject = JavaCore.create(project);
			    boolean isCandidate = jProject.findElement(ClojurePackageElementChangeListener.CLOJURE_PACKAGE_PATH) != null;
			    return isCandidate;
			}
		} catch (JavaModelException e) {
			CCWPlugin.logError("Error while  trying to determine if project " + project.getName() + " is a candidate to be converted to a clojure project", e);
			return false;
		} catch (CoreException e) {
			CCWPlugin.logError("Error while  trying to determine if project " + project.getName() + " is a candidate to be converted to a clojure project", e);
			return false;
		}
	}

}