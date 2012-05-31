package ccw.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;


public final class ClojurePackageElementChangeListener implements
		IElementChangedListener {

	private static final String CLOJURE_PACKAGE = "clojure.lang";
	static final IPath CLOJURE_PACKAGE_PATH = new Path("clojure/lang");

	public void elementChanged(ElementChangedEvent javaModelEvent) {
		IJavaElementDelta delta = javaModelEvent.getDelta();
		IJavaElement element = delta.getElement();
		
		if (element instanceof IJavaModel) {
			
			visitJavaModelDelta(delta);
			
		} else if (element instanceof IJavaProject) {
			
			visitJavaProjectDelta(delta);
			
		} else if (element instanceof IPackageFragmentRoot) {
			
			IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element;
			if (!ClojureNaturePropertyTest.hasClojureNature(packageFragmentRoot.getJavaProject().getProject())) {
				visitPackageFragmentRootDelta(delta);
			}
			
		}
	}
	
	private void visitJavaModelDelta(IJavaElementDelta javaModelDelta) {
		for (IJavaElementDelta javaProjectDelta: javaModelDelta.getAffectedChildren()) {
			visitJavaProjectDelta(javaProjectDelta);
		}
	}
	
	private void visitJavaProjectDelta(IJavaElementDelta javaProjectDelta) {
		IJavaProject javaProject = (IJavaProject) javaProjectDelta.getElement();
		
		if (ClojureNaturePropertyTest.hasClojureNature(javaProject.getProject()))
			return;
		
		for (IJavaElementDelta fragmentRootDelta: javaProjectDelta.getAffectedChildren()) {
			if (visitPackageFragmentRootDelta(fragmentRootDelta)) {
				// Clojure fragment found, stop 
				break;
			}
		}
	}

	private boolean visitPackageFragmentRootDelta(IJavaElementDelta packageFragmentRootDelta) {
		IPackageFragmentRoot packageElement = (IPackageFragmentRoot) packageFragmentRootDelta.getElement();
		if (isClojureElement(packageElement)) {
			addClojureNature(packageElement.getJavaProject().getProject());
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isClojureElement(IPackageFragmentRoot packageFragmentRoot) {
		return packageFragmentRoot.getPackageFragment(CLOJURE_PACKAGE). exists();
	}

	private void addClojureNature(final IProject project) {
		WorkspaceJob job = new ClojureNatureAdderWorkspaceJob(project);
		job.schedule();
	}
	
}