package ccw.util;

import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public final class ResourceUtil {

	private ResourceUtil() {}
	
	/**
	 * Create the missing parent folders of resource <code>r</code>.
	 * 
	 * r cannot be null, a project or the root workspace, and r's project
	 * must exist and be open. 
	 */
	public static void createMissingParentFolders(IResource r) throws CoreException {
		
		assert r != null && r.getType() != IResource.ROOT && r.getType() != IResource.PROJECT
				&& r.getProject().exists() && r.getProject().isOpen();
		
		Stack<IFolder> toCreate = new Stack<IFolder>();
		
		IContainer parent = r.getParent();
		while (!parent.exists()) {
			toCreate.push((IFolder) parent);
			parent = parent.getParent();
		}
		
		while (!toCreate.isEmpty()) {
			toCreate.pop().create(true, true, null);
		}
	}
	
	public static IPath createPathFromList(List<String> segments) {
		assert segments.size() > 0;
		IPath path = new Path(segments.get(0));
		
		for (int i = 1; i < segments.size(); i++) {
			path = path.append(segments.get(i));
		}
		return path;
	}
}
