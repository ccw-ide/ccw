package ccw.leiningen;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import ccw.CCWPlugin;
import ccw.util.ClojureInvoker;

public class LeiningenBuilder extends IncrementalProjectBuilder {

	public static final String ID = "ccw.leiningen.builder";

	private final ClojureInvoker classpathContainer = ClojureInvoker.newInvoker(
            CCWPlugin.getDefault(),
            "ccw.leiningen.classpath-container");

	private static final String updateProjectDependencies = "update-project-dependencies";

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {

		if (getProject() == null)
			return null;

		if (! ( kind == AUTO_BUILD) && ! ( kind == INCREMENTAL_BUILD))
			return null;

		if (projectCljPresentInDelta()) {
			// UpdateProjectDependencies can take a long time, we do it in a separate job
			Job j = new WorkspaceJob("Update Leiningen Managed Dependencies") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					try {
						IJavaProject javaProject = JavaCore.create(getProject());
						classpathContainer.__(updateProjectDependencies, javaProject);
						return Status.OK_STATUS;
					} catch (Exception e) {
						return CCWPlugin.createErrorStatus("Unexpected exception while trying to update Leiningen Managed Dependencies for project " + getProject().getName(), e);
					}
				}
			};
			j.setUser(true);
			j.schedule();
		}

		return null;
	}

	private boolean projectCljPresentInDelta() {
		IResourceDelta delta = getDelta(getProject());

		IResourceDelta deltaProjClj = delta.findMember(new Path("project.clj"));

		return deltaProjClj != null;
	}
}
