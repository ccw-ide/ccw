package ccw.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourcePathComputer;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;

import ccw.clojure.util.ClojurePlugin;

public class SourcePathComputerDelegate extends JavaSourcePathComputer {

	@Override
	public ISourceContainer[] computeSourceContainers(
			ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {

		
		ISourceContainer[] superResult = super.computeSourceContainers(configuration, monitor);
		
		List<ISourceContainer> result = new ArrayList<ISourceContainer>(superResult.length * 2);
		result.addAll(getSrcFoldersAsISourceContainers(configuration));
		
		for (ISourceContainer sourceContainer: superResult) {
			if (sourceContainer instanceof PackageFragmentRootSourceContainer) {
				PackageFragmentRootSourceContainer sc = (PackageFragmentRootSourceContainer) sourceContainer;

				IPath maybeSourcePath =	sc.getPackageFragmentRoot().getSourceAttachmentPath();
				if (maybeSourcePath != null) {
					if (maybeSourcePath.toFile().isFile()) {
						result.add(new ExternalArchiveSourceContainer(maybeSourcePath.toOSString(), false));
					} else {
						result.add(new DirectorySourceContainer(maybeSourcePath, false));
					}
				}
				// unconditionnally add the path of the archive, *after* the sourcePath, so that cljs in sourcePath
				// take precedence over cljs in "bin" path
				if (sc.getPackageFragmentRoot().isExternal()) {
					if (sc.getPackageFragmentRoot().isArchive()) {
						result.add(new ExternalArchiveSourceContainer(sc.getPackageFragmentRoot().getPath().toOSString(), false));
					} else {
						result.add(new DirectorySourceContainer(sc.getPackageFragmentRoot().getPath(), false));
					}
				} else {
					if (sc.getPackageFragmentRoot().isArchive()) {
						result.add(new ArchiveSourceContainer((IFile) sc.getPackageFragmentRoot().getCorrespondingResource(), false));
					} else {
						result.add(new FolderSourceContainer((IContainer) sc.getPackageFragmentRoot().getCorrespondingResource(), false));
					}
				}
			} else if (sourceContainer instanceof ExternalArchiveSourceContainer) {
				// TODO
				ExternalArchiveSourceContainer sc = (ExternalArchiveSourceContainer) sourceContainer;
			} else if (sourceContainer instanceof JavaProjectSourceContainer) {
				// TODO
				JavaProjectSourceContainer sc = (JavaProjectSourceContainer) sourceContainer;
				sc.getJavaProject();
			} else {
				/*		TODO manager other kinds ?
						DirectorySourceContainer

						ISourceContainer
							ArchiveSourceContainer
							ClassPathContainerSourceContainer
							ClassPathVariableSourceContainer
							FolderSourceContainer
							ProjectSourceContainer
							WorkingSetSourceContainer
							WorkspaceSourceContainer				 
				 */
				// nothing intelligent to do yet :)
			}
			result.add(sourceContainer);
		}
		return result.toArray(new ISourceContainer[result.size()]);
	}

	/*
	 * TODO : remove the hard coded src/ folder and really returns all java source folders instead
	 */
	private List<ISourceContainer> getSrcFoldersAsISourceContainers(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(LaunchUtils.ATTR_PROJECT_NAME, (String) null);
		
		if (projectName == null) {
			throw new CoreException(new Status(IStatus.ERROR, ClojurePlugin.ID, "Clojure SourcePathComputerDelegate unable to correctly set the clojure sources in the class because the considered launch configuration does not have an associated project"));
		} else {
			// TODO be smarter here : currently only works if src/ is the name of the dir :(
			//                        and only if one source directory is defined in the project :(
			return Arrays.asList(new ISourceContainer[] {
					new FolderSourceContainer(			
							ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFolder("src")
							, true)
			});
		}
	}
	
}
