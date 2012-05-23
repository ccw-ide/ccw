package ccw.leiningen;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import ccw.util.ClojureInvoker;

public class LeiningenBuilder extends IncrementalProjectBuilder {
	
	public static final String ID = "ccw.leiningen.builder";
	
	private static final ClojureInvoker classpathContainer = ClojureInvoker.newInvoker(
            Activator.getDefault(),
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
			try {
				IJavaProject javaProject = JavaCore.create(getProject());
				classpathContainer._(updateProjectDependencies, javaProject);
			} catch (Exception e) {
				throw new CoreException(Activator.createErrorStatus("Unexpected exception while trying to update Leiningen Managed Dependencies for project " + getProject().getName(), e));
			}
		}
		
		return null;
	}

	private boolean projectCljPresentInDelta() {
		IResourceDelta delta = getDelta(getProject());
		
		IResourceDelta deltaProjClj = delta.findMember(new Path("project.clj"));

		return deltaProjClj != null;
	}
}
