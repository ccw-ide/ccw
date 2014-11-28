package ccw.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;

final class LeiningenNatureAdderWorkspaceJob extends WorkspaceJob {

	private final ClojureInvoker leinHandlers = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.leiningen.handlers");

	private final IProject project;

	LeiningenNatureAdderWorkspaceJob(IProject project) {
		super("Checking/Adding Leiningen Nature for project " + project);

		assert project != null;
		this.project = project;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		try {
			if (project == null || !project.isOpen() ||!project.exists())
				return Status.OK_STATUS;

			if (hasJavaNature(project)) {
				// We don't override existing java natures
				return Status.OK_STATUS;
			}

			if (hasLeiningenNature(project)) {
				if (!checkLeiningenProjectConsistency(project)) {
					leinHandlers._("upgrade-project-build-path", JavaCore.create(project));
				}
				return Status.OK_STATUS;
			}

			if (isCandidateLeiningenProject(project)) {
				System.out.println("CREATING LEININGEN PROJECT " + project.getName());
				leinHandlers._("add-leiningen-nature-with-monitor", project, monitor);
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return CCWPlugin.createErrorStatus(
					"Exception occured while trying to automatically "
							+ "add Leiningen nature for project "
							+ project.getName(),
							e);
		}
	}

	private boolean checkLeiningenProjectConsistency(IProject project) {
		return project.getFile(".classpath").exists();
	}

	private boolean hasJavaNature(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
			// When in doubt, consider it has java nature, this will stop the process
			return true;
		}
	}
	private boolean hasLeiningenNature(IProject project) {
		try {
			return project.hasNature(CCWPlugin.LEININGEN_NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
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