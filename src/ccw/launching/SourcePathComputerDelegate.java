package ccw.launching;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourcePathComputer;

import ccw.clojure.util.ClojurePlugin;

public class SourcePathComputerDelegate extends JavaSourcePathComputer {

	@Override
	public ISourceContainer[] computeSourceContainers(
			ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {

		ISourceContainer[] superResult = super.computeSourceContainers(configuration, monitor);
		ISourceContainer[] result = new ISourceContainer[superResult.length + 1];
		System.arraycopy(superResult, 0, result, 1, superResult.length);
		result[0] = getSrcFolderAsISourceContainer(configuration);
		return result;
	}

	private ISourceContainer getSrcFolderAsISourceContainer(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null);
		
		if (projectName == null) {
			throw new CoreException(new Status(IStatus.ERROR, ClojurePlugin.ID, "Clojure SourcePathComputerDelegate unable to correctly set the clojure sources in the class because the considered launch configuration does not have an associated project"));
		} else {
			// TODO be smarter here : currently only works if src/ is the name of the dir :(
			//                        and only if one source directory is defined in the project :(
			return new FolderSourceContainer(
					ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFolder("src")
					, true);
			
		}
	}
	
}
