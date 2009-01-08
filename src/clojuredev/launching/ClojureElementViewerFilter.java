package clojuredev.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ClojureElementViewerFilter extends ViewerFilter {
	private final IProject project;
	
	public ClojureElementViewerFilter(IProject project) {
		this.project = project;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource = (IResource) element;
		IJavaProject javaProject = JavaCore.create(project);
		
		if (javaProject == null) {
			return true;
		}
		
		if (contains(project.getFolder("classes"), resource)) {
			return false;
		}
		
		IPackageFragmentRoot[] froots;
		try {
			froots = javaProject.getAllPackageFragmentRoots();
		} catch (JavaModelException e) {
			return true;
		}
		
		for (IPackageFragmentRoot froot: froots) {
			IResource frootResource = froot.getResource();
			if (frootResource == null) {
				continue; // Continue to test following fragment roots
			} else if (contains(resource, froot.getResource())) {
				return true;
			} else if (contains(froot.getResource(), resource)) {
				if (resource.getType() == IResource.FILE) {
					return ((IFile) resource).getFileExtension().equals("clj");
				} else {
					return true;
				}
			} else {
				continue; // Continue to test following fragment roots
			}
		}
		return false;
	
	}
	
	/** 
	 * @return true if <code>container</code> contains or is equal 
	 *         to <code>containee</code> 
	 */
	private boolean contains(IResource container, IResource containee) {
		return container.getFullPath().isPrefixOf(containee.getFullPath());
	}
}
