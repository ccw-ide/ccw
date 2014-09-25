package ccw.nature;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;

final class LeiningenNatureAdderWorkspaceJob extends WorkspaceJob {

	private final ClojureInvoker leinHandlers = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.leiningen.handlers");

	private final List<IProject> projects;

	LeiningenNatureAdderWorkspaceJob(IProject[] projects) {
		super("Checking/Adding Leiningen Nature for projects " + projects);

		assert projects.length != 0;
		this.projects = Arrays.asList(projects);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		for (IProject project: projects) {
			try {
				if (isCandidateLeiningenProject(project)) {
					leinHandlers._("add-leiningen-nature", project);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return CCWPlugin.createErrorStatus(
						"Exception occured while trying to automatically "
								+ "add Leiningen nature for project "
								+ project.getName(),
								e);
			}
		}
		return Status.OK_STATUS;
	}

	private boolean isCandidateLeiningenProject(IProject project) {
		try {
			boolean maybeCandidate = project.exists()
					&&
					project.isOpen()
					&&
					!project.hasNature(CCWPlugin.LEININGEN_NATURE_ID);
			if (!maybeCandidate) {
				return false;
			} else {
				return project.getFile("project.clj").exists();
			}
		} catch (CoreException e) {
			CCWPlugin.logError("Error while  trying to determine if project " + project.getName() + " is a candidate to be converted to a leiningen project", e);
			return false;
		}
	}

}