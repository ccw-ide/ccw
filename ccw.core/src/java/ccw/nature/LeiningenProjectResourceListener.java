package ccw.nature;

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

		for (IResourceDelta projectDelta: projectsDelta) {
			IProject project = (IProject) projectDelta.getResource();
			addLeiningenNature(project);
		}
	}

	private void addLeiningenNature(final IProject[] projects) {
		if (projects.length != 0) {
			for (IProject project: projects) {
				addLeiningenNature(project);
			}
		}
	}

	private void addLeiningenNature(final IProject project) {
		// Some failfast tests
		if (project == null || !project.exists() || !project.isOpen())
			return;

		if  (hasLeiningenNature(project)) {
			if (checkLeiningenProjectConsistency(project)) {
				return;
			} else {
				// continue
			}
		}

		if (!project.getFile("project.clj").exists()) {
			return;
		}

		WorkspaceJob job = new LeiningenNatureAdderWorkspaceJob(project);
		job.setRule(project.getParent());
		job.setUser(true);
		job.schedule();
	}

	private boolean checkLeiningenProjectConsistency(IProject project) {
		return project.getFile(".classpath").exists();
	}

	private boolean hasLeiningenNature(IProject project) {
		try {
			return project.hasNature(CCWPlugin.LEININGEN_NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
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