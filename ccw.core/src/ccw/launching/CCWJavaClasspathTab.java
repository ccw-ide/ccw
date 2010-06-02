package ccw.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathGroup;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

public class CCWJavaClasspathTab extends JavaClasspathTab {

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
		super.setDefaults(configuration);
		/*
		 * algo: ajouter tous les source folders en tete des user classpath
		 * entries
		 */

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		super.initializeFrom(configuration);
	}

	@Override
	protected ClasspathModel getModel() {
		ClasspathModel model = super.getModel();
		if (model != null) {
			addSourceFoldersToModel(model);
		}
		return model;
	}
	
	private void addSourceFoldersToModel(ClasspathModel model) {
		ILaunchConfiguration conf = getCurrentLaunchConfiguration();
		
		IJavaElement javaElement = getContext();
		IJavaProject javaProject = javaElement.getJavaProject();
		IProject project = javaProject.getProject();

		IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(project.getFolder("src")); // TODO dynamic !
		org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry userEntry = model.getUserEntry();
		model.createEntry(null, userEntry);
		((ClasspathGroup)userEntry).addEntry((ClasspathEntry) entry, userEntry.getEntries()[0]);
	}

}
