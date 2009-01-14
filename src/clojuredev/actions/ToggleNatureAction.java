package clojuredev.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import clojuredev.ClojureProjectNature;
import clojuredev.ClojuredevPlugin;

/**
 * Inspired from cusp
 * @author Laurent
 *
 */
public class ToggleNatureAction implements IObjectActionDelegate {

		private ISelection selection;
		private IWorkbenchPart targetPart;
		

		public void selectionChanged(IAction action, ISelection selection) {
			this.selection = selection;
		}

		public void setActivePart(IAction action, IWorkbenchPart targetPart) {
			this.targetPart = targetPart;
		}
		
		public void run(IAction action) {
			if (selection instanceof IStructuredSelection) {
				for (Iterator<?> it = ((IStructuredSelection) selection). iterator(); it
						.hasNext();) {
					Object element = it.next();
					IProject project = null;
					if (element instanceof IProject) {
						project = (IProject) element;
					} else if (element instanceof IAdaptable) {
						project = (IProject) ((IAdaptable) element)
								.getAdapter(IProject.class);
					}
					if (project != null) {
						toggleNature(project);
					}
				}
			}
		}

		private void toggleNature(IProject project) {
			String title = "Change Clojure language support";
			String message;
			try {
				boolean added = doToggleNature(project);
				message = "Clojure language support successfully "
					+ (added ? "added" : "removed") + ".";
			} catch (CoreException e) {
				message = "Error while trying to toggle clojure language support for project "
					+ project.getName();
				ClojuredevPlugin.logError(message, e);
			}
			MessageDialog.openInformation(targetPart.getSite().getShell(), title, message);
		}
		
		/**
		 * Toggles clojure nature on a project.
		 * @return true if nature added, false if nature removed
		 */
		private boolean doToggleNature(IProject project) throws CoreException {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			List<String> newNatures = new ArrayList<String>(natures.length + 1);
			
			boolean natureFound = false;
			for (String nature: natures) {
				if (nature.equals(ClojureProjectNature.NATURE_ID)) {
					// remove it, that is do not add it to newNatures
					natureFound = true;
				} else {
					newNatures.add(nature);
				}
			}
			
			if (!natureFound) {
				// Nature not found, so add it
				newNatures.add(ClojureProjectNature.NATURE_ID);
			}

			description.setNatureIds(newNatures.toArray(new String[0]));
			project.setDescription(description, null);
			
			return !natureFound;
		}

	}

