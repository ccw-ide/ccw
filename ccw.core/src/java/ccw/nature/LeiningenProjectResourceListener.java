package ccw.nature;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;

import ccw.CCWPlugin;
import ccw.preferences.PreferenceConstants;


public final class LeiningenProjectResourceListener implements IResourceChangeListener {

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (automaticNatureAdditionDisabled()) return;

		IResourceDelta rootDelta = event.getDelta();
		if (rootDelta == null) return;

		IResourceDelta[] projectsDelta = rootDelta.getAffectedChildren();

		List<IProject> projects = new ArrayList<IProject>();

		for (IResourceDelta projectDelta: projectsDelta) {
			IProject project = (IProject) projectDelta.getResource();

			if (project == null || !project.exists() || !project.isOpen() || hasLeiningenNature(project))
				continue;

			if (project.getFile("project.clj").exists())
				projects.add(project);
		}

		addLeiningenNature(projects.toArray(new IProject[projects.size()]));
	}

	private boolean hasLeiningenNature(IProject project) {
		try {
			return project.hasNature(CCWPlugin.LEININGEN_NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void addLeiningenNature(final IProject[] projects) {
		if (projects.length != 0) {
			WorkspaceJob job = new LeiningenNatureAdderWorkspaceJob(projects);
			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
			job.setUser(false);
			job.schedule(100);
		}
	}

	public void performFullScan() {
		if (automaticNatureAdditionDisabled()) return;

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		addLeiningenNature(workspaceRoot.getProjects());
	}

	private boolean automaticNatureAdditionDisabled() {
		return !CCWPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CCW_GENERAL_AUTOMATIC_NATURE_ADDITION);
	}

}